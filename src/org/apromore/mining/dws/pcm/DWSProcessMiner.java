package org.apromore.mining.dws.pcm;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.mining.EvaluatorUtil;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.dws.pcm.DWSClusterer.DWSNode;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
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

public class DWSProcessMiner {
	
	private static final Logger logger = LoggerFactory.getLogger(DWSProcessMiner.class);
	
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	private EPCSerializer epcSerializer = new EPCSerializer();
	
	public String mineEPC(DWSNode dwsNode) {
		
		try {
			long t1 = System.currentTimeMillis();
			
			HeuristicsNet net = dwsNode.cluster.getHeuristicsNet();
			if (net == null) {
				HeuristicsMiner miner = new HeuristicsMiner();
				HeuristicsMinerParameters params = miner.getParameters();
				params.setAndThreshold(0.1);
				
				HeuristicsNetResult result = (HeuristicsNetResult) miner.mine(dwsNode.getLog());
				net = result.getHeuriticsNet();
				dwsNode.cluster.setHeuristicsNet(net);
			}
			
			ProvidedObject po = new ProvidedObject("hm_results", new Object[] {net, dwsNode.getLog()});
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
			DWSEvaluatorUtil.addMiningTime(duration);
			
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
