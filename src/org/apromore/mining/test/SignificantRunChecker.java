package org.apromore.mining.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.graph.JBPT.CpfTask;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.guidetree.Prom5BasedMiner;
import org.apromore.mining.guidetree.pcm.GTEvaluatorUtil;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.mining.utils.CPFtoMultiDirectedGraphConverter;
import org.apromore.mining.utils.CycleFixer;
import org.apromore.mining.utils.SingleTerminalCycleFormer;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.out.XMxmlSerializer;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.util.progress.XMonitoredInputStream;
import org.deckfour.xes.util.progress.XProgressListener;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.hypergraph.abs.Vertex;
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

public class SignificantRunChecker {
	
	private DirectedGraphAlgorithms<DirectedEdge, Vertex> algo = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	private EPCSerializer epcSerializer = new EPCSerializer();
	
	private TraceRepetitionSignificance tps = new TraceRepetitionSignificance();
	private RepetitionSignificance<String> rs = new RepetitionSignificance<String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SignificantRunChecker().testRuns3();
	}
	
	private void testRuns2() {
		
		try {
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/splitted5_windscreen_system_subject/log_L_1_8578_238_23.mxml";
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/log_with_system_subject2_filtered_min3.mxml";
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/filtered/log_with_system_subject2_filtered_min3_filtered.mxml";
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/filtered/bpi_upto_dec_1_5312_filtered.mxml";
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/clog2_filtered_min3.mxml";
			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/bpi_upto_dec_1_5312.mxml";
			
			String largestTracePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/temp/largest_trace.mxml";
	
			System.out.println("Loading the log...");
			XLog log = getLog(logPath);
			System.out.println("Log is loaded.");
			long t1 = System.currentTimeMillis();
			Map<Integer, List<XTrace>> sizeTraceMapping = new HashMap<Integer, List<XTrace>>();
			int largestTraceSize = 0;
			for (XTrace t : log) {
				List<String> eventNames = new ArrayList<String>();
				for (XEvent event : t) {
					XAttributeMap xmap = event.getAttributes();
					XAttribute attr = xmap.get("concept:name");
					XAttributeLiteral al = (XAttributeLiteral) attr;
					String eventName = al.getValue();
					eventNames.add(eventName);
				}
				
				List<String> reducedNames = rs.maxCompressToRepetitionSignigficant(eventNames);
				int reducedSize = reducedNames.size();
				List<XTrace> sizeTraces = sizeTraceMapping.get(reducedSize);
				if (sizeTraces == null) {
					sizeTraces = new ArrayList<XTrace>();
					sizeTraceMapping.put(reducedSize, sizeTraces);
				}
				sizeTraces.add(t);
				
				System.out.println(t.size() + " reduced to " + reducedSize);
				if (reducedSize > largestTraceSize) {
					largestTraceSize = reducedSize;
				}
			}
			System.out.println("Largest (reduced) trace: " + largestTraceSize);
			
			int largestModelSize = 0;
			int traceLength = 0;
			XLog largestTraceLog = null;
			StringBuffer buffer = new StringBuffer();
			Prom5BasedMiner miner = new Prom5BasedMiner();
			List<XTrace> largestTraces = sizeTraceMapping.get(largestTraceSize);
			for (XTrace largeTrace : largestTraces) {
				XAttributeMap xmap = new XAttributeMapImpl();
				XLog largestLog = new XLogImpl(xmap);
				largestLog.add(largeTrace);
				
				CPF model = mineModel(largestLog);
				int largeModelSize = model.getVertices().size();
				System.out.println("Mined a large model of size model: " + largeModelSize);
				buffer.append(largeModelSize + ",");
				if (largeModelSize > largestModelSize) {
					largestModelSize = largeModelSize;
					traceLength = largeTrace.size();
					largestTraceLog = largestLog;
				}
			}
					
			long t2 = System.currentTimeMillis();
			long duration = (t2 - t1) / 1000;
			
			System.out.println("===========================================================================");
			System.out.println("Largest (reduced) trace: " + largestTraceSize);
			System.out.println("Large model sizes: " + buffer.toString());
			System.out.println("Size of the largest model: " + largestModelSize);
			System.out.println("Trace length of the largest model: " + traceLength);
			System.out.println("Time: " + duration);
			
			File outFile = new File(largestTracePath);
			FileOutputStream out = new FileOutputStream(outFile);
			XSerializer logSerializer = new XMxmlSerializer();
			logSerializer.serialize(largestTraceLog, out);
			out.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void testRuns3() {
		
		try {
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/splitted5_windscreen_system_subject/log_L_1_8578_238_23.mxml";
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/log_with_system_subject2_filtered_min3.mxml";
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/filtered/log_with_system_subject2_filtered_min3_filtered.mxml";
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/filtered/bpi_upto_dec_1_5312_filtered.mxml";
			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/clog2_filtered_min3.mxml";
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/bpi_upto_dec_1_5312.mxml";
			
			String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/temp/sizes3.csv";
	
			System.out.println("Loading the log...");
			XLog log = getLog(logPath);
			System.out.println("Log is loaded.");
			long t1 = System.currentTimeMillis();
			StringBuffer buffer = new StringBuffer();
			Prom5BasedMiner miner = new Prom5BasedMiner();
			buffer.append("Trace length, Reducued length, Model size\n");
			int count = 0;
			for (XTrace t : log) {
				List<String> eventNames = new ArrayList<String>();
				for (XEvent event : t) {
					XAttributeMap xmap = event.getAttributes();
					XAttribute attr = xmap.get("concept:name");
					XAttributeLiteral al = (XAttributeLiteral) attr;
					String eventName = al.getValue();
					eventNames.add(eventName);
				}
				
				List<String> reducedNames = rs.maxCompressToRepetitionSignigficant(eventNames);
				int reducedSize = reducedNames.size();
				int originalSize = eventNames.size();
				
				XAttributeMap xmap = new XAttributeMapImpl();
				XLog largestLog = new XLogImpl(xmap);
				largestLog.add(t);
				CPF model = mineModel(largestLog);
				int modelSize = model.getVertices().size();
				buffer.append(originalSize + "," + reducedSize + "," + modelSize + "\n");
				count++;
				System.out.println("Processed " + count + " out of " + log.size());
			}
			
			long t2 = System.currentTimeMillis();
			long duration = (t2 - t1) / 1000;
			
			System.out.println("===========================================================================");
			System.out.println("Time: " + duration);
			
			File outFile = new File(outPath);
			FileUtils.write(outFile, buffer.toString());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void testRuns() {
		
		try {
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/splitted5_windscreen_system_subject/log_L_1_8578_238_23.mxml";
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/log_with_system_subject2_filtered_min3.mxml";
//			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/bpi_upto_dec_1_5312.mxml";
			String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/clog2_filtered_min3.mxml";
	
			System.out.println("Loading the log...");
			XLog log = getLog(logPath);
			System.out.println("Log is loaded.");
			long t1 = System.currentTimeMillis();
			int largestTraceSize = 0;
			XTrace largestTrace = null;
			for (XTrace t : log) {
				XTrace reducedTrace = tps.minCompressTraceToRepetitionSignigficant(t);
				int reducedSize = reducedTrace.size();
				System.out.println(t.size() + " reduced to " + reducedSize);
				if (reducedSize > largestTraceSize) {
					largestTraceSize = reducedSize;
					largestTrace = t;
				}
			}
			long t2 = System.currentTimeMillis();
			long duration = (t2 - t1) / 1000;
			
			System.out.println("Largest trace: " + largestTraceSize);
			System.out.println("Time: " + duration);
			
			XAttributeMap xmap = new XAttributeMapImpl();
			XLog largestLog = new XLogImpl(xmap);
			largestLog.add(largestTrace);
			
			CPF model = mineModel(largestLog);
			System.out.println("Size of the largest model: " + model.getVertices().size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private XLog getLog(String logPath) throws FileNotFoundException {
		File logFile = new File(logPath);
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(logFile));
		long fileSizeInBytes = logFile.length();
		
		XFactory factory = new XFactoryNaiveImpl();
		XParser parser = new XMxmlParser(factory);
		Collection<XLog> logs = null;
		try {
			logs = (new XMxmlParser()).parse(new XMonitoredInputStream(input, fileSizeInBytes,
					new XProgressListener() {

						public boolean isAborted() {
							return false;
						}

						public void updateProgress(int arg0, int arg1) {

						}
					}));
		} catch (Exception e) {
			logs = null;
		}
		
		XLog log = null;
		if (logs != null) {
			log = logs.iterator().next();
		}
		return log;
	}
	
	public CPF mineModel(XLog log) throws Exception {
		
		String tempLogFilePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp/temp2/templog.mxml";
		
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
		
		try {
			CPFTransformer.correct(cpf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			SingleTerminalCycleFormer.formSingleTerminalCycles(cpf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			MultiDirectedGraph mdg = CPFtoMultiDirectedGraphConverter.covert(cpf);
			if (!algo.isMultiTerminal(mdg)) {
				boolean multiterminal = CycleFixer.fixCycles(cpf);
				if (!multiterminal) {
//					cpf = new CPF();
//					FlowNode node = new CpfTask();
//					node.setName("Empty task");
//					cpf.addFlowNode(node);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
