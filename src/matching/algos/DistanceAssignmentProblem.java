package matching.algos;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

import org.tartarus.snowball.SnowballStemmer;

import com.mallardsoft.tuple.Pair;

import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.JiangAndConrath;

import pcf.PcfSimilarity;
import nl.tue.tm.is.graph.SimpleGraph;
import nl.tue.tm.is.labels.TokenizedLabel;
import nl.tue.tm.is.labels.TokenizedLabelCache;
import nl.tue.tm.is.led.LabelEditDistance;
import nl.tue.tm.is.led.StringEditDistance;


public class DistanceAssignmentProblem implements DistanceAlgo{
	
	static double ED_THRESHOLD = 0.5;

	public double compute(SimpleGraph sg1, SimpleGraph sg2) {
		
		Set<String> g1FuncLabels = sg1.getLabels(sg1.getVertices());
		Set<String> g2FuncLabels = sg2.getLabels(sg2.getVertices());
		
		if (g1FuncLabels == null || g2FuncLabels == null || g1FuncLabels.size() == 0 || g2FuncLabels.size() == 0) {
			return 1;
		}

		int dimFunc = g1FuncLabels.size() > g2FuncLabels.size() ? g1FuncLabels.size() : g2FuncLabels.size();
		
		double costFunc[][] = new double[dimFunc][dimFunc];
		
		int i = 0;
		// function mapping score
		for (String g1Func : g1FuncLabels) {
			int j = 0;
			for (String g2Func : g2FuncLabels) {
											
				// find the score using edit distance 
				double edScore = StringEditDistance.similarity(g1Func, g2Func);

				if(edScore < ED_THRESHOLD)
					edScore = 0;

				costFunc[i][j] = (-1)*edScore;
				j++;
			}
			i++;
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
			mappingScore = mappedWeight*2 / (g1FuncLabels.size() + g2FuncLabels.size());
		}
	
		return 1 - mappingScore;
	}

	public LinkedList<StringPair> findSimilarityUsingAssignment(SimpleGraph sg1, SimpleGraph sg2, String delimeter, boolean labelEditDistance) {
		
		LinkedList<StringPair> solutionMappings = new LinkedList<StringPair> ();
		
		Set<String> g1FuncLabels = sg1.getLabels(sg1.getVertices());
		Set<String> g2FuncLabels = sg2.getLabels(sg2.getVertices());
		
		if (g1FuncLabels == null || g2FuncLabels == null || g1FuncLabels.size() == 0 || g2FuncLabels.size() == 0) {
			return solutionMappings;
		}

		int dimFunc = g1FuncLabels.size() > g2FuncLabels.size() ? g1FuncLabels.size() : g2FuncLabels.size();
		
		double costFunc[][] = new double[dimFunc][dimFunc];
		
		int i = 0;
		// function mapping score
		for (String g1Func : g1FuncLabels) {
			int j = 0;
			for (String g2Func : g2FuncLabels) {
											
				// find the score using edit distance or labelEdit distance
				double edScore = labelEditDistance ? LabelEditDistance.edTokens(g1Func, g2Func, delimeter)
												   : StringEditDistance.similarity(g1Func, g2Func);

				if(edScore < ED_THRESHOLD)
					edScore = 0;

				costFunc[i][j] = (-1)*edScore;
				j++;
			}
			i++;
		}

		int[] row_solu_Func = new int[dimFunc];
		int[] col_solu_Func = new int[dimFunc];
		double[] u_Func = new double[dimFunc];
		double[] v_Func = new double[dimFunc];
		

		Solver lap_solve_Func = new Solver(dimFunc, costFunc);
		lap_solve_Func.solve(row_solu_Func, col_solu_Func, u_Func,v_Func);
		
		LinkedList<String> g1FuncLabelsList = new LinkedList<String> (g1FuncLabels);
		LinkedList<String> g2FuncLabelsList = new LinkedList<String> (g2FuncLabels);
		
		for(Solution s : lap_solve_Func.getSolution()) {
			solutionMappings.add(new StringPair(g1FuncLabelsList.get(col_solu_Func[s.right]),  g2FuncLabelsList.get(row_solu_Func[s.left])));
		}
	
		return solutionMappings;
	}

