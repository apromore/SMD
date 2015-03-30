package matching.algos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mallardsoft.tuple.Pair;

import nl.tue.tm.is.graph.SimpleGraph;
import nl.tue.tm.is.led.OptimalLabelMapping;
import nl.tue.tm.is.led.StringEditDistance;

public class GraphEditDistanceOptimalLabel extends DistanceAlgoAbstr implements DistanceAlgo {

	private Set<Pair<Integer,Integer>> leafNodeMapping;
	private Map<Pair<Integer,Integer>,Double> pairToSimilarityScore;
	
	private Set<Pair<Integer,Integer>> common(Set<Integer> vertices1, Set<Integer> vertices2){
		Set<Pair<Integer,Integer>> result = new HashSet<Pair<Integer,Integer>>();
		
		for (Pair<Integer,Integer> p: leafNodeMapping){
			if (vertices1.contains(Pair.get1(p)) && vertices2.contains(Pair.get2(p))){
				result.add(p);
			}
		}
		
		return result;
	}
	
	private double commonScore(Set<Integer> vertices1, Set<Integer> vertices2){
		double result = 0.0;
		
		for (Pair<Integer,Integer> p: leafNodeMapping){
			if (vertices1.contains(Pair.get1(p)) && vertices2.contains(Pair.get2(p))){
				result +=  pairToSimilarityScore.get(p);
			}
		}
		
		return result;
	}
	
	public double compute(SimpleGraph sg1, SimpleGraph sg2) {
		init(sg1,sg2);
		
		leafNodeMapping = OptimalLabelMapping.optimalScore1(sg1, sg2);
		pairToSimilarityScore = new HashMap<Pair<Integer,Integer>,Double>();
		for (Pair<Integer,Integer> p: leafNodeMapping){
			pairToSimilarityScore.put(p, 1.0 - StringEditDistance.similarity(sg1.getLabel(Pair.get1(p)), sg2.getLabel(Pair.get2(p))));
		}

		Set<Integer> ns1 = sg1.getVertices();
		Set<Integer> ns2 = sg2.getVertices();
		
		/*
		//double nrCommon = (double) common(ns1,ns2).size();
		double nrNs1 = (double) ns1.size();
		double nrNs2 = (double) ns2.size();
		//return nrCommon / Math.max(nrNs1, nrNs2);
		return 1.0 - (commonScore(ns1,ns2) / Math.max(nrNs1, nrNs2));
		*/
		
		double nrCommon = (double) common(ns1,ns2).size();
		double skippedVertices = 1.0 * totalNrVertices - 2.0 * nrCommon;
		double substitutedVertices = commonScore(ns1,ns2);
		
		double vskip = skippedVertices / (1.0 * totalNrVertices);
		double vsubs = (2.0*substitutedVertices) / (1.0 * totalNrVertices - skippedVertices);

		return ((weightSkippedVertex * vskip) + (weightSubstitutedVertex * vsubs))/(weightSkippedVertex+weightSubstitutedVertex);
	}

}
