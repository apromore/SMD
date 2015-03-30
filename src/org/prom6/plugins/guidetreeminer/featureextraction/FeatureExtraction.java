// Om Ganesayanamaha
package org.prom6.plugins.guidetreeminer.featureextraction;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;

import org.prom6.plugins.guidetreeminer.swingx.ErrorDialog;
import org.prom6.plugins.guidetreeminer.types.GTMFeature;
import org.prom6.plugins.guidetreeminer.util.EquivalenceClass;
import org.prom6.plugins.guidetreeminer.util.UkkonenSuffixTree;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 June 2009
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */

public class FeatureExtraction {
	
	private void log(String msg) {
		
	}
	
	class DescendingStringLengthComparator implements Comparator<String> {
		public int compare(String str1, String str2) {
			return (str1.length() > str2.length()) ? 1 : str1.compareTo(str2);
		}
	}

	/*
	 * Sequence based feature set; INDIVIDUAL_EVENT, KGRAM, LOOP and {MAXIMAL,
	 * SUPERMAXIMAL, NEARSUPERMAXIMAL} Repeat features constitute the sequence
	 * based features
	 */
	Set<String> sequenceFeatureSet;

	/*
	 * For the alphabet based features, apart from the alphabet, we also need
	 * the sequences under the equivalence class of the alphabet; so, we need a
	 * map of alphabet (expressed as a Set<String)) and its corresponding
	 * equivalence class of sequences (repeats) expressed as Set<String>
	 * 
	 * {LOOP, MAXIMAL_REPEAT, NEARSUPERMAXIMAL_REPEAT,
	 * SUPERMAXIMAL_REPEAT}_ALPHABET features constitute this variable
	 */
	Map<Set<String>, Set<String>> alphabetFeatureSetMap;

	int encodingLength;
	List<String> charStreamList;

	/*
	 * ActivityCharMap is required for the repeat based features to form the
	 * combined charStream; Each trace need to be delimited by a distinct symbol
	 * which is provided in the activityCharMap as Tracei
	 */
	Map<String, String> activityCharMap;
	

	public FeatureExtraction() {

	}

	public FeatureExtraction(int encodingLength, List<String> charStreamList, Map<String, String> activityCharMap,
			GTMFeature feature) {
		this.encodingLength = encodingLength;
		this.charStreamList = charStreamList;
		this.activityCharMap = activityCharMap;
		EquivalenceClass equivalenceClass = new EquivalenceClass();
		switch (feature) {
			case IE:
				computeIndividualEventFeatureSet();
				alphabetFeatureSetMap = equivalenceClass.getAlphabetEquivalenceClassMap(encodingLength,
						sequenceFeatureSet);
				break;
			case TR :
				computeLoopFeatureSet();
				break;
			case TRA :
				computeLoopFeatureSet();
				alphabetFeatureSetMap = equivalenceClass.getAlphabetEquivalenceClassMap(encodingLength,
						sequenceFeatureSet);
				break;
			case MR :
			case SMR :
			case NSMR :
				computeRepeatFeatureSet(feature);
				break;
			case MRA :
			case SMRA :
			case NSMRA :
				computeRepeatAlphabetFeatureSet(feature);
				break;
			case KGram :
				ErrorDialog.showErrorDialog(new JFrame(), "Gram Size not specified; Can't proceed; Use other constructor for FeatureExtraction");
				System.exit(0);
				break;
			default :
				break;
		}
	}

	public FeatureExtraction(int encodingLength, List<String> charStreamList, GTMFeature feature, int gramSize) {
		if (feature == GTMFeature.KGram) {
			sequenceFeatureSet = new HashSet<String>();
			/*
			 * To reduce the computational/space complexity, we first remove all
			 * duplicates in the charStreamList
			 */
			Set<String> charStreamSet = new HashSet<String>();
			charStreamSet.addAll(charStreamList);

			int charStreamLength;
			for (String charStream : charStreamSet) {
				charStreamLength = charStream.length() / encodingLength;
				for (int i = 0; i < charStreamLength - gramSize + 1; i++) {
					sequenceFeatureSet.add(charStream.substring(i * encodingLength, (i + gramSize) * encodingLength));
				}
			}
		} else {
			System.out
					.println("Wrong Constructor invoked for FeatureExtraction; This constructor is to be used only for KGRAM feature type");
			System.exit(0);
		}
	}

	private void computeIndividualEventFeatureSet() {
		sequenceFeatureSet = new HashSet<String>();

		/*
		 * To reduce the computational/space complexity, we first remove all
		 * duplicates in the charStreamList
		 */
		Set<String> charStreamSet = new HashSet<String>();
		for (String charStream : charStreamList) {
			charStreamSet.add(charStream);
		}

		/*
		 * Process each charStream to extract the feature (individual event) and
		 * add it to the sequenceFeatureSet
		 */
		int charStreamLength;
		for (String charStream : charStreamSet) {
			charStreamLength = charStream.length() / encodingLength;
			for (int i = 0; i < charStreamLength; i++) {
				sequenceFeatureSet.add(charStream.substring(i * encodingLength, (i + 1) * encodingLength));
			}
		}
	}