	public LinkedList<StringPair> findSimilarityUsingAssignmentStemming(SimpleGraph sg1, SimpleGraph sg2, String delimeter, SnowballStemmer stemmer) {
		return findSimilarityUsingAssignmentStemming(sg1, sg2, delimeter, stemmer, ED_THRESHOLD);
	}
	
	public LinkedList<StringPair> findSimilarityUsingAssignmentStemming(SimpleGraph sg1, SimpleGraph sg2, String delimeter, SnowballStemmer stemmer, double threshold) {
		LinkedList<StringPair> solutionMappings = new LinkedList<StringPair> ();
		
		Set<String> g1FuncLabels = sg1.getLabels(sg1.getVertices());
		Set<String> g2FuncLabels = sg2.getLabels(sg2.getVertices());
		
		if (g1FuncLabels == null || g2FuncLabels == null || g1FuncLabels.size() == 0 || g2FuncLabels.size() == 0) {
			return solutionMappings;
		}

		int dimFunc = g1FuncLabels.size() > g2FuncLabels.size() ? g1FuncLabels.size() : g2FuncLabels.size();
		
		double costFunc[][] = new double[dimFunc][dimFunc];
		double costsCopy[][] = new double[dimFunc][dimFunc];
//		System.out.println(">>finging label ed: "+ g1FuncLabels.size()+" "+ g2FuncLabels.size());		
		long system1 = System.currentTimeMillis();
		int i = 0;
		boolean allZeros = true;
		// function mapping score
		for (String g1Func : g1FuncLabels) {
			int j = 0;
			for (String g2Func : g2FuncLabels) {
//				System.out.println(">>finging label ed: "+ g1Func+" "+ g2Func);				
				// find the score using edit distance or labelEdit distance
				long system11 = System.currentTimeMillis();
				double edScore = LabelEditDistance.edTokensWithStemming(g1Func, g2Func, delimeter, stemmer, true);
				long system12 = System.currentTimeMillis();
//				System.out.println(">>time for one label comparison   "+ (system12 - system11));
//				System.out.println(">>finging label ed: "+ g1Func+" <> "+ g2Func + " ed : "+edScore);	
				if(edScore < threshold)
					edScore = 0;

				if (edScore != 0) {
					allZeros = false;
				}
				costFunc[i][j] = (-1)*edScore;
				j++;
			}
			i++;
		}
		long system2 = System.currentTimeMillis();
//		System.out.println(">>afer table  "+ (system2 - system1));
//		System.out.println(">>finging  FINISHED");
		
		if (allZeros) {
			return solutionMappings;
		}

		for (int k = 0; k < costFunc.length; k++) {
			for (int l = 0; l < costFunc[0].length; l++) {
				costsCopy[k][l] = costFunc[k][l];
			}
		}

		int[][] result = HungarianAlgorithm.computeAssignments(costsCopy);
		long system3 = System.currentTimeMillis();
//		System.out.println(">>afer assingnment comp  "+ (system3 - system2));
		
		LinkedList<String> g1FuncLabelsList = new LinkedList<String> (g1FuncLabels);
		LinkedList<String> g2FuncLabelsList = new LinkedList<String> (g2FuncLabels);
		
		for (int k = 0; k < result.length; k++) {
			double pairCost = (-1) * costFunc[result[k][0]][result[k][1]];
			if (result[k][0] < g1FuncLabelsList.size()
					&& result[k][1] < g2FuncLabelsList.size()
					&& pairCost > 0) {
				solutionMappings.add(new StringPair(
						removeSpaces(g1FuncLabelsList.get(result[k][0]).trim()),  
						removeSpaces(g2FuncLabelsList.get(result[k][1]).trim()),
						pairCost));
			}
		}
	
		return solutionMappings;

	}

