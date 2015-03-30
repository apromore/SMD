package org.apromore.mining.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.graph.JBPT.CpfTask;
import org.apromore.mining.EvaluatorUtil;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.mining.utils.CPFtoMultiDirectedGraphConverter;
import org.apromore.mining.utils.CycleFixer;
import org.apromore.mining.utils.SingleTerminalCycleFormer;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.hypergraph.abs.Vertex;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.prom5.converting.HNNetToEPCConverter;
import org.prom5.exporting.epcs.EpmlExport;
import org.prom5.exporting.log.MXMLibPlainLogExport;
import org.prom5.framework.log.LogFile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.LogReaderFactory;
import org.prom5.framework.log.ProcessInstance;
import org.prom5.framework.log.rfb.BufferedLogReader;
import org.prom5.framework.models.epcpack.ConfigurableEPC;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.mining.epcmining.EPCResult;
import org.prom5.mining.heuristicsmining.HeuristicsMiner;
import org.prom5.mining.heuristicsmining.HeuristicsMinerParameters;
import org.prom5.mining.heuristicsmining.HeuristicsNetResult;

public class ThresholdFinder {
	
	private DirectedGraphAlgorithms<DirectedEdge, Vertex> algo = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	private EPCSerializer epcSerializer = new EPCSerializer();

	class OrderedTrace implements Comparable<OrderedTrace> {
		public LogReader l;
		public int length = 0;
		
		@Override
		public int compareTo(OrderedTrace o) {
			if (this.length < o.length) {
				return -1;
			} else if (this.length > o.length) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ThresholdFinder().findThresholdFromLongest();
	}
	
