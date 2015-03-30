package org.apromore.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import javax.sql.DataSource;

import org.apromore.common.Constants;
import org.apromore.dao.ContentDao;
import org.apromore.dao.FragmentVersionDagDao;
import org.apromore.dao.FragmentVersionDao;
import org.apromore.dao.NodeDao;
import org.apromore.dao.ProcessDao;
import org.apromore.dao.ProcessModelVersionDao;
import org.apromore.dao.model.Content;
import org.apromore.dao.model.FragmentVersion;
import org.apromore.dao.model.FragmentVersionDag;
import org.apromore.dao.model.FragmentVersionDagId;
import org.apromore.dao.model.Node;
import org.apromore.dao.model.ProcessFragmentMap;
import org.apromore.dao.model.ProcessModelVersion;
import org.apromore.exception.ExceptionDao;
import org.apromore.exception.LockFailedException;
import org.apromore.exception.RepositoryException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.ICpfNode;
import org.apromore.mining.dws.pcm.DWSSubprocessAwareComposer;
import org.apromore.mining.guidetree.pcm.GTSubprocessAwareComposer;
import org.apromore.service.ContentService;
import org.apromore.service.FragmentService;
import org.apromore.service.IComposer;
import org.apromore.service.LockService;
import org.apromore.service.model.FDNode;
import org.apromore.service.model.FragmentAssociation;
import org.apromore.service.model.FragmentDAG;
import org.apromore.service.utils.EPCSerializer;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.apromore.util.FragmentUtil;
import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the FragmentService Contract.
 *
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 */
@Service("FragmentService")
@Transactional(propagation = Propagation.REQUIRED)
public class FragmentServiceImpl implements FragmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentServiceImpl.class);

    @Autowired @Qualifier("ProcessDao")
    private ProcessDao pDao;
    
    @Autowired @Qualifier("FragmentVersionDao")
    private FragmentVersionDao fvDao;
    
    @Autowired @Qualifier("FragmentVersionDagDao")
    private FragmentVersionDagDao fvdDao;
    
    @Autowired @Qualifier("ProcessModelVersionDao")
    private ProcessModelVersionDao pmvDao;
    
    @Autowired @Qualifier("ContentService")
    private ContentService csrv;
    
    @Autowired @Qualifier("ContentDao")
    private ContentDao contentDao;
    
    @Autowired @Qualifier("IComposer")
    private IComposer composer;
    
    @Autowired @Qualifier("SubprocessAwareComposer")
    private SubprocessAwareComposer subprocessAwareComposer;
    
    @Autowired
    private GTSubprocessAwareComposer gtComposer;
    
    @Autowired
    private DWSSubprocessAwareComposer dwsComposer;

    @Autowired @Qualifier("LockService")
    private LockService lSrv;
    
    @Autowired @Qualifier("NodeDao")
    private NodeDao nDao;
    
    /**
     * @see org.apromore.service.ProcessService#addProcessFragmentMappings(Integer, java.util.List)
     * {@inheritDoc}
     */
    @Override
    public void addProcessFragmentMappings(Integer pmvid, List<String> composingFragmentIds) throws ExceptionDao {
        ProcessFragmentMap pfm;
        ProcessModelVersion pmv = pmvDao.findProcessModelVersion(pmvid);
        for (String composingFragmentId : composingFragmentIds) {
            pfm = new ProcessFragmentMap();
            pfm.setProcessModelVersion(pmv);
            pfm.setFragmentVersion(fvDao.findFragmentVersion(composingFragmentId));
        }
    }


    /**
     * @see FragmentService#getFragmentId(Integer, org.apromore.graph.JBPT.CPF, java.util.List)
     * {@inheritDoc}
     */
    @Override
    public String getFragmentId(Integer pmvid, CPF g, List<String> nodes) {
        FragmentDAG fdag = constructFragmentDAG(pmvid);
        List<String> originalNodes = getOriginalNodes(nodes, g);
        List<String> containingFragments = getContainingFragments(originalNodes, fdag);
        List<String> candidateContainingFragments = new ArrayList<String>();
        findCandidateContainingFragments(containingFragments.get(0), containingFragments, fdag, candidateContainingFragments);
        return findSmallestContainingFragment(candidateContainingFragments, fdag);
    }
    
    @Override
	public String getFragmentAsEPML(String fragmentId) throws RepositoryException {
    	String epmlString = "";
		try {
			CPF g = getFragment(fragmentId, false);
//			epmlString = new FormattableEPCSerializer().serializeToString(g);
			epmlString = new EPCSerializer().serializeToString(g);
		} catch (LockFailedException e) {
			throw new RepositoryException(e);
		}
		return epmlString;
	}
    
    @Override
	public String getFragmentAsFormattedEPML(String fragmentId) throws RepositoryException {
    	String epmlString = "";
		try {
			CPF g = getFragment(fragmentId, false);
			epmlString = new FormattableEPCSerializer().serializeToString(g);
		} catch (LockFailedException e) {
			throw new RepositoryException(e);
		}
		return epmlString;
	}
    
    @Override
    public CPF getFragmentWithSubprocesses(String fragmentId, Collection<String> subprocesses) throws RepositoryException {
    	try {
			CPF g = subprocessAwareComposer.compose(fragmentId, subprocesses);
			g.setProperty(Constants.ORIGINAL_FRAGMENT_ID, fragmentId);
			return g;
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
    }
    
    @Override
    public CPF getFragmentWithSubprocessesGT(String fragmentId, Collection<String> subprocesses) throws RepositoryException {
    	try {
			CPF g = gtComposer.compose(fragmentId, subprocesses);
			g.setProperty(Constants.ORIGINAL_FRAGMENT_ID, fragmentId);
			return g;
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
    }
    
    @Override
    public CPF getFragmentWithSubprocessesDWS(String fragmentId, Collection<String> subprocesses) throws RepositoryException {
    	try {
			CPF g = dwsComposer.compose(fragmentId, subprocesses);
			g.setProperty(Constants.ORIGINAL_FRAGMENT_ID, fragmentId);
			return g;
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
    }

    /**
     * @see FragmentService#getFragment(String, boolean)
     * {@inheritDoc}
     */
    @Override
    public CPF getFragment(String fragmentId, boolean lock) throws LockFailedException {
        CPF processModelGraph = null;
        try {
            if (lock) {
                LOGGER.debug("Obtaining a lock for the fragment " + fragmentId + "...");
                boolean locked = lSrv.lockFragment(fragmentId);
                if (!locked) {
                    throw new LockFailedException();
                }
            }

            LOGGER.debug("Composing the fragment " + fragmentId + "...");
            processModelGraph = composer.compose(fragmentId);
            processModelGraph.setProperty(Constants.ORIGINAL_FRAGMENT_ID, fragmentId);

            if (lock) {
                processModelGraph.setProperty(Constants.LOCK_STATUS, Constants.LOCKED);
            }
        } catch (Exception e) {
            String msg = "Failed to retrieve the fragment " + fragmentId;
            LOGGER.error(msg, e);
            return processModelGraph;
        }

        return processModelGraph;
    }

    /**
     * @see FragmentService#getFragmentVersion(String)
     * {@inheritDoc}
     */
    @Override
    public FragmentVersion getFragmentVersion(String fragmentVersionId) {
        return fvDao.findFragmentVersion(fragmentVersionId);
    }
    
    @Override
	public List<FragmentAssociation> getSharedFragments(int minSharings, int minFragmentSize) {
    	List<FragmentAssociation> fas = new ArrayList<FragmentAssociation>();
    	List<String> sharedFIds = fvDao.getSharedFragmentIds(minSharings, minFragmentSize);
    	for (String fid : sharedFIds) {
    		List<String> processNames = fvDao.getProcessNamesOfFragment(fid);
    		
    		FragmentAssociation fa = new FragmentAssociation();
    		fa.setFragmentId(fid);
    		fa.setProcessNames(processNames);
    		fas.add(fa);
    	}
    	return fas;
	}

	/**
     * @see FragmentService#addFragmentVersion(org.apromore.dao.model.Content, java.util.Map, String, int, int, int, String)
     * {@inheritDoc}
     */
    @Override
    public FragmentVersion addFragmentVersion(Content cid, Map<String, String> childMappings, String derivedFrom,
            int lockStatus, int lockCount, int originalSize, String fragmentType) {
        String childMappingCode = calculateChildMappingCode(childMappings);

        FragmentVersion fragVersion = new FragmentVersion();
        fragVersion.setContent(cid);
        fragVersion.setChildMappingCode(childMappingCode);
        fragVersion.setDerivedFromFragment(derivedFrom);
        fragVersion.setLockStatus(lockStatus);
        fragVersion.setLockCount(lockCount);
        fragVersion.setFragmentType(fragmentType);
        fragVersion.setFragmentSize(originalSize);
        fvDao.save(fragVersion);

        addChildMappings(fragVersion, childMappings);

        return fragVersion;
    }
    
    @Override
    public FragmentVersion storeFragment(String fragmentCode, RPSTNodeCopy fCopy, CPF g) {
    	
    	Content c = new Content();
    	c.setBoundaryS(fCopy.getEntry().getId());
    	c.setBoundaryE(fCopy.getExit().getId());
    	c.setCode(fragmentCode);
    	contentDao.save(c);
    	
    	FragmentVersion f = new FragmentVersion();
    	f.setContent(c);
        f.setLockStatus(-1);
        f.setLockCount(-1);
        f.setFragmentType(fCopy.getReadableNodeType());
        f.setFragmentSize(fCopy.getSize());
        fvDao.save(f);
        
        Map<String, Integer> nodeMappings = new HashMap<String, Integer>();
        
        for (ICpfNode v : fCopy.getVertices()) {
//        	String vtype = g.getVertexProperty(v.getId(), FSConstants.TYPE);
        	String vtype = FragmentUtil.getType(v);
        	
        	Node node = new Node();
            node.setContent(c);
            node.setVname(v.getLabel());
            node.setVtype(vtype);
            node.setConfiguration(v.isConfigurable());
            node.setCtype(v.getClass().getName());
            node.setOriginalId(v.getId());
            nDao.save(node);
            v.setId(String.valueOf(node.getVid()));
            
            nodeMappings.put(v.getId(), node.getVid());
        }
        
        for (AbstractDirectedEdge e : fCopy.getEdges()) {
        	Node source = nDao.findNode(Integer.valueOf(e.getSource().getId()));
            Node target = nDao.findNode(Integer.valueOf(e.getTarget().getId()));
            csrv.addEdge(c, e, source, target);
        }
        
        return f;
    	

//		try {
//			String sql1 = "insert into fs_fragment_versions " +
//					"(fs_fragment_version_id, fs_fragment_id, fs_content_id, fs_child_mapping_code, fs_derived_from_fragment, fs_lock_status, fs_lock_count, fs_fragment_size, fs_num_vertices, fs_fragment_type) values (?,?,?,?,?,?,?,?,?,?)";
//			PreparedStatement s1 = con.prepareStatement(sql1);
//			s1.setString(1, fragmentId);
//			s1.setInt(2, 1);
//			s1.setString(3, fragmentId);
//			s1.setString(4, null);
//			s1.setString(5, null);
//			s1.setInt(6, -1);
//			s1.setInt(7, -1);
//			s1.setInt(8, fCopy.getSize());
//			s1.setInt(9, fCopy.getNumVertices());
//			s1.setString(10, fCopy.getReadableNodeType());
//			s1.executeUpdate();
//			s1.close();
//			
//			String sql2 = "insert into fs_contents (fs_content_id, fs_boundary_s, fs_boundary_e, fs_content_hash) values (?,?,?,?)";
//			PreparedStatement s2 = con.prepareStatement(sql2);
//			s2.setString(1, fragmentId);
//			s2.setString(2, fCopy.getEntry().getId());
//			s2.setString(3, fCopy.getExit().getId());
//			s2.setString(4, fragmentCode);
//			s2.executeUpdate();
//			s2.close();
//			
//			ContentDAO.addVerticesInBatch(fragmentId, fCopy.getVertices(), op.getGraph(), con);
//			ContentDAO.addEdgesInBatch(fragmentId, fCopy.getEdges(), con);
//
//		} catch (Exception ex) {
//			String msg = "Failed to add fragment: " + fragmentCode;
//			log.error(msg, ex);
//			throw new DAOException(msg, ex);
//		}
	}

    /**
     * @see FragmentService#addChildMappings(org.apromore.dao.model.FragmentVersion, java.util.Map)
     * {@inheritDoc}
     */
    @Override
    public void addChildMappings(FragmentVersion fragVer, Map<String, String> childMappings) {
        Set<String> pocketIds = childMappings.keySet();
        for (String pid : pocketIds) {
            String cid = childMappings.get(pid);
            if (fragVer == null || cid == null || pid == null) {
                String msg = "Invalid child mapping parameters. child Id: " + cid + ", Pocket Id: " + pid;
                LOGGER.error(msg);
                //throw new ExceptionDao(msg);
            }

            FragmentVersionDagId id = new FragmentVersionDagId();
            id.setFragmentVersionId(fragVer.getFragmentVersionId());
            id.setChildFragmentVersionId(cid);
            id.setPocketId(pid);

            FragmentVersionDag fvd = new FragmentVersionDag();
//            fvd.setFragmentVersionByFragVerId(fragVer);
//            fvd.setFragmentVersionByChildFragVerId(fvDao.findFragmentVersion(cid));
            fvd.setId(id);

            fvdDao.save(fvd);
        }
    }

    @Override
    public FragmentVersion getMatchingFragmentVersionId(String contentId, Map<String, String> childMappings) {
        String childMappingCode = calculateChildMappingCode(childMappings);
        return fvDao.getMatchingFragmentVersionId(contentId, childMappingCode);
    }


    @Override
    public void deleteFragmentVersion(String fvid) {
        fvDao.delete(fvDao.findFragmentVersion(fvid));
    }

    @Override
    public void deleteChildRelationships(String fvid) {
        fvDao.delete(fvDao.findFragmentVersion(fvid));
    }

    @Override
    public void setDerivation(String fvid, String derivedFromFragmentId) {
        FragmentVersion fragVersion = fvDao.findFragmentVersion(fvid);
        fragVersion.setDerivedFromFragment(derivedFromFragmentId);
        fvDao.update(fragVersion);
    }
    
    @Override
    public List<FragmentVersion> getFragmentsOfProcess(String processName, int minSize) {
    	List<FragmentVersion> fs = fvDao.getFragmentsOfProcess(processName, minSize);
    	return fs;
    }
    
    private String calculateChildMappingCode(Map<String, String> childMapping) {
        StringBuilder buf = new StringBuilder();
        Set<String> pids = childMapping.keySet();
        PriorityQueue<String> q = new PriorityQueue<String>(pids);
        while (!q.isEmpty()) {
        	String pid = q.poll();
            String cid = childMapping.get(pid);
            buf.append(pid).append(":").append(cid).append("|");
        }
        return buf.toString();
    }

    private void fillMatchingChildIds(String parentId, int minSize, int maxSize, List<String> matchingChildren) {
        Map<String, Integer> cs = fvDao.getChildFragmentsWithSize(parentId);
        for (String childId : cs.keySet()) {
            int size = cs.get(childId);
            if (size >= minSize) {
                if (size <= maxSize) {
                    matchingChildren.add(childId);
                }
                fillMatchingChildIds(childId, minSize, maxSize, matchingChildren);
            }
        }
    }

    private FragmentDAG constructFragmentDAG(final Integer pmvid) {
        FragmentDAG fdag = new FragmentDAG();
        String rootfid = pDao.getRootFragmentVersionId(pmvid);
        fillFragmentDAG(rootfid, null, fdag);
        return fdag;
    }

    private void fillFragmentDAG(String fragmentId, String parentId, FragmentDAG fdag) {
        if (fdag.contains(fragmentId)) {
            FDNode fdNode = fdag.getFragment(fragmentId);
            fdNode.getParentIds().add(parentId);
        } else {
            FDNode fdNode = new FDNode(fragmentId);
            fdag.addFragment(fdNode);
            fdNode.getParentIds().add(parentId);

            List<FragmentVersionDagId> childIds = fvdDao.getChildMappings(fragmentId);
            fdNode.getChildIds().addAll(getChildIds(childIds));
            for (FragmentVersionDagId childId : childIds) {
                fillFragmentDAG(childId.getChildFragmentVersionId(), fragmentId, fdag);
            }
        }
    }

    private Collection<String> getChildIds(List<FragmentVersionDagId> fdags) {
        List<String> id = new ArrayList<String>();
        for (FragmentVersionDagId fdag : fdags) {
            id.add(fdag.getChildFragmentVersionId());
        }
        return id;
    }

    private List<String> getOriginalNodes(List<String> nodes, CPF g) {
        List<String> originalNodes = new ArrayList<String>();
        for (String node : nodes) {
            if (g.isDuplicateNode(node)) {
                originalNodes.add(g.getOriginalNode(node));
            } else {
                originalNodes.add(node);
            }
        }
        return originalNodes;
    }

    private List<String> getContainingFragments(List<String> nodes, FragmentDAG fdag) {
        List<String> containingFragments = fvDao.getContainingFragments(nodes);
        Set<String> processFragmentIds = fdag.getFragmentIds();
        containingFragments.retainAll(processFragmentIds);
        return containingFragments;
    }

    private void findCandidateContainingFragments(String candidateFragment, List<String> containingFragments, FragmentDAG fdag,
            List<String> candidateContainingFragments) {
        boolean contained = fdag.isIncluded(candidateFragment, containingFragments);
        if (contained) {
            candidateContainingFragments.add(candidateFragment);
        } else {
            List<String> parents = fdag.getFragment(candidateFragment).getParentIds();
            for (String parent : parents) {
                findCandidateContainingFragments(parent, containingFragments, fdag, candidateContainingFragments);
            }
        }
    }

    private String findSmallestContainingFragment(List<String> candidateContainingFragments, FragmentDAG fdag) {
        if (candidateContainingFragments.size() == 1) {
            return candidateContainingFragments.get(0);
        }

        int fragmentNumber = 0;
        String smallestContainingFragment = null;
        while (smallestContainingFragment == null) {
            smallestContainingFragment = candidateContainingFragments.get(fragmentNumber);
            fragmentNumber++;
            for (String f : candidateContainingFragments) {
                if (f.equals(smallestContainingFragment))
                    continue;

                if (!fdag.isIncluded(smallestContainingFragment, f)) {
                    smallestContainingFragment = null;
                    break;
                }
            }
        }
        return smallestContainingFragment;
    }




    /**
     * Set the Process DAO for this class. Mainly for spring tests.
     * @param pdao the dao
     */
    public void setProcessDao(ProcessDao pdao) {
        pDao = pdao;
    }

    /**
     * Set the fragment Version DAO for this class. Mainly for spring tests.
     * @param fvdao the dao
     */
    public void setFragmentVersionDao(FragmentVersionDao fvdao) {
        fvDao = fvdao;
    }

    /**
     * Set the fragment Version DAG DAO for this class. Mainly for spring tests.
     * @param fvddao the dao
     */
    public void setFragmentVersionDagDao(FragmentVersionDagDao fvddao) {
        fvdDao = fvddao;
    }

    /**
     * Set the Process Model Version DAO object for this class. Mainly for spring tests.
     * @param pmvDAOJpa the process model version
     */
    public void setProcessModelVersionDao(ProcessModelVersionDao pmvDAOJpa) {
        pmvDao = pmvDAOJpa;
    }

    /**
    * Set the Lock Service for this class. Mainly for spring tests.
    * @param lsrv the Lock service
    */
    public void setLockService(LockService lsrv) {
        lSrv = lsrv;
    }
}
