package org.apromore.mining.standardize;

import graph.Graph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import merge.MergingPaper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apromore.common.FSConstants;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.mining.EvaluatorUtil;
import org.apromore.mining.MiningConfig;
import org.apromore.service.FragmentService;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.apromore.service.utils.IDGenerator;
import org.jbpt.pm.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ProcessMerger {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessMerger.class);
	
	@Autowired @Qualifier("FragmentService")
	private FragmentService fsrv;
	
	private EPCSerializer epcSerializer = new EPCSerializer();
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	
	private Map<FragmentIdCollection, String> mergedFragments = new HashMap<ProcessMerger.FragmentIdCollection, String>();

	public void initialize() {
		mergedFragments.clear();
		String mergedModelsPath = MiningConfig.MERGED_MODELS_PATH;
		try {
			FileUtils.cleanDirectory(new File(mergedModelsPath));
		} catch (IOException e) {
			logger.error("Failed to clean the merged model storage folder.", e);
		}
	}
	
	public String merge(Collection<String> fids) {
		
		EvaluatorUtil.mergingStart();
		
		EvaluatorUtil.incrementMergingAttempts();
		EvaluatorUtil.addMergedFragments(fids.size());
		
		FragmentIdCollection fidCollection = new FragmentIdCollection(fids);
		String mergedFragmentId = mergedFragments.get(fidCollection);
		if (mergedFragmentId == null) {
			EvaluatorUtil.incrementNewMerges();
			try {
				mergedFragmentId = IDGenerator.generateFragmentID();
				String mergedModelsPath = MiningConfig.MERGED_MODELS_PATH;
				File mergedFile = new File(mergedModelsPath, mergedFragmentId + ".epml");
				mergeModels(fids, mergedFile.getAbsolutePath());
				mergedFragments.put(fidCollection, mergedFragmentId);
			} catch (Exception e) {
				logger.error("Failed to merge fragments {}", fidCollection.toString());
			}
		} else {
			EvaluatorUtil.incrementMergeCacheHits();
			logger.trace("Retreived merged fragment from the cache for {}", fidCollection.toString());
		}
		
		EvaluatorUtil.mergingEnd();
		return mergedFragmentId;
	}

	public CPF getMergedFragment(String mergedFragmentId) {
		String mergedModelsPath = MiningConfig.MERGED_MODELS_PATH;
		File mergedFile = new File(mergedModelsPath, mergedFragmentId + ".epml");
		if (!mergedFile.exists()) {
			return null;
		}
		CPF cpf = epcDeserializer.deserializeFile(mergedFile.getAbsolutePath());
		return cpf;
	}
	
	public void mergeModels(Collection<String> fids, String outPath) throws Exception {
		File outFile = new File(outPath);
		if (logger.isTraceEnabled()) {
			StringBuffer buffer = new StringBuffer();
			for (String fid : fids) {
				buffer.append(fid + ", ");
			}
			logger.trace("Merging fragments: {} into the fragment {}", buffer.toString(), outFile.getName());
		}

		String mergedEPML = null;
		for (String currentFid : fids) {
			CPF currentModel = fsrv.getFragment(currentFid, false);
			if (MiningConfig.ADD_COMMONG_START_END_EVENTS_BEFORE_MERGING) {
				addCommonStartAndEndEvents(currentModel);
			}
			String currentEPML = epcSerializer.serializeToString(currentModel);
			if (mergedEPML == null) {
				mergedEPML = currentEPML;
			} else {
				String newMergedModel = merge(mergedEPML, currentEPML);
				mergedEPML = newMergedModel;
			}
		}
		
		if (mergedEPML != null) {
			FileOutputStream outStream = new FileOutputStream(outFile);
			IOUtils.write(mergedEPML, outStream);
			outStream.close();
		}
	}
	
	private void addCommonStartAndEndEvents(CPF model) {
		
		Collection<FlowNode> entries = model.getEntries();
		FlowNode entry = entries.iterator().next();
		
		Collection<FlowNode> exists = model.getExits();
		FlowNode exit = exists.iterator().next();
		
		CpfEvent startEvent = new CpfEvent("Subprocess Start");
		model.addFlowNode(startEvent);
		model.setVertexProperty(startEvent.getId(), FSConstants.TYPE, FSConstants.EVENT);
		model.addControlFlow(startEvent, entry);
		
		CpfEvent endEvent = new CpfEvent("Subprocess End");
		model.addFlowNode(endEvent);
		model.setVertexProperty(endEvent.getId(), FSConstants.TYPE, FSConstants.EVENT);
		model.addControlFlow(exit, endEvent);
	}
	
	public String merge(String epml1, String epml2) {
		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
		
		Graph.cleanGraphIDs();
		
		Graph g1 = MergableEPCParser.readModels(epml1, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		Graph g2 = MergableEPCParser.readModels(epml2, false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();

		MergingPaper mp = new MergingPaper();
		Graph merged = mp.mergeModels(g1, g2);
		
		String mergedEPML = MergableEPCParser.writeModel(merged);
		return mergedEPML;
	}
	
	class FragmentIdCollection {
		private Set<String> fids = new HashSet<String>();
		
		public FragmentIdCollection(Collection<String> fragmentIds) {
			fids.addAll(fragmentIds);
		}
		
		public boolean match(Collection<String> fragmentIds) {
			if (this.fids.size() == fragmentIds.size()) {
				if (this.fids.containsAll(fragmentIds)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hashCode = 0;
			for (String fid : fids) {
				hashCode += fid.hashCode();
			}
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			
			if (obj == null) {
				return false;
			}
			
			if (!(obj instanceof FragmentIdCollection)) {
				return false;
			}
			
			FragmentIdCollection otherFids = (FragmentIdCollection) obj;
			return otherFids.match(fids);
		}

		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			for (String fid : this.fids) {
				buffer.append(fid + ", ");
			}
			return buffer.toString();
		}
	}
}
