package matching.algos;

import nl.tue.tm.is.graph.SimpleGraph;

public interface DistanceAlgo {
	
	/**
	 * Given two graphs, returns a value by which graphs can be sorted for relevance,
	 * lowest value first. E.g. the value can be:
	 * - an edit distance (lower edit distance means better match between graphs)
	 * - 1.0 - similarity score (lower value means higher similarity score, means better match between graphs) 
	 * 
	 * @param sg1 A graph.
	 * @param sg2 A graph.
	 * @return A value, where a lower value represents a more relevant match between graphs.
	 */
	public double compute(SimpleGraph sg1, SimpleGraph sg2);
	
}
