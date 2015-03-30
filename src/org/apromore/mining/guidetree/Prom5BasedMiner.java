package org.apromore.mining.guidetree;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.guidetree.pcm.GTEvaluatorUtil;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.service.utils.EPCDeserializer;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XMxmlSerializer;
import org.deckfour.xes.out.XSerializer;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.prom5.converting.HNNetToEPCConverter;
import org.prom5.exporting.epcs.EpmlExport;
import org.prom5.framework.log.LogFile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.rfb.BufferedLogReader;
import org.prom5.framework.models.epcpack.ConfigurableEPC;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.mining.epcmining.EPCResult;
import org.prom5.mining.heuristicsmining.HeuristicsMiner;
import org.prom5.mining.heuristicsmining.HeuristicsMinerParameters;
import org.prom5.mining.heuristicsmining.HeuristicsNetResult;

public class Prom5BasedMiner {
	
//	private String tempLogFilePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp/prom6to5/templog.mxml";
	private String tempLogFilePath = new File(MiningConfig.TEMP_LOG_FILE_PATH, "templog.mxml").getAbsolutePath();
	
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	
	public CPF mineModel(XLog log) throws Exception {
		
		GTEvaluatorUtil.miningStart();
		
		File outFile = new File(tempLogFilePath);
		FileOutputStream out = new FileOutputStream(outFile);
		XSerializer logSerializer = new XMxmlSerializer();
		logSerializer.serialize(log, out);
		out.close();
		
		LogFile lf = LogFile.getInstance(tempLogFilePath);
		LogReader log1 = BufferedLogReader.createInstance(null, lf);
		
		HeuristicsMiner miner = new HeuristicsMiner();
		HeuristicsMinerParameters params = miner.getParameters();
		params.setAndThreshold(0.1);
		
		HeuristicsNetResult result = (HeuristicsNetResult) miner.mine(log1);
		HeuristicsNet net = result.getHeuriticsNet();
		
		ProvidedObject po = new ProvidedObject("hm results", new Object[] {net, log1});
		HNNetToEPCConverter converter = new HNNetToEPCConverter();
		EPCResult epcResult = (EPCResult) converter.convert(po);
		
		ConfigurableEPC epc = epcResult.getEPC();
		ProvidedObject po2 = new ProvidedObject("epc result", new Object[] {epc});
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		EpmlExport exporter = new EpmlExport();
		exporter.export(po2, output);
		String epml = output.toString();
		epml = epml.replaceAll("&", "-");
		
		CPF cpf = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(epml));
		if (MiningConfig.REMOVE_EVENTS) {
			cpf = removeEvents(cpf);
		}
		GTEvaluatorUtil.miningEnd();
		return cpf;
	}
	
	private CPF removeEvents(CPF model) {
		
//		CPF model = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(originalEPML));
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
		
//		String newEPML = epcSerializer.serializeToString(model);
//		return newEPML;
		return model;
	}

}
