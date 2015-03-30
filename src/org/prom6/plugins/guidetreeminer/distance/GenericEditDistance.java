package org.prom6.plugins.guidetreeminer.distance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.prom6.plugins.guidetreeminer.util.FileIO;

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

public class GenericEditDistance {
	/*
	 * the encoding length of activities
	 */
	int encodingLength;

	/*
	 * the scale factor between 0.0 and 1.0 to be used for the indel scores;
	 * Large positive indel values would allow the alignment to have too many
	 * indels
	 */
	float indelScaleFactor;
	float lengthRatioThreshold;

	Map<String, Integer> substitutionScoreMap;
	Map<String, Integer> indelRightGivenLeftScoreMap;
	Map<String, Integer> indelLeftGivenRightScoreMap;

	int[][] M;

	int minSubstitutionScore, gapOpenCost, incrementLikeSubstitutionScore;
	String dash = "-";

	float[][] similarityMatrix;
	
	public GenericEditDistance(int encodingLength, List<String> encodedTraceList, Map<String, Integer> substitutionScoreMap,
			Map<String, Integer> indelRightGivenLeftScoreMap, int incrementLikeSubstitutionScore, float scaleFactor) {
		this.encodingLength = encodingLength;
		
		
		indelScaleFactor = scaleFactor;
		TreeSet<Integer> substitutionIndelScoreValuesSet = new TreeSet<Integer>(substitutionScoreMap.values());
		int maxSubstitutionScore = substitutionIndelScoreValuesSet.last();
		
		substitutionIndelScoreValuesSet.clear();
		substitutionIndelScoreValuesSet.addAll(indelRightGivenLeftScoreMap.values());
		
		int maxIndelScore = substitutionIndelScoreValuesSet.last();
		
		indelScaleFactor = maxSubstitutionScore*1.0f/(maxIndelScore+1);
		if(indelScaleFactor > 0.4)
			indelScaleFactor = 0.4f;

		incrementLikeSubstitutionScore = maxIndelScore;
		this.substitutionScoreMap = new HashMap<String, Integer>();
		String[] substitutionPairSplit;
		for(String substitutionPair : substitutionScoreMap.keySet()){
			substitutionPairSplit = substitutionPair.split(" @ ");
			if(substitutionPairSplit[0].equals(substitutionPairSplit[1]))
				this.substitutionScoreMap.put(substitutionPair, substitutionScoreMap.get(substitutionPair)+incrementLikeSubstitutionScore);
			else
				this.substitutionScoreMap.put(substitutionPair, substitutionScoreMap.get(substitutionPair));
		}
		FileIO io = new FileIO();
		String tempDir = System.getProperty("java.io.tmpdir");
		io.writeToFile(tempDir, "incrSubstitutionScoreMap.txt", this.substitutionScoreMap, "\\^");

		this.indelRightGivenLeftScoreMap = new HashMap<String, Integer>();
		/*
		 * To reduce the computational load during the alignment, it is better
		 * to scale the values and store them
		 */
		int indelValue;
		for (String indelSymbolPair : indelRightGivenLeftScoreMap.keySet()) {
			indelValue = Math.round(indelRightGivenLeftScoreMap.get(indelSymbolPair) * indelScaleFactor);
			this.indelRightGivenLeftScoreMap.put(indelSymbolPair, indelValue);
//			if(indelValue > 0)
//				this.indelRightGivenLeftScoreMap.put(indelSymbolPair, 1);
//			else
//				this.indelRightGivenLeftScoreMap.put(indelSymbolPair, -1);
		}

		/*
		 * Get the minimum score of the substitution matrix; It would be helpful
		 * to assign the gap-open cost for undefined indel symbolPairs to be <=
		 * minimum score
		 */
		minSubstitutionScore = 0;
		for (String substitutionSymbolPair : substitutionScoreMap.keySet()) {
			if (substitutionScoreMap.get(substitutionSymbolPair) < minSubstitutionScore) {
				minSubstitutionScore = substitutionScoreMap.get(substitutionSymbolPair);
			}
		}

		if (gapOpenCost == Integer.MIN_VALUE) {
			gapOpenCost = minSubstitutionScore - 1;
		}
		
		int noTraces = encodedTraceList.size();
		similarityMatrix = new float[noTraces][noTraces];
		for(int i = 0; i < noTraces; i++){
			for(int j = i; j < noTraces; j++){
				similarityMatrix[i][j] = similarityMatrix[j][i] = getPairWiseGlobalAlignScore(encodedTraceList.get(i), encodedTraceList.get(j));
			}
		}
	}
	protected void printMatrix(int[][] matrix){
		int noRows = matrix.length;
		int noCols = matrix[0].length;
		
		for(int i = 0; i < noRows; i++){
			for(int j = 0; j < noCols; j++){
				System.out.print(matrix[i][j]+"  ");
			}
			System.out.println();
		}
	}
	
