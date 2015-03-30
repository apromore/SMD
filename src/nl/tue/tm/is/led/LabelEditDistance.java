package nl.tue.tm.is.led;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.tartarus.snowball.SnowballStemmer;
import matching.algos.HungarianAlgorithm;
import matching.algos.DistanceAssignmentProblem.Solver;

public class LabelEditDistance {

	public static Map<String, LinkedList<String>> stringMap = new HashMap<String, LinkedList<String>>();
	
	public static double edTokensWithStemming(String a, String b, String delimeter, SnowballStemmer stemmer, boolean stem) {
		
		LinkedList<String> aTokens = stringMap.get(a);
		
		if (aTokens == null) {
			aTokens = new LinkedList<String>();
			StringTokenizer tokensA = new StringTokenizer(a, delimeter);
			while (tokensA.hasMoreTokens()) {
				String aToken = tokensA.nextToken();
				aTokens.add(aToken);
			}
			if (stem) { 
				aTokens = removeStopWordsAndStem(aTokens, stemmer);
			}
		}
		
		LinkedList<String> bTokens = stringMap.get(b);
		
		if (bTokens == null) {
			bTokens = new LinkedList<String>();
		
			StringTokenizer tokensB = new StringTokenizer(b, delimeter);
			while (tokensB.hasMoreTokens()) {
				String bToken = tokensB.nextToken();
				bTokens.add(bToken);
			}
	
			if (stem) { 
				bTokens = removeStopWordsAndStem(bTokens, stemmer);
			}
		}

		int dimFunc = aTokens.size() > bTokens.size() ? aTokens.size() : bTokens.size();
		double costFunc[][] = new double[dimFunc][dimFunc];
		
		for (int i = 0; i < aTokens.size(); i++) {
			for (int j = 0; j < bTokens.size(); j++) {
											
				// find the score using edit distance 
				double edScore = ed(aTokens.get(i), bTokens.get(j));
				
				edScore = edScore == 0 ? 1 : 
					(1 - edScore/(Double.valueOf(Math.max(aTokens.get(i).length(), bTokens.get(j).length())))); 

				costFunc[i][j] = edScore > 0 ? (-1)*edScore : edScore;
			}
		}
		double costFuncCopy[][] = new double[dimFunc][dimFunc];

		for(int i = 0; i < costFuncCopy.length; i++) {
			for (int j = 0; j < costFuncCopy[0].length; j++) {
				costFuncCopy[i][j] = costFunc[i][j];
			}
		}

		double mappedWeightFunc = 0;
		
		int[][] result = HungarianAlgorithm.computeAssignments(costFuncCopy);
		
		for(int i = 0; i < result.length; i++) {
			mappedWeightFunc += (-1)*costFunc[result[i][0]][result[i][1]];
		}

		// TOTAL mappingscore
		double mappingScore = 0;
		double mappedWeight = mappedWeightFunc;
		
		
		if (mappedWeight == 0) {
			mappingScore = 0;
		}
		else {
			mappingScore = mappedWeight*2 / (aTokens.size() + bTokens.size());
		}
		
		return mappingScore;
	}
	
	public static double edTokens1(LinkedList<String> aTokens, LinkedList<String> bTokens, String delimeter, SnowballStemmer stemmer, boolean stem) {
		
		int dimFunc = aTokens.size() > bTokens.size() ? aTokens.size() : bTokens.size();
		
		double costFunc[][] = new double[dimFunc][dimFunc];
		
		for (int i = 0; i < aTokens.size(); i++) {
			for (int j = 0; j < bTokens.size(); j++) {
											
				// find the score using edit distance 
				double edScore = ed(aTokens.get(i), bTokens.get(j));
				
				edScore = edScore == 0 ? 1 : 
					(1 - edScore/(Double.valueOf(Math.max(aTokens.get(i).length(), bTokens.get(j).length())))); 

				costFunc[i][j] = edScore > 0 ? (-1)*edScore : edScore;
			}
		}
		double costFuncCopy[][] = new double[dimFunc][dimFunc];

		for(int i = 0; i < costFuncCopy.length; i++) {
			for (int j = 0; j < costFuncCopy[0].length; j++) {
				costFuncCopy[i][j] = costFunc[i][j];
			}
		}

		double mappedWeightFunc = 0;
		
		int[][] result = HungarianAlgorithm.computeAssignments(costFuncCopy);
		
		for(int i = 0; i < result.length; i++) {
			mappedWeightFunc += (-1)*costFunc[result[i][0]][result[i][1]];
		}

		// TOTAL mappingscore
		double mappingScore = 0;
		double mappedWeight = mappedWeightFunc;
		
		
		if (mappedWeight == 0) {
			mappingScore = 0;
		}
		else {
			mappingScore = mappedWeight*2 / (aTokens.size() + bTokens.size());
		}
		
		return mappingScore;
	}
	public static LinkedList<String> tokenizeAndStem(String a, String delimeter, SnowballStemmer stemmer) {
		
		LinkedList<String> aTokens = new LinkedList<String>();
		
		StringTokenizer tokensA = new StringTokenizer(a, delimeter);
		while (tokensA.hasMoreTokens()) {
			String aToken = tokensA.nextToken();
			aTokens.add(aToken);
		}

		aTokens = removeStopWordsAndStem(aTokens, stemmer);
		
		return aTokens;
	}
	
