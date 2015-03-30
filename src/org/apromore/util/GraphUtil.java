package org.apromore.util;

import org.apromore.common.Constants;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfNode;
import org.apromore.graph.JBPT.CpfOrGateway;
import org.apromore.graph.JBPT.CpfXorGateway;
import org.apromore.service.utils.FileUtil;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.jbpt.graph.algo.rpst.RPST;
import org.jbpt.pm.Activity;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Chathura Ekanayake
 */
public class GraphUtil {

    private static final Logger log = LoggerFactory.getLogger(GraphUtil.class);


    /**
     * Copies the graph g to ng by duplicating all vertices and edges.
     *
     * @param g  Source graph
     * @param ng Target graph
     * @return mapping old node Id -> new node Id
     */
    public static Map<String, String> copyContentGraph(CPF g, CPF ng) {
        Collection<FlowNode> vertices = g.getVertices();
        Collection<ControlFlow<FlowNode>> edges = g.getEdges();
        Map<String, String> vMap = new HashMap<String, String>(0);
        Map<String, String> pocketMap = new HashMap<String, String>(0);

        // copy vertices to the new graph
        for (FlowNode v : vertices) {
            String type = g.getVertexProperty(v.getId(), Constants.TYPE);
            FlowNode newV = new Activity(v.getName());

            ng.addVertex(newV);
            ng.setVertexProperty(newV.getId(), Constants.TYPE, type);
            vMap.put(v.getId(), newV.getId());

            if (Constants.POCKET.equals(type)) {
                pocketMap.put(v.getId(), newV.getId());
            }
        }

        // add edges connecting new vertices
        for (ControlFlow<FlowNode> e : edges) {
            FlowNode newSource = ng.getVertex(vMap.get(e.getSource().getId()));
            FlowNode newTarget = ng.getVertex(vMap.get(e.getTarget().getId()));
            ng.addEdge(newSource, newTarget);
        }

        return vMap;
    }

    public static void fillGraph(CPF g, CPF sg) {
        fillVertices(g, sg);
        fillEdges(g, sg);
    }

    public static void fillVertices(CPF g, CPF sg) {
        Collection<FlowNode> vs = sg.getVertices();
        for (FlowNode v : vs) {
            String type = sg.getVertexProperty(v.getId(), Constants.TYPE);
            g.addVertex(v);
            g.setVertexProperty(v.getId(), Constants.TYPE, type);
        }
    }

    public static void fillEdges(CPF g, CPF sg) {
        Collection<ControlFlow<FlowNode>> edges = sg.getEdges();
        for (ControlFlow<FlowNode> edge : edges) {
            g.addEdge(edge.getSource(), edge.getTarget());
        }
    }
    
    private void correctGraph(CPF g) {
    	Collection<FlowNode> ns = g.getFlowNodes();
    	Set<FlowNode> sources = new HashSet<FlowNode>();
    	for (FlowNode n : ns) {
    		if (g.getDirectPredecessors(n).isEmpty()) {
    			sources.add(n);
    		}
    	}
    }

