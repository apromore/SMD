package org.apromore.mining.complexity;

import java.util.Collection;

import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfAndGateway;
import org.apromore.graph.JBPT.CpfOrGateway;
import org.apromore.graph.JBPT.CpfXorGateway;
import org.jbpt.hypergraph.abs.Vertex;
import org.jbpt.pm.FlowNode;

public class ComplexityCalculator {
	
	public static double getCFC(CPF model) {
		
		double cfc = 0;
		
		Collection<FlowNode> ns = model.getFlowNodes();
		for (FlowNode n : ns) {
			// check if n is a split
			int postset = model.getDirectSuccessors(n).size();
			if (postset > 1) {
				
				if (n instanceof CpfXorGateway) {
					cfc += postset;
				} else if (n instanceof CpfOrGateway) {
					double cfcOR = Math.pow(2, postset) - 1;
					cfc += cfcOR;
				} else if (n instanceof CpfAndGateway) {
					cfc += 1;
				}
			}
		}
		
		return cfc;
	}
	
	public static int getMCC(CPF model) {
		int numVertices = model.getVertices().size();
		int numEdges = model.getEdges().size();
		
		int mcc = numEdges - numVertices + 2;
		
		return mcc;
	}
	
	public static double getCNC(CPF model) {
		int numVertices = model.getVertices().size();
		int numEdges = model.getEdges().size();
		
		double cnc = (double) numEdges / (double) numVertices;
		return cnc;
	}
	
	public static int getNOAJS(CPF model) {
		int noajs = model.getVertices().size();
		return noajs;
	}
	
	public static double getDensity(CPF model) {
		double n = model.getVertices().size();
		double a = model.getEdges().size();
		double density = 0;
		if (a > 0) {
			density = a / (n * (n - 1));
		}
		return density;
	}
	
	public static double getAverageConnectorDegree(CPF model) {
		
		double totalConnectorDegree = 0;
		int numConnectors = 0;
		
		Collection<FlowNode> ns = model.getFlowNodes();
		for (FlowNode n : ns) {
			
			int preset = model.getDirectPredecessors(n).size();
			int postset = model.getDirectSuccessors(n).size();
			
			if (preset > 1) {
				totalConnectorDegree += preset;
				numConnectors++;
			}
			
			if (postset > 1) {
				totalConnectorDegree += postset;
				numConnectors++;
			}
		}
		
		double averageConnectorDegree = 0;
		if (numConnectors > 0) {
			averageConnectorDegree = totalConnectorDegree / numConnectors;
		}
		return averageConnectorDegree;
	}

}
