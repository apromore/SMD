package org.prom6.plugins.guidetreeminer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.framework.plugin.PluginContext;
import org.prom6.plugins.guidetreeminer.algorithm.AgglomerativeHierarchicalClustering;
import org.prom6.plugins.guidetreeminer.distance.EuclideanDistance;
import org.prom6.plugins.guidetreeminer.distance.GenericEditDistance;
import org.prom6.plugins.guidetreeminer.distance.LevenshteinDistance;
import org.prom6.plugins.guidetreeminer.encoding.ActivityOverFlowException;
import org.prom6.plugins.guidetreeminer.encoding.EncodeActivitySet;
import org.prom6.plugins.guidetreeminer.encoding.EncodeTraces;
import org.prom6.plugins.guidetreeminer.encoding.EncodingNotFoundException;
import org.prom6.plugins.guidetreeminer.featureextraction.FeatureExtraction;
import org.prom6.plugins.guidetreeminer.featureextraction.FeatureMatrix;
import org.prom6.plugins.guidetreeminer.featureextraction.FeatureSelection;
import org.prom6.plugins.guidetreeminer.scoringmatrices.IndelSubstitutionMatrix;
import org.prom6.plugins.guidetreeminer.similarity.FScoreSimilarity;
import org.prom6.plugins.guidetreeminer.tree.GuideTree;
import org.prom6.plugins.guidetreeminer.tree.GuideTreeNode;
import org.prom6.plugins.guidetreeminer.types.DistanceMetricType;
import org.prom6.plugins.guidetreeminer.types.GTMFeature;
import org.prom6.plugins.guidetreeminer.types.GTMFeatureType;
import org.prom6.plugins.guidetreeminer.types.LearningAlgorithmType;
import org.prom6.plugins.guidetreeminer.types.Normalization;
import org.prom6.plugins.guidetreeminer.types.SimilarityDistanceMetricType;
import org.prom6.plugins.guidetreeminer.util.EquivalenceClass;
import org.prom6.plugins.guidetreeminer.util.FileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 July 2010
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */


public class MineGuideTree {
	
	private static final Logger logger = LoggerFactory.getLogger(MineGuideTree.class);
	
	PluginContext context;
	XLog log;

	// The input settings for mining the guide tree
	GuideTreeMinerInput input;
	// The guide tree to output
	GuideTree guideTree;

	int encodingLength, averageTraceLength;

	Map<String, String> charActivityMap, activityCharMap;
	List<String> encodedTraceList, uniqueEncodedTraceList;
	Set<String> duplicateTraceSet;
	Map<String, TreeSet<Integer>> encodedTraceIdenticalIndicesMap;

	Map<String, Integer> encodedActivityCountMap,
			encodedActivityUniqueTraceCountMap;

	Map<GTMFeature, Set<String>> featureOriginalSequenceFeatureSetMap;
	Map<GTMFeature, Set<String>> featureBaseSequenceFeatureSetMap;
	Map<GTMFeature, Set<String>> featureActualSequenceFeatureSetMap = new HashMap<GTMFeature, Set<String>>();

	Map<GTMFeature, Map<Set<String>, Set<String>>> featureOriginalAlphabetFeatureSetMap = new HashMap<GTMFeature, Map<Set<String>, Set<String>>>();
	Map<GTMFeature, Map<Set<String>, Set<String>>> featureBaseAlphabetFeatureSetMap = new HashMap<GTMFeature, Map<Set<String>, Set<String>>>();
	Map<GTMFeature, Map<Set<String>, Set<String>>> featureActualAlphabetFeatureSetMap = new HashMap<GTMFeature, Map<Set<String>, Set<String>>>();
	Set<String> unionSequenceFeatureSet;
	Set<String> filteredSequenceFeatureSet;

	Map<String, Integer> actualNonOverlapSequenceCountMap = new HashMap<String, Integer>();
	Map<String, Integer> actualNonOverlapSequenceInstanceCountMap = new HashMap<String, Integer>();

	Map<Set<String>, Integer> actualNonOverlapAlphabetCountMap = new HashMap<Set<String>, Integer>();
	Map<Set<String>, Integer> actualNonOverlapAlphabetInstanceCountMap = new HashMap<Set<String>, Integer>();
	Map<Set<String>, Set<String>> actualNonOverlapPatternAlphabetEquivalenceClassPatternSetMap = new HashMap<Set<String>, Set<String>>();

	float[][] similarityDistanceMatrix;
	ClusterLogOutput clusterLogOutput;

	int MAX_STRLENGTH = 5000;
	AgglomerativeHierarchicalClustering ahc;
	boolean isSimilarity = false;
	
