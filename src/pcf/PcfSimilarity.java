package pcf;

import java.util.LinkedList;

import org.tartarus.snowball.SnowballStemmer;

import nl.tue.tm.is.led.LabelEditDistance;

public class PcfSimilarity {
	private static String STRING_DELIMETER = " ,.:;&/?!#()1234567890";
	
	static final ClassificationTree classificationTree = PcfLoader.loadClassificationTree(STRING_DELIMETER);
	
	public static double pcfSimilarity(LinkedList<String> s1, LinkedList<String> s2, SnowballStemmer englishStemmer) {

		double similarity = 0;
		ClassificationTreeNode s1Best = null;
		ClassificationTreeNode s2Best = null;
		double s1Score = 0;
		double s2Score = 0;		
		
//		long time1 = System.currentTimeMillis();
		for (LinkedList<String> e : classificationTree.stringNodeMap.keySet()) {
			double s1Sim =  LabelEditDistance.edTokens1(s1, e, STRING_DELIMETER, englishStemmer, true);
			if (s1Sim > s1Score) {
				s1Best = classificationTree.stringNodeMap.get(e);
				s1Score = s1Sim;
			}
			double s2Sim =  LabelEditDistance.edTokens1(s2, e, STRING_DELIMETER, englishStemmer, true);
			if (s2Sim > s2Score) {
				s2Best = classificationTree.stringNodeMap.get(e);
				s2Score = s2Sim;
			}
		}
//		long time2 = System.currentTimeMillis();
//		System.out.println("time for class trree "+(time2 - time1));
		
		if (s1Best != null && s2Best != null) {
//			System.out.println("hop SIM");
			similarity = findHopSimilarity(s1Best, s2Best);
		}
		else {
//			System.out.println("LabelEditDistance SIM");
			similarity = LabelEditDistance.edTokens1(s1, s2, STRING_DELIMETER, englishStemmer, true);
		}
		
		return similarity;
	}
	
	private static double findHopSimilarity(ClassificationTreeNode s1, ClassificationTreeNode s2) {
//		System.out.println("<<<< "+s1.label + " : "+s2.label);
		int s1Hops = 0;
		int s2Hops = 0;
		
		while (s1.level > s2.level) {
			s1 = s1.parent;
			s1Hops++;
		}
		while (s2.level > s1.level) {
			s2 = s2.parent;
			s2Hops++;
		}
		while (!s1.equals(s2)) {
			s1 = s1.parent; 
			s2 = s2.parent;
			s1Hops++;
			s2Hops++;
		}
//		System.out.println(s1Hops+ " "+ s2Hops);
		return ((s1Hops + s2Hops == 0) ? 1 : (1 - (double)(s1Hops + s2Hops)/9));
	}
}
