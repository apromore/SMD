package org.apromore.mining.complexity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apromore.graph.JBPT.CPF;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectiveProcessComplexityChecker {

	private static final Logger logger = LoggerFactory.getLogger(SelectiveProcessComplexityChecker.class);
	
	private static final int noajsThreshold = 50;
	private static final double cncThreshold = 1.5d;

	public Set<MyCluster> getComplexProcessesToReprocess(Map<MyCluster, CPF> processes) {
		
		double maxNOAJS = 0;
		double maxCNC = 0;
		MyCluster mostComplextModelId = null;
		
		Set<MyCluster> complexProcessIds = new HashSet<MyCluster>();
		for (MyCluster logCluster : processes.keySet()) {
			CPF model = processes.get(logCluster);
			
			double nojas = getExcessNOAJS(model, noajsThreshold);
			double cnc = getExcessCNC(model, cncThreshold);
			if (nojas > maxNOAJS) {
				maxNOAJS = nojas;
				mostComplextModelId = logCluster;
				maxCNC = 0;
				
			} else if (maxNOAJS == 0 && cnc > maxCNC) {
				maxCNC = cnc;
				mostComplextModelId = logCluster;
			}
		}
		
		if (mostComplextModelId != null) {
			complexProcessIds.add(mostComplextModelId);
			logger.debug("{} with NOAJS: {} and CNC: {} is selected as the complex model out of {} process models.", 
					new Object[] {mostComplextModelId.getID(), maxNOAJS, maxCNC, processes.size()});
		} else {
			logger.debug("NONE OF THE MODELS ARE COMPLEX OUT OF {} MODELS.", processes.size());
		}
		return complexProcessIds;
	}
	
	public boolean isComplex(CPF model) {
		return false;
	}
	
	public Set<String> getComplexSubprocessesToReprocess(Map<String, CPF> fragments) {
		
		double maxNOAJS = 0;
		double maxCNC = 0;
		String mostComplextFragmentId = null;
		
		Set<String> complexFragmentIds = new HashSet<String>();
		for (String fid : fragments.keySet()) {
			CPF model = fragments.get(fid);
			
			double nojas = getExcessNOAJS(model, noajsThreshold);
			double cnc = getExcessCNC(model, cncThreshold);
			if (nojas > maxNOAJS) {
				maxNOAJS = nojas;
				mostComplextFragmentId = fid;
				
			} else if (maxNOAJS == 0 && cnc > maxCNC) {
				maxCNC = cnc;
				mostComplextFragmentId = fid;
			}
		}
		
		if (mostComplextFragmentId != null) {
			complexFragmentIds.add(mostComplextFragmentId);
		}
		
		logger.debug("{} with NOAJS: {} and CNC: {} is selected as the complex fragment out of {} fragments.", 
				new Object[] {mostComplextFragmentId, maxNOAJS, maxCNC, fragments.size()});
		return complexFragmentIds;
	}
	
	private boolean checkCFC(CPF model, double threshold) {
		return false;
	}
	
	private double getExcessMCC(CPF model, double threshold) {
		int numVertices = model.getVertices().size();
		int numEdges = model.getEdges().size();
		
		int mcc = numEdges - numVertices + 2;
		
		double excess = mcc - threshold;
		excess = excess < 0? 0 : excess;
		return excess;
	}
	
	private double getExcessCNC(CPF model, double threshold) {
		int numVertices = model.getVertices().size();
		int numEdges = model.getEdges().size();
		
		double cnc = (double) numEdges / (double) numVertices;
		
		double excess = cnc - threshold;
		excess = excess < 0? 0 : excess;
		return excess;
	}
	
	private double getExcessNOAJS(CPF model, double threshold) {
		int noajs = model.getVertices().size();
		
		double excess = noajs - threshold;
		excess = excess < 0? 0 : excess;
		return excess;
	}
}
