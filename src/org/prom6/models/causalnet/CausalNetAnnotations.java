package org.prom6.models.causalnet;

import java.util.HashMap;

import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;

public class CausalNetAnnotations {

	public final static String counterTask = "cout";
	public final static String counterStartTask = "cous";
	public final static String counterEndTask = "coue";
	public final static String directDependency = "ddep";
	public final static String id = "id";
	public final static String longDistanceDependency = "lddep";
	public final static String longDistanceRelations = "ldrel";
	public final static String parameters = "par";
	public final static String relations = "rel";
	public final static String splitJoinPatterns = "sjpat";
	
	private HashMap<String,Object> infoExecution;
	private HashMap<DirectedGraphNode,HashMap<String,Object>> infoNodes;
	private HashMap<DirectedGraphEdge<?,?>,HashMap<String,Object>> infoEdges;
	
	public CausalNetAnnotations(){
	
		this.infoExecution = new HashMap<String,Object>();
		this.infoNodes = new HashMap<DirectedGraphNode,HashMap<String,Object>>();
		this.infoEdges = new HashMap<DirectedGraphEdge<?,?>,HashMap<String,Object>>();
	}
	
	public void addExecutionInfo(String attribute, Object info){ this.infoExecution.put(attribute, info); }
	
	public Object getExecutionInfo(String attribute){
		
		return this.infoExecution.get(attribute);
	}
	
	public void addNodeInfo(DirectedGraphNode node, String attribute, Object info){
		
		HashMap<String, Object> stack = null;
		if(this.infoNodes.containsKey(node)) stack = this.infoNodes.get(node);
		else{
			
			stack = new HashMap<String, Object>();
			this.infoNodes.put(node, stack);
		}
		
		stack.put(attribute, info);
	}
	
	public Object getNodeInfo(DirectedGraphNode node, String attribute){
		
		if(this.infoNodes.containsKey(node)){
			
			return this.infoNodes.get(node).get(attribute);
		}
		else return null;
	}
	
	public void addEdgeInfo(DirectedGraphEdge<?,?> edge, String attribute, Object info){
		
		HashMap<String, Object> stack = null;
		if(this.infoEdges.containsKey(edge)) stack = this.infoEdges.get(edge);
		else{
			
			stack = new HashMap<String, Object>();
			this.infoEdges.put(edge, stack);
		}
		
		stack.put(attribute, info);
	}
	
	public Object getEdgeInfo(DirectedGraphEdge<?,?> edge, String attribute){
		
		if(this.infoEdges.containsKey(edge)){
			
			return this.infoEdges.get(edge).get(attribute);
		}
		else return null;
	}
	
	public String toString(){
		
		return this.infoExecution.toString() + "\n" + this.infoNodes.toString() + "\n" + this.infoEdges.toString();
	}
}
