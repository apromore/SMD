package matching.algos;

import nl.tue.tm.is.graph.SimpleGraph;

public interface Helper {
	double matchingCost(SimpleGraph graph1, Integer v1,
			SimpleGraph graph2, Integer v2);
	double matchingCost(String a, String b);
}