	private static LinkedList<String> removeStopWordsAndStem(LinkedList<String> toRemove, SnowballStemmer stemmer) {

		LinkedList<String> result = new LinkedList<String>();
		Set<String> stopWords = stemmer.getStopWords();
		int repeat = 1;

		for (String s : toRemove) {
			s = s.toLowerCase();
//			if (s.length() <= 2) {
//				result.add(s);
//			}
//			else 
				if ( s.length() > 2 && (!stemmer.hasStopWords() || stemmer.hasStopWords() && !stopWords.contains(s))) {
				stemmer.setCurrent(s);
				for (int i = repeat; i != 0; i--) {
					stemmer.stem();
				}
				String stemmedString = stemmer.getCurrent();
				result.add(stemmedString);
			}
		}
		return result;
	}

	public static double edNormalized(String a, String b){
		return (1 - (double)ed(a, b) / (double) Math.max(a.length(), b.length()));
	}
	
	public static int ed(String a, String b){
		int[][] ed = new int[a.length()+1][b.length()+1];
		
		for (int i = 0; i < a.length()+1; i++) {
			ed[i][0] = i;
		}
		
		for (int j = 1; j < b.length()+1; j++) {
			
			ed[0][j] = j;
			
			for (int i = 1; i < a.length()+1; i++) {
				
				ed[i][j] = Math.min(ed[i-1][j]+1, 
								Math.min(ed[i][j-1]+1, 
										ed[i-1][j-1] + (a.charAt(i-1) == b.charAt(j-1) ? 0 : 1)));
			}
		}
		return ed[a.length()][b.length()];
	}

	
	public static double edTokens(String a, String b, String delimeter) {
		
		LinkedList<String> aTokens = new LinkedList<String>();
		LinkedList<String> bTokens = new LinkedList<String>();
		
		StringTokenizer tokensA = new StringTokenizer(a, delimeter);
		while (tokensA.hasMoreTokens()) {
			aTokens.add(tokensA.nextToken());
		}
		
		StringTokenizer tokensB = new StringTokenizer(b, delimeter);
		while (tokensB.hasMoreTokens()) {
			bTokens.add(tokensB.nextToken());
		}

		int dimFunc = aTokens.size() > bTokens.size() ? aTokens.size() : bTokens.size();
		
		double costFunc[][] = new double[dimFunc][dimFunc];
		
		for (int i = 0; i < aTokens.size(); i++) {
			for (int j = 0; j < bTokens.size(); j++) {
											

				// find the score using edit distance 
				double edScore = StringEditDistance.editDistance(aTokens.get(i), bTokens.get(j));
				
				edScore = edScore == 0 ? 1 : 
					(1 - edScore/(Double.valueOf(Math.max(aTokens.get(i).length(), bTokens.get(j).length())))); 

				costFunc[i][j] = (-1)*edScore;
			}
		}
		
		int[] row_solu_Func = new int[dimFunc];
		int[] col_solu_Func = new int[dimFunc];
		double[] u_Func = new double[dimFunc];
		double[] v_Func = new double[dimFunc];
		double mappedWeightFunc = 0;
		

		Solver lap_solve_Func = new Solver(dimFunc, costFunc);
		mappedWeightFunc = (-1)*lap_solve_Func.solve(row_solu_Func, col_solu_Func, u_Func,v_Func);
		
		
		// TOTAL mappingscore
		double mappingScore = 0;
		double mappedWeight = mappedWeightFunc;
		
		
		if (mappedWeight == 0) {
			mappingScore = 0;
		}
		else {
			mappingScore = mappedWeight*2 / (aTokens.size() + bTokens.size());
		}
		
		return mappingScore;
	}


}