	public LinkedList<StringPair> findSimilarityStringEditDistance(SimpleGraph sg1, SimpleGraph sg2, double threshold) {
		LinkedList<StringPair> solutionMappings = new LinkedList<StringPair> ();
		
		Set<String> g1FuncLabels = sg1.getLabels(sg1.getVertices());
		Set<String> g2FuncLabels = sg2.getLabels(sg2.getVertices());
		
		if (g1FuncLabels == null || g2FuncLabels == null || g1FuncLabels.size() == 0 || g2FuncLabels.size() == 0) {
			return solutionMappings;
		}

		int dimFunc = g1FuncLabels.size() > g2FuncLabels.size() ? g1FuncLabels.size() : g2FuncLabels.size();
		
		double costFunc[][] = new double[dimFunc][dimFunc];
		double costsCopy[][] = new double[dimFunc][dimFunc];
		
		int i = 0;
		boolean allZeros = true;
		// function mapping score
		for (String g1Func : g1FuncLabels) {
			int j = 0;
			for (String g2Func : g2FuncLabels) {
				// find the score using edit distance or labelEdit distance
				double edScore = StringEditDistance.similarity(g1Func, g2Func);
				if(edScore < threshold)
					edScore = 0;

				if (edScore != 0) {
					allZeros = false;
				}
				costFunc[i][j] = (-1)*edScore;
				j++;
			}
			i++;
		}
		
		if (allZeros) {
			return solutionMappings;
		}

		for (int k = 0; k < costFunc.length; k++) {
			for (int l = 0; l < costFunc[0].length; l++) {
				costsCopy[k][l] = costFunc[k][l];
			}
		}

		int[][] result = HungarianAlgorithm.computeAssignments(costsCopy);
		
		LinkedList<String> g1FuncLabelsList = new LinkedList<String> (g1FuncLabels);
		LinkedList<String> g2FuncLabelsList = new LinkedList<String> (g2FuncLabels);
		
		for (int k = 0; k < result.length; k++) {
			double pairCost = (-1) * costFunc[result[k][0]][result[k][1]];
			if (result[k][0] < g1FuncLabelsList.size()
					&& result[k][1] < g2FuncLabelsList.size()
					&& pairCost > 0) {
				solutionMappings.add(new StringPair(
						removeSpaces(g1FuncLabelsList.get(result[k][0]).trim()),  
						removeSpaces(g2FuncLabelsList.get(result[k][1]).trim()),
						pairCost));
			}
		}
		return solutionMappings;
	}	
	
	public LinkedList<StringPair> findSimilarityUsingTokenizedLabels(String model1, String model2, TokenizedLabelCache tlcache) {
		LinkedList<StringPair> solutionMappings = new LinkedList<StringPair> ();
		
		LinkedList<TokenizedLabel> g1FuncLabels = new LinkedList<TokenizedLabel> (tlcache.get(model1));
		LinkedList<TokenizedLabel> g2FuncLabels = new LinkedList<TokenizedLabel> (tlcache.get(model2));
		
		if (g1FuncLabels == null || g2FuncLabels == null || g1FuncLabels.size() == 0 || g2FuncLabels.size() == 0) {
			return solutionMappings;
		}

		int dimFunc = g1FuncLabels.size() > g2FuncLabels.size() ? g1FuncLabels.size() : g2FuncLabels.size();
		
		double costFunc[][] = new double[dimFunc][dimFunc];
		double costsCopy[][] = new double[dimFunc][dimFunc];
		
		int i = 0;
		boolean allZeros = true;
		// function mapping score
		for (TokenizedLabel t1 : g1FuncLabels) {
			int j = 0;
			for (TokenizedLabel t2 : g2FuncLabels) {
				double edScore = t1.similarity(t2);

				if (edScore != 0) {
					allZeros = false;
				}
				costFunc[i][j] = (-1)*edScore;
				j++;
			}
			i++;
		}
		
		if (allZeros) {
			return solutionMappings;
		}

		for (int k = 0; k < costFunc.length; k++) {
			for (int l = 0; l < costFunc[0].length; l++) {
				costsCopy[k][l] = costFunc[k][l];
			}
		}

		int[][] result = HungarianAlgorithm.computeAssignments(costsCopy);
		
		for (int k = 0; k < result.length; k++) {
			double pairCost = (-1) * costFunc[result[k][0]][result[k][1]];
			if (result[k][0] < g1FuncLabels.size()
					&& result[k][1] < g2FuncLabels.size()
					&& pairCost > 0) {
				solutionMappings.add(new StringPair(
						removeSpaces(g1FuncLabels.get(result[k][0]).getLabel().trim()),  
						removeSpaces(g2FuncLabels.get(result[k][1]).getLabel().trim()),
						pairCost));
			}
		}
	
		return solutionMappings;

	}

