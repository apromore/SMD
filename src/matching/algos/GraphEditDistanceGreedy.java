package matching.algos;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

//import com.sun.org.apache.bcel.internal.generic.CPInstruction;

import nl.tue.tm.is.graph.SimpleGraph;
import nl.tue.tm.is.graph.TwoVertices;
import nl.tue.tm.is.led.StringEditDistance;

/**
 * Class that implements the algorithm to compute the edit distance between two
 * SimpleGraph instances. Use the algorithm by calling the constructor with the two
 * SimpleGraph instances between which you want to compute the edit distance. Then call
 * compute(), which will return the edit distance.
 */
public class GraphEditDistanceGreedy extends DistanceAlgoAbstr implements DistanceAlgo{

	private Set<TwoVertices> times(Set<Integer> a, Set<Integer> b){
		Set<TwoVertices> result = new HashSet<TwoVertices>();
		for (Integer ea: a){
			for (Integer eb: b){
				if (StringEditDistance.similarity(sg1.getLabel(ea), sg2.getLabel(eb)) >= this.ledcutoff){
					result.add(new TwoVertices(ea,eb));
				}
			}
		}
		return result;
	}

	public double compute(SimpleGraph sg1, SimpleGraph sg2){
		init(sg1,sg2);

		//INIT
		Set<TwoVertices> mapping = new HashSet<TwoVertices>();
		Set<TwoVertices> openCouples = times(sg1.getVertices(), sg2.getVertices());
		double shortestEditDistance = Double.MAX_VALUE;
		Random randomized = new Random();
		int stepn = 0;
		//STEP
		boolean doStep = true;
		while (doStep){
			doStep = false;
			stepn++;
			Vector<TwoVertices> bestCandidates = new Vector<TwoVertices>();
			double newShortestEditDistance = shortestEditDistance;
//			long s1 = System.currentTimeMillis();
//			System.out.println("step : "+stepn);
			for (TwoVertices couple: openCouples){
				Set<TwoVertices> newMapping = new HashSet<TwoVertices>(mapping);
				newMapping.add(couple);
//				long t1 = System.currentTimeMillis();
				double newEditDistance = this.editDistance(newMapping); 
//				long t2 = System.currentTimeMillis();
//				System.out.println((t2-t1)+ " ms: openpair "+sg1.getLabel(couple.v1) + " "+ sg2.getLabel(couple.v2) + " "+newEditDistance+ " "+mapping.size());
				if (newEditDistance < newShortestEditDistance){
					bestCandidates = new Vector<TwoVertices>();
					bestCandidates.add(couple);
					newShortestEditDistance = newEditDistance;
				}else if (newEditDistance == newShortestEditDistance){
					bestCandidates.add(couple);
				}
			}

			if (bestCandidates.size() > 0){
				//Choose a random candidate
				TwoVertices couple = bestCandidates.get(randomized.nextInt(bestCandidates.size()));

				Set<TwoVertices> newOpenCouples = new HashSet<TwoVertices>();
				for (TwoVertices p: openCouples){
					if (!p.v1.equals(couple.v1) && !p.v2.equals(couple.v2)){
						newOpenCouples.add(p);
					}
				}
				openCouples = newOpenCouples;

				mapping.add(couple);
				shortestEditDistance = newShortestEditDistance;
				doStep = true;
			}
//			long s2 = System.currentTimeMillis();
//			System.out.println("step took time : "+(s2-s1));
		}
		

		//Return the smallest edit distance
		return shortestEditDistance;
	}
}
