package org.apromore.mining;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apromore.exception.ImportException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.complexity.ComplexityCalculator;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.mining.utils.CPFUtil;
import org.apromore.mining.utils.CPFtoMultiDirectedGraphConverter;
import org.apromore.mining.utils.CycleFixer;
import org.apromore.mining.utils.SingleTerminalCycleFormer;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.hypergraph.abs.Vertex;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessComplexityChecker {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessComplexityChecker.class);
	
	private DirectedGraphAlgorithms<DirectedEdge, Vertex> algo = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	
	private int noajsThreshold = MiningConfig.COMPLEXITY_MATRIC_N;
	private double cncThreshold = 5.0d;
	private double cfcThreshold = 25.0d;

	public Set<MyCluster> getComplexProcessesToReprocess(Map<MyCluster, CPF> processes) {
		Set<MyCluster> complexProcessIds = new HashSet<MyCluster>();
		for (MyCluster logCluster : processes.keySet()) {
			CPF model = processes.get(logCluster);
			if (isComplex(model)) {
				complexProcessIds.add(logCluster);
			}
		}
		logger.debug("{} out of {} process models are complex.", complexProcessIds.size(), processes.size());
		return complexProcessIds;
	}
	
	public Set<String> getComplexSubprocessesToReprocess(Map<String, CPF> fragments) {
		Set<String> complexFragmentIds = new HashSet<String>();
		for (String fid : fragments.keySet()) {
			CPF model = fragments.get(fid);
			if (isComplex(model)) {
				complexFragmentIds.add(fid);
			}
		}
		logger.debug("{} out of {} subprocesses are complex.", complexFragmentIds.size(), fragments.size());
		return complexFragmentIds;
	}
	
	public boolean isComplexCFC(CPF model) {
		return checkCFC(model, cfcThreshold);
	}
	
	public boolean isComplex(CPF model) {
		return checkNOAJS(model, noajsThreshold) || checkCNC(model, cncThreshold);
	}
	
	public boolean isComplexWithBuffer(CPF model) {
		int modelSize = model.getVertices().size();
		
		if (modelSize > MiningConfig.COMPLEXITY_MATRIC_N) {
			return true;
		}
		
		if (modelSize + MiningConfig.N_PREPROCESSING_BUFFER <= MiningConfig.COMPLEXITY_MATRIC_N) {
			return false;
		}
		
		try {
			CPFTransformer.correct(model);
			if (model.getVertices().size() > MiningConfig.COMPLEXITY_MATRIC_N) {
				return true;
			}
			
			SingleTerminalCycleFormer.formSingleTerminalCycles(model);
			if (model.getVertices().size() > MiningConfig.COMPLEXITY_MATRIC_N) {
				return true;
			}
			
			MultiDirectedGraph mdg = CPFtoMultiDirectedGraphConverter.covert(model);
			if (!algo.isMultiTerminal(mdg)) {
				logger.info("Process model of size {} is not multiterminal. Fixing cycles...", model.getVertices().size());
				boolean multiterminal = CycleFixer.fixCycles(model);
			}
			
			if (model.getVertices().size() > MiningConfig.COMPLEXITY_MATRIC_N) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Error in fixing the process model graph of size {}. Marking the graph as complex.", model.getVertices().size());
			return true;
		}
	}
	
	private boolean checkCFC(CPF model, double threshold) {
		double cfc = ComplexityCalculator.getCFC(model);
		if (cfc > threshold) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean checkMCC(CPF model, double threshold) {
		int numVertices = model.getVertices().size();
		int numEdges = model.getEdges().size();
		
		int cfc = numEdges - numVertices + 2;
		if (cfc > threshold) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean checkCNC(CPF model, double threshold) {
		int numVertices = model.getVertices().size();
		int numEdges = model.getEdges().size();
		
		double cnc = (double) numEdges / (double) numVertices;
		if (cnc > threshold) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean checkNOAJS(CPF model, double threshold) {
		if (model.getVertices().size() > threshold) {
			return true;
		}
		return false;
	}
	
}
