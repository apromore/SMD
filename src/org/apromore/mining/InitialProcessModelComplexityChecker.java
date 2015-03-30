package org.apromore.mining;

import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.utils.CPFtoMultiDirectedGraphConverter;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.hypergraph.abs.Vertex;

public class InitialProcessModelComplexityChecker {

	DirectedGraphAlgorithms<DirectedEdge, Vertex> algo = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();

	public void process(MiningData data) {

		if (isComplex(data.getProcessModel())) {
			
		} else {
			data.getMinedModels().put(data.getLogCluster(), data.getProcessModel());
		}
	}

	private boolean isComplex(CPF processModel) {
		
		MultiDirectedGraph mdg = CPFtoMultiDirectedGraphConverter.covert(processModel);
//        if (!algo.isMultiTerminal(mdg)) {
//        	return true;
//        }
		
		return false;
	}

}