	public Object[] mine(PluginContext context, GuideTreeMinerInput input,
			XLog log) {
		this.context = context;
		this.log = log;
		this.input = input;
		
		context.log("Starting to encode log");
		encodeLog();
		context.log("Finished encoding log");

//		List<String> tempList = new ArrayList<String>();
//		tempList.addAll(uniqueEncodedTraceList);
//		
//		uniqueEncodedTraceList.clear();
//		uniqueEncodedTraceList.addAll(tempList.subList(0, 15));
		try{
		if (input.getFeatureType().equals(GTMFeatureType.Sequence)) {
			context.log("Computing Feature Sets");
			computeFeatureSets();
			context.log("Feature Sets Computed");
			setActualSequencePattern();
			if (featureActualSequenceFeatureSetMap.size() > 1)
				findNonOverlapSequencePatternCountOptimized(featureActualSequenceFeatureSetMap
						.get(GTMFeature.MIX));
			else {
				findNonOverlapSequencePatternCountOptimized(featureActualSequenceFeatureSetMap
						.get(input.getSelectedFeatureSet().toArray()[0]));
			}
			similarityDistanceMatrix = computeSimilarityDistanceMatrix();
		} else if (input.getFeatureType().equals(GTMFeatureType.Alphabet)) {
			context.log("Computing Feature Sets");
			computeFeatureSets();
			context.log("Feature Sets Computed");

			context.log("Setting Feature Sets");
			setActualAlphabetPattern();
			context.log("Feature Set set");
			
//			System.out.println(featureActualAlphabetFeatureSetMap.size()+" @ "+featureActualAlphabetFeatureSetMap.keySet());
			
			if (featureActualAlphabetFeatureSetMap.size() > 1)
				findNonOverlapAlphabetPatternCountOptimized(featureActualAlphabetFeatureSetMap
						.get(GTMFeature.MIXA));
			else
				findNonOverlapAlphabetPatternCountOptimized(featureActualAlphabetFeatureSetMap
						.get(input.getSelectedFeatureSet().toArray()[0]));
			similarityDistanceMatrix = computeSimilarityDistanceMatrix();
		} else if (input.getFeatureType().equals(GTMFeatureType.WholeTrace)) {
			if (input.getDistanceMetricType() == DistanceMetricType.GenericEditDistance) {
				IndelSubstitutionMatrix indelSubstitutionMatrix = new IndelSubstitutionMatrix(
						encodingLength, encodedTraceList);
				Map<String, Integer> substitutionScoreMap = indelSubstitutionMatrix
						.getSubstitutionScoreMap();
				Map<String, Integer> indelRightGivenLeftScoreMap = indelSubstitutionMatrix
						.getIndelRightGivenLeftScoreMap();
				FileIO io = new FileIO();
				String tempDir = System.getProperty("java.io.tmpdir");
				context.log("Temp Dir: " + tempDir);
				io.writeToFile(tempDir, "substitutionScoreMap.txt",
						substitutionScoreMap, "\\^");
				io.writeToFile(tempDir, "indelScoreMap.txt",
						indelRightGivenLeftScoreMap, "\\^");
				GenericEditDistance ged = new GenericEditDistance(
						encodingLength, uniqueEncodedTraceList,
						substitutionScoreMap, indelRightGivenLeftScoreMap, 4, 1);
				similarityDistanceMatrix = ged.getSimilarityMatrix();
				isSimilarity = true;
			} else {
				LevenshteinDistance led = new LevenshteinDistance(
						encodingLength, uniqueEncodedTraceList);
				similarityDistanceMatrix = led.getDistanceMatrix();
				isSimilarity = false;
			}
		}

		if (input.getLearningAlgorithmType() == LearningAlgorithmType.AHC) {

			// Perform the agglomerative hierarchical clustering
			if (isSimilarity)
				ahc = new AgglomerativeHierarchicalClustering(
						uniqueEncodedTraceList, similarityDistanceMatrix,
						SimilarityDistanceMetricType.Similarity, input
								.getAhcJoinType());
			else
				ahc = new AgglomerativeHierarchicalClustering(
						uniqueEncodedTraceList, similarityDistanceMatrix,
						SimilarityDistanceMetricType.Distance, input
								.getAhcJoinType());

			guideTree = ahc.getGuideTree();
			guideTree
					.setEncodedTraceIdenticalIndicesSetMap(encodedTraceIdenticalIndicesMap);
			guideTree.setEncodingLength(encodingLength);
			guideTree.setEncodedTraceList(encodedTraceList);
			guideTree.setLog(log);
			guideTree.setActivityCharMap(activityCharMap);
			guideTree.setCharActivityMap(charActivityMap);

			// If the user has chosen to output the traces pertaining to each
			// cluster as a separate log, then create the cluster logs and push
			// it onto the framework
			if (input.isOutputClusterLogs) {
				int noClusters = input.getNoClusters();
				
				List<XLog> clusterLogList = new ArrayList<XLog>();

				List<List<String>> clusterEncodedTraceList = guideTree
						.getClusters(noClusters);
				Set<Integer> identicalTraceSet;
				List<String> encodedTraceList;
				XLog currentClusterLog;
				
				for (int i = 0; i < noClusters; i++){
					currentClusterLog = new XLogImpl(log.getAttributes());
					encodedTraceList = clusterEncodedTraceList.get(i);
//					System.out.println("Cluster: "+i+" @ "+encodedTraceList.size());
					for (String encodedTrace : encodedTraceList) {
						if (encodedTraceIdenticalIndicesMap
								.containsKey(encodedTrace)) {
							identicalTraceSet = encodedTraceIdenticalIndicesMap
									.get(encodedTrace);
							
							for (Integer traceIndex : identicalTraceSet) {
								currentClusterLog.add(log
										.get(traceIndex));
							}
						} else {
							context.log("EncodedTrace Not Found");
						}
					}
//					context.getProvidedObjectManager().createProvidedObject(
//							"GuideTreeClusterLog " + i, currentClusterLog,
//							XLog.class, context);
//					context.getResourceManager().getResourceForInstance(clusterLog[i]).setFavorite(
//										true);
					
					// original code
//					context.getProvidedObjectManager().createProvidedObject(
//							"GuideTreeClusterLog " + i, currentClusterLog,
//							XLog.class, context);
					// end of original code
					
					clusterLogList.add(currentClusterLog);
				}
				clusterLogOutput = new ClusterLogOutput(noClusters, clusterLogList);
			}else{
				clusterLogOutput = new ClusterLogOutput(0, new ArrayList<XLog>());
				guideTree.getClusters(0);
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
//		context.addConnection(new GuideTreeMinerConnection(guideTree, clusterLogOutput, input, log));
//		context.getFutureResult(0).setLabel("Guide Tree");
//		context.getFutureResult(1).setLabel("Cluster Log Output");
		return new Object[]{guideTree, clusterLogOutput, input, log};
	}
	
	public XLog getLog(GuideTreeNode node) {
		XLog nodeLog = new XLogImpl(log.getAttributes());
		
		Collection<String> encodedTraces = getEncodedTraces(node);
		for (String encodedTrace : encodedTraces) {
			if (encodedTraceIdenticalIndicesMap.containsKey(encodedTrace)) {
				Collection<Integer> tids = encodedTraceIdenticalIndicesMap.get(encodedTrace);
				for (int tid : tids) {
					nodeLog.add(log.get(tid));
				}
			}
		}
		
		return nodeLog;
	}
	
	public Collection<String> getEncodedTraces(GuideTreeNode clusterRoot) {
		
		Collection<String> traces = new ArrayList<String>();
		
		Queue<GuideTreeNode> q = new LinkedList<GuideTreeNode>();
		q.add(clusterRoot);
		
		while (!q.isEmpty()) {
			GuideTreeNode n = q.poll();
			
			if (n.getLeft() == null && n.getRight() == null) {
				String trace = n.getEncodedTrace();
				traces.add(trace);
			}
			
			if (n.getLeft() != null) {
				q.add(n.getLeft());
			}
			
			if (n.getRight() != null) {
				q.add(n.getRight());
			}
		}
		return traces;
	}

	private void encodeLog() {
		/*
		 * traceIndex is used to keep track of the number of traces;
		 * totalNoEvents counts the number of events/activities in the entire
		 * event log. activitySet accumulates the set of distinct
		 * activities/events in the event log; it doesn't store the trace
		 * identifier for encoding; Encoding trace identifier is required only
		 * when any of the maximal repeat (alphabet) features is selected
		 */

		int traceIndex = 0;
		int totalNoEvents = 0;
		Set<String> activitySet = new HashSet<String>();
		XAttributeMap attributeMap;
		Set<String> eventTypeSet = new HashSet<String>();

		for (XTrace trace : log) {
			totalNoEvents += trace.size();
			for (XEvent event : trace) {
				attributeMap = event.getAttributes();
//				System.out.println("ATTRMAP: "+attributeMap.keySet());
				if(!attributeMap.containsKey("concept:name"))
					continue;
				activitySet.add(attributeMap.get("concept:name").toString()
						+ "-"
						+ attributeMap.get("lifecycle:transition").toString());
				eventTypeSet.add(attributeMap.get("lifecycle:transition")
						.toString());
			}
		}

		averageTraceLength = totalNoEvents / log.size();
		encodedTraceIdenticalIndicesMap = new HashMap<String, TreeSet<Integer>>();
		try {
			EncodeActivitySet encodeActivitySet = new EncodeActivitySet(
					activitySet);
			encodingLength = encodeActivitySet.getEncodingLength();

			activityCharMap = encodeActivitySet.getActivityCharMap();
			charActivityMap = encodeActivitySet.getCharActivityMap();
			/*
			 * Encode each trace to a charStream
			 */
			EncodeTraces encodeTraces = new EncodeTraces(activityCharMap, log);
			encodedTraceList = encodeTraces.getCharStreamList();

			uniqueEncodedTraceList = new ArrayList<String>();

			traceIndex = 0;
			TreeSet<Integer> encodedTraceIdenticalIndicesSet;
			for (String encodedTrace : encodedTraceList) {
				if (encodedTraceIdenticalIndicesMap.containsKey(encodedTrace)) {
					encodedTraceIdenticalIndicesSet = encodedTraceIdenticalIndicesMap
							.get(encodedTrace);
				} else {
					encodedTraceIdenticalIndicesSet = new TreeSet<Integer>();
					uniqueEncodedTraceList.add(encodedTrace);
				}
				encodedTraceIdenticalIndicesSet.add(traceIndex);
				encodedTraceIdenticalIndicesMap.put(encodedTrace,
						encodedTraceIdenticalIndicesSet);

				traceIndex++;
			}
			duplicateTraceSet = new HashSet<String>();
			for (String encodedTrace : encodedTraceIdenticalIndicesMap.keySet()) {
				if (encodedTraceIdenticalIndicesMap.get(encodedTrace).size() > 1)
					duplicateTraceSet.add(encodedTrace);
			}

			encodedActivityCountMap = new HashMap<String, Integer>();
			encodedActivityUniqueTraceCountMap = new HashMap<String, Integer>();
			int traceLength, noIdenticalTraces, count;
			String encodedActivity;

			for (String encodedTrace : uniqueEncodedTraceList) {
				noIdenticalTraces = encodedTraceIdenticalIndicesMap.get(
						encodedTrace).size();
				traceLength = encodedTrace.length() / encodingLength;

				for (int i = 0; i < traceLength; i++) {
					encodedActivity = encodedTrace.substring(
							i * encodingLength, (i + 1) * encodingLength);
					count = noIdenticalTraces;
					if (encodedActivityCountMap.containsKey(encodedActivity)) {
						count += encodedActivityCountMap.get(encodedActivity);
					}
					encodedActivityCountMap.put(encodedActivity, count);

					count = 1;
					if (encodedActivityUniqueTraceCountMap
							.containsKey(encodedActivity)) {
						count += encodedActivityUniqueTraceCountMap
								.get(encodedActivity);
					}
					encodedActivityUniqueTraceCountMap.put(encodedActivity,
							count);
				}
			}
		} catch (ActivityOverFlowException e) {
			e.printStackTrace();
		} catch (EncodingNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void computeFeatureSets() {
		if (featureOriginalSequenceFeatureSetMap == null)
			featureOriginalSequenceFeatureSetMap = new HashMap<GTMFeature, Set<String>>();
		if (featureBaseSequenceFeatureSetMap == null)
			featureBaseSequenceFeatureSetMap = new HashMap<GTMFeature, Set<String>>();
		if (featureOriginalAlphabetFeatureSetMap == null)
			featureOriginalAlphabetFeatureSetMap = new HashMap<GTMFeature, Map<Set<String>, Set<String>>>();
		if (featureBaseAlphabetFeatureSetMap == null)
			featureBaseAlphabetFeatureSetMap = new HashMap<GTMFeature, Map<Set<String>, Set<String>>>();

		featureOriginalSequenceFeatureSetMap.clear();
		featureBaseSequenceFeatureSetMap.clear();

		featureOriginalAlphabetFeatureSetMap.clear();
		featureBaseAlphabetFeatureSetMap.clear();

		Set<GTMFeature> selectedFeatureSet = input.getSelectedFeatureSet();
		
		List<GTMFeature> repeatFeatureTypeList = new ArrayList<GTMFeature>();
		List<GTMFeature> repeatAlphabetFeatureTypeList = new ArrayList<GTMFeature>();
		Map<Set<String>, Set<String>> alphabetFeatureSetMap;
		FeatureSelection featureSelection;
		for (GTMFeature feature : selectedFeatureSet) {
			switch (feature) {
			case IE:
				featureOriginalSequenceFeatureSetMap.put(feature,
						charActivityMap.keySet());
				featureBaseSequenceFeatureSetMap.put(feature, charActivityMap
						.keySet());
				alphabetFeatureSetMap = (new FeatureExtraction(
						encodingLength, encodedTraceList, activityCharMap,
						feature)).getAlphabetFeatureSetMap();
				featureOriginalAlphabetFeatureSetMap.put(feature,
						alphabetFeatureSetMap);
				featureSelection = new FeatureSelection(encodingLength);
				featureBaseAlphabetFeatureSetMap
				.put(feature,featureSelection.getBaseFeatureAlphabetFeatureSetMap(alphabetFeatureSetMap));
				break;
			case KGram:
				Set<String> kGramFeatureSet = new FeatureExtraction(
						encodingLength, encodedTraceList, feature, input
								.getKGramValue()).getSequenceFeatureSet();
				featureOriginalSequenceFeatureSetMap.put(feature,
						kGramFeatureSet);
				featureBaseSequenceFeatureSetMap.put(feature, kGramFeatureSet);
				break;
			case TR:
				Set<String> tandemRepeatFeatureSet = (new FeatureExtraction(
						encodingLength, encodedTraceList, activityCharMap,
						feature)).getSequenceFeatureSet();
				featureOriginalSequenceFeatureSetMap.put(feature,
						tandemRepeatFeatureSet);
				featureSelection = new FeatureSelection(
						encodingLength);
				featureBaseSequenceFeatureSetMap.put(feature, featureSelection
						.getBaseFeatureSet(tandemRepeatFeatureSet));

				break;
			case MR:
			case SMR:
			case NSMR:
				repeatFeatureTypeList.add(feature);
				break;
			case TRA:
				alphabetFeatureSetMap = (new FeatureExtraction(
						encodingLength, encodedTraceList, activityCharMap,
						feature)).getAlphabetFeatureSetMap();
				featureSelection = new FeatureSelection(encodingLength);
				featureOriginalAlphabetFeatureSetMap.put(feature,
						alphabetFeatureSetMap);
				featureBaseAlphabetFeatureSetMap
						.put(
								feature,
								featureSelection
										.getBaseFeatureAlphabetFeatureSetMap(alphabetFeatureSetMap));
				break;
			case MRA:
			case SMRA:
			case NSMRA:
				repeatAlphabetFeatureTypeList.add(feature);
				break;
			default:
				break;
			}
		}

		if ((repeatFeatureTypeList.size() > 0)
				|| (repeatAlphabetFeatureTypeList.size() > 0)) {
			/*
			 * Create a new encoded trace set so that the trace indices are
			 * considered for encoding; This would be used as a distinct
			 * delimiter for combining the streams
			 */

			int traceIndex = 0, totalNoEvents = 0;
			int encodingLength = 1;
			Set<String> activitySet = new HashSet<String>();
			XAttributeMap attributeMap;
			Map<String, String> extendedActivityCharMap = null;
			Map<String, String> extendedCharActivityMap = null;

			StringBuilder combinedStringBuilder = new StringBuilder();
			for (XTrace trace : log) {
				totalNoEvents += trace.size();
				for (XEvent event : trace) {
					attributeMap = event.getAttributes();
					if(attributeMap == null || !attributeMap.containsKey("concept:name"))
						continue;
					activitySet.add(attributeMap.get("concept:name").toString()
							+ "-"
							+ attributeMap.get("lifecycle:transition")
									.toString());
				}
				activitySet.add("Trace" + traceIndex);
				traceIndex++;
			}

			try {
				EncodeActivitySet encodeActivitySet = new EncodeActivitySet(
						activitySet);
				encodingLength = encodeActivitySet.getEncodingLength();

				extendedActivityCharMap = encodeActivitySet
						.getActivityCharMap();
				extendedCharActivityMap = encodeActivitySet
						.getCharActivityMap();

				/*
				 * Encode each trace to a charStream
				 */
				EncodeTraces encodeTraces = new EncodeTraces(
						extendedActivityCharMap, log);
				List<String> extendedEncodedTraceList = encodeTraces
						.getCharStreamList();

				Set<String> extendedEncodedTraceSet = new HashSet<String>();
				extendedEncodedTraceSet.addAll(extendedEncodedTraceList);

				traceIndex = 0;
				for (String charStream : extendedEncodedTraceSet) {
					combinedStringBuilder.append(charStream);
					combinedStringBuilder.append(extendedActivityCharMap
							.get("Trace" + traceIndex));
					traceIndex++;
				}
			} catch (ActivityOverFlowException e) {
				context.log("Error while encoding Log: "+e.getLocalizedMessage());
			} catch (EncodingNotFoundException e) {
				context.log("Error while encoding Log: "+e.getLocalizedMessage());
			}

			FeatureExtraction featureExtraction = new FeatureExtraction();
			if (repeatFeatureTypeList.size() > 0) {
				Set<String> repeatFeatureSet;
				featureSelection = new FeatureSelection(
						encodingLength);
				Map<GTMFeature, Set<String>> repeatFeatureFeatureSetMap = featureExtraction
						.getRepeatFeatureFeatureSetMap(encodingLength,
								combinedStringBuilder.toString(),
								repeatFeatureTypeList);
				for (GTMFeature feature : repeatFeatureFeatureSetMap.keySet()) {
					repeatFeatureSet = getOriginalEncodedFeatureSet(
							encodingLength, repeatFeatureFeatureSetMap
									.get(feature), extendedCharActivityMap);
					featureOriginalSequenceFeatureSetMap.put(feature,
							repeatFeatureSet);
					featureBaseSequenceFeatureSetMap.put(feature,
							featureSelection
									.getBaseFeatureSet(repeatFeatureSet));
				}
			} else if (repeatAlphabetFeatureTypeList.size() > 0) {
				Map<Set<String>, Set<String>> alphabetFeatureFeatureSetMap;
				featureSelection = new FeatureSelection(
						encodingLength);
				Map<GTMFeature, Map<Set<String>, Set<String>>> repeatAlphabetFeatureFeatureSetMap = featureExtraction
						.getRepeatAlphabetFeatureFeatureSetMap(encodingLength,
								combinedStringBuilder.toString(),
								repeatAlphabetFeatureTypeList);
				for (GTMFeature feature : repeatAlphabetFeatureFeatureSetMap
						.keySet()) {
					alphabetFeatureFeatureSetMap = getOriginalEncodedAlphabetFeatureSetMap(
							encodingLength, repeatAlphabetFeatureFeatureSetMap
									.get(feature), extendedCharActivityMap);
					featureOriginalAlphabetFeatureSetMap.put(feature,
							alphabetFeatureFeatureSetMap);
					featureBaseAlphabetFeatureSetMap
							.put(
									feature,
									featureSelection
											.getBaseFeatureAlphabetFeatureSetMap(alphabetFeatureFeatureSetMap));
				}
			}

		}

		// printFeatureSets();

		computeUnionFeatureSet();
	}

	private Set<String> getOriginalEncodedFeatureSet(
			int extendedEncodingLength, Set<String> featureSet,
			Map<String, String> extendedCharActivityMap) {
		Set<String> originalEncodedFeatureSet = new HashSet<String>();

		int featureLength;
		String symbol, originalEncodedFeature;
		for (String feature : featureSet) {
			featureLength = feature.length() / extendedEncodingLength;
			originalEncodedFeature = "";
			for (int i = 0; i < featureLength; i++) {
				symbol = feature.substring(i * extendedEncodingLength, (i + 1)
						* extendedEncodingLength);
				originalEncodedFeature += activityCharMap
						.get(extendedCharActivityMap.get(symbol));
			}
			originalEncodedFeatureSet.add(originalEncodedFeature);
		}

		return originalEncodedFeatureSet;
	}

	private Map<Set<String>, Set<String>> getOriginalEncodedAlphabetFeatureSetMap(
			int extendedEncodingLength,
			Map<Set<String>, Set<String>> alphabetFeatureSetMap,
			Map<String, String> extendedCharActivityMap) {
		Set<String> unionRepeatSet = new HashSet<String>();
		Set<String> repeatAlphabetEquivalenceClassSet;
		int repeatLength;
		String originalEncodedRepeat;
		for (Set<String> repeatAlphabet : alphabetFeatureSetMap.keySet()) {
			repeatAlphabetEquivalenceClassSet = alphabetFeatureSetMap
					.get(repeatAlphabet);
			for (String repeat : repeatAlphabetEquivalenceClassSet) {
				repeatLength = repeat.length() / extendedEncodingLength;
				originalEncodedRepeat = "";
				for (int i = 0; i < repeatLength; i++) {
					originalEncodedRepeat += activityCharMap
							.get(extendedCharActivityMap.get(repeat.substring(i
									* extendedEncodingLength, (i + 1)
									* extendedEncodingLength)));
				}
				unionRepeatSet.add(originalEncodedRepeat);
			}
		}

		EquivalenceClass equivalenceClass = new EquivalenceClass();
		return equivalenceClass.getAlphabetEquivalenceClassMap(encodingLength,
				unionRepeatSet);
	}

	@SuppressWarnings("unused")
	private void printFeatureSets() {
		FileIO io = new FileIO();
		String outputDir = "";
		String delim = "\\^";
		Set<GTMFeature> selectedFeatureSet = input.getSelectedFeatureSet();
		for (GTMFeature feature : selectedFeatureSet) {
			switch (feature) {
			case IE:
				io.writeToFile(outputDir, "individualEventFeatureSet.txt",
						featureOriginalSequenceFeatureSetMap.get(feature));
				break;
			case KGram:
				io.writeToFile(outputDir, input.getKGramValue()
						+ "GramFeatureSet.txt",
						featureOriginalSequenceFeatureSetMap.get(feature));
				break;
			case TR:
				io.writeToFile(outputDir, "tandemRepeatFeatureSet.txt",
						featureOriginalSequenceFeatureSetMap.get(feature));
				break;
			case MR:
				io.writeToFile(outputDir, "maximalRepeatFeatureSet.txt",
						featureOriginalSequenceFeatureSetMap.get(feature));
				break;
			case SMR:
				io.writeToFile(outputDir, "superMaximalRepeatFeatureSet.txt",
						featureOriginalSequenceFeatureSetMap.get(feature));
				break;
			case NSMR:
				io.writeToFile(outputDir,
						"nearSuperMaximalRepeatFeatureSet.txt",
						featureOriginalSequenceFeatureSetMap.get(feature));
				break;
			case TRA:
				io.writeToFile(outputDir,
						"tandemRepeatAlphabetFeatureSetMap.txt",
						featureOriginalAlphabetFeatureSetMap.get(feature),
						delim);
				break;
			case MRA:
				io.writeToFile(outputDir,
						"maximalRepeatAlphabetFeatureSetMap.txt",
						featureOriginalAlphabetFeatureSetMap.get(feature),
						delim);
				break;
			case SMRA:
				io.writeToFile(outputDir,
						"superMaximalRepeatAlphabetFeatureSetMap.txt",
						featureOriginalAlphabetFeatureSetMap.get(feature),
						delim);
				break;
			case NSMRA:
				io.writeToFile(outputDir,
						"nearSuperMaximalRepeatAlphabetFeatureSetMap.txt",
						featureOriginalAlphabetFeatureSetMap.get(feature),
						delim);
				break;
			default:
				context.log("Other");
			}
		}
	}

	private void computeUnionFeatureSet() {
		context.log("Computing union feature set");
		Set<GTMFeature> selectedFeatureSet = input.getSelectedFeatureSet();

		if (selectedFeatureSet.size() > 1) {
			if (input.getFeatureType().equals(GTMFeatureType.Sequence)) {
				Set<String> unionSequenceFeatureSet = new HashSet<String>();
				for (GTMFeature feature : featureOriginalSequenceFeatureSetMap
						.keySet()) {
					unionSequenceFeatureSet
							.addAll(featureOriginalSequenceFeatureSetMap
									.get(feature));
				}
				featureOriginalSequenceFeatureSetMap.put(GTMFeature.MIX,
						unionSequenceFeatureSet);

				unionSequenceFeatureSet = new HashSet<String>();
				for (GTMFeature feature : featureBaseSequenceFeatureSetMap
						.keySet()) {
					unionSequenceFeatureSet
							.addAll(featureBaseSequenceFeatureSetMap
									.get(feature));
				}
				featureBaseSequenceFeatureSetMap.put(GTMFeature.MIX,
						unionSequenceFeatureSet);
			} else if (input.getFeatureType().equals(GTMFeatureType.Alphabet)) {
				Map<Set<String>, Set<String>> unionAlphabetFeatureSetMap = new HashMap<Set<String>, Set<String>>();
				Map<Set<String>, Set<String>> alphabetFeatureSetMap;
				Set<String> alphabetEquivalenceClassFeatureSet;
				for (GTMFeature feature : featureOriginalAlphabetFeatureSetMap
						.keySet()) {
					alphabetFeatureSetMap = featureOriginalAlphabetFeatureSetMap
							.get(feature);
					for (Set<String> alphabet : alphabetFeatureSetMap.keySet()) {
						if (unionAlphabetFeatureSetMap.containsKey(alphabet)) {
							alphabetEquivalenceClassFeatureSet = unionAlphabetFeatureSetMap
									.get(alphabet);
						} else {
							alphabetEquivalenceClassFeatureSet = new TreeSet<String>();
						}
						alphabetEquivalenceClassFeatureSet
								.addAll(alphabetFeatureSetMap.get(alphabet));
						unionAlphabetFeatureSetMap.put(alphabet,
								alphabetEquivalenceClassFeatureSet);
					}
				}

				featureOriginalAlphabetFeatureSetMap.put(GTMFeature.MIXA,
						unionAlphabetFeatureSetMap);

				unionAlphabetFeatureSetMap = new HashMap<Set<String>, Set<String>>();
				for (GTMFeature feature : featureBaseAlphabetFeatureSetMap
						.keySet()) {
					alphabetFeatureSetMap = featureBaseAlphabetFeatureSetMap
							.get(feature);
					for (Set<String> alphabet : alphabetFeatureSetMap.keySet()) {
						if (unionAlphabetFeatureSetMap.containsKey(alphabet)) {
							alphabetEquivalenceClassFeatureSet = unionAlphabetFeatureSetMap
									.get(alphabet);
						} else {
							alphabetEquivalenceClassFeatureSet = new TreeSet<String>();
						}
						alphabetEquivalenceClassFeatureSet
								.addAll(alphabetFeatureSetMap.get(alphabet));
						unionAlphabetFeatureSetMap.put(alphabet,
								alphabetEquivalenceClassFeatureSet);
					}
				}
				featureBaseAlphabetFeatureSetMap.put(GTMFeature.MIXA,
						unionAlphabetFeatureSetMap);
			}
		}
		context.log("Union feature set computed");
	}

	private void setActualSequencePattern() {
		/*
		 * Set the actualPattern; If baseFeatureCheckBox is selected, then
		 * consider baseFeatureSet else, consider original
		 */
		featureActualSequenceFeatureSetMap.clear();
		if (input.isBaseFeatures) {
			for (GTMFeature feature : featureBaseSequenceFeatureSetMap.keySet()) {
				featureActualSequenceFeatureSetMap.put(feature,
						featureBaseSequenceFeatureSetMap.get(feature));
			}
		} else {
			for (GTMFeature feature : featureOriginalSequenceFeatureSetMap
					.keySet()) {
				featureActualSequenceFeatureSetMap.put(feature,
						featureOriginalSequenceFeatureSetMap.get(feature));
			}
		}
	}

	private void setActualAlphabetPattern() {
		/*
		 * Set the actualPattern; If baseFeatureCheckBox is selected, then
		 * consider baseFeatureSet else, consider original
		 */
		featureActualAlphabetFeatureSetMap.clear();
		if (input.isBaseFeatures) {
			for (GTMFeature feature : featureBaseAlphabetFeatureSetMap.keySet()) {
				featureActualAlphabetFeatureSetMap.put(feature,
						featureBaseAlphabetFeatureSetMap.get(feature));
			}
		} else {
			for (GTMFeature feature : featureOriginalAlphabetFeatureSetMap
					.keySet()) {
				featureActualAlphabetFeatureSetMap.put(feature,
						featureOriginalAlphabetFeatureSetMap.get(feature));
			}
		}
	}

	private void findNonOverlapSequencePatternCountOptimized(
			Set<String> sequenceFeatureSet) {
		actualNonOverlapSequenceCountMap.clear();
		actualNonOverlapSequenceInstanceCountMap.clear();

		int patternCount;

		int encodedTraceLength, currentSubTraceLength, currentTraceIndex, splitIndex, count;

		String currentSubTrace;

		Pattern pattern;
		Matcher matcher;
		int noMatches, repeatLength;

		Set<String> encodedTraceContributingSequenceSet = new HashSet<String>();
		for (String encodedTrace : uniqueEncodedTraceList) {
			encodedTraceContributingSequenceSet.clear();

			encodedTraceLength = encodedTrace.length() / encodingLength;
			currentTraceIndex = 0;
			splitIndex = 0;
			while (currentTraceIndex < encodedTraceLength) {
				if (encodedTraceLength - currentTraceIndex < MAX_STRLENGTH) {
					currentSubTrace = encodedTrace.substring(currentTraceIndex
							* encodingLength, encodedTraceLength
							* encodingLength);
					currentTraceIndex = encodedTraceLength + 1;
				} else {
					currentSubTrace = encodedTrace.substring(currentTraceIndex
							* encodingLength,
							(currentTraceIndex + MAX_STRLENGTH)
									* encodingLength);
					currentTraceIndex += MAX_STRLENGTH;
					splitIndex++;
				}
				currentSubTraceLength = currentSubTrace.length()
						/ encodingLength;

				for (String repeatPattern : sequenceFeatureSet) {
					if (!currentSubTrace.contains(repeatPattern))
						continue;
					for (int i = 0; i < currentSubTraceLength; i++) {
						if (currentSubTrace.indexOf(repeatPattern, i
								* encodingLength) == i * encodingLength) {
							pattern = Pattern.compile("(" + repeatPattern
									+ "){1,}");
							matcher = pattern.matcher(currentSubTrace);
							if (matcher.find(i * encodingLength)
									&& matcher.start() == i * encodingLength) {
								repeatLength = repeatPattern.length()
										/ encodingLength;
								noMatches = (matcher.end() - matcher.start())
										/ (repeatLength * encodingLength);

								i += repeatLength * noMatches - 1;

								patternCount = 0;
								if (actualNonOverlapSequenceCountMap
										.containsKey(repeatPattern)) {
									patternCount = actualNonOverlapSequenceCountMap
											.get(repeatPattern);
								}
								actualNonOverlapSequenceCountMap
										.put(repeatPattern, noMatches
												+ patternCount);
								encodedTraceContributingSequenceSet
										.add(repeatPattern);
							}
						}
					}
				}

			}
			for (String sequenceFeature : encodedTraceContributingSequenceSet) {
				count = 0;
				if (actualNonOverlapSequenceInstanceCountMap
						.containsKey(sequenceFeature)) {
					count = actualNonOverlapSequenceInstanceCountMap
							.get(sequenceFeature);
				}
				actualNonOverlapSequenceInstanceCountMap.put(sequenceFeature,
						count + 1);
			}
		}
	}

	private void findNonOverlapAlphabetPatternCountOptimized(
			Map<Set<String>, Set<String>> alphabetFeatureSetMap) {
//		System.out.println("Here: find NonOverlapAlphabetPatternCount: "+alphabetFeatureSetMap.keySet());
		actualNonOverlapAlphabetCountMap.clear();
		actualNonOverlapAlphabetInstanceCountMap.clear();
		actualNonOverlapPatternAlphabetEquivalenceClassPatternSetMap.clear();
		
		Set<String> alphabetEquivalenceClassFeatureSet;
		boolean currentTraceHasPattern;
		int encodedTraceLength;
		int maxCount, patternCount, repeatLength, noMatches, noIdenticalTraces, count;
		String maxCountPattern;
		Pattern pattern;
		Matcher matcher;
		Set<Set<String>> encodedTraceContributingAlphabetSet = new HashSet<Set<String>>();

		/*
		 * For each alphabet, check first whether this sub-trace
		 * contains the repeat under the alphabet; if so, get the
		 * non-overlapping count of that repeat alphabet
		 */
		for(String encodedTrace : uniqueEncodedTraceList){
			encodedTraceLength = encodedTrace.length()/encodingLength;
			noIdenticalTraces = encodedTraceIdenticalIndicesMap.get(encodedTrace).size();
			
			encodedTraceContributingAlphabetSet.clear();
			for (Set<String> alphabet : alphabetFeatureSetMap.keySet()) {
				alphabetEquivalenceClassFeatureSet = alphabetFeatureSetMap.get(alphabet);
				currentTraceHasPattern = false;
				for (String repeatPattern : alphabetEquivalenceClassFeatureSet) {
					if (encodedTrace.contains(repeatPattern)) {
						currentTraceHasPattern = true;
						break;
					}
				}
				
				if (currentTraceHasPattern) {
					for (int i = 0; i < encodedTraceLength; i++) {
						maxCount = 0;
						maxCountPattern = "";
						for (String repeatPattern : alphabetEquivalenceClassFeatureSet) {
							/*
							 * First check if this repeat pattern exists
							 * starting at this index; only if it exists
							 * then use the pattern matcher
							 */
							if(encodedTrace.indexOf(repeatPattern, i*encodingLength) == i*encodingLength){
								pattern = Pattern.compile("(" + repeatPattern + "){1,}");
								matcher = pattern.matcher(encodedTrace);
								if (matcher.find(i * encodingLength) && matcher.start() == i * encodingLength) {
									repeatLength = repeatPattern.length() / encodingLength;
									noMatches = (matcher.end() - matcher.start()) / (repeatLength * encodingLength);
									if (noMatches > maxCount) {
										maxCount = noMatches;
										maxCountPattern = repeatPattern;
									}
								}
							}
						}
						if (maxCount > 0) {
							/*
							 * No need to actually compute the counts again
							 * as we have already stored the maxCount and
							 * maxCountPattern information
							 */
							repeatLength = maxCountPattern.length() / encodingLength;
							
							maxCount *= noIdenticalTraces;
							
							i += repeatLength * maxCount - 1;
	
							patternCount = 0;
							if (actualNonOverlapAlphabetCountMap.containsKey(alphabet)) {
								patternCount = actualNonOverlapAlphabetCountMap.get(alphabet);
							}
							actualNonOverlapAlphabetCountMap.put((TreeSet<String>) alphabet, maxCount + patternCount);
							
							encodedTraceContributingAlphabetSet.add((TreeSet<String>)alphabet);
						}
					}
				}
			}

			for(Set<String> alp : encodedTraceContributingAlphabetSet){
				count = 0;
				if(actualNonOverlapAlphabetInstanceCountMap.containsKey(alp)){
					count = actualNonOverlapAlphabetInstanceCountMap.get(alp);
				}
				
				actualNonOverlapAlphabetInstanceCountMap.put(alp, count+noIdenticalTraces);
				if(!actualNonOverlapPatternAlphabetEquivalenceClassPatternSetMap.containsKey(alp)){
					actualNonOverlapPatternAlphabetEquivalenceClassPatternSetMap.put(alp, alphabetFeatureSetMap.get(alp));
				}
			}
		}
		
//		System.out.println("Done");
		
	}

	private float[][] computeSimilarityDistanceMatrix() {
		if (input.getFeatureType().equals(GTMFeatureType.Sequence)) {
			Set<String> filteredSequenceFeatureSet = getFilteredSequenceFeatures();
			FeatureMatrix featureMatrix = new FeatureMatrix(encodingLength,
					uniqueEncodedTraceList, filteredSequenceFeatureSet);
			if (input.getSimilarityDistanceMetricType() == SimilarityDistanceMetricType.Similarity) {
				isSimilarity = true;
				return new FScoreSimilarity(encodingLength).getFScore(featureMatrix.getFeatureMatrix());
			} else if (input.getSimilarityDistanceMetricType() == SimilarityDistanceMetricType.Distance) {
				isSimilarity = false;
				if (input.getDistanceMetricType() == DistanceMetricType.Euclidean) {
					EuclideanDistance ed = new EuclideanDistance(featureMatrix
							.getFeatureMatrix(), Normalization.NONE);
					return ed.getDistanceMatrix();
				}
			}
		} else if (input.getFeatureType().equals(GTMFeatureType.Alphabet)) {
			Set<Set<String>> filteredAlphabetFeatureSet = getFilteredAlphabetFeatures();
			Map<Set<String>, Set<String>> filteredAlphabetFeatureSetMap = new HashMap<Set<String>, Set<String>>();
			for (Set<String> filteredAlphabet : filteredAlphabetFeatureSet) {
				filteredAlphabetFeatureSetMap.put(filteredAlphabet,
						actualNonOverlapPatternAlphabetEquivalenceClassPatternSetMap
								.get(filteredAlphabet));
			}
			FeatureMatrix featureMatrix = new FeatureMatrix(
					encodingLength, uniqueEncodedTraceList,
					filteredAlphabetFeatureSetMap);
			
			if (input.getSimilarityDistanceMetricType().equals(
					SimilarityDistanceMetricType.Similarity)) {
				isSimilarity = true;
				return new FScoreSimilarity(encodingLength).getFScore(featureMatrix.getFeatureMatrix());
			} else if (input.getSimilarityDistanceMetricType().equals(
					SimilarityDistanceMetricType.Distance)) {
				isSimilarity = false;
				if (input.getDistanceMetricType() == DistanceMetricType.Euclidean) {
					EuclideanDistance ed = new EuclideanDistance(featureMatrix
							.getFeatureMatrix(), Normalization.NONE);
					return ed.getDistanceMatrix();
				}
			}
		}
		return null;
	}

	private Set<String> getFilteredSequenceFeatures() {
		int minFrequencyThreshold = input.getMinFrequencyCountThreshold();
		int minInstanceCountPercentageThreshold = input
				.getMinInstancePercentageCountThreshold();

		Set<String> filteredSequenceFeatureSet = new HashSet<String>();
		filteredSequenceFeatureSet.addAll(actualNonOverlapSequenceCountMap
				.keySet());
		for (String sequenceFeature : actualNonOverlapSequenceCountMap.keySet()) {
			if (actualNonOverlapSequenceCountMap.get(sequenceFeature) < minFrequencyThreshold
					|| actualNonOverlapSequenceInstanceCountMap
							.get(sequenceFeature) < minInstanceCountPercentageThreshold)
				filteredSequenceFeatureSet.remove(sequenceFeature);
		}
		return filteredSequenceFeatureSet;
	}

	private Set<Set<String>> getFilteredAlphabetFeatures() {
		int minFrequencyThreshold = input.getMinFrequencyCountThreshold();
		int minInstanceCountPercentageThreshold = input
				.getMinInstancePercentageCountThreshold();
		int minAlphabetSize = input.getMinAlphabetSizeThreshold();
		int maxAlphabetSize = input.getMaxAlphabetSizeThreshold();
		Set<Set<String>> filteredAlphabetFeatureSet = new HashSet<Set<String>>();
		filteredAlphabetFeatureSet.addAll(actualNonOverlapAlphabetCountMap
				.keySet());
		for (Set<String> alphabetFeature : actualNonOverlapAlphabetCountMap
				.keySet()) {
			if (actualNonOverlapAlphabetCountMap.get(alphabetFeature) < minFrequencyThreshold
					|| actualNonOverlapAlphabetInstanceCountMap
							.get(alphabetFeature) < minInstanceCountPercentageThreshold
					|| alphabetFeature.size() < minAlphabetSize
					|| alphabetFeature.size() > maxAlphabetSize)
				filteredAlphabetFeatureSet.remove(alphabetFeature);
		}
		return filteredAlphabetFeatureSet;
	}

	protected void printMatrix(float[][] matrix) {
		int noRows = matrix.length;
		int noCols = matrix[0].length;

		for (int i = 0; i < noRows; i++) {
			for (int j = 0; j < noCols; j++) {
				logger.trace("Matrix - {}", matrix[i][j]);
//				System.out.format("%3.2f", matrix[i][j]);
//				System.out.print("  ");
			}
//			System.out.println();
		}
	}

	protected void printMatrix(int[][] matrix) {
		int noRows = matrix.length;
		int noCols = matrix[0].length;

		for (int i = 0; i < noRows; i++) {
			for (int j = 0; j < noCols; j++) {
				logger.trace(matrix[i][j] + "  ");
//				System.out.print(matrix[i][j] + "  ");
			}
//			System.out.println();
		}
	}

	public GuideTree getGuideTree() {
		return guideTree;
	}
}