	public void setIndelRightGivenLeftScoreScaleFactor(Map<String, Integer> indelRightGivenLeftScoreMap,
			float indelScaleFactor) {
		this.indelScaleFactor = indelScaleFactor;

		if (this.indelRightGivenLeftScoreMap == null) {
			this.indelRightGivenLeftScoreMap = new HashMap<String, Integer>();
		}

		int indelValue;
		for (String indelSymbolPair : indelRightGivenLeftScoreMap.keySet()) {
			indelValue = Math.round(indelRightGivenLeftScoreMap.get(indelSymbolPair) * indelScaleFactor);
			indelLeftGivenRightScoreMap.put(indelSymbolPair, indelValue);
		}
	}

	public void incrementLikeSubstitutions(int incrementValue) {
		String[] substitutionSymbolPairSplit;
		int substitutionScore;
		for (String substitutionSymbolPair : substitutionScoreMap.keySet()) {
			substitutionSymbolPairSplit = substitutionSymbolPair.split(" @ ");
			if (substitutionSymbolPairSplit[0].trim().equals(substitutionSymbolPairSplit[1].trim())) {
				substitutionScore = substitutionScoreMap.get(substitutionSymbolPair);
				substitutionScoreMap.put(substitutionSymbolPair, substitutionScore + incrementValue);
			}
		}
	}

	public float getPairWiseGlobalAlignScore(String sequence1, String sequence2) {
		int sequence1Length, sequence2Length;

		/*
		 * Make sure that the first parameter that we pass to
		 * pairWiseGlobalAlign is longer than the second parameter
		 */

		sequence1Length = sequence1.length() / encodingLength;
		sequence2Length = sequence2.length() / encodingLength;
		String[] seq1, seq2;
		if (sequence1Length >= sequence2Length) {
			seq1 = new String[sequence1Length];
			for (int i = 0; i < sequence1Length; i++) {
				seq1[i] = sequence1.substring(i * encodingLength, (i + 1) * encodingLength);
			}

			seq2 = new String[sequence2Length];
			for (int i = 0; i < sequence2Length; i++) {
				seq2[i] = sequence2.substring(i * encodingLength, (i + 1) * encodingLength);
			}

		} else {
			seq1 = new String[sequence2Length];
			for (int i = 0; i < sequence2Length; i++) {
				seq1[i] = sequence2.substring(i * encodingLength, (i + 1) * encodingLength);
			}

			seq2 = new String[sequence1Length];
			for (int i = 0; i < sequence1Length; i++) {
				seq2[i] = sequence1.substring(i * encodingLength, (i + 1) * encodingLength);
			}

		}
		pairWiseAlign(seq1, seq2);
		
		return (M[seq1.length][seq2.length]*1.0f/(sequence1Length+sequence2Length));
	}

