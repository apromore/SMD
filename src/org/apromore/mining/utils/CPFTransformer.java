package org.apromore.mining.utils;

import java.util.Collection;

import org.apromore.common.Constants;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfAndGateway;
import org.apromore.graph.JBPT.CpfGateway;
import org.apromore.graph.JBPT.CpfNode;
import org.apromore.graph.JBPT.CpfOrGateway;
import org.apromore.graph.JBPT.CpfXorGateway;
import org.apromore.mining.MiningConfig;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;

public class CPFTransformer {

	public static void correct(CPF g) {
		
		if (MiningConfig.PURE_MODELS) {
			return;
		}

		boolean errorFound = false;
		do {
			errorFound = false;
			for (FlowNode v : g.getFlowNodes()) {
				Collection<FlowNode> preset = g.getDirectPredecessors(v);
				Collection<FlowNode> postset = g.getDirectSuccessors(v);
				
				errorFound = removeFloatingNodes(v, g);
				if (errorFound) {
					break;
				}

				errorFound = fixNonGatewayConnectors(v, g);
				if (errorFound) {
					break;
				}
				
				if (preset.size() > 1 && postset.size() > 1) {
					errorFound = true;
					correctConnections(v, g);
					break;
				}
				
				if (preset.size() == 0 && postset.size() == 0) {
					errorFound = true;
					g.removeFlowNode(v);
					break;
				}
			}
		} while (errorFound);
	}
	
	public static String findErrors(CPF g) {
		String errors = "";
		for (FlowNode v : g.getFlowNodes()) {
			Collection<FlowNode> preset = g.getDirectPredecessors(v);
			Collection<FlowNode> postset = g.getDirectSuccessors(v);
			
			if (g.getVertices().size() > 1) {
				if (preset.size() == 0 && postset.size() == 0) {
					errors += v.getLabel() + " is floating.\n";
				}
			}
			
			if (!(v instanceof CpfGateway)) {
				if (preset.size() > 1 || postset.size() > 1) {
					errors += v.getLabel() + " acts as a gateway.\n";
				}
			}
			
			if (preset.size() > 1 && postset.size() > 1) {
				errors += v.getLabel() + " has too many inputs and too many outputs.\n";
			}
		}
		return errors;
	}

	private static boolean removeFloatingNodes(FlowNode v, CPF g) {
		boolean errorFound = false;
		if (g.getVertices().size() > 1) {
			if (g.getDirectPredecessors(v).size() == 0 && g.getDirectSuccessors(v).size() == 0) {
				g.removeVertex(v);
				errorFound = true;
			}
		}
		return errorFound;
	}

	private static boolean fixNonGatewayConnectors(FlowNode v, CPF g) {
		
		if (v instanceof CpfGateway) {
			// we are looking for non-gateways acting as gateways
			return false;
		}
		
		Collection<FlowNode> preset = g.getDirectPredecessors(v);
		Collection<FlowNode> postset = g.getDirectSuccessors(v);

		if (preset.size() <= 1 && postset.size() <= 1) {
			// this node does not act as a gateway
			return false;
		}
		
		if (preset.size() > 1) {
			// this is a non-gateway with more than one predecessors. we have add a join gateway to fix it.
			
			CpfOrGateway newNode = new CpfOrGateway("OR");
			g.addFlowNode(newNode);
			g.setVertexProperty(newNode.getId(), Constants.TYPE, Constants.CONNECTOR);
			
			Collection<ControlFlow<FlowNode>> oldIncomingEdges = g.getEdgesWithTarget(v);
			g.removeControlFlows(oldIncomingEdges);

			g.addControlFlow(newNode, v);
			for (FlowNode pn : preset) {
				g.addControlFlow(pn, newNode);
			}
		}
		
		if (postset.size() > 1) {
			// this is a non-gateway with more than one successors. we have add a split gateway to fix it.
			
			CpfOrGateway newNode = new CpfOrGateway("OR");
			g.addFlowNode(newNode);
			g.setVertexProperty(newNode.getId(), Constants.TYPE, Constants.CONNECTOR);
			
			Collection<ControlFlow<FlowNode>> oldOutgoingEdges = g.getEdgesWithSource(v);
			g.removeControlFlows(oldOutgoingEdges);

			g.addControlFlow(v, newNode);
			for (FlowNode pn : postset) {
				g.addControlFlow(newNode, pn);
			}
		}
		
		return true;
	}

	private static void correctConnections(FlowNode v, CPF g) {

		Collection<FlowNode> postset = g.getDirectSuccessors(v);
		Collection<ControlFlow<FlowNode>> outEdges = g.getOutgoingEdges(v);
		g.removeControlFlows(outEdges);

		FlowNode s = duplicateFlowNode(v, g);
		g.addControlFlow(v, s);

		for (FlowNode psNode : postset) {
			g.addControlFlow(s, psNode);
		}
	}

	public static FlowNode duplicateFlowNode(FlowNode v, CPF og) {
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