	public LinkedList<StringPair> findSimilarityUsingTokenizedLabelsWordnet(String model1, String model2, TokenizedLabelCache tlcache, JiangAndConrath jcn) {
		LinkedList<StringPair> solutionMappings = new LinkedList<StringPair> ();
		
		LinkedList<TokenizedLabel> g1FuncLabels = new LinkedList<TokenizedLabel> (tlcache.get(model1));
		LinkedList<TokenizedLabel> g2FuncLabels = new LinkedList<TokenizedLabel> (tlcache.get(model2));
		
		if (g1FuncLabels == null || g2FuncLabels == null || g1FuncLabels.size() == 0 || g2FuncLabels.size() == 0) {
			return solutionMappings;
		}

		int dimFunc = g1FuncLabels.size() > g2FuncLabels.size() ? g1FuncLabels.size() : g2FuncLabels.size();
		
		double costFunc[][] = new double[dimFunc][dimFunc];
		double costsCopy[][] = new double[dimFunc][dimFunc];
		
		int i = 0;
		boolean allZeros = true;
		// function mapping score
		for (TokenizedLabel t1 : g1FuncLabels) {
			int j = 0;
			for (TokenizedLabel t2 : g2FuncLabels) {
//				System.out.println(t1.getLabel() + " <> "+ t2.getLabel());
				double edScore = t1.similarityWordnet(t2, jcn);

				if (edScore != 0) {
					allZeros = false;
				}
				costFunc[i][j] = (-1)*edScore;
				j++;
			}
			i++;
		}
		
		if (allZeros) {
			return solutionMappings;
		}

		for (int k = 0; k < costFunc.length; k++) {
			for (int l = 0; l < costFunc[0].length; l++) {
				costsCopy[k][l] = costFunc[k][l];
			}
		}

		int[][] result = HungarianAlgorithm.computeAssignments(costsCopy);
		
		for (int k = 0; k < result.length; k++) {
			double pairCost = (-1) * costFunc[result[k][0]][result[k][1]];
			if (result[k][0] < g1FuncLabels.size()
					&& result[k][1] < g2FuncLabels.size()
					&& pairCost > 0) {
				solutionMappings.add(new StringPair(
						removeSpaces(g1FuncLabels.get(result[k][0]).getLabel().trim()),  
						removeSpaces(g2FuncLabels.get(result[k][1]).getLabel().trim()),
						pairCost));
			}
		}
	
		return solutionMappings;

	}
	