	public float getPairWiseSemiGlobalAlign(String sequence1, String sequence2) {
		int sequence1Length, sequence2Length;

		/*
		 * Make sure that the first parameter that we pass to
		 * pairWiseGlobalAlign is longer than the second parameter
		 */

		sequence1Length = sequence1.length() / encodingLength;
		sequence2Length = sequence2.length() / encodingLength;
		String[] seq1, seq2;
		if (sequence1Length >= sequence2Length) {
			seq1 = new String[sequence1Length];
			for (int i = 0; i < sequence1Length; i++) {
				seq1[i] = sequence1.substring(i * encodingLength, (i + 1) * encodingLength);
			}

			seq2 = new String[sequence2Length];
			for (int i = 0; i < sequence2Length; i++) {
				seq2[i] = sequence2.substring(i * encodingLength, (i + 1) * encodingLength);
			}

		} else {
			seq1 = new String[sequence2Length];
			for (int i = 0; i < sequence2Length; i++) {
				seq1[i] = sequence2.substring(i * encodingLength, (i + 1) * encodingLength);
			}

			seq2 = new String[sequence1Length];
			for (int i = 0; i < sequence1Length; i++) {
				seq2[i] = sequence1.substring(i * encodingLength, (i + 1) * encodingLength);
			}

		}
		pairWiseAlign(seq1, seq2);
		int i = seq1.length;
		int j = seq2.length;

		int maxJScore = Integer.MIN_VALUE;
		int maxIScore = Integer.MIN_VALUE;
		// Get the maximum score in the last column
		for (int k = 0; k <= seq1.length; k++) {
			if (M[k][j] > maxJScore) {
				maxJScore = M[k][j];
			}
		}

		//Check the maximum score over the last row and the index
		for (int k = 0; k <= seq2.length; k++) {
			if (M[i][k] > maxIScore) {
				maxIScore = M[i][k];
			}
		}

		if (maxJScore >= maxIScore) {
			return maxJScore;
		} else {
			return maxIScore;
		}
	}

