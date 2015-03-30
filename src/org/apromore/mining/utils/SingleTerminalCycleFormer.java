package org.apromore.mining.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apromore.common.Constants;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfAndGateway;
import org.apromore.graph.JBPT.CpfNode;
import org.apromore.graph.JBPT.CpfOrGateway;
import org.apromore.graph.JBPT.CpfXorGateway;
import org.apromore.mining.MiningConfig;
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

public class SingleTerminalCycleFormer {

	public static int attempts = 0;
	public static int success = 0;

	private static final Logger logger = LoggerFactory.getLogger(SingleTerminalCycleFormer.class);

	private static DirectedGraphAlgorithms<DirectedEdge, Vertex> ga = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	private static StronglyConnectedComponents<DirectedEdge, Vertex> sa = new StronglyConnectedComponents<DirectedEdge, Vertex>();

	public static void formSingleTerminalCycles(CPF model) {
		
		if (MiningConfig.PURE_MODELS) {
			return;
		}

		boolean found = true;
		while (found) {
			MultiDirectedGraph mdg = CPFtoMultiDirectedGraphConverter.covert(model);
			found = formSingleTerminalCycles(model, mdg);
		}
	}

	private static boolean formSingleTerminalCycles(CPF model, MultiDirectedGraph g) {

		attempts++;

		boolean found = false;

		if (ga.isAcyclic(g)) {
			return false;
		}

		Set<Set<Vertex>> sccs = sa.compute(g);
		for (Set<Vertex> scc : sccs) {
			if (scc.size() < 2) {
				continue;
			}

			found = resolveMultiEntryLoops(scc, g, model);
			if (found == true) {
				break;
			}

			found = resolveMultiExitLoops(scc, g, model);
			if (found == true) {
				break;
			}
		}

//		 FormattableEPCSerializer serializer = new FormattableEPCSerializer();
//		 String outpath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/t2/fs/ff1.epml";
//		 serializer.serialize(model, outpath);

		success++;
		return found;
	}

	private static boolean resolveMultiEntryLoops(Set<Vertex> scc, MultiDirectedGraph g, CPF model) {

		// IF we can find a join vertex whose preset intersects with AT LEAST TWO non scc vertices and with
		// scc vertices, and whose postset vertex is in scc,
		// THEN we have to duplicate that connector and form a single entry terminal

		boolean found = false;

		Collection<Vertex> gvs = g.getVertices();
		Collection<Vertex> nonSCC = new HashSet<Vertex>();
		nonSCC.addAll(gvs);
		nonSCC.removeAll(scc);

		for (Vertex v : scc) {
			Collection<Vertex> preset = g.getDirectPredecessors(v);
			Collection<Vertex> postset = g.getDirectSuccessors(v);

			if (preset.size() > 1 && postset.size() == 1) {
				// this is a join

				Set<Vertex> intersectionPresetNonSCC = new HashSet<Vertex>(preset);
				intersectionPresetNonSCC.retainAll(nonSCC);
				if (intersectionPresetNonSCC.size() > 1) {
					// preset intersects with at least 2 non scc vertices

					if (!Collections.disjoint(preset, scc)) {
						// preset intersects with scc vertices

						if (!Collections.disjoint(postset, scc)) {
							// postset vertex is in scc

							logger.debug("{} found as an entry point to a multi-entry cycle.", v.getLabel());
							formSingleEntryCycle(v, scc, intersectionPresetNonSCC, g, model);
							found = true;
							break;
						}
					}
				}
			}
		}

		return found;
	}

	private static void formSingleEntryCycle(Vertex v, Set<Vertex> scc, Set<Vertex> intersectionPresetNonSCC,
			MultiDirectedGraph g, CPF model) {

		// add a new connector
		String fnId = v.getDescription();
		FlowNode oldConnector = model.getVertex(fnId);
		FlowNode newConnector = duplicateCPFFlowNode(oldConnector, model);

		for (Vertex nonSCCPreset : intersectionPresetNonSCC) {

			// remove the edge connecting non-scc preset and old connector
			DirectedEdge e = g.getDirectedEdge(nonSCCPreset, v);
			String sid = e.getSource().getDescription();
			FlowNode s = model.getVertex(sid);
			String tid = e.getTarget().getDescription();
			FlowNode t = model.getVertex(tid);
			ControlFlow<FlowNode> flowToRemove = model.getEdge(s, t);
			model.removeControlFlow(flowToRemove);

			// add an edge between non-scc preset node and new connector
			String presetId = nonSCCPreset.getDescription();
			FlowNode presetFN = model.getVertex(presetId);
			model.addEdge(presetFN, newConnector);
		}

		// add an edge between new connector and old connector
		model.addControlFlow(newConnector, oldConnector);
	}

