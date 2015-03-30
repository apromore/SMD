package org.apromore.mining.utils;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apromore.graph.JBPT.CPF;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.graph.algo.StronglyConnectedComponents;
import org.jbpt.hypergraph.abs.Vertex;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CycleRemover {
	
	private static String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp/faulty";
	private static FormattableEPCSerializer epcSerializer = new FormattableEPCSerializer();
	private static int faultyModelNumber = 0;
	
	public static int attempts = 0;
	public static int success = 0;
	
	private static final Logger logger = LoggerFactory.getLogger(CycleRemover.class);
	
	private static DirectedGraphAlgorithms<DirectedEdge, Vertex> ga = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	private static StronglyConnectedComponents<DirectedEdge, Vertex> sa = new StronglyConnectedComponents<DirectedEdge, Vertex>();
	
	private static Collection<DirectedEdge> allEdgesToBeRemoved = new HashSet<DirectedEdge>();
	
	public static boolean removeCycles(CPF model, boolean untilMultiTerminal) {
		MultiDirectedGraph mdg = CPFtoMultiDirectedGraphConverter.covert(model);
		return removeCycles(model, mdg, untilMultiTerminal);
	}
	
	public static boolean removeCycles(CPF model, MultiDirectedGraph g, boolean untilMultiTerminal) {
		
		attempts++;
		
		allEdgesToBeRemoved.clear();
		
		if (ga.isAcyclic(g)) {
			return false;
		}
		
		Set<Set<Vertex>> sccs = sa.compute(g);
		for (Set<Vertex> scc : sccs) {
			if (scc.size() < 2) {
				continue;
			}
			
			// IF we can find a join vertex whose preset intersects with non scc vertices and with scc vertices, and whose
			// postset vertex is in scc,
			// THEN we can try by removing incoming edges to v within scc.
			Collection<Vertex> gvs = g.getVertices();
			Collection<Vertex> nonSCC = new HashSet<Vertex>();
			nonSCC.addAll(gvs);
			nonSCC.removeAll(scc);
			
			for (Vertex v : scc) {
				Collection<Vertex> preset = g.getDirectPredecessors(v);
				Collection<Vertex> postset = g.getDirectSuccessors(v);
				
				if (preset.size() > 1 && postset.size() == 1) {
					// this is a join
					
					if (!Collections.disjoint(preset, nonSCC)) {
						// preset intersects with non scc vertices
						
						if (!Collections.disjoint(preset, scc)) {
							// preset intersects with scc vertices
							
							if (!Collections.disjoint(postset, scc)) {
								// postset vertex is in scc
								
								logger.debug("{} found as a entry point to a cycle.", v.getLabel());
								removeCycle(v, scc, g);
								
								if (untilMultiTerminal && ga.isMultiTerminal(g)) {
									logger.debug("Graph is now multi-terminal. Terminating the cycle removal process...");
									break;
								}
							}
						}
					}
				}
			}
		}
		
		if (ga.isMultiTerminal(g)) {
			applyChanges(model);
		} else {
			logger.debug("Graph is still not multi-terminal after cycle removal.");
			if (logger.isDebugEnabled()) {
				if (model.getVertices().size() < 20) {
					faultyModelNumber++;
					File faulyModel = new File(outPath, "faulty_" + faultyModelNumber + ".epml");
					epcSerializer.serialize(model, faulyModel.getAbsolutePath());
				}
			}
			return false;
		}
		
		success++;
		return true;
	}

	private static void applyChanges(CPF model) {

		logger.debug("Applying cycle removal changes to the process model...");
		
		Collection<ControlFlow<FlowNode>> flowsToBeRemoved = new HashSet<ControlFlow<FlowNode>>();
		
		for (DirectedEdge e : allEdgesToBeRemoved) {
			String sid = e.getSource().getDescription();
			String tid = e.getTarget().getDescription();
			
			FlowNode s = model.getVertex(sid);
			FlowNode t = model.getVertex(tid);
			ControlFlow<FlowNode> cflow = model.getEdge(s, t);
			flowsToBeRemoved.add(cflow);
		}
		model.removeControlFlows(flowsToBeRemoved);
	}

	private static void removeCycle(Vertex v, Set<Vertex> scc, MultiDirectedGraph g) {
		
		Collection<DirectedEdge> edgesToBeRemoved = new HashSet<DirectedEdge>();
		Collection<DirectedEdge> inEdges = g.getIncomingEdges(v);
		
		for (DirectedEdge inEdge : inEdges) {
			
			Vertex s = inEdge.getSource();
			if (scc.contains(s)) {
				edgesToBeRemoved.add(inEdge);
			}
		}
		allEdgesToBeRemoved.addAll(edgesToBeRemoved);
		g.removeEdges(edgesToBeRemoved);
		logger.debug("Removed {} edges to remove the cycle.", edgesToBeRemoved.size());
	}

}
