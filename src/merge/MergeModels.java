package merge;

import graph.Edge;
import graph.Graph;
import graph.Vertex;
import java.util.HashSet;
import java.util.LinkedList;
import planarGraphMathing.PlanarGraphMathing;
import planarGraphMathing.PlanarGraphMathing.MappingRegions;
import common.Settings;
import common.VertexPair;

public class MergeModels implements MergeI {
	
	private static boolean printGraphInfo = false;
	
	public Graph mergeModels (Graph g1, Graph g2) {
		Graph merged = new Graph();
		
		if (printGraphInfo) {
			int[] g1Inf = g1.getNrOfVertices();
			int[] g2Inf = g2.getNrOfVertices();
			System.out.print(g1Inf[0]+"\t"+g1Inf[1]+"\t"+g1Inf[2]+"\t"+g1Inf[3]+"\t"+g2Inf[0]+"\t"+g2Inf[1]+"\t"+g2Inf[2]+"\t"+g2Inf[3]+"\t");
		}
		
		MappingRegions mappings = PlanarGraphMathing.findMatchWithGW(g1, g2, Settings.MERGE_THRESHOLD, true);
		long startTime = System.currentTimeMillis();
//		for (LinkedList<VertexPair> mapping : mappings.getRegions()) {
//			System.out.println("-------------------------------------");
//			for (VertexPair vp : mapping) {
//				System.out.println(vp.getLeft().getLabel()+" ("+vp.getLeft().getID()+") "+ " <> "+ vp.getRight().getLabel()+" ("+vp.getRight().getID()+") " + " "+vp.getLeft().getChildren().size() + " "+vp.getRight().getChildren().size());
//			}
//			System.out.print(mapping.size()+"\t");
//		}
//		System.out.print("\n");
		
		for (LinkedList<VertexPair> mapping : mappings.getRegions()) {
			for (VertexPair vp : mapping) {
				if (vp.getLeft().getParents().size() == 0) {
					vp.getLeft().sourceBefore = true;
				}
				if (vp.getLeft().getChildren().size() == 0) {
					vp.getLeft().sinkBefore = true;
				}
				if (vp.getRight().getParents().size() == 0) {
					vp.getRight().sourceBefore = true;
				}
				if (vp.getRight().getChildren().size() == 0) {
					vp.getRight().sinkBefore = true;
				}

			}
		}

		merged.addVertices(g1.getVertices());
		merged.addEdges(g1.getEdges());
		merged.addVertices(g2.getVertices());
		merged.addEdges(g2.getEdges());

		mergeMapping(merged, mappings, g1, g2);
		long mergeTime = System.currentTimeMillis();
		merged.cleanGraph();
		
		// labels for all edges should be added to the model
		for (Edge e : merged.getEdges()) {
			e.addLabelToModel();
		}
		
		long cleanTime = System.currentTimeMillis();
		
		merged.mergetime = mergeTime - startTime;
		merged.cleanTime = cleanTime - startTime;
		
		if (printGraphInfo) {
			int[] mergedInf = merged.getNrOfVertices();
			System.out.print(mergedInf[0]+"\t"+mergedInf[1]+"\t"+mergedInf[2]+"\t"+mergedInf[3]+"\n");
		}

		merged.name = "";
		for (String l : merged.getEdgeLabels()) {
			merged.name += l + ",";
		}
		merged.name = merged.name.substring(0, merged.name.length() - 1);
		
		merged.ID = String.valueOf(Graph.nextId++);
		
		return merged;

	}

	
	private static void mergeMapping(Graph merged, MappingRegions mappings, Graph g1, Graph g2) {
		HashSet<String> labelsg1g2 = new HashSet<String>();
		labelsg1g2.add(g1.getGraphLabel());
		labelsg1g2.add(g2.getGraphLabel());

		// mapping from the first graph to the second
		for (LinkedList<VertexPair> mapping : mappings.getRegions()) {
			
//			if (mapping.size() == 1) {
//				continue;
//			}
			
			LinkedList<VertexPair> sources = findSources(mapping);
			LinkedList<VertexPair> sinks = findSinks(mapping);

			// process sources
			for (VertexPair source : sources) {
//				System.out.println("Source "+ source.getLeft().getLabel());
				Vertex g1Source = source.getLeft();
				Vertex g2Source = source.getRight();
				LinkedList<Vertex> g1SourcePrev = new LinkedList<Vertex>(g1Source.getParents());//removeFromList(g1Source.getParents(), mapping);
				LinkedList<Vertex> g2SourcePrev = new LinkedList<Vertex>(g2Source.getParents());//removeFromList(g2Source.getParents(), mapping);
				
				if (!g1Source.getType().equals(Vertex.Type.gateway)) {
					
//					if ()
					Vertex newSource = new Vertex(Vertex.GWType.xor, ""+Graph.nextId++);
					newSource.setConfigurable(true);	
					merged.addVertex(newSource);
	
					merged.connectVertices(newSource, g1Source, labelsg1g2);
					for (Vertex v : g1SourcePrev) {
						g1Source.removeParent(v.getID());
						v.removeChild(g1Source.getID());
						HashSet<String> labels = merged.removeEdge(v.getID(), g1Source.getID());
						merged.connectVertices(v, newSource, labels);
//						newEdge.addLabelToModel();
			//					System.out.println(" newSource connect  "+ newSource.getID()+" "+ v.getID()+" ");
					}
					
					for (Vertex v : g2SourcePrev) {
		//					merged.removeEdge(""+v.getID(), ""+g2Source.getID());
						v.removeChild(g2Source.getID());
						HashSet<String> labels = merged.getEdgeLabels(v.getID(), g2Source.getID());
						merged.connectVertices(v, newSource, labels);
//						newEdge.addLabelToModel();
		//					System.out.println(" newSource connect  "+ newSource.getID()+" "+ v.getID());
					}
					
					// add fake nodes?
					if (g1Source.sourceBefore || g2Source.sourceBefore) {
	//					System.out.println("g1SourcePrev.size() == 0 || g2SourcePrev.size() == 0");
						Vertex fakeEvent = new Vertex(Vertex.Type.event, "e", ""+Graph.nextId++);
						Vertex fakeFn = new Vertex(Vertex.Type.function, "e", ""+Graph.nextId++);
						merged.addVertex(fakeEvent);
						merged.addVertex(fakeFn);
						merged.connectVertices(fakeEvent, fakeFn);
						Edge newEdge = merged.connectVertices(fakeFn, newSource);
						if (g1Source.sourceBefore) {
							newEdge.addLabel(g1.getGraphLabel());
						}
						else {
							newEdge.addLabel(g2.getGraphLabel());
						}
//						newEdge.addLabelToModel();
					}
				}
				// this is gateway
				else {
					for (Vertex v : g2SourcePrev) {
						v.removeChild(g2Source.getID());
						if(!containsVertex(mapping, v)) {
							HashSet<String> labels = merged.getEdgeLabels(v.getID(), g2Source.getID());
							merged.connectVertices(v, g1Source, labels);
//							newEdge.addLabelToModel();

						}
					}
				}
			}
			
			// process sinks
			for (VertexPair sink : sinks) {
	
//				System.out.println(">>newSink "+ sink.getLeft().getLabel()+ "("+sink.getLeft().getID()+")");
				Vertex g1Sink = sink.getLeft();
				Vertex g2Sink = sink.getRight();
				
				LinkedList<Vertex> g1SourceFoll = new LinkedList<Vertex>(g1Sink.getChildren());//removeFromList(g1Sink.getChildren(), mapping);
				LinkedList<Vertex> g2SourceFoll = new LinkedList<Vertex>(g2Sink.getChildren());//removeFromList(g2Sink.getChildren(), mapping);

				if (!g1Sink.getType().equals(Vertex.Type.gateway)) {
					Vertex newSink = new Vertex(Vertex.GWType.xor, ""+Graph.nextId++);
					newSink.setConfigurable(true);
//					System.out.println("newSink "+ newSink.getID());
					try {
						merged.getVertex(Integer.valueOf(newSink.getID()));
//						System.out.println("ALREADY EXISTS");
					}
					catch (Exception e) {}
					
					merged.addVertex(newSink);
	
					merged.connectVertices(g1Sink, newSink, labelsg1g2);
		
					for (Vertex v : g1SourceFoll) {
						g1Sink.removeChild(v.getID());
						v.removeParent(g1Sink.getID());
						HashSet<String> labels = merged.removeEdge(g1Sink.getID(), v.getID());
						merged.connectVertices(newSink, v, labels);
					}
					
					for (Vertex v : g2SourceFoll) {
						v.removeParent(g2Sink.getID());
						HashSet<String> labels = merged.getEdgeLabels(g2Sink.getID(), v.getID());
						merged.connectVertices(newSink, v, labels);
					}
		
					// add fake nodes?
					if (g1Sink.sinkBefore || g2Sink.sinkBefore) {
						Vertex fakeEvent = new Vertex(Vertex.Type.event, "e", ""+Graph.nextId++);
						Vertex fakeFn = new Vertex(Vertex.Type.function, "e", ""+Graph.nextId++);
						merged.addVertex(fakeEvent);
						merged.addVertex(fakeFn);
						merged.connectVertices(fakeFn, fakeEvent);
						Edge newEdge = merged.connectVertices(newSink, fakeFn);
						
						if (g1Sink.sourceBefore) {
							newEdge.addLabel(g1.getGraphLabel());
						}
						else {
							newEdge.addLabel(g2.getGraphLabel());
						}
					}
				}
				else {
					for (Vertex v : g2SourceFoll) {
						v.removeParent(g2Sink.getID());
						if(!containsVertex(mapping, v)) {
							HashSet<String> labels = merged.getEdgeLabels(g2Sink.getID(), v.getID());
							merged.connectVertices(g1Sink, v, labels);
						}
					}
				}
			}
			
			for (VertexPair vp : mapping) {
				for(Vertex v : vp.getLeft().getParents()) {
					// this edge is in mapping
					// save labels from the both graph
					if (containsVertex(mapping, v)) {
						Edge e = merged.containsEdge(v.getID(), vp.getLeft().getID());
						if (e != null) {
							// this is a part of a mapping
							Vertex v2 = getMappingPair(mapping, v);
							if (v2 != null) {
								Edge e2 = g2.containsEdge(v2.getID(), vp.getRight().getID());
								if (e2 != null) {
									e.addLabels(e2.getLabels());
									// the common part should also have the labels of both graph
								}
							}
							e.addLabel(g1.getGraphLabel());
							e.addLabel(g2.getGraphLabel());
						}
					}
				}
			}
			
//			System.out.println("REMOVE MAPPiNG start");
			// remove mapping
			for (VertexPair vp : mapping) {
				// remove edges
				for (Vertex v : vp.getRight().getParents()) {
//					System.out.println("parents : "+ v.getID());
					merged.removeEdge(v.getID(), vp.getRight().getID());
				}
				for (Vertex v : vp.getRight().getChildren()) {
//					System.out.println("children : "+ v.getID());
					merged.removeEdge(vp.getRight().getID(), v.getID());
				}
//				System.out.println("REMOVE vertex "+ vp.getRight().getLabel());
				
				if (vp.getLeft().getType().equals(Vertex.Type.gateway) && 
						vp.getLeft().getGWType().equals(vp.getRight().getGWType()) 
								&& (vp.getLeft().isAddedGW() || vp.getRight().isAddedGW())) {

					vp.getLeft().setConfigurable(true);
				}
				
				if (vp.getLeft().getType().equals(Vertex.Type.gateway)  
								&& (vp.getLeft().isInitialGW() || vp.getRight().isInitialGW())) {

					vp.getLeft().setInitialGW();
				}
				
				// change gateways
				if (vp.getLeft().getType().equals(Vertex.Type.gateway) && 
						!vp.getLeft().getGWType().equals(vp.getRight().getGWType())) {
					vp.getLeft().setGWType(Vertex.GWType.or);
					vp.getLeft().setConfigurable(true);
					
					}
				merged.removeVertex(vp.getRight().getID());
			}
//			System.out.println("REMOVE MAPPiNG end");
		}
		merged.removeSplitJoins();
	}
	