	private void computeLoopFeatureSet() {
		log("Computing Loop Feature Set");
		sequenceFeatureSet = new HashSet<String>();

		/*
		 * To reduce the computational/space complexity, we first remove all
		 * duplicates in the charStreamList
		 */
		Set<String> charStreamSet = new HashSet<String>();
		for (String charStream : charStreamList) {
			charStreamSet.add(charStream);
		}

		UkkonenSuffixTree suffixTree;
		Map<TreeSet<String>, TreeSet<String>> loopAlphabetLoopPatternSetMap;
		/*
		 * For each charStream compute the set of tandem repeats
		 */
		log("No. CharStreams: "+charStreamSet.size());
		for(String charStream : charStreamSet)
			log(charStream);
		for (String charStream : charStreamSet) {
			if(charStream == null)
				continue;
			if(charStream.length() <= encodingLength)
				continue;
			try{
				log("Calling UST(L): "+charStream);
				suffixTree = new UkkonenSuffixTree(encodingLength, charStream);
				suffixTree.LZDecomposition();
			
				//TODO
				/*
				 * Currently, the ukkonenSuffixTree code, the simple/complex tandem
				 * repeats are computed in the getPrimitiveTandemRepeats method; we
				 * need to modify this when ukkonenSuffixTree is recoded
				 */
				loopAlphabetLoopPatternSetMap = suffixTree.getPrimitiveTandemRepeats();
				if(loopAlphabetLoopPatternSetMap!= null){
					// Extract the loop patterns from the map and put it in the sequenceFeatureSet
					for (TreeSet<String> loopAlphabet : loopAlphabetLoopPatternSetMap.keySet()) {
						sequenceFeatureSet.addAll(loopAlphabetLoopPatternSetMap.get(loopAlphabet));
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			suffixTree = null;
			log("Processed "+charStream);
		}

		//Set no longer required variables to null
		charStreamSet = null;
		log("Computed Loop Feature Set");
	}

	private void computeRepeatFeatureSet(GTMFeature feature) {
		sequenceFeatureSet = new HashSet<String>();

		/*
		 * To reduce the computational/space complexity, we first remove all
		 * duplicates in the charStreamList
		 */
		Set<String> charStreamSet = new HashSet<String>();
		for (String charStream : charStreamList) {
			charStreamSet.add(charStream);
		}

		StringBuilder combinedCharStreamBuilder = new StringBuilder();
		int charStreamIndex = 0;
		int totalCharStreamLength = 0;
		for (String charStream : charStreamSet) {
			combinedCharStreamBuilder.append(charStream);
			totalCharStreamLength += charStream.length();
			if (activityCharMap.containsKey("Trace" + charStreamIndex)) {
				combinedCharStreamBuilder.append(activityCharMap.get("Trace" + charStreamIndex));
			} else {
				ErrorDialog.showErrorDialog(new JFrame(),"Activity Char Map doesn't contain encoding for trace indices");
				System.exit(0);
			}
			charStreamIndex++;
		}
		log("Computing maximal repeats");
		UkkonenSuffixTree suffixTree = new UkkonenSuffixTree(encodingLength, combinedCharStreamBuilder.toString());
		suffixTree.findLeftDiverseNodes();
		log("Computing maximal repeats");
		switch (feature) {
			case MR :
				sequenceFeatureSet.addAll(suffixTree.getMaximalRepeats());
				break;
			case SMR :
				sequenceFeatureSet.addAll(suffixTree.getSuperMaximalRepeats());
				break;
			case NSMR :
				sequenceFeatureSet.addAll(suffixTree.getNearSuperMaximalRepeats());
				break;
			default :
				break;
		}

		//Set no longer required variables to null
		combinedCharStreamBuilder = null;
		charStreamSet = null;
		suffixTree = null;
	}

	private void computeRepeatAlphabetFeatureSet(GTMFeature feature) {
		alphabetFeatureSetMap = new HashMap<Set<String>, Set<String>>();

		/*
		 * To reduce the computational/space complexity, we first remove all
		 * duplicates in the charStreamList
		 */
		Set<String> charStreamSet = new HashSet<String>();
		for (String charStream : charStreamList) {
			charStreamSet.add(charStream);
		}

		StringBuilder combinedCharStreamBuilder = new StringBuilder();
		int charStreamIndex = 0;
		for (String charStream : charStreamSet) {
			combinedCharStreamBuilder.append(charStream);
			if (activityCharMap.containsKey("Trace" + charStreamIndex)) {
				combinedCharStreamBuilder.append(activityCharMap.get("Trace" + charStreamIndex));
			} else {
				ErrorDialog.showErrorDialog(new JFrame(), "Activity Char Map doesn't contain encoding for trace indices");
				System.exit(0);
			}
			charStreamIndex++;
		}

		UkkonenSuffixTree suffixTree = new UkkonenSuffixTree(encodingLength, combinedCharStreamBuilder.toString());
		suffixTree.findLeftDiverseNodes();
		EquivalenceClass equivalenceClass = new EquivalenceClass();
		switch (feature) {
			case MRA :
				alphabetFeatureSetMap = equivalenceClass.getAlphabetEquivalenceClassMap(encodingLength, suffixTree
						.getMaximalRepeats());
				break;
			case SMRA :
				alphabetFeatureSetMap = equivalenceClass.getAlphabetEquivalenceClassMap(encodingLength, suffixTree
						.getSuperMaximalRepeats());
				break;
			case NSMRA :
				alphabetFeatureSetMap = equivalenceClass.getAlphabetEquivalenceClassMap(encodingLength, suffixTree
						.getNearSuperMaximalRepeats());
				break;
			default :
				break;
		}
	}

	public Set<String> getSequenceFeatureSet() {
		return sequenceFeatureSet;
	}

	public Map<Set<String>, Set<String>> getAlphabetFeatureSetMap() {
		return alphabetFeatureSetMap;
	}

	public Map<GTMFeature, Set<String>> getRepeatFeatureFeatureSetMap(int encodingLength,
			List<String> charStreamList, Map<String, String> activityCharStreamMap, List<GTMFeature> featureList) {
		StringBuilder combinedStreamBuilder = new StringBuilder();

		Set<String> charStreamSet = new HashSet<String>();
		charStreamSet.addAll(charStreamList);
		int traceIndex = 0;
		for (String charStream : charStreamSet) {
			combinedStreamBuilder.append(charStream);
			if (activityCharStreamMap.containsKey("Trace" + traceIndex)) {
				combinedStreamBuilder.append(activityCharStreamMap.get("Trace" + traceIndex));
			} else {
				return null;
			}
		}
		return getRepeatFeatureFeatureSetMap(encodingLength, combinedStreamBuilder.toString(), featureList);
	}

	public Map<GTMFeature, Set<String>> getRepeatFeatureFeatureSetMap(int encodingLength, String charStream,
			List<GTMFeature> featureList) {
		Map<GTMFeature, Set<String>> repeatFeatureFeatureSetMap = new HashMap<GTMFeature, Set<String>>();

		UkkonenSuffixTree suffixTree = new UkkonenSuffixTree(encodingLength, charStream);
		suffixTree.findLeftDiverseNodes();
		for (GTMFeature feature : featureList) {
			switch (feature) {
				case MR :
					repeatFeatureFeatureSetMap.put(feature, suffixTree.getMaximalRepeats());
					break;
				case SMR :
					repeatFeatureFeatureSetMap.put(feature, suffixTree
							.getSuperMaximalRepeats());
					break;
				case NSMR :
					repeatFeatureFeatureSetMap.put(feature, suffixTree
							.getNearSuperMaximalRepeats());
					break;
				default :
					return null;
			}
		}

		return repeatFeatureFeatureSetMap;
	}

	public Map<GTMFeature, Map<Set<String>, Set<String>>> getRepeatAlphabetFeatureFeatureSetMap(
			int encodingLength, List<String> charStreamList, Map<String, String> activityCharStreamMap,
			List<GTMFeature> featureList) {
		StringBuilder combinedStreamBuilder = new StringBuilder();

		Set<String> charStreamSet = new HashSet<String>();
		charStreamSet.addAll(charStreamList);
		int traceIndex = 0;
		for (String charStream : charStreamSet) {
			combinedStreamBuilder.append(charStream);
			if (activityCharStreamMap.containsKey("Trace" + traceIndex)) {
				combinedStreamBuilder.append(activityCharStreamMap.get("Trace" + traceIndex));
			} else {
				return null;
			}
		}

		return getRepeatAlphabetFeatureFeatureSetMap(encodingLength, combinedStreamBuilder.toString(),
				featureList);
	}

	public Map<GTMFeature, Map<Set<String>, Set<String>>> getRepeatAlphabetFeatureFeatureSetMap(
			int encodingLength, String charStream, List<GTMFeature> featureList) {
		Map<GTMFeature, Map<Set<String>, Set<String>>> repeatAlphabetFeatureTypeFeatureSetMap = new HashMap<GTMFeature, Map<Set<String>, Set<String>>>();

		UkkonenSuffixTree suffixTree = new UkkonenSuffixTree(encodingLength, charStream);
		suffixTree.findLeftDiverseNodes();
		EquivalenceClass equivalenceClass = new EquivalenceClass();
		for (GTMFeature feature : featureList) {
			switch (feature) {
				case MRA :
					repeatAlphabetFeatureTypeFeatureSetMap.put(feature, equivalenceClass
							.getAlphabetEquivalenceClassMap(encodingLength, suffixTree.getMaximalRepeats()));
					break;
				case SMRA :
					repeatAlphabetFeatureTypeFeatureSetMap.put(feature,
							equivalenceClass.getAlphabetEquivalenceClassMap(encodingLength, suffixTree
									.getSuperMaximalRepeats()));
					break;
				case NSMRA :
					repeatAlphabetFeatureTypeFeatureSetMap.put(feature, equivalenceClass
							.getAlphabetEquivalenceClassMap(encodingLength, suffixTree.getNearSuperMaximalRepeats()));
					break;
				default :
					return null;
			}
		}

		return repeatAlphabetFeatureTypeFeatureSetMap;
	}
}
