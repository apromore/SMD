package org.apromore.mining.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apromore.graph.JBPT.CPF;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.hypergraph.abs.Vertex;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;

public class CPFtoMultiDirectedGraphConverter {
	
	public static MultiDirectedGraph covert(CPF cpf) {
		
		Map<FlowNode, Vertex> vmap = new HashMap<FlowNode, Vertex>();
		
		MultiDirectedGraph g = new MultiDirectedGraph();
		
		Collection<FlowNode> vs = cpf.getFlowNodes();
		for (FlowNode cv : vs) {
			Vertex v = new Vertex(cv.getLabel());
			v.setDescription(cv.getId());
			vmap.put(cv, v);
		}
		
		Collection<ControlFlow<FlowNode>> edges = cpf.getEdges();
		for (ControlFlow<FlowNode> e : edges) {
			Vertex v1 = vmap.get(e.getSource());
			Vertex v2 = vmap.get(e.getTarget());
			g.addEdge(v1, v2);
		}
		
		return g;
	}

}