	private void findThresholdFromLongest() {
		
//		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/log_with_system_subject2_filtered_min3.mxml";
//		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/filtered/log_with_system_subject2_filtered_min3_filtered.mxml";
//		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/bpi_upto_dec_1_5312.mxml";
		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/filtered/bpi_upto_dec_1_5312_filtered.mxml";
//		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/filtered/clog2_filtered_min3_filtered.mxml";
		
		String largestTracePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/temp/exahaustive_largest_trace.mxml";
		String largestModelsPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/temp/largest_models";
	
		try {
			List<OrderedTrace> orderedTraces = new LinkedList<OrderedTrace>();
			
			LogFile lf = LogFile.getInstance(logPath);
			BufferedLogReader log = (BufferedLogReader) BufferedLogReader.createInstance(null, lf);
			List<ProcessInstance> traces = log.getInstances();
			long t1 = System.currentTimeMillis();
			for (int i = 0; i < traces.size(); i++) {
				LogReader newLog = LogReaderFactory.createInstance(log, new int[] {i});
				int traceLength = traces.get(i).getAuditTrailEntryList().size();
				OrderedTrace ot = new OrderedTrace();
				ot.l = newLog;
				ot.length = traceLength;
				orderedTraces.add(ot);
				System.out.println("Measured " + i + " out of " + traces.size() + ". Current length: " + traceLength);
			}
			
			Collections.sort(orderedTraces);
			
			System.out.println("Finding the largest model...");
			
			int traceSizeOfTheLargestModel = 0;
			int largestModelSize = 0;
			OrderedTrace largestTrace = null;
			List<CPF> largestModels = new ArrayList<CPF>();
			while (!orderedTraces.isEmpty()) {
				OrderedTrace t = orderedTraces.remove(orderedTraces.size() - 1);
				if (largestModelSize > t.length) {
					break;
				}
				
				CPF model = mineEPC(t.l);
				int modelSize = model.getVertices().size();
				if (modelSize > largestModelSize) {
					largestModelSize = modelSize;
					traceSizeOfTheLargestModel = t.length;
					largestTrace = t;
					largestModels.clear();
					largestModels.add(model);
				} else if (modelSize == largestModelSize) {
					largestModels.add(model);
				}
			}
			
			long t2 = System.currentTimeMillis();
			long duration = (t2 - t1) / 1000;
			
			int modelNum = 0;
			for (CPF largestModel: largestModels) {
				modelNum++;
				File modelFile = new File(largestModelsPath, "model_" + modelNum + ".epml");
				String epml = epcSerializer.serializeToString(largestModel);
				FileUtils.write(modelFile, epml);
			}
			
			System.out.println("Largest model: " + largestModelSize);
			System.out.println("Trace size of the largest model: " + traceSizeOfTheLargestModel);
			System.out.println("Time: " + duration);
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void findThreshold() {
		
//		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/log_with_system_subject2_filtered_min3.mxml";
//		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/bpi_upto_dec_1_5312.mxml";
		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/clog2_filtered_min3.mxml";
		
		try {
			LogFile lf = LogFile.getInstance(logPath);
			BufferedLogReader log = (BufferedLogReader) BufferedLogReader.createInstance(null, lf);
			List<ProcessInstance> traces = log.getInstances();
			long t1 = System.currentTimeMillis();
			int largestSize = 0;
			for (int i = 0; i < traces.size(); i++) {
				LogReader newLog = LogReaderFactory.createInstance(log, new int[] {i});
				CPF model = mineEPC(newLog);
				int size = model.getVertices().size();
				System.out.println("Processing " + i + " out of " + traces.size() + ". Current size: " + size);
				if (size > largestSize) {
					largestSize = size;
				}
			}
			
			long t2 = System.currentTimeMillis();
			long duration = (t2 - t1) / 1000;
			
			System.out.println("Largest model: " + largestSize);
			System.out.println("Time: " + duration);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getSingleTraceLogs() {
		
		MXMLibPlainLogExport exporter = new MXMLibPlainLogExport();
		
		try {
			String logFilePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/splitted5_windscreen_system_subject/log_L_1_8548_6_8.mxml";
			String outPath = "/home/cn/temp/pm/t4";
			File outFolder = new File(outPath);
			FileUtils.cleanDirectory(outFolder);
			
			LogFile lf = LogFile.getInstance(logFilePath);
			BufferedLogReader log = (BufferedLogReader) BufferedLogReader.createInstance(null, lf);
			List<ProcessInstance> traces = log.getInstances();
			for (int i = 0; i < traces.size(); i++) {
				LogReader newLog = LogReaderFactory.createInstance(log, new int[] {i});
				System.out.println(newLog.getLogSummary().getNumberOfUniqueProcessInstances());
				
				ByteArrayOutputStream outstream = new ByteArrayOutputStream();
				ProvidedObject o = new ProvidedObject("log", new Object[] {newLog});
				exporter.export(o, outstream);
				
				String logData = outstream.toString();
				File file = new File(outFolder, "log_" + i + ".mxml");
				FileUtils.write(file, logData);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public CPF mineEPC(LogReader log) {
		
		try {
			long t1 = System.currentTimeMillis();
			
			HeuristicsMiner miner = new HeuristicsMiner();
			HeuristicsMinerParameters params = miner.getParameters();
			params.setAndThreshold(0.1);
			
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
			
			CPF cpf = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(epml));
			if (MiningConfig.REMOVE_EVENTS) {
				cpf = removeEvents(cpf);
			}
			CPFTransformer.correct(cpf);
			SingleTerminalCycleFormer.formSingleTerminalCycles(cpf);
			
			MultiDirectedGraph mdg = CPFtoMultiDirectedGraphConverter.covert(cpf);
            if (!algo.isMultiTerminal(mdg)) {
            	boolean multiterminal = CycleFixer.fixCycles(cpf);
            	if (!multiterminal) {
            		cpf = new CPF();
            		FlowNode node = new CpfTask();
            		node.setName("Empty task");
            		cpf.addFlowNode(node);
            	}
            }
			
			return cpf;
			
		} catch (Exception e) {
			System.out.println("Failed to mine process model.");
			e.printStackTrace();
		}
		
		return null;
	}
	
	private CPF removeEvents(CPF model) {
		
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
		return model;
	}
}
