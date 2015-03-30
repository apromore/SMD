package org.apromore.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tue.tm.is.graph.SimpleGraph;

import org.apromore.common.Constants;
import org.apromore.dao.ContentDao;
import org.apromore.dao.EdgeDao;
import org.apromore.dao.FragmentVersionDagDao;
import org.apromore.dao.NodeDao;
import org.apromore.dao.dao.model.ContentDO;
import org.apromore.dao.model.Content;
import org.apromore.dao.model.FragmentVersionDagId;
import org.apromore.dao.model.GEdge;
import org.apromore.dao.model.GNode;
import org.apromore.exception.ExceptionDao;
import org.apromore.exception.PocketMappingException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfAndGateway;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.graph.JBPT.CpfMessage;
import org.apromore.graph.JBPT.CpfNode;
import org.apromore.graph.JBPT.CpfOrGateway;
import org.apromore.graph.JBPT.CpfTask;
import org.apromore.graph.JBPT.CpfTimer;
import org.apromore.graph.JBPT.CpfXorGateway;
import org.apromore.service.GraphService;
import org.apromore.service.IComposer;
import org.apromore.service.helper.OperationContext;
import org.apromore.util.FragmentUtil;
import org.apromore.util.GraphUtil;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("SGAwareComposer")
@Transactional(propagation = Propagation.REQUIRED)
public class SimpleGraphAwareComposer implements IComposer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleGraphAwareComposer.class);

    @Autowired @Qualifier("ContentDao")
    private ContentDao cDao;
    @Autowired @Qualifier("FragmentVersionDagDao")
    private FragmentVersionDagDao fvdDao;
    
    @Autowired @Qualifier("EdgeDao")
    private EdgeDao edgeDao;

    @Autowired @Qualifier("NodeDao")
    private NodeDao nDao;

    @Autowired @Qualifier("GraphService")
    private GraphService gSrv;
    
    private Map<String, SimpleGraph> cache = new HashMap<String, SimpleGraph>();
    private int minCachableFragmentSize = 2;
    private int maxCachableFragmentSize = 100;
    
    private Map<String, List<GNode>> nodeCache = new HashMap<String, List<GNode>>();
    private Map<String, List<GEdge>> edgeCache = new HashMap<String, List<GEdge>>();
	private Map<String, ContentDO> contentCache = new HashMap<String, ContentDO>();
	private Map<String, List<FragmentVersionDagId>> childMappingsCache = new HashMap<String, List<FragmentVersionDagId>>();
    
    /**
     * Compose a process Model graph from the DB.
     * @param fragmentVersionId the fragment version Id we are looking to construct from.
     * @return the process model graph
     * @throws ExceptionDao if something fails.
     */
	public CPF compose(String fragmentVersionId) throws ExceptionDao {
		OperationContext op = new OperationContext();
        CPF g = new CPF();
		op.setGraph(g);
		composeFragment(op, fragmentVersionId, null);
		return g;
	}

	public void setCache(Map<String, SimpleGraph> cache) {
		this.cache = cache;
	}
	
	public void setMinCachableFragmentSize(int minCachableFragmentSize) {
		this.minCachableFragmentSize = minCachableFragmentSize;
	}

	public void setMaxCachableFragmentSize(int maxCachableFragmentSize) {
		this.maxCachableFragmentSize = maxCachableFragmentSize;
	}
	
	private ContentDO getContent(String fragmentId) {
		ContentDO contentDO = contentCache.get(fragmentId);
		if (contentDO == null) {
			contentDO = cDao.getContentDOByFragmentVersion(fragmentId);
			contentCache.put(fragmentId, contentDO);
		}
		return contentDO;
	}

	private void composeFragment(OperationContext op, String fragmentVersionId, String pocketId) throws ExceptionDao {
		ContentDO content = getContent(fragmentVersionId);
		
		if (op.getContentUsage(content.getContentId()) == 0) {
			composeNewContent(op, fragmentVersionId, pocketId, content);
		} else {
			composeDuplicateContent(op, fragmentVersionId, pocketId, content);
		}
	}
	
	private void composeNewContent(OperationContext op, String fragmentVersionId, String pocketId, ContentDO contentDO)
            throws ExceptionDao {
		op.incrementContentUsage(contentDO.getContentId());

        CPF g = op.getGraph();
        fillNodes(g, contentDO.getContentId());
        fillEdges(g, contentDO.getContentId());

        Collection<FlowNode> nodesToBeRemoved = new HashSet<FlowNode>();
        Collection<ControlFlow<FlowNode>> edgesToBeRemoved = new HashSet<ControlFlow<FlowNode>>();
		if (pocketId != null) {
			Collection<ControlFlow<FlowNode>> edges = g.getEdges();
			for (ControlFlow<FlowNode> edge: edges) {
				if (edge.getTarget() != null && edge.getTarget().getId().equals(pocketId)) {
					FlowNode boundaryS = g.getVertex(contentDO.getBoundary1());
					FlowNode parentT1 = edge.getSource();
					if (canCombineSplit(parentT1, boundaryS)) {
						Collection<FlowNode> childTs = g.getDirectSuccessors(boundaryS);
						for (FlowNode ct : childTs) {
							g.addEdge(parentT1, ct);
						}
						nodesToBeRemoved.add(boundaryS);
						
					} else {
						edge.setTarget(g.getVertex(contentDO.getBoundary1()));
					}
				}
				
				if (edge.getSource() != null && edge.getSource().getId().equals(pocketId)) {
					FlowNode boundaryE = g.getVertex(contentDO.getBoundary2());
					FlowNode parentT2 = edge.getTarget();
					if (canCombineJoin(parentT2, boundaryE)) {
						Collection<FlowNode> childTs = g.getDirectPredecessors(boundaryE);
						for (FlowNode ct : childTs) {
							g.addEdge(ct, parentT2);
						}
						nodesToBeRemoved.add(boundaryE);
						
					} else {
						edge.setSource(g.getVertex(contentDO.getBoundary2()));
					}
				}
			}
			g.removeVertex(g.getVertex(pocketId));
			g.removeVertices(nodesToBeRemoved);
		}
		
		List<FragmentVersionDagId> childMappings = getChildMappings(fragmentVersionId);
        for (FragmentVersionDagId fvd : childMappings) {
            composeFragment(op, fvd.getChildFragmentVersionId(), fvd.getPocketId());
        }
	}
	
	private boolean canCombineSplit(FlowNode parentT1, FlowNode boundaryS) {
		
		if (parentT1 == null || boundaryS == null) {
			return false;
		}
		
		if ((parentT1 instanceof CpfXorGateway) && (boundaryS instanceof CpfXorGateway)) {
			return true;
		}
		
		if ((parentT1 instanceof CpfAndGateway) && (boundaryS instanceof CpfAndGateway)) {
			return true;
		}
		
		if ((parentT1 instanceof CpfOrGateway) && (boundaryS instanceof CpfOrGateway)) {
			return true;
		}
		
		if (("XOR".equals(parentT1.getName())) && ("XOR".equals(boundaryS.getName()))) {
			return true;
		}
		
		if (("AND".equals(parentT1.getName())) && ("AND".equals(boundaryS.getName()))) {
			return true;
		}
		
		if (("OR".equals(parentT1.getName())) && ("OR".equals(boundaryS.getName()))) {
			return true;
		}
		
		return false;
	}
	
	private boolean canCombineJoin(FlowNode parentT2, FlowNode boundaryE) {
		
		if (parentT2 == null || boundaryE == null) {
			return false;
		}
		
		if ((parentT2 instanceof CpfXorGateway) && (boundaryE instanceof CpfXorGateway)) {
			return true;
		}
		
		if ((parentT2 instanceof CpfAndGateway) && (boundaryE instanceof CpfAndGateway)) {
			return true;
		}
		
		if ((parentT2 instanceof CpfOrGateway) && (boundaryE instanceof CpfOrGateway)) {
			return true;
		}
		
		if (("XOR".equals(parentT2.getName())) && ("XOR".equals(boundaryE.getName()))) {
			return true;
		}
		
		if (("AND".equals(parentT2.getName())) && ("AND".equals(boundaryE.getName()))) {
			return true;
		}
		
		if (("OR".equals(parentT2.getName())) && ("OR".equals(boundaryE.getName()))) {
			return true;
		}
		
		return false;
	}

	private void composeDuplicateContent(OperationContext op, String fragmentVersionId, String pocketId,
            ContentDO contentDO) throws ExceptionDao {
		op.incrementContentUsage(contentDO.getContentId());

		CPF g = op.getGraph();
        CPF contentGraph = getGraph(contentDO.getContentId());
        CPF duplicateGraph = new CPF();
		Map<String, String> vMap = GraphUtil.copyContentGraph(contentGraph, duplicateGraph);
		GraphUtil.fillGraph(g, duplicateGraph);
		fillOriginalNodeMappings(vMap, g);
		
		Collection<FlowNode> nodesToBeRemoved = new HashSet<FlowNode>();
		if (pocketId != null) {
			Collection<ControlFlow<FlowNode>> edges = g.getEdges();
			for (ControlFlow<FlowNode> edge: edges) {
				if (edge.getTarget() != null && edge.getTarget().getId().equals(pocketId)) {
					FlowNode boundaryS = g.getVertex(vMap.get(contentDO.getBoundary1()));
					FlowNode parentT1 = edge.getSource();
					if (canCombineSplit(parentT1, boundaryS)) {
						Collection<FlowNode> childTs = g.getDirectSuccessors(boundaryS);
						for (FlowNode ct : childTs) {
							g.addEdge(parentT1, ct);
						}
						nodesToBeRemoved.add(boundaryS);
						
					} else {
						edge.setTarget(g.getVertex(vMap.get(contentDO.getBoundary1())));
					}
				}
				if (edge.getSource().getId().equals(pocketId)) {
					FlowNode boundaryE = g.getVertex(vMap.get(contentDO.getBoundary2()));
					FlowNode parentT2 = edge.getTarget();
					if (canCombineJoin(parentT2, boundaryE)) {
						Collection<FlowNode> childTs = g.getDirectPredecessors(boundaryE);
						for (FlowNode ct : childTs) {
							g.addEdge(ct, parentT2);
						}
						nodesToBeRemoved.add(boundaryE);
						
					} else {
						edge.setSource(g.getVertex(vMap.get(contentDO.getBoundary2())));
					}
				}
			}
			g.removeVertex(g.getVertex(pocketId));
			g.removeVertices(nodesToBeRemoved);
		}

        List<FragmentVersionDagId> childMappings = getChildMappings(fragmentVersionId);
		Map<String, String> newChildMapping = null;
		try {
			newChildMapping = FragmentUtil.remapChildren(childMappings, vMap);
		} catch (PocketMappingException e) {
			String msg = "Failed a pocked mapping of fragment " + fragmentVersionId;
			LOGGER.error(msg, e);
		}
		Set<String> pids = newChildMapping.keySet();
		for (String pid: pids) {
			String childId = newChildMapping.get(pid);
			composeFragment(op, childId, pid);
		}
	}
	
	private void fillOriginalNodeMappings(Map<String, String> vMap, CPF g) {
		for (String originalNode : vMap.keySet()) {
			String duplicateNode = vMap.get(originalNode);
			if (!g.getVertexProperty(duplicateNode, Constants.TYPE).equals(Constants.POCKET)) {
				g.addOriginalNodeMapping(duplicateNode, originalNode);
			}
		}
	}





    /**
     * Set the Content DAO object for this class. Mainly for spring tests.
     * @param cntDAOJpa the content Dao.
     */
    public void setContentDao(ContentDao cntDAOJpa) {
        cDao = cntDAOJpa;
    }

    /**
     * Set the Fragment Version Dag DAO object for this class. Mainly for spring tests.
     * @param fvdDAOJpa the Fragment Version Dag Dao.
     */
    public void setFragmentVersionDagDao(FragmentVersionDagDao fvdDAOJpa) {
        fvdDao = fvdDAOJpa;
    }

    /**
     * Set the Graph Service object for this class. Mainly for spring tests.
     * @param gService the Graph Service.
     */
    public void setGraphService(GraphService gService) {
        gSrv = gService;
    }
    
	public void clearCache(Collection<String> fids) {
		for (String fid : fids) {
			clearCache(fid);
		}
	}
	
	public void clearCache(String fragmentId) {
		ContentDO contentDO = contentCache.get(fragmentId);
		if (contentDO != null) {
			nodeCache.remove(contentDO.getContentId());
			edgeCache.remove(contentDO.getContentId());
			contentCache.remove(fragmentId);
			childMappingsCache.remove(fragmentId);
		}
	}
	
    @Transactional(readOnly = true)
    public void fillNodes(CPF procModelGraph, String contentID) {
        FlowNode v;
        List<GNode> nodes = nodeCache.get(contentID);
        if (nodes == null) {
        	nodes =	nDao.getGNodesByContent(contentID);
        	nodeCache.put(contentID, nodes);
        }
        
        for (GNode node : nodes) {
            v = buildNodeByType(node);
            procModelGraph.addVertex(v);
            procModelGraph.setVertexProperty(String.valueOf(node.getVid()), Constants.TYPE, node.getVtype());
        }
    }
    
    @Transactional(readOnly = true)
    public void fillEdges(CPF procModelGraph, String contentID) {
        
    	List<GEdge> edges = edgeCache.get(contentID);
    	if (edges == null) {
    		edges = edgeDao.getGEdgesByContent(contentID);
    		edgeCache.put(contentID, edges);
    	}
        
        for (GEdge edge : edges) {
            FlowNode v1 = procModelGraph.getVertex(String.valueOf(edge.getSourceVId()));
            FlowNode v2 = procModelGraph.getVertex(String.valueOf(edge.getTargetVId()));
            
            if (v1 != null && v2 != null) {
            	procModelGraph.addEdge(v1, v2);
            } else {
            	if (v1 == null && v2 != null) {
            		LOGGER.info("Null source node found for the edge terminating at " + v2.getId() + " = " + v2.getName() + " in content " + contentID);
            	}
            	
            	if (v2 == null && v1 != null) {
            		LOGGER.info("Null target node found for the edge originating at " + v1.getId() + " = " + v1.getName() + " in content " + contentID);
            	}
            	
            	if (v1 == null && v2 == null) {
            		LOGGER.info("Null source and target nodes found for an edge in content " + contentID);
            	}
            }
        }
    }
    
    private List<FragmentVersionDagId> getChildMappings(String fragmentId) {
    	List<FragmentVersionDagId> childMappings = childMappingsCache.get(fragmentId);
    	if (childMappings == null) {
			childMappings = fvdDao.getChildMappings(fragmentId);
			childMappingsCache.put(fragmentId, childMappings);
    	}
    	return childMappings;
    }
    
    @Transactional(readOnly = true)
    public CPF getGraph(String contentID){
        CPF g = new CPF();
        fillNodes(g, contentID);
        fillEdges(g, contentID);
        return g;
    }
	
	/* Build the correct type of Node so we don't loss Information */
    private FlowNode buildNodeByType(GNode node) {
        FlowNode result = null;
        if (node.getCtype().equals(CpfNode.class.getName())) {
            result = new CpfNode(node.getVname());
            result.setId(String.valueOf(node.getVid()));
        } else if (node.getCtype().equals(CpfMessage.class.getName())) {
            result = new CpfMessage(node.getVname());
            result.setId(String.valueOf(node.getVid()));

        } else if (node.getCtype().equals(CpfTimer.class.getName())) {
            result = new CpfTimer(node.getVname());
            result.setId(String.valueOf(node.getVid()));

        } else if (node.getCtype().equals(CpfTask.class.getName())) {
            result = new CpfTask(node.getVname());
            result.setId(String.valueOf(node.getVid()));

        } else if (node.getCtype().equals(CpfEvent.class.getName())) {
            result = new CpfEvent(node.getVname());
            result.setId(String.valueOf(node.getVid()));

        } else if (node.getCtype().equals(CpfOrGateway.class.getName())) {
            result = new CpfOrGateway(node.getVname());
            result.setId(String.valueOf(node.getVid()));

        } else if (node.getCtype().equals(CpfXorGateway.class.getName())) {
            result = new CpfXorGateway(node.getVname());
            result.setId(String.valueOf(node.getVid()));

        }  else if (node.getCtype().equals(CpfAndGateway.class.getName())) {
            result = new CpfAndGateway(node.getVname());
            result.setId(String.valueOf(node.getVid()));

        }
        return result;
    }
}
