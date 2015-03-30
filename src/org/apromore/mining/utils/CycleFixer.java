package org.apromore.mining.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apromore.common.Constants;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfAndGateway;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.graph.JBPT.CpfGateway;
import org.apromore.graph.JBPT.CpfNode;
import org.apromore.graph.JBPT.CpfOrGateway;
import org.apromore.graph.JBPT.CpfXorGateway;
import org.apromore.mining.MiningConfig;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.apromore.util.DebugUtil;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.graph.algo.StronglyConnectedComponents;
import org.jbpt.hypergraph.abs.Vertex;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CycleFixer {

	public static int attempts = 0;
	public static int success = 0;

	private static final Logger logger = LoggerFactory.getLogger(CycleFixer.class);

	private static DirectedGraphAlgorithms<DirectedEdge, Vertex> ga = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	private static StronglyConnectedComponents<DirectedEdge, Vertex> sa = new StronglyConnectedComponents<DirectedEdge, Vertex>();

	public static boolean fixCycles(CPF model) {
		
		if (MiningConfig.PURE_MODELS) {
			return true;
		}
		
		attempts++;
		
		removeFloatingFictiveStart(model);
		removeFloatingFictiveEnd(model);
		
		boolean fixed = true;
		while (fixed) {
			MultiDirectedGraph mdg = CPFtoMultiDirectedGraphConverter.covert(model);
			fixed = fixCycles(model, mdg);
		}
		
		MultiDirectedGraph resultGraph = CPFtoMultiDirectedGraphConverter.covert(model);
		boolean multiterminal = ga.isMultiTerminal(resultGraph);
		if (multiterminal) {
			if (model.getVertices().size() < 50) {
//				DebugUtil.writeModel("fixed", model);
			}
			success++;
		}
		return multiterminal;
	}

	private static boolean fixCycles(CPF model, MultiDirectedGraph g) {

		

		boolean fixed = false;

		if (ga.isAcyclic(g)) {
			return false;
		}

		Set<Set<Vertex>> sccs = sa.compute(g);
		for (Set<Vertex> scc : sccs) {
			if (scc.size() < 2) {
				continue;
			}
			
			fixed = fixNoEntryCycles(scc, g, model);
			if (fixed) {
				break;
			}
			
			fixed = fixNoExitCycles(scc, g, model);
			if (fixed) {
				break;
			}
			
			fixed = fixIsolatedCycles(scc, g, model);
			if (fixed) {
				break;
			}

//			fixed = resolveMultiEntryLoops(scc, g, model);
//			if (fixed == true) {
//				break;
//			}
//
//			fixed = resolveMultiExitLoops(scc, g, model);
//			if (fixed == true) {
//				break;
//			}
		}

		return fixed;
	}

	private static boolean fixIsolatedCycles(Set<Vertex> scc, MultiDirectedGraph g, CPF model) {
		
		// there can be loops with no gateways. these will occur as isolated graphs in the model.
		// if there is a part of graph, other than a isolated loop, include the isolated loop into that graph.
		// if there is no other graph, add an entry and exit for the loop
		
		Collection<Vertex> gvs = g.getVertices();
		Collection<Vertex> nonSCC = new HashSet<Vertex>();
		nonSCC.addAll(gvs);
		nonSCC.removeAll(scc);
		
		// check whether there are CPF gateway nodes in the loop
		boolean gatewayFound = false;
		for (Vertex v : scc) {
			String flowNodeId = v.getDescription();
			FlowNode flowNode = model.getVertex(flowNodeId);
			if (flowNode instanceof CpfGateway) {
				gatewayFound = true;
				break;
			}
		}
		
		// check whether there are non-gateways acting as gateways
		// e.g. sometimes there are functions and events acting as gateways
		boolean invalidGatewayFound = false;
		for (Vertex v : scc) {
			if (g.getDirectPredecessors(v).size() > 1 || g.getDirectSuccessors(v).size() > 1) {
				invalidGatewayFound = true;
				break;
			}
		}
		
		if (gatewayFound || invalidGatewayFound) {
			// there are gateways in this loop. so it does not qualify for this fix.
			return false;
		}
		
		// now we know that this is an isolated loop.
		
		// select two different edges to insert xor gateways
		DirectedEdge[] selectedEdges = selectEdges(g, scc);
		DirectedEdge edge1 = selectedEdges[0];
		DirectedEdge edge2 = selectedEdges[1];
//		Collection<DirectedEdge> loopEdges = g.getEdges(scc);
//		for (DirectedEdge edge : loopEdges) {
//			if (edge1 == null) {
//				edge1 = edge;
//			} else {
//				edge2 = edge;
//			}
//		}
		
		// add loop entry to edge 1
		CpfXorGateway loopEntry = insertNode(edge1, model);
		
		// add loop exit to edge 2
		CpfXorGateway loopExit = insertNode(edge2, model);
		
		if (nonSCC.isEmpty()) {
			// loop is the only part of the model. add artificial start and end events.
			connectToArtificialTerminals(loopEntry, loopExit, model);
		} else {
			// there are other parts in the model. connect the loop with one of those parts.
			connectToExistingParts(loopEntry, loopExit, nonSCC, g, model);
		}
		
		return true;
	}

	private static DirectedEdge[] selectEdges(MultiDirectedGraph g, Set<Vertex> scc) {
		
		DirectedEdge edge1 = null;
		int edge1Hash = 0;
		Collection<DirectedEdge> loopEdges = g.getEdges(scc);
		for (DirectedEdge edge : loopEdges) {
			if (edge1 == null) {
				edge1 = edge;
				edge1Hash = computeEdgeHash(edge, g);
			} else {
				int edgeHash = computeEdgeHash(edge, g);
				if (edgeHash < edge1Hash) {
					edge1 = edge;
					edge1Hash = edgeHash;
				}
			}
		}
		
		DirectedEdge edge2 = null;
		int edge2Hash = 0;
		
		Collection<DirectedEdge> loopEdges2 = g.getEdges(scc);
		for (DirectedEdge edge : loopEdges2) {
			if (edge.equals(edge1)) {
				continue;
			}
			
			if (edge2 == null) {
				edge2 = edge;
				
			} else {
				int edgeHash = computeEdgeHash(edge, g);
				if (edgeHash > edge2Hash) {
					edge2 = edge;
					edge2Hash = edgeHash;
				}
			}
		}
		
		return new DirectedEdge[] {edge1, edge2};
	}

	private static int computeEdgeHash(DirectedEdge edge, MultiDirectedGraph g) {
		int edgeHash = 0;
		
		Vertex source = edge.getSource();
		if (source != null && source.getName() != null) {
			edgeHash += source.getName().hashCode();
		}
		
		Vertex target = edge.getTarget();
		if (target != null && target.getName() != null) {
			edgeHash += target.getName().hashCode();
		}
		return edgeHash;
	}

	private static void connectToExistingParts(CpfXorGateway loopEntry, CpfXorGateway loopExit,
			Collection<Vertex> nonSCC, MultiDirectedGraph g, CPF model) {
		
		// we are adding the loop just before the sink of the non-loop part
		
		// find the sink
		FlowNode end = null;
		for (Vertex v : nonSCC) {
			if (g.getDirectSuccessors(v).isEmpty()) {
				String nodeId = v.getDescription();
				end = model.getVertex(nodeId);
				break;
			}
		}
		
		Collection<FlowNode> preset = model.getDirectPredecessors(end);
		FlowNode source = null;
		for (FlowNode n : preset) {
			source = n;
			break;
		}
		
		ControlFlow<FlowNode> oldEdge = model.getEdge(source, end);
		model.removeControlFlow(oldEdge);
		
		model.addControlFlow(source, loopEntry);
		model.addControlFlow(loopExit, end);
		
		logger.debug("Fixed an isolated cycle by connecting to it between {} and {} in existing parts in a model of size {}.", 
				new Object[]{source.getName(), end.getName(), model.getVertices().size()});
	}

	private static void connectToArtificialTerminals(CpfXorGateway loopEntry, CpfXorGateway loopExit, CPF model) {
		
		CpfEvent start = new CpfEvent("fictive start");
		model.addFlowNode(start);
		model.setVertexProperty(start.getId(), Constants.TYPE, Constants.EVENT);
		model.addControlFlow(start, loopEntry);
		
		CpfEvent end = new CpfEvent("fictive end");
		model.addFlowNode(end);
		model.setVertexProperty(end.getId(), Constants.TYPE, Constants.EVENT);
		model.addControlFlow(loopExit, end);
		
		logger.debug("Fixed an isolated cycle by adding fictive start and fictive end in a model of size {}.", 
				model.getVertices().size());
	}

	private static CpfXorGateway insertNode(DirectedEdge edge, CPF model) {
		
		CpfXorGateway newNode = new CpfXorGateway("XOR");
		model.addFlowNode(newNode);
		model.setVertexProperty(newNode.getId(), Constants.TYPE, Constants.CONNECTOR);
		
		String sourceId = edge.getSource().getDescription();
		FlowNode source = model.getVertex(sourceId);
		
		String targetId = edge.getTarget().getDescription();
		FlowNode target = model.getVertex(targetId);
		
		// remove the existing edge from the model
		ControlFlow<FlowNode> currentEdge = model.getDirectedEdge(source, target);
		model.removeControlFlow(currentEdge);
		
		model.addControlFlow(source, newNode);
		model.addControlFlow(newNode, target);
		
		return newNode;
	}
	
	private static boolean fixNoEntryCycles(Set<Vertex> scc, MultiDirectedGraph g, CPF model) {
		
		boolean fixed = false;
		
		// each cycle should have at least one entry.
		// entry is an XOR join in scc, where at least one preset is not within scc
		// IF we cannot find such XOR join,
		// AND IF there is at least one XOR join,
		// we should add artificial start event as a new preset of that XOR split.
		
		Collection<Vertex> gvs = g.getVertices();
		Collection<Vertex> nonSCC = new HashSet<Vertex>();
		nonSCC.addAll(gvs);
		nonSCC.removeAll(scc);
		
		boolean entryFound = false;
		Vertex candidateEntry = null;
		int candidateNeighbourHash = 0;
		for (Vertex v : scc) {
			
			String flowNodeId = v.getDescription();
			FlowNode flowNode = model.getVertex(flowNodeId);
			if (!(flowNode instanceof CpfXorGateway) && !(flowNode instanceof CpfOrGateway)) {
				// candidate exit should be an XOR or OR join
				continue;
			}
			
			Collection<Vertex> preset = g.getDirectPredecessors(v);
			Collection<Vertex> postset = g.getDirectSuccessors(v);
			
			if (postset.size() == 1) {
				// this is not a split (i.e. this can be a join).
				
				if (candidateEntry == null) {
					// if this loop does not have an entry, let's assign this join as a potential candidate entry
					candidateEntry = v;
					candidateNeighbourHash = computeNeighbourHash(v, g);
				} else {
					// we select the join with lowest neighbour hash as the entry to improve predictability
					int neighbourHash = computeNeighbourHash(v, g);
					if (neighbourHash < candidateNeighbourHash) {
						candidateEntry = v;
						candidateNeighbourHash = neighbourHash;
					}
				}
				
				if (!Collections.disjoint(nonSCC, preset)) {
					entryFound = true;
					break;
				}
			}
		}
		
		if (!entryFound) {
			// there is no entry for this loop. let's add an artificial entry.
			
			if (candidateEntry != null) {
				String joinId = candidateEntry.getDescription();
				FlowNode join = model.getVertex(joinId);
				CpfEvent startEvent = new CpfEvent("fictive start");
				model.addFlowNode(startEvent);
				model.setVertexProperty(startEvent.getId(), Constants.TYPE, Constants.EVENT);
				model.addEdge(startEvent, join);
				fixed = true;
				
				logger.debug("Fixed a no-entry loop by inserting a artificial start event in a model of size {}", 
						model.getVertices().size());
			}
		}
		
		return fixed;
	}

	private static int computeNeighbourHash(Vertex v, MultiDirectedGraph g) {
		
		int neighbourHash = 0;
		Collection<Vertex> preset = g.getDirectPredecessors(v);
		for (Vertex pre : preset) {
			String preName = pre.getName();
			if (preName != null) {
				int h = preName.hashCode();
				neighbourHash += h;
			}
		}
		
		Collection<Vertex> postset = g.getDirectSuccessors(v);
		for (Vertex post : postset) {
			String postName = post.getName();
			if (postName != null) {
				int h = postName.hashCode();
				neighbourHash += h;
			}
		}
		
		return neighbourHash;
	}

	private static boolean fixNoExitCycles(Set<Vertex> scc, MultiDirectedGraph g, CPF model) {
		
		boolean fixed = false;
		
		// each cycle should have at least one exit.
		// exit is a XOR split in scc, where at least one postset is not within scc
		// IF we cannot find such XOR split,
		// AND IF there is at least one XOR split,
		// we should add artificial end event as a new postset of that XOR split.
		
		Collection<Vertex> gvs = g.getVertices();
		Collection<Vertex> nonSCC = new HashSet<Vertex>();
		nonSCC.addAll(gvs);
		nonSCC.removeAll(scc);
		
		boolean exitFound = false;
		Vertex candidateExit = null;
		int candidateNeighbourHash = 0;
		for (Vertex v : scc) {
			
			String flowNodeId = v.getDescription();
			FlowNode flowNode = model.getVertex(flowNodeId);
			if (!(flowNode instanceof CpfXorGateway)  && !(flowNode instanceof CpfOrGateway)) {
				// candidate exit should be an XOR or OR split
				continue;
			}
			
			Collection<Vertex> preset = g.getDirectPredecessors(v);
			Collection<Vertex> postset = g.getDirectSuccessors(v);
			
			if (preset.size() == 1) {
				// this is not a join.
				
				if (candidateExit == null) {
					// if this loop does not have an exit, let's assign this split as a potential candidate exit
					candidateExit = v;
					candidateNeighbourHash = computeNeighbourHash(v, g);
				} else {
					// we select the candidate with lowest neighbour hash to improve predictability.
					int neighbourHash = computeNeighbourHash(v, g);
					if (neighbourHash < candidateNeighbourHash) {
						candidateExit = v;
						candidateNeighbourHash = neighbourHash;
					}
				}
				
				if (!Collections.disjoint(nonSCC, postset)) {
					exitFound = true;
					break;
				}
			}
		}
		
		if (!exitFound) {
			// there is no exit for this loop. let's add an artificial exit.
			
			if (candidateExit != null) {
				String splitId = candidateExit.getDescription();
				FlowNode split = model.getVertex(splitId);
				CpfEvent endEvent = new CpfEvent("fictive end");
				model.addFlowNode(endEvent);
				model.setVertexProperty(endEvent.getId(), Constants.TYPE, Constants.EVENT);
				model.addEdge(split, endEvent);
				fixed = true;
				
				logger.debug("Fixed a no-exit loop by inserting a artificial start event in a model of size {}", 
						model.getVertices().size());
			}
		}
		
		return fixed;
	}
	
	public static boolean removeFloatingFictiveStart(CPF model) {
		// it is common to have unconnected "fictive start" with one XOR as postset. remove those if exists.
		
		Set<FlowNode> nodesToBeRemoved = new HashSet<FlowNode>();
		
		boolean removed = false;
		for (FlowNode fn : model.getFlowNodes()) {
			if ("fictive start".equals(fn.getName())) {
				// this is an artificial start
				
				if (model.getDirectPredecessors(fn).isEmpty()) {
					// this is a terminal
					
					Collection<FlowNode> postset = model.getDirectSuccessors(fn);
					if (postset.size() == 1) {
						// there is only one postset node
						for (FlowNode pn : postset) {
							if (pn instanceof CpfXorGateway) {
								// postset is an XOR
								
								if (model.getDirectSuccessors(pn).isEmpty()) {
									// fn is a floating fictive start
									nodesToBeRemoved.add(fn);
									nodesToBeRemoved.add(pn);
									removed = true;
								}
							}
						}
					}
				}
			}
		}
		
		model.removeVertices(nodesToBeRemoved);
		return removed;
	}
	
	public static boolean removeFloatingFictiveEnd(CPF model) {
		// it is common to have unconnected "fictive end" with one XOR as preset. remove those if exists.
		
		Set<FlowNode> nodesToBeRemoved = new HashSet<FlowNode>();
		
		boolean removed = false;
		for (FlowNode fn : model.getFlowNodes()) {
			if ("fictive end".equals(fn.getName())) {
				// this is an artificial end
				
				if (model.getDirectSuccessors(fn).isEmpty()) {
					// this is a terminal
					
					Collection<FlowNode> preset = model.getDirectPredecessors(fn);
					if (preset.size() == 1) {
						// there is only one preset node
						for (FlowNode pn : preset) {
							if (pn instanceof CpfXorGateway) {
								// preset is an XOR
								
								if (model.getDirectPredecessors(pn).isEmpty()) {
									// fn is a floating fictive end
									nodesToBeRemoved.add(fn);
									nodesToBeRemoved.add(pn);
									removed = true;
								}
							}
						}
					}
				}
			}
		}
		
		model.removeVertices(nodesToBeRemoved);
		return removed;
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
