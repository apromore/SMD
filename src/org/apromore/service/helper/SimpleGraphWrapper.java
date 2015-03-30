/**
 * 
 */
package org.apromore.service.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.tue.tm.is.graph.SimpleGraph;
import nl.tue.tm.is.graph.TwoVertices;

import org.apromore.common.FSConstants;
import org.apromore.graph.JBPT.CPF;
import org.jbpt.pm.FlowNode;

/**
 * @author Chathura C. Ekanayake
 *
 */
public class SimpleGraphWrapper extends SimpleGraph {
	
	public SimpleGraphWrapper(CPF pg) {
		super();
		
		Map<String,Integer> nodeId2vertex = new HashMap<String,Integer>();
		Map<Integer,String> vertex2nodeId = new HashMap<Integer,String>();

		vertices = new HashSet<Integer>();
		edges = new HashSet<TwoVertices>();
		connectors = new HashSet<Integer>();
		events = new HashSet<Integer>();;
		functions = new HashSet<Integer>();
		
		outgoingEdges = new HashMap<Integer,Set<Integer>>();
		incomingEdges = new HashMap<Integer,Set<Integer>>();		
		labels = new HashMap<Integer,String>();	
		functionLabels = new HashSet<String> ();
		eventLabels = new HashSet<String> ();

		int vertexId = 0;
		for (FlowNode n: pg.getFlowNodes()) {
			vertices.add(vertexId);
			labels.put(vertexId, n.getName().replace('\n', ' ').replace("\\n", " "));

			nodeId2vertex.put(n.getId(), vertexId);
			vertex2nodeId.put(vertexId, n.getId());
			
			if (FSConstants.FUNCTION.equals(pg.getVertexProperty(n.getId(), FSConstants.TYPE)) &&
				n.getName() != null) {
				functionLabels.add(n.getName().replace('\n', ' '));
				functions.add(vertexId);
				
			} else if (FSConstants.EVENT.equals(pg.getVertexProperty(n.getId(), FSConstants.TYPE)) &&
					n.getName() != null) {
				eventLabels.add(n.getName().replace('\n', ' '));
				events.add(vertexId);
				
			} else if (FSConstants.CONNECTOR.equals(pg.getVertexProperty(n.getId(), FSConstants.TYPE))) {
				connectors.add(vertexId);
			}
			
			vertexId++;
		}
		
		for (Integer v = 0; v < vertexId; v++){
			
			FlowNode pgv = pg.getVertex(vertex2nodeId.get(v));
			
			Set<Integer> incomingCurrent = new HashSet<Integer>();
			Collection<FlowNode> preset = pg.getDirectPredecessors(pgv);
			for (FlowNode preV : preset) {
				incomingCurrent.add(nodeId2vertex.get(preV.getId()));
			}
			incomingEdges.put(v, incomingCurrent);
			
			Set<Integer> outgoingCurrent = new HashSet<Integer>();
			Collection<FlowNode> postset = pg.getDirectSuccessors(pgv);
			for (FlowNode postV : postset) {
				outgoingCurrent.add(nodeId2vertex.get(postV.getId()));
				TwoVertices edge = new TwoVertices(v, nodeId2vertex.get(postV.getId()));
				edges.add(edge);
			}
			outgoingEdges.put(v, outgoingCurrent);
		}
	}
}
