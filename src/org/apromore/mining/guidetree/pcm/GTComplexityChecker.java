package org.apromore.mining.guidetree.pcm;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.EvaluatorUtil;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.complexity.ComplexityCalculator;
import org.apromore.mining.guidetree.pcm.GTClusterer.GTNode;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public class GTComplexityChecker {
	
	private static final Logger logger = LoggerFactory.getLogger(GTComplexityChecker.class);

	public Set<GTNode> getComplexProcessesToReprocessAll(GTProcessStandardizer standardizer,
			Set<GTNode> undividableModels) {
		
		int totalExceededSize = 0;
		
		StringBuffer complexProcessInfo = new StringBuffer();
		Map<GTNode, CPF> processes = standardizer.getStandardizedProcesses();
		Set<GTNode> complexProcesses = new HashSet<GTNode>();
		for (GTNode node : processes.keySet()) {
			if (!undividableModels.contains(node)) {
				CPF cpf = processes.get(node);
				if (isComplex(cpf)) {
					if (logger.isDebugEnabled()) {
						int complexProcessSize = cpf.getVertices().size();
						int exceededSize = complexProcessSize - MiningConfig.COMPLEXITY_MATRIC_N;
						totalExceededSize += exceededSize;
						complexProcessInfo.append(complexProcessSize + ", ");
					}
					complexProcesses.add(node);
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			GTEvaluatorUtil.addStepData(processes.size(), complexProcesses.size(), totalExceededSize);
			logger.debug("{} out of {} processes are complex with total excess size {}. Complex process sizes: {}", 
					new Object[] {complexProcesses.size(), processes.size(), totalExceededSize, complexProcessInfo.toString()});
		}
		
		return complexProcesses;
	}

	public boolean isComplex(CPF cpf) {
		if (cpf.getVertices().size() > MiningConfig.COMPLEXITY_MATRIC_N) {
			return true;
		} else {
			return false;
		}
	}
	
	public Set<GTNode> getComplexModelsToReprocess(List<GTNode> models) {
		Set<GTNode> selectedModels = new HashSet<GTNode>();
		Set<GTNode> allModels = getAllComplexProcessesToReprocess(models);
		selectedModels.addAll(allModels);
		return selectedModels;
	}
	
	public Set<GTNode> getAllComplexProcessesToReprocess(Collection<GTNode> processes) {
		Set<GTNode> complexProcessIds = new HashSet<GTNode>();
		for (GTNode logCluster : processes) {
			CPF model = logCluster.cpf;
			if (isComplex(model)) {
				complexProcessIds.add(logCluster);
			}
		}
		
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (GTNode complexProcessId : complexProcessIds) {
				CPF model = complexProcessId.cpf;
				int n = ComplexityCalculator.getNOAJS(model);
				b.append(n + ",");
			}
			logger.debug("{} out of {} processe models are complex. N: {}", 
					new Object[] {complexProcessIds.size(), processes.size(), b.toString()});
		}
		
		return complexProcessIds;
	}

}