	public LinkedList<StringPair> findSimilarityUsingWordnet(LinkedList<Pair<String, String>> g1FuncLabels, 
									LinkedList<Pair<String, String>> g2FuncLabels, JiangAndConrath jcn) {
		LinkedList<StringPair> solutionMappings = new LinkedList<StringPair> ();
		
		if (g1FuncLabels == null || g2FuncLabels == null || g1FuncLabels.size() == 0 || g2FuncLabels.size() == 0) {
			return solutionMappings;
		}

		int dimFunc = g1FuncLabels.size() > g2FuncLabels.size() ? g1FuncLabels.size() : g2FuncLabels.size();
		
		double costFunc[][] = new double[dimFunc][dimFunc];
		double costsCopy[][] = new double[dimFunc][dimFunc];
		
		int i = 0;
		boolean allZeros = true;
		// function mapping score
		for (Pair<String, String> t1 : g1FuncLabels) {
			int j = 0;
			for (Pair<String, String> t2 : g2FuncLabels) {
//				System.out.println(Pair.get1(t1) + " <> "+ Pair.get1(t2));
				double edScore = 0;
				String tag1 = Pair.get2(t1);
				String tag2 = Pair.get2(t2);
				
				PrintStream out = System.out;
				PrintStream tmpStream = new PrintStream(new ByteArrayOutputStream());
				try {
					System.setOut(tmpStream);

					if (tag1.equals(tag2)) {
						if (tag1.equals("n")) {
							edScore = jcn.max(Pair.get1(t1), Pair.get1(t2), "n");
						}
						else {
							edScore = jcn.max(Pair.get1(t1), Pair.get1(t2), "v");
						}
					}

				} finally {
					System.setOut(out);
					tmpStream.close();
				}
				
				if (edScore != 0) {
					allZeros = false;
				}
				costFunc[i][j] = (-1)*edScore;
				j++;
			}
			i++;
		}
		
		if (allZeros) {
			return solutionMappings;
		}

		for (int k = 0; k < costFunc.length; k++) {
			for (int l = 0; l < costFunc[0].length; l++) {
				costsCopy[k][l] = costFunc[k][l];
			}
		}

		int[][] result = HungarianAlgorithm.computeAssignments(costsCopy);
		
		for (int k = 0; k < result.length; k++) {
			double pairCost = (-1) * costFunc[result[k][0]][result[k][1]];
			if (result[k][0] < g1FuncLabels.size()
					&& result[k][1] < g2FuncLabels.size()
					&& pairCost > 0) {
				solutionMappings.add(new StringPair(
						removeSpaces(Pair.get1(g1FuncLabels.get(result[k][0])).trim()),  
						removeSpaces(Pair.get1(g2FuncLabels.get(result[k][1])).trim()),
						pairCost));
			}
		}
	
		return solutionMappings;

	}

	
	public LinkedList<StringPair> findSimilarityUsingPCFAssignmentStemming(LinkedList<LinkedList<String>> g1FuncLabels, LinkedList<LinkedList<String>> g2FuncLabels, String delimeter, SnowballStemmer stemmer, double threshold) {
		LinkedList<StringPair> solutionMappings = new LinkedList<StringPair> ();
		
		
		if (g1FuncLabels == null || g2FuncLabels == null || g1FuncLabels.size() == 0 || g2FuncLabels.size() == 0) {
			return solutionMappings;
		}

		int dimFunc = g1FuncLabels.size() > g2FuncLabels.size() ? g1FuncLabels.size() : g2FuncLabels.size();
		
		double costFunc[][] = new double[dimFunc][dimFunc];
		double costsCopy[][] = new double[dimFunc][dimFunc];
		
		int i = 0;
		boolean allZeros = true;
		
		// function mapping score
		for (LinkedList<String> g1Func : g1FuncLabels) {
			int j = 0;
			for (LinkedList<String> g2Func : g2FuncLabels) {
//				System.out.println(">>finging label ed: "+ g1Func+" "+ g2Func);				
				// find the score using edit distance or labelEdit distance
				long system11 = System.currentTimeMillis();
				double edScore = PcfSimilarity.pcfSimilarity(g1Func, g2Func, stemmer);
				long system12 = System.currentTimeMillis();
//				System.out.println(">>time for one label comparison   "+ (system12 - system11));

//				System.out.println("\t edscore : "+ edScore);
				if(edScore < threshold)
					edScore = 0;

				if (edScore != 0) {
					allZeros = false;
				}
				costFunc[i][j] = (-1)*edScore;
				j++;
			}
			i++;
		}
		long system2 = System.currentTimeMillis();
//		System.out.println(">>afer table  "+ (system2 - system1));

//		System.out.println(">>finging  FINISHED");
		
		if (allZeros) {
			return solutionMappings;
		}

		for (int k = 0; k < costFunc.length; k++) {
			for (int l = 0; l < costFunc[0].length; l++) {
				costsCopy[k][l] = costFunc[k][l];
			}
		}

		int[][] result = HungarianAlgorithm.computeAssignments(costsCopy);

		long system3 = System.currentTimeMillis();
//		System.out.println(">>afer assingnment comp  "+ (system3 - system2));

		LinkedList<LinkedList<String>> g1FuncLabelsList = new LinkedList<LinkedList<String>> (g1FuncLabels);
		LinkedList<LinkedList<String>> g2FuncLabelsList = new LinkedList<LinkedList<String>> (g2FuncLabels);
		
		for (int k = 0; k < result.length; k++) {
			double pairCost = (-1) * costFunc[result[k][0]][result[k][1]];
			if (result[k][0] < g1FuncLabelsList.size()
					&& result[k][1] < g2FuncLabelsList.size()
					&& pairCost > 0) {
				String l1 = "";
				for (String labl : g1FuncLabelsList.get(result[k][0])) {
					l1 += removeSpaces(labl) + " "; 
				}
				
				String l2 = "";
				for (String labl : g2FuncLabelsList.get(result[k][1])) {
					l2 += removeSpaces(labl) + " "; 
				}
				solutionMappings.add(new StringPair(
						l1,  
						l2,
						pairCost));
			}
		}
	
		return solutionMappings;

	}

