package org.apromore.mining.complexity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apromore.dao.ProcessDao;
import org.apromore.exception.LockFailedException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.EvaluatorUtil;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.ProcessCollectionMiner;
import org.apromore.mining.ProcessModelImporter;
import org.apromore.mining.standardize.ProcessStandardizer;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.mining.utils.CPFtoMultiDirectedGraphConverter;
import org.apromore.mining.utils.CycleFixer;
import org.apromore.mining.utils.SingleTerminalCycleFormer;
import org.apromore.service.FragmentService;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.hypergraph.abs.Vertex;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class AggregatedComplexityChecker {

	private static final Logger logger = LoggerFactory.getLogger(AggregatedComplexityChecker.class);
	
	@Autowired
	private ProcessCollectionMiner pcMiner;
	
	@Autowired
	private ProcessModelImporter processImporter;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdoa;
	
	@Autowired @Qualifier("FragmentService")
	private FragmentService fsrv;
	
	private DirectedGraphAlgorithms<DirectedEdge, Vertex> algo = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	
	private static int N = 2;
	
	private double cfcThreshold = 2;
	private double cncThreshold = 5.0d; // TODO: old = 1.5d
	private int mccThreshold = 0;
//	private int noajsThreshold = MiningConfig.COMPLEXITY_MATRIC_N; // TODO: old = 50
	
	public Set<MyCluster> getComplexProcessesToReprocessAll(ProcessStandardizer standardizer, Set<MyCluster> undividableModels) {
		
		int totalExceededSize = 0;
		
		StringBuffer complexProcessInfo = new StringBuffer();
		Map<MyCluster, CPF> processes = standardizer.getStandardizedProcesses();
		Set<MyCluster> complexProcesses = new HashSet<MyCluster>();
		for (MyCluster myCluster : processes.keySet()) {
			if (!undividableModels.contains(myCluster)) {
				CPF cpf = processes.get(myCluster);
				if (isComplex(cpf)) {
					if (logger.isDebugEnabled()) {
						int complexProcessSize = cpf.getVertices().size();
						int exceededSize = complexProcessSize - MiningConfig.COMPLEXITY_MATRIC_N;
						totalExceededSize += exceededSize;
						complexProcessInfo.append(complexProcessSize + ", ");
					}
					complexProcesses.add(myCluster);
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			EvaluatorUtil.addStepData(processes.size(), complexProcesses.size(), totalExceededSize);
			logger.debug("{} out of {} processes are complex with total excess size {}. Complex process sizes: {}", 
					new Object[] {complexProcesses.size(), processes.size(), totalExceededSize, complexProcessInfo.toString()});
		}
		
		return complexProcesses;
	}
	
	public Set<MyCluster> getComplexProcessesToReprocessTopX(ProcessStandardizer standardizer, Set<MyCluster> undividableModels) {
		
		// identify complex models and map them according to sizes
		Map<MyCluster, CPF> processes = standardizer.getStandardizedProcesses();
		Map<Integer, List<MyCluster>> complexProcesses = new HashMap<Integer, List<MyCluster>>();
		List<Integer> sizes = new ArrayList<Integer>();
		for (MyCluster myCluster : processes.keySet()) {
			if (!undividableModels.contains(myCluster)) {
				CPF cpf = processes.get(myCluster);
				if (isComplex(cpf)) {
					int modelSize = cpf.getVertices().size();
					sizes.add(modelSize);
					List<MyCluster> modelsWithSize = complexProcesses.get(modelSize);
					if (modelsWithSize == null) {
						modelsWithSize = new ArrayList<MyCluster>();
						complexProcesses.put(modelSize, modelsWithSize);
					}
					modelsWithSize.add(myCluster);
				}
			}
		}
		int complexModelsCount = sizes.size();
		
		// compute the minimum size we are considering for reprocessing (cutoffSize) and the number of models to
		// reprocess.
		Set<MyCluster> selectedModels = new HashSet<MyCluster>();
		if (sizes.isEmpty()) {
			return selectedModels;
		}
		
		Collections.sort(sizes);
		int largestSize = sizes.get((sizes.size() - 1));
		double cutoffSize = largestSize * 0.8d;
		int maxProcessesToSelect = (int) Math.ceil(sizes.size() * 0.5);
		
		// select process models to be reprocessed based on the computed parameters.
		StringBuffer complexProcessInfo = new StringBuffer();
		while (!sizes.isEmpty()) {
			int currentSize = sizes.remove((sizes.size() - 1));
			if (currentSize < cutoffSize) {
				break;
			}
			
			List<MyCluster> modelsWithSize = complexProcesses.get(currentSize);
			for (MyCluster myCluster : modelsWithSize) {
				selectedModels.add(myCluster);
				if (logger.isDebugEnabled()) {
					CPF cpf = processes.get(myCluster);
					complexProcessInfo.append(cpf.getVertices().size() + ", ");
				}
				if (selectedModels.size() == maxProcessesToSelect) {
					break;
				}
			}
			
			if (selectedModels.size() == maxProcessesToSelect) {
				break;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("{} were selected out of {} complex processes (total: {}) : {}", 
					new Object[] {selectedModels.size(), complexModelsCount, processes.size(), complexProcessInfo.toString()});
		}
		
		return selectedModels;
	}
	
	public Set<MyCluster> getComplexProcessesToReprocessMostComplex(ProcessStandardizer standardizer, Set<MyCluster> undividableModels) {
		
		int complexProcessCount = 0;
		StringBuffer complexProcessInfo = new StringBuffer();
		int complexSubprocessCount = 0;
		StringBuffer complexSubprocessInfo = new StringBuffer();
		
		int largestProcessSize = 0;
		MyCluster largestProcess = null;
		Map<MyCluster, CPF> processes = standardizer.getStandardizedProcesses();
		for (MyCluster c : processes.keySet()) {
			if (!undividableModels.contains(c)) {
				CPF model = processes.get(c);
				if (isComplex(model)) {
					if (logger.isDebugEnabled()) {
						complexProcessCount++;
						complexProcessInfo.append(model.getVertices().size() + ", ");
					}
					if (model.getVertices().size() > largestProcessSize) {
						largestProcessSize = model.getVertices().size();
						largestProcess = c;
					}
				}
			}
		}

		int largestSPSize = 0;
		MyCluster processWithLargestSP = null;
		Map<String, CPF> fragments = standardizer.getStandardizedSubprocesses();
		for (String sp : fragments.keySet()) {
			CPF model = fragments.get(sp);
			if (isComplex(model)) {
				if (logger.isDebugEnabled()) {
					complexSubprocessCount++;
					complexSubprocessInfo.append(model.getVertices().size() + ", ");
					
					if (model.getVertices().size() > MiningConfig.MIN_GED_FRAGMENT_SIZE) {
						logger.error("SUBPROCESS {} HAS SIZE: {}", sp, model.getVertices().size());
					}
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
					if (model.getVertices().size() > largestSPSize) {
						largestSPSize = model.getVertices().size();
						processWithLargestSP = containingProcess;
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
		if (largestProcess != null && largestProcessSize > largestSPSize) {
			selectedModels.add(largestProcess);
			logger.debug("Process {} with size {} is selected for splitting.", 
					largestProcess.getID(), largestProcessSize);
		} else if (processWithLargestSP != null) {
			selectedModels.add(processWithLargestSP);
			logger.debug("Process {} containing a subprocess of size {} is selected for splitting.", 
					processWithLargestSP.getID(), largestSPSize);
		}
		return selectedModels;
	}
	
	public Set<MyCluster> getComplexProcessesToReprocessTempDep(ProcessStandardizer standardizer, Set<MyCluster> undividableModels) {
		
		int complexProcessCount = 0;
		StringBuffer complexProcessInfo = new StringBuffer();
		int complexSubprocessCount = 0;
		StringBuffer complexSubprocessInfo = new StringBuffer();
		
		int largestProcessSize = 0;
		MyCluster largestProcess = null;
		Map<MyCluster, CPF> processes = standardizer.getStandardizedProcesses();
		for (MyCluster c : processes.keySet()) {
			if (!undividableModels.contains(c)) {
				CPF model = processes.get(c);
				if (isComplex(model)) {
					if (logger.isDebugEnabled()) {
						complexProcessCount++;
						complexProcessInfo.append(model.getVertices().size() + ", ");
					}
					if (model.getVertices().size() > largestProcessSize) {
						largestProcessSize = model.getVertices().size();
						largestProcess = c;
					}
				}
			}
		}

		int largestSPSize = 0;
		MyCluster processWithLargestSP = null;
		Map<String, Integer> fragments = standardizer.getStandardizedSubprocessSizes();
		for (String sp : fragments.keySet()) {
			Integer ssize = fragments.get(sp);
			if (ssize > MiningConfig.COMPLEXITY_MATRIC_N) {
				if (logger.isDebugEnabled()) {
					complexSubprocessCount++;
					complexSubprocessInfo.append(ssize + ", ");
					
					if (ssize > MiningConfig.MIN_GED_FRAGMENT_SIZE) {
						logger.error("SUBPROCESS {} HAS SIZE: {}", sp, ssize);
					}
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
					if (ssize > largestSPSize) {
						largestSPSize = ssize;
						processWithLargestSP = containingProcess;
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
		if (largestProcess != null && largestProcessSize > largestSPSize) {
			selectedModels.add(largestProcess);
			logger.debug("Process {} with size {} is selected for splitting.", 
					largestProcess.getID(), largestProcessSize);
		} else if (processWithLargestSP != null) {
			selectedModels.add(processWithLargestSP);
			logger.debug("Process {} containing a subprocess of size {} is selected for splitting.", 
					processWithLargestSP.getID(), largestSPSize);
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
	
	public Set<MyCluster> getComplexProcessesToReprocessAll(Map<MyCluster, CPF> processes) {
		
		Set<MyCluster> selectedModels = new HashSet<MyCluster>();
		Set<MyCluster> allModels = getAllComplexProcessesToReprocess(processes);
		selectedModels.addAll(allModels);
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
		return checkNOAJS(model, MiningConfig.COMPLEXITY_MATRIC_N) || checkCNC(model, cncThreshold);
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
