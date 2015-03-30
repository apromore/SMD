package org.apromore.mining.complexity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apromore.graph.JBPT.CPF;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SizePriorityBasedProcessComplexityChecker2 {

	private static final Logger logger = LoggerFactory.getLogger(SizePriorityBasedProcessComplexityChecker2.class);
	
	private static int N = 2;

	public Set<MyCluster> getComplexProcessesToReprocess(Map<MyCluster, CPF> processes) {
		
		List<Integer> sizes = new ArrayList<Integer>();
		Set<MyCluster> selectedModels = new HashSet<MyCluster>();
		
		Set<MyCluster> allModels = getAllComplexProcessesToReprocess(processes);
		if (allModels.size() <= N) {
			return allModels;
		}
		
		for (MyCluster logCluster : allModels) {
			CPF model = processes.get(logCluster);
			int size = model.getVertices().size();
			sizes.add(size);
		}
		
		Collections.sort(sizes);
		
		List<Integer> largestSizes = new ArrayList<Integer>();
		int lowerBound = sizes.size() - N;
		for (int i = sizes.size() - 1; i >= lowerBound; i--) {
			largestSizes.add(sizes.get(i));
		}
		
		for (MyCluster logCluster : allModels) {
			CPF model = processes.get(logCluster);
			int size = model.getVertices().size();
			if (largestSizes.contains(size)) {
				selectedModels.add(logCluster);
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
		
		List<Integer> sizes = new ArrayList<Integer>();
		Set<String> selectedModels = new HashSet<String>();
		
		Set<String> allModels = getAllComplexSubprocessesToReprocess(fragments);
		if (allModels.size() <= N) {
			return allModels;
		}
		
		for (String fid : allModels) {
			CPF model = fragments.get(fid);
			int size = model.getVertices().size();
			sizes.add(size);
		}
		
		Collections.sort(sizes);
		
		List<Integer> largestSizes = new ArrayList<Integer>();
		int lowerBound = sizes.size() - N;
		for (int i = sizes.size() - 1; i >= lowerBound; i--) {
			largestSizes.add(sizes.get(i));
		}
		
		for (String fid : allModels) {
			CPF model = fragments.get(fid);
			int size = model.getVertices().size();
			if (largestSizes.contains(size)) {
				selectedModels.add(fid);
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
		return checkNOAJS(model, 50) || checkCNC(model, 1.5d);
	}
	
	private boolean checkCFC(CPF model, double threshold) {
		return false;
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