	public LinkedList<StringPair> findSimilarityUsingThreshold(SimpleGraph sg1, SimpleGraph sg2, SnowballStemmer stemmer) {

		LinkedList<StringPair> solutionMappings = new LinkedList<StringPair> ();
		
		Set<String> g1FuncLabels = sg1.getLabels(sg1.getVertices());
		Set<String> g2FuncLabels = sg2.getLabels(sg2.getVertices());
		
		if (g1FuncLabels == null || g2FuncLabels == null || g1FuncLabels.size() == 0 || g2FuncLabels.size() == 0) {
			return solutionMappings;
		}
		
		// function mapping score
		for (String g1Func : g1FuncLabels) {
			for (String g2Func : g2FuncLabels) {
											
				// find the score using edit distance 
				double edScore = StringEditDistance.similarity(g1Func, g2Func);

				// add all the result that has the ed >= threshold
				if(edScore >= ED_THRESHOLD)  {
					solutionMappings.add(new StringPair(g1Func,  g2Func));
				}
			}
		}
		
		return solutionMappings;
	}

	
	public void setThreshold(double threshold) {
		ED_THRESHOLD = threshold;
	}
	private static String removeSpaces(String s) {
		StringTokenizer st = new StringTokenizer(s);
		String result = "";
		
		while (st.hasMoreTokens()) {
			result += st.nextToken()+ (st.hasMoreTokens() ? " " : "");
		}
		
		return result;
	}

	
	public static class Solver {
		// input:
		private int dim;
		private double assigncost[][];
		private int numfree = 0;
		private boolean unassignedfound;
		private int i, imin, prvnumfree, f, i0, k, freerow, pred[], free[];
		private int j, j1, j2, endofpath, last, low, up, collist[], matches[];
		private double min, h, umin, usubmin, v2, d[];
		public LinkedList<Solution> solution = new LinkedList<Solution>();

		
		public Solver (int Dim, double Assigncost[][]) {

			dim = Dim;
			free = new int[dim];
			collist = new int[dim];
			matches = new int[dim];
			d = new double[dim];
			pred = new int[dim];
			assigncost = new double[dim][dim];

			for (int i = 0; i < dim; i++) {
				matches[i] = 0;
				for (int j = 0; j < dim; j++)
					assigncost[i][j] = Assigncost[i][j];
			}

		}

