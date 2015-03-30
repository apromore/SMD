package matching.algos;

import nl.tue.tm.is.graph.SimpleGraph;
import nl.tue.tm.is.led.StringEditDistance;

public class EPCHelper implements Helper {

	public double matchingCost(SimpleGraph graph1, Integer v1,
			SimpleGraph graph2, Integer v2) {
		double resultado = 0.0;
		if (graph1.connectors.contains(v1) && graph2.connectors.contains(v2))
			resultado = 0.1;
		else {
			if (!((graph1.functions.contains(v1) && graph2.functions.contains(v2)) ||
					(graph1.events.contains(v1) && graph2.events.contains(v2))))
				return Double.POSITIVE_INFINITY;
			double sim = StringEditDistance.similarity(graph1.getLabel(v1), graph2.getLabel(v2));
			resultado = sim > 0.4 ? sim : Double.POSITIVE_INFINITY;
		}
		return resultado;
	}

	public double matchingCost(String a, String b) {
		if (a.length() > 0 && b.length() > 0) {
			double sim = StringEditDistance.similarity(a, b);
			
			return sim;
		}
		
		return 0.0;
	}
}
