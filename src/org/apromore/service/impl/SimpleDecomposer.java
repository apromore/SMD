package org.apromore.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apromore.common.FSConstants;
import org.apromore.dao.model.FragmentVersion;
import org.apromore.exception.RepositoryException;
import org.apromore.graph.TreeVisitor;
import org.apromore.graph.JBPT.CPF;
import org.apromore.service.FragmentService;
import org.apromore.service.IDecomposer;
import org.apromore.service.helper.OperationContext;
import org.apromore.util.FragmentUtil;
import org.apromore.util.GraphUtil;
import org.apromore.util.HashUtil;
import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.jbpt.graph.algo.rpst.RPST;
import org.jbpt.graph.algo.rpst.RPSTNode;
import org.jbpt.graph.algo.tctree.TCType;
import org.jbpt.hypergraph.abs.IVertex;
import org.jbpt.hypergraph.abs.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("SimpleDecomposer")
@Transactional(propagation = Propagation.REQUIRED)
public class SimpleDecomposer implements IDecomposer {
	
	private static final Logger log = LoggerFactory.getLogger(SimpleDecomposer.class);
	
	@Autowired @Qualifier("FragmentService")
    private FragmentService fsrv;
	
	private Map<String, String> fragmentIdMap = new HashMap<String, String>();

//	public static void main(String[] args) {
//
//		String modelPath = "/home/cn/projects/processBase/data/t16/suncorp_p3_sim0_8_ged0_4/clusters/Cluster_1/B_1.epml";
////		String modelPath = "/home/cn/projects/processBase/data/t16/input_suncorp/02.00 MC. Assessment - lvl 3.epml";
//		EPCDeserializer deserializer = new EPCDeserializer();
//		ProcessModelGraph g = deserializer.deserialize(modelPath);
//
//		try {
//			String fragmentCode = new SimpleDecomposer().decompose(g, null, null);
//			System.out.println("Fragment code: " + fragmentCode);
//		} catch (RepositoryException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	private TreeVisitor visitor = new TreeVisitor();

	public FragmentVersion decompose(CPF graph, List<String> fragmentIds)
			throws RepositoryException {

		TreeVisitor visitor = new TreeVisitor();
		OperationContext op = new OperationContext();
		op.setGraph(graph);
		op.setTreeVisitor(visitor);

		try {
			RPST rpst = GraphUtil.normalizeGraph(graph);
			RPSTNode rootFragment = rpst.getRoot();
			log.debug("Starting the processing of the root fragment...");
			FragmentVersion rootfv = process(rpst, rootFragment, op, fragmentIds, graph);
			fragmentIds.add(rootfv.getFragmentVersionId());
			return rootfv;

		} catch (Exception e) {
			String msg = "Failed to add root fragment version of the process model.";
			log.error(msg, e);
			throw new RepositoryException(msg, e);
		}
	}

