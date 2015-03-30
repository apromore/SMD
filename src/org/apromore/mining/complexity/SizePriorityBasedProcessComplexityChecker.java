package org.apromore.mining.complexity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.standardize.ProcessStandardizer;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SizePriorityBasedProcessComplexityChecker {

	private static final Logger logger = LoggerFactory.getLogger(SizePriorityBasedProcessComplexityChecker.class);
	
	private static int N = 2;
	
	private double cfcThreshold = 20;
	private double cncThreshold = 5.0d; // TODO: old = 1.5d
	private int mccThreshold = 0;
	private int noajsThreshold = 30; // TODO: old = 50
	
	public Set<MyCluster> getComplexProcessesToReprocess(Map<MyCluster, CPF> processes) {
		
		Set<MyCluster> selectedModels = new HashSet<MyCluster>();
		int maxSize = 0;
		MyCluster largestModel = null;
		
		Set<MyCluster> allModels = getAllComplexProcessesToReprocess(processes);
		for (MyCluster logCluster : allModels) {
			CPF model = processes.get(logCluster);
			int size = model.getVertices().size();
			if (size > maxSize) {
				maxSize = size;
				largestModel = logCluster;
			}
		}
		
		if (largestModel != null) {
			selectedModels.add(largestModel);
			
			if (logger.isDebugEnabled()) {
				CPF s = processes.get(largestModel);
				int noajs = ComplexityCalculator.getNOAJS(s);
				double cnc = ComplexityCalculator.getCNC(s);
				double cfc = ComplexityCalculator.getCFC(s);
				logger.debug("Model Id:{} with NOAJS={}, CNC={} and CFC={} is selected as the complex model.", 
						new Object[] {largestModel.getID(), noajs, cnc, cfc});
			}
		}
		return selectedModels;
	}
	
	public Set<MyCluster> getAllComplexProcessesToReprocess(Map<MyCluster, CPF> processes) {
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
		
		Set<String> selectedModels = new HashSet<String>();
		int maxSize = 0;
		String largestModel = null;
		
		Set<String> allModels = getAllComplexSubprocessesToReprocess(fragments);
		for (String fid : allModels) {
			CPF model = fragments.get(fid);
			int size = model.getVertices().size();
			if (size > maxSize) {
				maxSize = size;
				largestModel = fid;
			}
		}
		
		if (largestModel != null) {
			selectedModels.add(largestModel);
			
			if (logger.isDebugEnabled()) {
				CPF s = fragments.get(largestModel);
				int noajs = ComplexityCalculator.getNOAJS(s);
				double cnc = ComplexityCalculator.getCNC(s);
				double cfc = ComplexityCalculator.getCFC(s);
				logger.debug("Fragmen Id:{} with NOAJS={}, CNC={} and CFC={} is selected as the complex model.", 
						new Object[] {largestModel, noajs, cnc, cfc});
			}
		}
		return selectedModels;
	}
	
	public Set<String> getAllComplexSubprocessesToReprocess(Map<String, CPF> fragments) {
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
	
	public boolean isComplex(CPF model) {
		return checkNOAJS(model, noajsThreshold) || checkCNC(model, cncThreshold);
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
		int mcc = ComplexityCalculator.getMCC(model);
		if (mcc > threshold) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean checkCNC(CPF model, double threshold) {
		double cnc = ComplexityCalculator.getCNC(model);
		if (cnc > threshold) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean checkNOAJS(CPF model, double threshold) {
		int nojas = ComplexityCalculator.getNOAJS(model);
		if (nojas > threshold) {
			return true;
		}
		return false;
	}
}