	public static boolean containsVertex(LinkedList<VertexPair> mapping, Vertex v){
		
		for (VertexPair vp : mapping) {
//			System.out.println(vp.getLeft().getID()+" "+vp.getRight().getID()+ " "+ v.getID());
			if (vp.getLeft().getID() == v.getID() || vp.getRight().getID() == v.getID()) {
				
				return true;
			}
		}
		
		return false;
	}
	
	public static Vertex getMappingPair(LinkedList<VertexPair> mapping, Vertex v){
		
		for (VertexPair vp : mapping) {
//			System.out.println(vp.getLeft().getID()+" "+vp.getRight().getID()+ " "+ v.getID());
			if (vp.getLeft().getID() == v.getID()) {
				return vp.getRight();
			}
			else if (vp.getRight().getID() == v.getID()) {
				return vp.getLeft();
			}
		}
		
		return null;
	}
	
	private static LinkedList<VertexPair> findSources(LinkedList<VertexPair> mapping){
		LinkedList<VertexPair> sources = new LinkedList<VertexPair>();
		for (VertexPair vp : mapping) {
			boolean added = false;
			for (Vertex v : vp.getLeft().getParents()) {
				// the mapping does not contain 
				if (!containsVertex(mapping, v)) {
					sources.add(vp);
					added = true;
					break;
				}
			}
			if (!added) {
				for (Vertex v : vp.getRight().getParents()) {
					// the mapping does not contain 
					if (!containsVertex(mapping, v)) {
						sources.add(vp);
						break;
					}
				}
			}
		}
		return sources;
	}
	
	private static LinkedList<VertexPair> findSinks(LinkedList<VertexPair> mapping){
		LinkedList<VertexPair> sinks = new LinkedList<VertexPair>();
		for (VertexPair vp : mapping) {
			boolean added = false;
			for (Vertex v : vp.getLeft().getChildren()) {
				// the mapping does not contain 
				if (!containsVertex(mapping, v)) {
					sinks.add(vp);
					added = true;
					break;
				}
			}
			if (!added) {
				for (Vertex v : vp.getRight().getChildren()) {
					// the mapping does not contain 
					if (!containsVertex(mapping, v)) {
						sinks.add(vp);
						break;
					}
				}
			}
		}
		return sinks;
	}
}