    public static RPST<ControlFlow<FlowNode>, FlowNode> normalizeGraph(CPF graph) {
        log.debug("Normalizing graph with size " + graph.getVertices().size());

        List<FlowNode> srcs = graph.getSourceVertices();
        List<FlowNode> tgts = graph.getSinkVertices();

        // remove isolated vertices
        List<FlowNode> isolatedVertices = new ArrayList<FlowNode>(0);
        for (FlowNode isrc : srcs) {
            if (tgts.contains(isrc)) {
                isolatedVertices.add(isrc);
            }
        }
        srcs.removeAll(isolatedVertices);
        tgts.removeAll(isolatedVertices);
        graph.removeVertices(isolatedVertices);

        FlowNode entry = null;
        FlowNode exit;

        for (FlowNode src : srcs) {
            String srcLabel = src.getName();
            if ("_entry_".equals(srcLabel)) {
                entry = src;
            }
        }

        for (FlowNode tgt : tgts) {
            String tgtLabel = tgt.getName();
            if ("_exit_".equals(tgtLabel)) {
                exit = tgt;
            }
        }

        if (entry == null) {
            srcs.retainAll(tgts);
            // remove nodes that have no input and output edges
            for (FlowNode v : srcs) {
                graph.removeVertex(v);
            }

            srcs = graph.getSourceVertices();
            tgts = graph.getSinkVertices();

            entry = new CpfNode("_entry_");
            graph.addVertex(entry);

            exit = new CpfNode("_exit_");
            graph.addVertex(exit);

            if (srcs.size() == 1) {
                for (FlowNode tgt : srcs) {
                    graph.addEdge(entry, tgt);
                }
            } else {
//                FlowNode sourceXOR = new CpfXorGateway("XOR");
                FlowNode sourceAggregator = new CpfOrGateway("OR");
                graph.addFlowNode(sourceAggregator);
                graph.setVertexProperty(sourceAggregator.getId(), Constants.TYPE, Constants.CONNECTOR);
                graph.addEdge(entry, sourceAggregator);
                for (FlowNode tgt : srcs) {
                    graph.addEdge(sourceAggregator, tgt);
                }
            }

            if (tgts.size() == 1) {
                for (FlowNode src : tgts) {
                    graph.addEdge(src, exit);
                }
            } else {
//                FlowNode sinkXOR = new CpfXorGateway("XOR");
                FlowNode sinkAggregator = new CpfOrGateway("OR");
                graph.addFlowNode(sinkAggregator);
                graph.setVertexProperty(sinkAggregator.getId(), Constants.TYPE, Constants.CONNECTOR);
                graph.addEdge(sinkAggregator, exit);
                for (FlowNode src : tgts) {
                    graph.addEdge(src, sinkAggregator);
                }
            }
        }
        
//        String epml = new FormattableEPCSerializer().serializeToString(graph);
//        String outputFilePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/fragments/f10.epml";
//        FileUtil.createFileWithOverwrite(outputFilePath, epml);
        
     // TODO: experimental code
     GraphUtil.removeArticialTerminalNodes(graph);
     // end of experimental code

        return new RPST<ControlFlow<FlowNode>, FlowNode>(graph);
    }

	public static void removeArticialTerminalNodes(CPF graph) {
		
		Collection<FlowNode> nodesToRemove = new HashSet<FlowNode>();
		Collection<ControlFlow<FlowNode>> edgesToRemove = new HashSet<ControlFlow<FlowNode>>();
		
		List<FlowNode> srcs = graph.getSourceVertices();
		if (srcs.size() == 1) {
			FlowNode src = srcs.get(0);
			if ("_entry_".equals(src.getName())) {
				nodesToRemove.add(src);
				edgesToRemove.addAll(graph.getEdgesWithSource(src));
				Collection<FlowNode> secondNodes = graph.getDirectSuccessors(src);
				if (secondNodes.size() == 1) {
					for (FlowNode sn : secondNodes) {
						if ("fictive start".equals(sn.getName())) {
							// we remove fictive start only if its successor is not a join. otherwise, we the graph
							// will not have a proper entry point.
							Collection<FlowNode> thirdNodes = graph.getDirectSuccessors(sn);
							if (thirdNodes.size() == 1) {
								for (FlowNode tn : thirdNodes) {
									if (graph.getIncomingEdges(tn).size() == 1) {
										// only incoming edge is from fictive start. we can remove it.
										nodesToRemove.add(sn);
										edgesToRemove.addAll(graph.getEdgesWithSource(sn));
									}
								}
							}
						}
					}
				}
			}
		}
		
		List<FlowNode> sinks = graph.getSinkVertices();
		if (sinks.size() == 1) {
			FlowNode sink = sinks.get(0);
			if ("_exit_".equals(sink.getName())) {
				nodesToRemove.add(sink);
				edgesToRemove.addAll(graph.getEdgesWithTarget(sink));
				Collection<FlowNode> tailerNodes = graph.getDirectPredecessors(sink);
				if (tailerNodes.size() == 1) {
					for (FlowNode tn : tailerNodes) {
						if ("fictive end".equals(tn.getName())) {
							// we remove fictive end only if its predecessor is not a split. otherwise, we the graph
							// will not have a proper exit point.
							Collection<FlowNode> preTailerNodes = graph.getDirectPredecessors(tn);
							if (preTailerNodes.size() == 1) {
								for (FlowNode pt : preTailerNodes) {
									if (graph.getOutgoingEdges(pt).size() == 1) {
										// only outgoing edge is to fictive end. we can remove it.
										nodesToRemove.add(tn);
										edgesToRemove.addAll(graph.getEdgesWithTarget(tn));
									}
								}
							}
						}
					}
				}
			}
		}
		
		graph.removeControlFlows(edgesToRemove);
		graph.removeVertices(nodesToRemove);
	}
}
