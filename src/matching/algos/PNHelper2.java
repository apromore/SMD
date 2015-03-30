package matching.algos;

import nl.tue.tm.is.graph.SimpleGraph;
import nl.tue.tm.is.led.StringEditDistance;

public class PNHelper2 implements Helper {

	public double matchingCost(SimpleGraph graph1, Integer v1,
			SimpleGraph graph2, Integer v2) {
		double resultado = 0.0;

		if (graph1.getLabel(v1).length() > 0
				&& graph2.getLabel(v2).length() > 0) {

			resultado = StringEditDistance.similarity(graph1.getLabel(v1),
					graph2.getLabel(v2));

			return resultado;
		}
		
		return 0.0;
	}

	public double matchingCost(String a, String b) {
		if (a != null && b != null && a.length() > 0 && b.length() > 0) {
			double sim = StringEditDistance.similarity(a, b);
			
			return sim;
		}
		
		return 0.0;

	}
}
