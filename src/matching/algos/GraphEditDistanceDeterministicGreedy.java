package matching.algos;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import com.google.common.collect.TreeMultimap;
import com.google.common.collect.TreeMultiset;

import nl.tue.tm.is.graph.SimpleGraph;
import nl.tue.tm.is.graph.TwoVertices;
import nl.tue.tm.is.led.StringEditDistance;

/**
 * Class that implements the algorithm to compute the edit distance between two
 * SimpleGraph instances. Use the algorithm by calling the constructor with the two
 * SimpleGraph instances between which you want to compute the edit distance. Then call
 * compute(), which will return the edit distance.
 */
public class GraphEditDistanceDeterministicGreedy extends DistanceAlgoAbstr implements DistanceAlgo{
	
	boolean deterministic = true;
	public void resetDeterminismFlag() {
		deterministic = true;
	}
	public boolean isDeterministic() {
		return deterministic;
	}
	
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
				
				TwoVertices couple;
				
				// Case 1: Only one candidate pair
				if (bestCandidates.size() == 1)
					couple = bestCandidates.firstElement();
				else {
				//  CASE 2: Lexicographical order is enough
					TreeMultimap<String, TwoVertices> tmap = TreeMultimap.create();
					for (TwoVertices pair: bestCandidates) {
						String label1, label2;
						label1 = sg1.getLabel(pair.v1);
						label2 = sg2.getLabel(pair.v2);
						if (label1.compareTo(label2) > 0) {
							String tmp = label1;
							label1 = label2;
							label2 = tmp;
						}
						tmap.put(label1+label2, pair);
					}
					String firstkey = tmap.keySet().first();
					
					if (tmap.get(firstkey).size() == 1)
						couple = tmap.get(firstkey).first();
					else if (tmap.get(firstkey).size() > 1) {
						Set<TwoVertices> set = tmap.get(firstkey);
						TreeMultimap<String, TwoVertices> tmapp = TreeMultimap.create();
						
						TreeMultiset<String> mset = TreeMultiset.create();
						for (TwoVertices pair: set) {
							String label1, label2;
							label1 = sg1.getLabel(pair.v1);
							mset.clear();
							for (Integer n: sg1.preSet(pair.v1))
								mset.add(sg1.getLabel(n));
							label1 += mset.toString();
							mset.clear();
							for (Integer n: sg1.postSet(pair.v1))
								mset.add(sg1.getLabel(n));
							label1 += mset.toString();

							label2 = sg2.getLabel(pair.v2);
							mset.clear();
							for (Integer n: sg2.preSet(pair.v2))
								mset.add(sg2.getLabel(n));
							label2 += mset.toString();
							mset.clear();
							for (Integer n: sg2.postSet(pair.v2))
								mset.add(sg2.getLabel(n));
							label2 += mset.toString();
							
							if (label1.compareTo(label2) > 0) {
								String tmp = label1;
								label1 = label2;
								label2 = tmp;
							}
							tmapp.put(label1+label2, pair);
						}
						String contextkey = tmapp.keySet().first();
						// CASE 3: Composite labels (concatenation of labels of nodes surrounding the target vertex)
						if (tmapp.get(contextkey).size() == 1)
							couple = tmapp.get(contextkey).first();
						else {
							// CASE 4: Non deterministic choice (Choose a random candidate)
							deterministic = false;
							couple = bestCandidates.get(randomized.nextInt(bestCandidates.size()));
						}
					} else {
						// CASE 5: Non deterministic choice (Choose a random candidate)
						System.out.println("oops ...");
						deterministic = false;
						couple = bestCandidates.get(randomized.nextInt(bestCandidates.size()));
					}

				}

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
