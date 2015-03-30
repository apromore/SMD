package org.apromore.mining.complexity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apromore.dao.ProcessDao;
import org.apromore.exception.LockFailedException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.ProcessCollectionMiner;
import org.apromore.mining.ProcessModelImporter;
import org.apromore.mining.standardize.ProcessStandardizer;
import org.apromore.service.FragmentService;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class AggregatedMetricComplexityChecker {

	private static final Logger logger = LoggerFactory.getLogger(AggregatedMetricComplexityChecker.class);
	
	@Autowired
	private ProcessCollectionMiner pcMiner;
	
	@Autowired
	private ProcessModelImporter processImporter;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdoa;
	
	@Autowired @Qualifier("FragmentService")
	private FragmentService fsrv;
	
	private static int N = 2;
	
	private double cfcThreshold = 25;
	private double cncThreshold = 5.0d; // TODO: old = 1.5d
	private int mccThreshold = 0;
	private int noajsThreshold = 30; // TODO: old = 50
	
	public Set<MyCluster> getComplexProcessesToReprocess(ProcessStandardizer standardizer, Set<MyCluster> undividableModels) {
		
		int complexProcessCount = 0;
		StringBuffer complexProcessInfo = new StringBuffer();
		int complexSubprocessCount = 0;
		StringBuffer complexSubprocessInfo = new StringBuffer();
		
		double mostComplexMetricValue = 0;
		MyCluster mostComplexProcess = null;
		Map<MyCluster, CPF> processes = standardizer.getStandardizedProcesses();
		for (MyCluster c : processes.keySet()) {
			if (!undividableModels.contains(c)) {
				CPF model = processes.get(c);
				if (isComplex(model)) {
					double metricValue = ComplexityCalculator.getCFC(model);
					if (logger.isDebugEnabled()) {
						complexProcessCount++;
						complexProcessInfo.append(metricValue + ", ");
					}
					if (metricValue > mostComplexMetricValue) {
						mostComplexMetricValue = metricValue;
						mostComplexProcess = c;
					}
				}
			}
		}

		double mostComplexSPMetricValue = 0;
		MyCluster processWithMostComplexSP = null;
		Map<String, CPF> fragments = standardizer.getStandardizedSubprocesses();
		for (String sp : fragments.keySet()) {
			CPF model = fragments.get(sp);
			if (isComplex(model)) {
				double metricValue = ComplexityCalculator.getCFC(model);
				if (logger.isDebugEnabled()) {
					complexSubprocessCount++;
					complexSubprocessInfo.append(metricValue + ", ");
				}
				
				Set<String> complexFids = new HashSet<String>();
				complexFids.add(sp);
				Set<MyCluster> sharedProcesses = pcMiner.getSharedProcesses(complexFids);
				// fragments are not shared. so at most one process is returned.
				if (sharedProcesses == null || sharedProcesses.isEmpty()) {
					continue;
				}
				MyCluster containingProcess = sharedProcesses.iterator().next();
				if (!undividableModels.contains(containingProcess)) {
					if (metricValue > mostComplexSPMetricValue) {
						mostComplexSPMetricValue = metricValue;
						processWithMostComplexSP = containingProcess;
					}
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("{} out of {} processes are complex: {}", 
					new Object[] {complexProcessCount, processes.size(), complexProcessInfo.toString()});
			logger.debug("{} out of {} subprocesses are complex: {}", 
					new Object[] {complexSubprocessCount, fragments.size(), complexSubprocessInfo.toString()});
		}
		
		Set<MyCluster> selectedModels = new HashSet<MyCluster>();
		if (mostComplexProcess != null && mostComplexMetricValue > mostComplexSPMetricValue) {
			selectedModels.add(mostComplexProcess);
			logger.debug("Process {} with size {} is selected for splitting.", 
					mostComplexProcess.getID(), mostComplexMetricValue);
		} else if (processWithMostComplexSP != null) {
			selectedModels.add(processWithMostComplexSP);
			logger.debug("Process {} containing a subprocess of size {} is selected for splitting.", 
					processWithMostComplexSP.getID(), mostComplexSPMetricValue);
		}
		return selectedModels;
	}
	
	public Set<MyCluster> getComplexProcessesToReprocessOld(ProcessStandardizer standardizer, Set<MyCluster> undividableModels) {
		
		Map<String, CPF> fragments = standardizer.getStandardizedSubprocesses();
		Map<MyCluster, CPF> processes = standardizer.getStandardizedProcesses();
		
		Set<MyCluster> complexModels = new HashSet<MyCluster>();
		Set<String> allComplexSubprocesses = getAllComplexSubprocessesToReprocess(fragments);
		complexModels.addAll(pcMiner.getSharedProcesses(allComplexSubprocesses));
		
		Set<MyCluster> complexProcesses = getAllComplexProcessesToReprocess(processes);
		complexModels.addAll(complexProcesses);
		
		int largestSize = 0;
		MyCluster largestModel = null;
		for (MyCluster logCluster : complexModels) {
			// if this complex model cannot be divided, don't consider it for reprocessing
			if (!undividableModels.contains(logCluster)) {
				String processName = processImporter.getProcessNameOfLogCluster(logCluster);
				String rootfid = pdoa.getRootFragmentId(processName);
				if (rootfid != null) {
					try {
						CPF cpf = fsrv.getFragment(rootfid, false);
						int size = cpf.getVertices().size();
						if (size > largestSize) {
							largestSize = size;
							largestModel = logCluster;
						}
					} catch (LockFailedException e) {
						logger.error("Fragment lock failed.");
					}
				} else {
					logger.error("Process {} does not have a root fragment. Cannot consider it for reprocessing.", 
							processName);
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			if (largestModel != null) {
				CPF s = processes.get(largestModel);
				int simplifiedSize = ComplexityCalculator.getNOAJS(s);
				logger.debug("Model: {} with Plain_N: {} and Simplified_N: {} is selected for reprocessing.", 
						new Object[] {largestModel.getID(), largestSize, simplifiedSize});
			}
		}
		Set<MyCluster> selectedModels = new HashSet<MyCluster>();
		if (largestModel != null) {
			selectedModels.add(largestModel);
		}
		return selectedModels;
	}
	
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
		
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (MyCluster complexProcessId : complexProcessIds) {
				CPF model = processes.get(complexProcessId);
				int n = ComplexityCalculator.getNOAJS(model);
				b.append(n + ",");
			}
			logger.debug("{} out of {} processe models are complex. N: {}", 
					new Object[] {complexProcessIds.size(), processes.size(), b.toString()});
		}
		
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
		
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (String complexFId : complexFragmentIds) {
				CPF model = fragments.get(complexFId);
				int n = ComplexityCalculator.getNOAJS(model);
				b.append(n + ",");
			}
			logger.debug("{} out of {} subprocesses are complex. N: {}", 
					new Object[] {complexFragmentIds.size(), fragments.size(), b.toString()});
		}
		
		return complexFragmentIds;
	}
	
	public boolean isComplex(CPF model) {
		return checkCFC(model, cfcThreshold);
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
