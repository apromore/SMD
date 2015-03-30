package common;

import java.util.HashSet;
import java.util.StringTokenizer;

import common.stemmer.SnowballStemmer;

import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.JiangAndConrath;


public class Settings {
	public static HashSet<String> jura = new HashSet<String>();
	public static String STRING_DELIMETER = " ,.:;&/?!#()";
	public static double DEFAULT_THRESHOLD = 0.5;
	public static double MERGE_THRESHOLD = 0.5;
	public static double MERGE_CONTEXT_THRESHOLD = 0.75;
	
	public static boolean REMOVE_ENTANGLEMENT = true;
	public static ComparisonMethod COMPARISON_METHOD = ComparisonMethod.Greedy;
	
	// indicates if we need English or Dutch stemmer, 0 - english, 1 dutch
	public static int[] englishDutch = new int [] {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
	public static boolean logResult = true;
	public static boolean considerEvents = true;
	public static boolean considerGateways = true;
	
	public static boolean removeDuplicates = true;
	static JiangAndConrath jcn = null;
	
	// weights for greedy and other algorithms
	public static double vweight = 1.0;
	public static double sweight = 1.0;
	public static double eweight = 1.0;	
	
	public enum ComparisonMethod {
		Greedy,
		Hungarian
	}
	
	private static SnowballStemmer englishStemmer;
	
	public static SnowballStemmer getEnglishStemmer() {
		if (englishStemmer == null) {
			englishStemmer = getStemmer("english");
		}
		
		return englishStemmer;
	}
	
	public static JiangAndConrath getWordnet(){
		if (jcn == null) {
			String dir = "./lib/wordnet";
			JWS ws = new JWS(dir, "3.0", "ic-bnc-resnik-add1.dat");
			jcn = ws.getJiangAndConrath();
		}
		return jcn;
	}
	
	public static SnowballStemmer getStemmer(String language){
		@SuppressWarnings("rawtypes")
		Class stemClass;
		SnowballStemmer stemmer;

		try {
			stemClass = Class.forName("common.stemmer.ext." + language + "Stemmer");
			stemmer = (SnowballStemmer) stemClass.newInstance();
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return stemmer;
	}
	
	public static String removeSpaces(String s) {
		StringTokenizer st = new StringTokenizer(s);
		String result = "";
		
		while (st.hasMoreTokens()) {
			result += st.nextToken()+ (st.hasMoreTokens() ? " " : "");
		}
		
		return result;
	}

}
