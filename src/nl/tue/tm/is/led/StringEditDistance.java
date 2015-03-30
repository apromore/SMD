package nl.tue.tm.is.led;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import edu.sussex.nlp.jws.JiangAndConrath;
import nl.tue.tm.is.labels.TokenizedLabel;
import nl.tue.tm.is.labels.TokenizedLabelCache;
import nl.tue.tm.is.labels.TokenizedLabelCacheStrings;

public class StringEditDistance {
	
	private static int totalCalls = 0;
	private static int cacheHits = 0;
	
	private static TokenizedLabelCacheStrings cache = null;
	public static Map<String, Double> wordnetCache = new HashMap<String, Double>();
	public static Map<String, Double> wordCache = new HashMap<String, Double>();

	
	private static JiangAndConrath jcn = null;
	
	public static void addCache(TokenizedLabelCacheStrings cache1) {
		cache = cache1;
	}
	
	public static boolean hasCache() {
		return cache != null;
	}
	
	public static void clearWordCache() {
		wordCache.clear();
	}
	
	public static TokenizedLabel getLabelFromCache(String label){
		// there should be only one tokenized label for one label
//		System.out.println("Get label from cache "+ label);
		return cache.get(label);
	}
	
	public static int editDistance(String label1, String label2) {
		String s = label1;
		String t = label2;

		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}
		int MAX_N = m + n;

		short[] swap; // placeholder to assist in swapping p and d

		// indexes into strings s and t
		short i; // iterates through s
		short j; // iterates through t

		Object t_j = null; // jth object of t

		short cost; // cost

		short[] d = new short[MAX_N + 1];
		short[] p = new short[MAX_N + 1];

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			t_j = t.charAt(j - 1);
			d[0] = j;

			Object s_i = null; // ith object of s
			for (i = 1; i <= n; i++) {
				s_i = s.charAt(i - 1);
				cost = s_i.equals(t_j) ? (short) 0 : (short) 1;
				// minimum of cell to the left+1, to the top+1, diagonally left
				// and up +cost
				d[i] = (short) Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}

			// copy current distance counts to 'previous row' distance counts
			swap = p;
			p = d;
			d = swap;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		int costcount = p[n];
		
//		System.out.println(costcount + " "+ Math.max(label1.length(), label2.length()));
		// equivalence score = 1 - (costcount / max_costcount)
		// where max_costcount = sum of string lengths
		return costcount;
		//return 1 - (costcount * 1.0) / (s.length() * 1.0 + t.length() * 1.0);
	}
	// 1 similar ; 0 - different
	public static double similarity(String label1, String label2) {
//		if (true) 
//			return 0.5;
//		
		if ((label1.length() == 0) && (label2.length() == 0)){
			return 1.0;
		}
		Double res = wordCache.get(label1+";"+label2);
//		totalCalls++;
		if (res != null) {
//			cacheHits++;
//			int hitRatio = cacheHits * 100 / totalCalls;
//			System.out.println("Cache hits: " + cacheHits + " out of: " + totalCalls + " | Hit ratio: " + hitRatio);
			return res;
		}
		
		if (StringEditDistance.hasWordnet()) {
			TokenizedLabel t1 = StringEditDistance.getLabelFromCache(label1);
			TokenizedLabel t2 = StringEditDistance.getLabelFromCache(label2);
			double sim = 0;
			PrintStream out = System.out;
			PrintStream tmpStream = new PrintStream(new ByteArrayOutputStream());
			try {
				System.setOut(tmpStream);
				sim = t1.similarityWordnet(t2, jcn);

			} finally {
				System.setOut(out);
				tmpStream.close();
				tmpStream = null;
			}
			wordCache.put(label1+";"+label2, /*1.0 - */sim);
			return /*1.0 - */sim;
		}
		
		if (StringEditDistance.hasCache() && 
				label1 != null && label1.length() != 0 && 
				label2 != null && label2.length() != 0) { 
			TokenizedLabel t1 = StringEditDistance.getLabelFromCache(label1);
			TokenizedLabel t2 = StringEditDistance.getLabelFromCache(label2);
			res = /*1.0 - */t1.similarity(t2);
			wordCache.put(label1+";"+label2, res);
			return res;
		}
		res = 1 - (editDistance(label1, label2)*1.0)/(Math.max(label1.length(), label2.length())*1.0);
		wordCache.put(label1+";"+label2, res);
		return res;  
	}

	public static void addWordnet(JiangAndConrath jcn1) {
		jcn = jcn1;
	}
	
	public static JiangAndConrath getWordnet() {
		return jcn;
	}
	
	public static boolean hasWordnet() {
		return jcn != null;
	}
}