	private static boolean resolveMultiExitLoops(Set<Vertex> scc, MultiDirectedGraph g, CPF model) {

		// IF we can find a split vertex whose postset intersects with AT LEAST TWO non scc vertices and with
		// scc vertices, and whose preset vertex is in scc,
		// THEN we have to duplicate that connector and form a single exit terminal

		boolean found = false;

		Collection<Vertex> gvs = g.getVertices();
		Collection<Vertex> nonSCC = new HashSet<Vertex>();
		nonSCC.addAll(gvs);
		nonSCC.removeAll(scc);

		for (Vertex v : scc) {
			Collection<Vertex> preset = g.getDirectPredecessors(v);
			Collection<Vertex> postset = g.getDirectSuccessors(v);

			if (preset.size() == 1 && postset.size() > 1) {
				// this is a split

				Set<Vertex> intersectionPostsetNonSCC = new HashSet<Vertex>(postset);
				intersectionPostsetNonSCC.retainAll(nonSCC);
				if (intersectionPostsetNonSCC.size() > 1) {
					// postset intersects with at least 2 non scc vertices

					if (!Collections.disjoint(postset, scc)) {
						// postset intersects with scc vertices

						if (!Collections.disjoint(preset, scc)) {
							// preset vertex is in scc

							logger.debug("{} found as an exit point to a multi-exit cycle.", v.getLabel());
							formSingleExitCycle(v, scc, intersectionPostsetNonSCC, g, model);
							found = true;
							break;
						}
					}
				}
			}
		}

		return found;
	}

	private static void formSingleExitCycle(Vertex v, Set<Vertex> scc, Set<Vertex> intersectionPostsetNonSCC,
			MultiDirectedGraph g, CPF model) {

		// add a new connector
		String fnId = v.getDescription();
		FlowNode oldConnector = model.getVertex(fnId);
		FlowNode newConnector = duplicateCPFFlowNode(oldConnector, model);

		for (Vertex nonSCCPostset : intersectionPostsetNonSCC) {

			// remove the edge connecting old connector and non-scc postset
			DirectedEdge e = g.getDirectedEdge(v, nonSCCPostset);
			String sid = e.getSource().getDescription();
			FlowNode s = model.getVertex(sid);
			String tid = e.getTarget().getDescription();
			FlowNode t = model.getVertex(tid);
			ControlFlow<FlowNode> flowToRemove = model.getEdge(s, t);
			model.removeControlFlow(flowToRemove);

			// add an edge between new connector and non-scc posetset
			String postsetId = nonSCCPostset.getDescription();
			FlowNode postsetFN = model.getVertex(postsetId);
			model.addEdge(newConnector, postsetFN);
		}

		// add an edge between old connector and new connector
		model.addControlFlow(oldConnector, newConnector);
	}

	private static FlowNode duplicateVertex(Vertex v) {
		String label = v.getName();

		FlowNode newV = null;
		if (v instanceof CpfXorGateway) {
			newV = new CpfXorGateway(label);
		} else if (v instanceof CpfAndGateway) {
			newV = new CpfAndGateway(label);
		} else if (v instanceof CpfOrGateway) {
			newV = new CpfOrGateway(label);
		} else {
			newV = new CpfNode(label);
		}
		return newV;
	}

	private static FlowNode duplicateCPFFlowNode(Vertex v, CPF og) {
		String label = v.getName();
		String type = og.getVertexProperty(v.getId(), Constants.TYPE);

		FlowNode newV = null;
		if (v instanceof CpfXorGateway) {
			newV = new CpfXorGateway(label);
		} else if (v instanceof CpfAndGateway) {
			newV = new CpfAndGateway(label);
		} else if (v instanceof CpfOrGateway) {
			newV = new CpfOrGateway(label);
		} else {
			newV = new CpfNode(label);
		}
		og.addVertex(newV);
		og.setVertexProperty(newV.getId(), Constants.TYPE, type);
		return newV;
	}

}
