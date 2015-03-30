package common2.similarity;


import common.Settings;

import graph.Graph;
import graph.Vertex;
import graph.Vertex.Type;

public class NodeSimilarity {
	
	private SemanticSimilarity semanticSimilarity = null;
	private AssingmentProblem assingmentProblem = null;
	private LabelEditDistance labelEditDistance = null;

	public double findNodeSimilarity(Vertex n, Vertex m) {
		
		if (semanticSimilarity == null) {
			semanticSimilarity = new SemanticSimilarity();
			assingmentProblem = new AssingmentProblem();
			labelEditDistance = new LabelEditDistance();
		}
		
		// functions or events -
		// compare the labels of these nodes 
		// tokenize, stem and find the similarity score
		if ((n.getType().equals(Type.function) && m.getType().equals(Type.function)
		 || n.getType().equals(Type.event) && m.getType().equals(Type.event)) 
		 && assingmentProblem.canMap(n, m)) {
			return labelEditDistance.edTokensWithStemming(m.getLabel(), 
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
			double sim =  semanticSimilarity.getSemanticSimilarity(n, m);
//			System.out.println(">Similarity : "+sim);
			return sim;
		}
		return 0;
	}
}