		public double solve (int rowsol[], int colsol[], double u[], double v[]) {
			// free = new int[dim]; // list of unassigned rows.
			// collist = new int[dim]; // list of columns to be scanned in various
			// ways.
			// matches = new int[dim]; // counts how many times a row could be
			// assigned.
			// d = new int[dim]; // 'cost-distance' in augmenting path calculation.
			// pred = new int[dim]; // row-predecessor of column in
			// augmenting/alternating path.

			// init how many times a row will be assigned in the column reduction.
			for (i = 0; i < dim; i++) {
				matches[i] = 0;
			}

			// COLUMN REDUCTION
			for (j = dim - 1; j >= 0; j--) {
				// find minimum cost over rows.
				min = assigncost[0][j];
				imin = 0;
				for (i = 1; i < dim; i++)
					if (assigncost[i][j] < min) {
						min = assigncost[i][j];
						imin = i;
					}
				v[j] = min;

				if (++matches[imin] == 1) {
					// init assignment if minimum row assigned for first time.
					rowsol[imin] = j;
					colsol[j] = imin;
				} else
					colsol[j] = -1; // row already assigned, column not assigned.
			}

			// REDUCTION TRANSFER
			for (i = 0; i < dim; i++)
				if (matches[i] == 0) // fill list of unassigned 'free' rows.
					free[numfree++] = i;
				else if (matches[i] == 1) {// transfer reduction from rows that are
				// assigned once.
				
					j1 = rowsol[i];
					min = 100000;
					for (j = 0; j < dim; j++)
						if (j != j1)
							if (assigncost[i][j] - v[j] < min)
								min = assigncost[i][j] - v[j];
					v[j1] = v[j1] - min;
				}

			// AUGMENTING ROW REDUCTION
			int loopcnt = 0; // do-loop to be done twice.
			do {
				loopcnt++;

				// scan all free rows.
				// in some cases, a free row may be replaced with another one to be
				// scanned next.
				k = 0;
				prvnumfree = numfree;
				numfree = 0; // start list of rows still free after augmenting row
				// reduction.
				while (k < prvnumfree) {
					i = free[k];
					k++;

					// find minimum and second minimum reduced cost over columns.
					umin = assigncost[i][0] - v[0];
					j1 = 0;
					usubmin = 100000;
					for (j = 1; j < dim; j++) {
						h = assigncost[i][j] - v[j];
						if (h < usubmin)
							if (h >= umin) {
								usubmin = h;
								j2 = j;
							} else {
								usubmin = umin;
								umin = h;
								j2 = j1;
								j1 = j;
							}
					}

					i0 = colsol[j1];
					if (umin < usubmin)
						// change the reduction of the minimum column to increase
						// the minimum
						// reduced cost in the row to the subminimum.
						v[j1] = v[j1] - (usubmin - umin);
					else // minimum and subminimum equal.
					if (i0 >= 0) // minimum column j1 is assigned.
					{
						// swap columns j1 and j2, as j2 may be unassigned.
						j1 = j2;
						i0 = colsol[j2];
					}

					// (re-)assign i to j1, possibly de-assigning an i0.
					rowsol[i] = j1;
					colsol[j1] = i;

					if (i0 >= 0) // minimum column j1 assigned earlier.
						if (umin < usubmin)
							// put in current k, and go back to that k.
							// continue augmenting path i - j1 with i0.
							free[--k] = i0;
						else
							// no further augmenting reduction possible.
							// store i0 in list of free rows for next phase.
							free[numfree++] = i0;
				}
			}

			while (loopcnt < 2); // repeat once.
			
			// AUGMENT SOLUTION for each free row.
			for (f = 0; f < numfree; f++) {
				freerow = free[f]; // start row of augmenting path.

				// Dijkstra shortest path algorithm.
				// runs until unassigned column added to shortest path tree.
				for (j = 0; j < dim; j++) {
					d[j] = assigncost[freerow][j] - v[j];
					pred[j] = freerow;
					collist[j] = j; // init column list.
				}

				low = 0; // columns in 0..low-1 are ready, now none.
				up = 0; // columns in low..up-1 are to be scanned for current
				// minimum, now none.
				// columns in up..dim-1 are to be considered later to find new
				// minimum,
				// at this stage the list simply contains all columns
				unassignedfound = false;
				do {
					if (up == low) // no more columns to be scanned for current
					// minimum.
					{
						last = low - 1;

						// scan columns for up..dim-1 to find all indices for which
						// new minimum occurs.
						// store these indices between low..up-1 (increasing up).
						min = d[collist[up++]];
						for (k = up; k < dim; k++) {
							j = collist[k];
							h = d[j];
							if (h <= min) {
								if (h < min) // new minimum.
								{
									up = low; // restart list at index low.
									min = h;
								}
								// new index with same minimum, put on undex up, and
								// extend list.
								collist[k] = collist[up];
								collist[up++] = j;
							}
						}

						// check if any of the minimum columns happens to be
						// unassigned.
						// if so, we have an augmenting path right away.
						for (k = low; k < up; k++)
							if (colsol[collist[k]] < 0) {
								endofpath = collist[k];
								unassignedfound = true;
								break;
							}
					}

					if (!unassignedfound) {
						// update 'distances' between freerow and all unscanned
						// columns, via next scanned column.
						j1 = collist[low];
						low++;
						i = colsol[j1];
						h = assigncost[i][j1] - v[j1] - min;

						for (k = up; k < dim; k++) {
							j = collist[k];
							v2 = assigncost[i][j] - v[j] - h;
							if (v2 < d[j]) {
								pred[j] = i;
								if (v2 == min) // new column found at same minimum
									// value
									if (colsol[j] < 0) {
										// if unassigned, shortest augmenting path
										// is complete.
										endofpath = j;
										unassignedfound = true;
										break;
									}
									// else add to list to be scanned right away.
									else {
										collist[k] = collist[up];
										collist[up++] = j;
									}
								d[j] = v2;
							}
						}
					}
				} while (!unassignedfound);

				// update column prices.
				for (k = 0; k <= last; k++) {
					j1 = collist[k];
					v[j1] = v[j1] + d[j1] - min;
				}

				// reset row and column assignments along the alternating path.
				do {
					i = pred[endofpath];
					colsol[endofpath] = i;
					j1 = endofpath;
					endofpath = rowsol[i];
					rowsol[i] = j1;
				} while (i != freerow);
			}

			// calculate optimal cost.
			double lapcost = 0;
			for (i = 0; i < dim; i++) {
				j = rowsol[i];

				if (assigncost[i][j] < 0) {
					solution.add(new Solution(i, j, assigncost[i][j]));
				}

				lapcost = lapcost + assigncost[i][j];
			}
			return lapcost;
		}

		public LinkedList<Solution> getSolution() {
			return solution;
		}		
	}
	public static class StringPair{
		public String left;
		public String right;
		public double weight;
		
		public boolean related = false;
		public boolean toRemove = false;
		public boolean chekced = false;
		
		public StringPair (String left, String right) {
			this.left =  left;
			this.right = right;
		}
		
		public StringPair (String left, String right, double weight) {
			this.left =  left;
			this.right = right;
			this.weight = weight;
		}

		public void relate() {
			this.related = true;
		}
		
		public void markToRemove() {
			this.toRemove = true;
		}
	}

	public static class Solution {

		public int left;
		public int right;
		public double weight;

		Solution (int left, int right, double weight) {
			this.left = left;
			this.right = right;
			this.weight = weight;
		}
	}

}