	/**
	 * Stores the fragment as it is
	 * Removes all child fragments and replace them with their fragment codes
	 * Returns the fragment code of the fragment
	 * 
	 * @param rpst
	 * @param f
	 * @param op
	 * @param fragmentIds
	 * @param con
	 * @return
	 * @throws DAOException
	 * @throws RepositoryException
	 */
	public FragmentVersion process(RPST rpst, RPSTNode f, OperationContext op, List<String> fragmentIds, CPF g)
			throws RepositoryException {
		
//		PSEnvironment pse = PSEnvironment.getPSEnvironment();
		
//		removeRundandantBoundaryConnectors(f, op);
		
		RPSTNodeCopy fCopy = new RPSTNodeCopy(f);
		fCopy.setReadableNodeType(FragmentUtil.getFragmentType(f));
		
		TCType fragmentType = f.getType();
		if (fragmentType.equals(TCType.T)) {
			FragmentVersion tempFV = new FragmentVersion();
			tempFV.setFragmentVersionId("");
			return tempFV;
		}
		
//		String keywords = KeywordIndexer.getKeywordsAsString(f, op, true);
		Collection<RPSTNode> cs = rpst.getChildren(f);

		// child id -> uuid (we use uuid instead of a pocket id here as we are not using pockets in this simple decomposition)
		Map<String, String> childFragmentIds = new HashMap<String, String>();
		for (RPSTNode c : cs) {
			
			if (c.getType().equals(TCType.T)) {
				continue;
			}
			
			IVertex cEntry = c.getEntry();
			IVertex cExit = c.getExit();
			Collection<IVertex> cvs = c.getFragment().getVertices();
			for (IVertex cv : cvs) {
				if (!cv.equals(cEntry) && !cv.equals(cExit)) {
					f.getFragment().removeVertex(cv);
				}
			}
			f.getFragment().removeEdges(c.getFragment().getEdges());
			
			FragmentVersion childFragment = process(rpst, c, op, fragmentIds, g);
			
			// if the fragment f has no edges after removing a child content, it is a P around B with only two connectors.
			// its connectors have been removed in the redundant connector removal phase and it is now equivalent to its B child.
			if (f.getFragment().getEdges().isEmpty()) {
				return childFragment;
			}
			
			fragmentIds.add(childFragment.getFragmentVersionId());
			childFragmentIds.put(UUID.randomUUID().toString(), childFragment.getFragmentVersionId());
			
			Vertex childFragmentComposite = new Vertex(childFragment.getFragmentVersionId());
			op.getGraph().setVertexProperty(childFragmentComposite.getId(), FSConstants.TYPE, FSConstants.FUNCTION);
			f.getFragment().addVertex(childFragmentComposite);

			f.getFragment().addEdge(cEntry, childFragmentComposite);
			f.getFragment().addEdge(childFragmentComposite, cExit);
		}
		
		Set<Vertex> vertices = new HashSet<Vertex>(f.getFragment().getVertices());
		Set<AbstractDirectedEdge> edges = new HashSet<AbstractDirectedEdge>(f.getFragment().getEdges());
		
		if (f.getEntry().getName() == null) {
			System.out.println(vertices);
		}
		
		log.debug("Computing the code for fragment: " + fCopy.getReadableNodeType() + " : " + fCopy.getNumVertices());
		
//		String fragmentCode = null;
//		if (fragmentType.equals(TCType.P)) {
//			fragmentCode = visitor.visitSNode(op.getGraph(), edges, vertices, f.getEntry(), f.getExit());
//		} else if (fragmentType.equals(TCType.B)) {
//			fragmentCode = visitor.visitBNode(op.getGraph(), edges, vertices, f.getEntry(), f.getExit());
//		} else if (fragmentType.equals(TCType.R)) {
//			fragmentCode = visitor.visitRNode(op.getGraph(), edges, vertices, f.getEntry(), f.getExit());
//		}
		
		String fragmentCode = HashUtil.computeHash(f, op);
//		String fragmentCode = "NOT COMPUTED";
		
		log.debug("Computation of code complete.");
		
//		String fragmentId = SimpleClusterDAO.getFragmentId(fragmentCode, con);
		FragmentVersion fv = null;
		if (fv == null) {
			fv = fsrv.storeFragment(fragmentCode, fCopy, g);
			fsrv.addChildMappings(fv, childFragmentIds);
//			FragmentVersionDAO.addChildMappings(fragmentId, childFragmentIds, con);
			
//			if (!fCopy.getReadableNodeType().equals("T") && fCopy.getNumVertices() >= pse.getSettings().getMinClusteringFragmentSize() && 
//					fCopy.getNumVertices() <= pse.getSettings().getMaxClusteringFragmentSize()) {
//				op.addProcessedFragmentType(fCopy.getReadableNodeType());
//				KeywordIndexer.index(fragmentId, keywords, fCopy.getSize(), op.getProcessedFragmentTypes());
//			}
		}
		return fv;
	}
	
	private void removeRundandantBoundaryConnectors(RPSTNode f, OperationContext op) {
		
		if (f.getType().equals(TCType.P)) {
			CPF g = op.getGraph();
			
			if (FSConstants.CONNECTOR.equals(g.getVertexProperty(f.getEntry().getId(), FSConstants.TYPE))) {
				Collection<Vertex> entryPostset = f.getFragment().getDirectSuccessors(f.getEntry());
				if (entryPostset.size() == 1) {
					f.getFragment().removeVertex(f.getEntry());
					
					for (Vertex newEntry : entryPostset) {
						f.setEntry(newEntry);
						break;
					}
				}
			}
			
			if (FSConstants.CONNECTOR.equals(g.getVertexProperty(f.getExit().getId(), FSConstants.TYPE))) {
				Collection<Vertex> exitPreset = f.getFragment().getDirectPredecessors(f.getExit());
				if (exitPreset.size() == 1) {
					f.getFragment().removeVertex(f.getExit());
					
					for (Vertex newExit : exitPreset) {
						f.setExit(newExit);
						break;
					}
				}
			}
		}
	}

	/* 
	 * Fragment level decomposition is not supported by the simple decomposer.
	 */
	public String decomposeFragment(CPF graph, List<String> fragmentIds)
			throws RepositoryException {
		throw new UnsupportedOperationException("Fragment level decomposition is not supported by the simple decomposer.");
	}
}
