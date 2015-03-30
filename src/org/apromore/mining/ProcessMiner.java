package org.apromore.mining;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apromore.exception.RepositoryException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.jbpt.graph.Edge;
import org.jbpt.hypergraph.abs.Vertex;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.processmining.lib.mxml.writing.ProcessInstance;
import org.prom5.converting.HNNetToEPCConverter;
import org.prom5.exporting.epcs.EpmlExport;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.epcpack.ConfigurableEPC;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.mining.epcmining.EPCResult;
import org.prom5.mining.heuristicsmining.HeuristicsMiner;
import org.prom5.mining.heuristicsmining.HeuristicsMinerParameters;
import org.prom5.mining.heuristicsmining.HeuristicsNetResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessMiner {
	
	private final Logger logger = LoggerFactory.getLogger(ProcessMiner.class);

	@Autowired
	private InitialProcessModelComplexityChecker processComplexityChecker;
	
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	private EPCSerializer epcSerializer = new EPCSerializer();
	
	public void process(MiningData data) {
		
		logger.debug("Mining a process model for the log cluster: " + data.getClusterIdAsString());
		
		String epml = mineEPC(data.getLog());
		try {
			CPF cpf = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(epml));
			data.setProcessModel(cpf);
			processComplexityChecker.process(data);
		} catch (Exception e) {
			String errorPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp/e1.epml";
			try {
				FileUtils.write(new File(errorPath), epml);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw new RuntimeException(e);
		}
	}
	
	public String mineEPC(LogReader log) {
		
		try {
			logger.debug("Mining a process model from a log with {} traces.", 
					log.getLogSummary().getNumberOfUniqueProcessInstances());
			
			long t1 = System.currentTimeMillis();
			
			HeuristicsMiner miner = new HeuristicsMiner();
			HeuristicsMinerParameters params = miner.getParameters();
			params.setAndThreshold(0.1);
			
			// test code
//			params.setRelativeToBestThreshold(0.05);
//			params.setPositiveObservationsThreshold(1);
//			params.setDependencyThreshold(0.0);
//			params.setL1lThreshold(0.9);
//			params.setL2lThreshold(0.9);
//			params.setLDThreshold(0.0);
//			params.setUseAllConnectedHeuristics(true);
			// end of test code
			
			HeuristicsNetResult result = (HeuristicsNetResult) miner.mine(log);
			HeuristicsNet net = result.getHeuriticsNet();
			
			ProvidedObject po = new ProvidedObject("hm_results", new Object[] {net, log});
			HNNetToEPCConverter converter = new HNNetToEPCConverter();
			EPCResult epcResult = (EPCResult) converter.convert(po);
			
			ConfigurableEPC epc = epcResult.getEPC();
			ProvidedObject po2 = new ProvidedObject("epc_result", new Object[] {epc});
			
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			
			EpmlExport exporter = new EpmlExport();
			exporter.export(po2, output);
			
			String epml = output.toString();
			epml = epml.replaceAll("&", "-");
			
			long t2 = System.currentTimeMillis();
			long duration = t2 - t1;
			EvaluatorUtil.addMiningTime(duration);
			
			if (MiningConfig.REMOVE_EVENTS) {
				epml = removeEvents(epml);
			}
			return epml;
			
		} catch (Exception e) {
			logger.error("Failed to mine process model.", e);
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String removeEvents(String originalEPML) {
		
		CPF model = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(originalEPML));
		if (logger.isTraceEnabled()) {
			logger.trace(CPFTransformer.findErrors(model));
		}
		CPFTransformer.correct(model);
		
		for (FlowNode v : model.getVertices()) {
			if (v instanceof CpfEvent) {
				String name = v.getName();
				if ("fictive start".equals(name) || "fictive end".equals(name)) {
					continue;
				}
				
				Collection<FlowNode> preset = model.getDirectPredecessors(v);
				Collection<FlowNode> postset = model.getDirectSuccessors(v);
				if (preset.size() > 1 || postset.size() > 1) {
					continue;
				}
				
				Collection<ControlFlow<FlowNode>> edgeToRemove = new ArrayList<ControlFlow<FlowNode>>();
				edgeToRemove.addAll(model.getIncomingEdges(v));
				edgeToRemove.addAll(model.getOutgoingEdges(v));
				model.removeEdges(edgeToRemove);
				model.removeVertex(v);
				
				if (preset.size() == 1 && postset.size() == 1) {
					FlowNode n1 = preset.iterator().next();
					FlowNode n2 = postset.iterator().next();
					model.addEdge(n1, n2);
				}
			}
		}
		
		String newEPML = epcSerializer.serializeToString(model);
		return newEPML;
	}
}
