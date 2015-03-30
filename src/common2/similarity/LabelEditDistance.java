package common2.similarity;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

import common.Settings;
import common.stemmer.SnowballStemmer;


public class LabelEditDistance {
	
	private HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm();

	public double edTokensWithStemmingWordnet(String a, String b, String delimeter, SnowballStemmer stemmer, boolean stem) {
		
		LinkedList<String> aTokensInit = new LinkedList<String>();
		LinkedList<String> bTokensInit = new LinkedList<String>();
		
		LinkedList<String> aTokens = new LinkedList<String>();
		LinkedList<String> bTokens = new LinkedList<String>();
		
		StringTokenizer tokensA = new StringTokenizer(a, delimeter);
		while (tokensA.hasMoreTokens()) {
			String aToken = tokensA.nextToken();
			aTokensInit.add(aToken);
		}
		
		StringTokenizer tokensB = new StringTokenizer(b, delimeter);
		while (tokensB.hasMoreTokens()) {
			String bToken = tokensB.nextToken();
			bTokensInit.add(bToken);
		}

		if (stem) { 
			aTokens = removeStopWordsAndStem(aTokensInit, stemmer);
			bTokens = removeStopWordsAndStem(bTokensInit, stemmer);
			
			if (aTokens.size() == 0) {
				aTokens = removeStopWordsAndStem1(aTokensInit, stemmer);
			}
			
			if (bTokens.size() == 0) {
				bTokens = removeStopWordsAndStem1(bTokensInit, stemmer);
			}
		}

		int dimFunc = aTokens.size() > bTokens.size() ? aTokens.size() : bTokens.size();
		
		
		double costFunc[][] = new double[dimFunc][dimFunc];
		
		
		for (int i = 0; i < aTokens.size(); i++) {
			for (int j = 0; j < bTokens.size(); j++) {
											
				// find the score using edit distance 
				// TODO change methods
				PrintStream out = System.out;
				PrintStream tmpStream = new PrintStream(new ByteArrayOutputStream());
				
				double edScore = 0;
				try {
					System.setOut(tmpStream);
					int ed = ed(aTokens.get(i), bTokens.get(j));
					double ed1 = ed == 0 ? 1 : 
						(1 - ed/(Double.valueOf(Math.max(aTokens.get(i).length(), bTokens.get(j).length())))); 
					
					edScore = Math.min(1,
							Math.max(
									Math.max(Settings.getWordnet().max(aTokens.get(i), bTokens.get(j), "v"), 
											Settings.getWordnet().max(aTokens.get(i), bTokens.get(j), "n")), 
									ed1));

				} finally {
					System.setOut(out);
					tmpStream.close();
				}
				
//				System.out.println("edScore "+ edScore );
//					edScore = ed;

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
		
		int[][] result = hungarianAlgorithm.computeAssignments(costFuncCopy);
		
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
	
	public double edTokensWithStemming(String a, String b, String delimeter, SnowballStemmer stemmer, boolean stem) {
		
		LinkedList<String> aTokensInit = new LinkedList<String>();
		LinkedList<String> bTokensInit = new LinkedList<String>();
		
		LinkedList<String> aTokens = new LinkedList<String>();
		LinkedList<String> bTokens = new LinkedList<String>();
		
		StringTokenizer tokensA = new StringTokenizer(a, delimeter);
		while (tokensA.hasMoreTokens()) {
			String aToken = tokensA.nextToken();
			aTokensInit.add(aToken.toLowerCase());
		}
		
		StringTokenizer tokensB = new StringTokenizer(b, delimeter);
		while (tokensB.hasMoreTokens()) {
			String bToken = tokensB.nextToken();
			bTokensInit.add(bToken.toLowerCase());
		}
		if (aTokensInit.contains("not") && !bTokensInit.contains("not")
				|| !aTokensInit.contains("not") && bTokensInit.contains("not")) {
			return 0;
		}

		if (stem) { 
			aTokens = removeStopWordsAndStem(aTokensInit, stemmer);
			bTokens = removeStopWordsAndStem(bTokensInit, stemmer);
			
			if (aTokens.size() == 0) {
				aTokens = removeStopWordsAndStem1(aTokensInit, stemmer);
			}
			
			if (bTokens.size() == 0) {
				bTokens = removeStopWordsAndStem1(bTokensInit, stemmer);
			}
		}

		for (String at : aTokens) {
			Settings.jura.add(at);
		}
		
		for (String bt : bTokens) {
			Settings.jura.add(bt);
		}
		
		int dimFunc = aTokens.size() > bTokens.size() ? aTokens.size() : bTokens.size();
		
		double costFunc[][] = new double[dimFunc][dimFunc];
		
		for (int i = 0; i < aTokens.size(); i++) {
			for (int j = 0; j < bTokens.size(); j++) {
											
				// find the score using edit distance 
			double edScore = 0;
				
			int ed = ed(aTokens.get(i), bTokens.get(j));
			edScore = ed == 0 ? 1 : 
						(1 - ed/(Double.valueOf(Math.max(aTokens.get(i).length(), bTokens.get(j).length())))); 
					
//				System.out.println("edScore "+ edScore );
//					edScore = ed;

				costFunc[i][j] = edScore > 0 ? (-1)*edScore : edScore;
			}
		}
		double costFuncCopy[][] = new double[dimFunc][dimFunc];
		int nrzeros = 0;
		for(int i = 0; i < costFuncCopy.length; i++) {
			for (int j = 0; j < costFuncCopy[0].length; j++) {
				if (costFunc[i][j] == 0) {
					nrzeros++;
				}
				costFuncCopy[i][j] = costFunc[i][j];
			}
		}

		if (nrzeros == dimFunc*dimFunc) {
			return 0;
		}
		
		double mappedWeightFunc = 0;
		
//		System.out.println("============================================== ");
//		System.out.println(a);
//		System.out.println(b);
		for (int ma = 0; ma < 100; ma++) {
			int mb = 1;
		}
////		for (int ma = 0; ma < costFunc.length; ma++) {
////			StringBuffer buffer = new StringBuffer();
////		    for (int mb = 0; mb < costFunc[ma].length; mb++) {  
////		        double p1 = costFunc[ma][mb];
////		        buffer.append(p1 + ", ");
////		    }
////		    System.out.println(buffer.toString());
////		}  
//		System.out.println("==============================================");
		
		int[][] result = hungarianAlgorithm.computeAssignments(costFuncCopy);
		
		for(int i = 0; i < result.length; i++) {
//			if (result[i][0] < aTokens.size()
//			&& result[i][1] < bTokens.size()) {
//				System.out.println(aTokens.get(result[i][0]) + 
//				" "+ bTokens.get(result[i][1]) + " "+ (-1)*costFunc[result[i][0]][result[i][1]]);
//			}
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
//		System.out.println("score:  "+mappingScore);
		return mappingScore;
	}

	
	private LinkedList<String> removeStopWordsAndStem(LinkedList<String> toRemove, SnowballStemmer stemmer) {

		LinkedList<String> result = new LinkedList<String>();
		Set<String> stopWords = stemmer.getStopWords();
		int repeat = 1;

		for (String s : toRemove) {
			s = s.toLowerCase();
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

	private LinkedList<String> removeStopWordsAndStem1(LinkedList<String> toRemove, SnowballStemmer stemmer) {

		LinkedList<String> result = new LinkedList<String>();
		int repeat = 1;

		for (String s : toRemove) {
			s = s.toLowerCase();
			if ( s.length() > 2) {
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
	
	public int ed(String a, String b){
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

	public static void main(String[] a) {
		new LabelEditDistance().edTokensWithStemming("Determine caller s relationship to policy",
				"Determine if customer wants to continue with claim", 
				Settings.STRING_DELIMETER,
				Settings.getEnglishStemmer(), true);
	}
}
