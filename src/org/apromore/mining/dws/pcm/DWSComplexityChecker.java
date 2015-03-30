package org.apromore.mining.dws.pcm;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.complexity.ComplexityCalculator;
import org.apromore.mining.dws.pcm.DWSClusterer.DWSNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DWSComplexityChecker {
	
	private static final Logger logger = LoggerFactory.getLogger(DWSComplexityChecker.class);

	public Set<DWSNode> getComplexProcessesToReprocessAll(DWSProcessStandardizer standardizer,
			Set<DWSNode> undividableModels) {
		
		int totalExceededSize = 0;
		
		StringBuffer complexProcessInfo = new StringBuffer();
		Map<DWSNode, CPF> processes = standardizer.getStandardizedProcesses();
		Set<DWSNode> complexProcesses = new HashSet<DWSNode>();
		for (DWSNode node : processes.keySet()) {
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
			DWSEvaluatorUtil.addStepData(processes.size(), complexProcesses.size(), totalExceededSize);
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

	public Set<DWSNode> getComplexModelsToReprocess(List<DWSNode> models) {
		Set<DWSNode> selectedModels = new HashSet<DWSNode>();
		Set<DWSNode> allModels = getAllComplexProcessesToReprocess(models);
		selectedModels.addAll(allModels);
		return selectedModels;
	}
	
	public Set<DWSNode> getAllComplexProcessesToReprocess(Collection<DWSNode> processes) {
		Set<DWSNode> complexProcessIds = new HashSet<DWSNode>();
		for (DWSNode logCluster : processes) {
			CPF model = logCluster.cpf;
			if (isComplex(model)) {
				complexProcessIds.add(logCluster);
			}
		}
		
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (DWSNode complexProcessId : complexProcessIds) {
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