	/**
	 * This method assumes that the first parameter seq1 is longer than the
	 * second parameter seq2; this is to guide the indels in the shorter
	 * sequence
	 */
	private void pairWiseAlign(String[] seq1, String[] seq2) {
		int sequence1Length = seq1.length;
		int sequence2Length = seq2.length;
		float lengthRatio;
		if (sequence1Length > sequence2Length) {
			lengthRatio = (float) sequence1Length / sequence2Length;
		} else {
			lengthRatio = (float) sequence2Length / sequence1Length;
		}

		int indelScoreSeq1, indelScoreSeq2, substitutionScore, maxScore;
		String indelSymbolPairSeq1, indelSymbolPairSeq2, substitutionSymbolPair;

		// the alignment score matrix
		M = new int[sequence1Length + 1][sequence2Length + 1];

		M[0][0] = 0;

		/*
		 * Fill the first row; the first symbol in sequence2 can always be
		 * inserted with no cost
		 */
		M[0][1] = 0;
		for (int j = 1; j < sequence2Length; j++) {
			indelSymbolPairSeq2 = seq2[j - 1] + " @ " + seq2[j];
			if (indelRightGivenLeftScoreMap.containsKey(indelSymbolPairSeq2)) {
				indelScoreSeq2 = indelRightGivenLeftScoreMap.get(indelSymbolPairSeq2);
			} else {
				indelScoreSeq2 = gapOpenCost;
			}
			M[0][j + 1] = M[0][j] + indelScoreSeq2;
		}

		M[1][0] = 0;
		for (int i = 1; i < sequence1Length; i++) {
			indelSymbolPairSeq1 = seq1[i - 1] + " @ " + seq1[i];
			if (indelRightGivenLeftScoreMap.containsKey(indelSymbolPairSeq1)) {
				indelScoreSeq1 = indelRightGivenLeftScoreMap.get(indelSymbolPairSeq1);
			} else {
				indelScoreSeq1 = gapOpenCost;
			}
			M[i + 1][0] = M[i][0] + indelScoreSeq1;
		}

		boolean lastGapI, lastGapJ;
		lastGapI = lastGapJ = false;
		for (int i = 0; i < sequence1Length; i++) {
			/*
			 * Get the indel Score for sequence1
			 */
			indelScoreSeq1 = gapOpenCost;
			if (i > 0) {
				indelSymbolPairSeq1 = seq1[i - 1] + " @ " + seq1[i];
				if (indelRightGivenLeftScoreMap.containsKey(indelSymbolPairSeq1)) {
					indelScoreSeq1 = indelRightGivenLeftScoreMap.get(indelSymbolPairSeq1);
					if(!lastGapI)
						indelScoreSeq1 += gapOpenCost;
				}
			} else {
				indelScoreSeq1 = 0;
			}
			lastGapJ = false;
			for (int j = 0; j < sequence2Length; j++) {
				/*
				 * Get the indel Score for sequence2
				 */
				indelScoreSeq2 = gapOpenCost;
				if (j > 0) {
					indelSymbolPairSeq2 = seq2[j - 1] + " @ " + seq2[j];
					if (indelRightGivenLeftScoreMap.containsKey(indelSymbolPairSeq2)) {
						indelScoreSeq2 = indelRightGivenLeftScoreMap.get(indelSymbolPairSeq2);
						if(!lastGapJ)
							indelScoreSeq2 += gapOpenCost;
					}
				} else {
					indelScoreSeq2 = 0;
				}

				/*
				 * Get the substitution score
				 */
				substitutionScore = minSubstitutionScore;

				substitutionSymbolPair = seq1[i] + " @ " + seq2[j];
				if (substitutionScoreMap.containsKey(substitutionSymbolPair)) {
					substitutionScore = substitutionScoreMap.get(substitutionSymbolPair);
				} else {
					substitutionSymbolPair = seq2[j] + " @ " + seq1[i];
					if (substitutionScoreMap.containsKey(substitutionSymbolPair)) {
						substitutionScore = substitutionScoreMap.get(substitutionSymbolPair);
					}
				}

				maxScore = Math.max(M[i][j] + substitutionScore, Math.max(M[i][j + 1] + indelScoreSeq1, M[i + 1][j]
						+ indelScoreSeq2));

				if (lengthRatio < lengthRatioThreshold) {
					if (maxScore == M[i][j] + substitutionScore) {
						M[i + 1][j + 1] = M[i][j] + substitutionScore;
						lastGapI = false;
						lastGapJ = false;
					} else if (maxScore == M[i][j + 1] + indelScoreSeq1) {
						M[i + 1][j + 1] = M[i][j + 1] + indelScoreSeq1;
						lastGapI = true;
					} else if (maxScore == M[i + 1][j] + indelScoreSeq2) {
						M[i + 1][j + 1] = M[i + 1][j] + indelScoreSeq2;
						lastGapJ = true;
					}
				} else {
					/*
					 * The two sequences vary in length a lot; It can be because
					 * of loops or too may repeats; Problems can arise when a
					 * small number of symbols at the end are consistent (the
					 * problem being the sequences would be biased to get
					 * aligned at the end). In such cases, try to prefer a indel
					 * at the end in case two scores are equal
					 */
					if ((maxScore == M[i][j] + substitutionScore)
							&& ((maxScore != M[i][j + 1] + indelScoreSeq1) && (maxScore != M[i + 1][j] + indelScoreSeq2))) {
						M[i + 1][j + 1] = M[i][j] + substitutionScore;
					} else if (maxScore == M[i][j + 1] + indelScoreSeq1) {

						M[i + 1][j + 1] = M[i][j + 1] + indelScoreSeq1;
					} else if (maxScore == M[i + 1][j] + indelScoreSeq2) {
						M[i + 1][j + 1] = M[i + 1][j] + indelScoreSeq2;
					}
				}
			}
		}
	}

	public void setLengthRatioThreshold(float lengthRatioThreshold) {
		this.lengthRatioThreshold = lengthRatioThreshold;
	}

	public void setGapPenalty(int gapPenalty) {
		gapOpenCost = gapPenalty;
	}
	
	public float[][] getSimilarityMatrix(){
		return similarityMatrix;
	}
}
