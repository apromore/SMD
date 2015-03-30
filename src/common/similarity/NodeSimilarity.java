package common.similarity;


import common.Settings;

import graph.Graph;
import graph.Vertex;
import graph.Vertex.Type;

public class NodeSimilarity {

	public static double findNodeSimilarity(Vertex n, Vertex m) {
		// functions or events -
		// compare the labels of these nodes 
		// tokenize, stem and find the similarity score
		if ((n.getType().equals(Type.function) && m.getType().equals(Type.function)
		 || n.getType().equals(Type.event) && m.getType().equals(Type.event)) 
		 && AssingmentProblem.canMap(n, m)) {
			return LabelEditDistance.edTokensWithStemming(m.getLabel(), 
					n.getLabel(), Settings.STRING_DELIMETER,
					Settings.getEnglishStemmer(), true);
			
		} 
		// gateways
		else if (n.getType().equals(Type.gateway) && m.getType().equals(Type.gateway)) {
			// splits can not be merged with joins
			if (Graph.isSplit(n) && Graph.isJoin(m)
					|| Graph.isSplit(m) && Graph.isJoin(n)) {
				return 0;
			}
			double sim =  SemanticSimilarity.getSemanticSimilarity(n, m);
//			System.out.println(">Similarity : "+sim);
			return sim;
		}
		return 0;
	}
}
