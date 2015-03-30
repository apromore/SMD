package org.apromore.dao.jpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.apromore.dao.FragmentVersionDao;
import org.apromore.dao.NamedQueries;
import org.apromore.dao.model.FragmentVersion;
import org.apromore.service.model.FragmentAssociation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate implementation of the org.apromore.dao.FragmentVersionDao interface.
 *
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 * @since 1.0
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class FragmentVersionDaoJpa implements FragmentVersionDao {

    @PersistenceContext
    private EntityManager em;
    
    private JdbcTemplate jdbcTemplate;
    
    public void setDataSource(DataSource dataSource) {
    	this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    @Override
    public Collection<String> getFragmentIdsOfProcess(Integer pmvId) {
    	
    	String sql = "select fragment_version_id from process_fragment_map where process_model_version_id=?";
    	
    	Collection<String> fids = this.jdbcTemplate.query(
    		    sql,
    		    new Object[] { pmvId },
    		    new RowMapper<String>() {
    		        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    		            String fid = rs.getString("fragment_version_id");
    		            return fid;
    		        }
    		    });
    	
    	return fids;
    }
    
    @Override
    public Collection<String> getProcessNamesOfFragmentId(String fid) {
    	
    	String sql = "select p.name " +
    			"from process p, process_branch b, process_model_version v, process_fragment_map m " +
    			"where m.process_model_version_id=v.process_model_version_id and v.branch_id=b.branch_id and " +
    			"b.process_id=p.processId and m.fragment_version_id=?";
    	
    	Collection<String> pnames = this.jdbcTemplate.query(
    		    sql,
    		    new Object[] { fid },
    		    new RowMapper<String>() {
    		        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    		            String pname = rs.getString("name");
    		            return pname;
    		        }
    		    });
    	
    	return pnames;
    }
    
    @Override
	public void deleteFragments(Collection<String> fids) {
    	for (String fid : fids) {
	    	String sql = "delete from fragment_version where fragment_version_id=?";
	    	jdbcTemplate.update(sql, fid);
    	}
	}

	@Override
    public void deleteFragmentVersion(String fragmentVersionId) {
    	
    	String sql = "delete from fragment_version " +
    			"where fragment_version_id=?";
    	
    	jdbcTemplate.update(sql, fragmentVersionId);
    }
    
    @Override
	public void setLockStatus(String fragmentVersionId, Integer lockStatus) {
		FragmentVersion f = new FragmentVersion();
		f.setFragmentVersionId(fragmentVersionId);
		f.setLockStatus(lockStatus);
		em.merge(f);
	}

	@Override
    public Map<String, Integer> getAllFragmentIdsWithSize() {
    	
    	Query query = em.createNamedQuery(NamedQueries.GET_ALL_FRAGMENTS_WITH_SIZE);
    	List<Object[]> fragmentSizes = query.getResultList();
    	
    	Map<String, Integer> fsizeMap = new HashMap<String, Integer>();
    	for (Object[] fsize : fragmentSizes) {
    		String fid = (String) fsize[0];
    		Integer size = (Integer) fsize[1];
    		fsizeMap.put(fid, size.intValue());
    	}
    	return fsizeMap;
    }
	
    public Map<String, Integer> getAllFragmentIdsWithSizeJPA() {
    	
    	Query query = em.createNamedQuery(NamedQueries.GET_ALL_FRAGMENTS_WITH_SIZE);
    	List<Object[]> fragmentSizes = query.getResultList();
    	
    	Map<String, Integer> fsizeMap = new HashMap<String, Integer>();
    	for (Object[] fsize : fragmentSizes) {
    		String fid = (String) fsize[0];
    		Integer size = (Integer) fsize[1];
    		fsizeMap.put(fid, size.intValue());
    	}
    	return fsizeMap;
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#findFragmentVersion(String)
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public FragmentVersion findFragmentVersion(final String fragmentId) {
        return em.find(FragmentVersion.class, fragmentId);
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#getAllFragmentVersion()
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<FragmentVersion> getAllFragmentVersion() {
        Query query = em.createNamedQuery(NamedQueries.GET_ALL_FRAGMENT_VERSION);
        return query.getResultList();
    }
    

    /**
     * @see org.apromore.dao.FragmentVersionDao#getMatchingFragmentVersionId(String, String)
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public FragmentVersion getMatchingFragmentVersionId(final String contentId, final String childMappingCode) {
        Query query = em.createNamedQuery(NamedQueries.GET_FRAGMENT_BY_CONTENT_MAPPING );
        query.setParameter("contentId", contentId);
        query.setParameter("mappingCode", childMappingCode);
        List<FragmentVersion> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#getUsedProcessModels(String)
     * {@inheritDoc}
     */
    @Override
    public Integer getUsedProcessModels(final String fvid) {
        Query query = em.createNamedQuery(NamedQueries.GET_USED_PROCESS_MODEL_FOR_FRAGMENT);
        query.setParameter("fragVersionId", fvid);
        List<Integer> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#getParentFragments(String)
     * {@inheritDoc}
     */
    @Override
    public List<FragmentVersion> getParentFragments(final String fvid) {
        Query query = em.createNamedQuery(NamedQueries.GET_PARENT_FRAGMENT_VERSIONS);
        query.setParameter("childId", fvid);
        return (List<FragmentVersion>) query.getResultList();
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#getLockedParentFragmentIds(String)
     * {@inheritDoc}
     */
    @Override
    public List<String> getLockedParentFragmentIds(final String fvid) {
        Query query = em.createNamedQuery(NamedQueries.GET_LOCKED_PARENT_FRAGMENTS);
        query.setParameter("childFragVersionId", fvid);
        return (List<String>) query.getResultList();
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#getChildFragmentsWithSize(String)
     * {@inheritDoc}
     */
    @Override
    public Map<String, Integer> getChildFragmentsWithSize(final String fvid) {
        Query query = em.createNamedQuery(NamedQueries.GET_CHILD_FRAGMENTS_WITH_SIZE);
        query.setParameter("fragVersionId", fvid);
        return (Map<String, Integer>) query.getResultList();
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#getChildFragmentsWithType(int)
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, String> getChildFragmentsWithType(final int fvid) {
        Query query = em.createNamedQuery(NamedQueries.GET_CHILD_FRAGMENTS_WITH_TYPE);
        query.setParameter("fragVersionId", fvid);
        return (Map<Integer, String>) query.getResultList();
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#getContentId(String)
     * {@inheritDoc}
     */
    @Override
    public String getContentId(final String fvid) {
        Query query = em.createNamedQuery(NamedQueries.GET_FRAGMENT_VERSION);
        query.setParameter("id", fvid);
        return ((FragmentVersion) query.getResultList()).getContent().getContentId();
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#getFragmentDataOfProcessModel(String)
     * {@inheritDoc}
     */
    @Override
    public List<FragmentVersion> getFragmentDataOfProcessModel(final String pmvid) {
        Query query = em.createNamedQuery(NamedQueries.GET_FRAGMENT_DATA_OF_PROCESS_MODEL);
        query.setParameter("procModelId", pmvid);
        return (List<FragmentVersion>) query.getResultList();
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#getFragmentData(String)
     * {@inheritDoc}
     */
    @Override
    public FragmentVersion getFragmentData(final String fragmentId) {
        Query query = em.createNamedQuery(NamedQueries.GET_FRAGMENT_DATA);
        query.setParameter("fragVersionId", fragmentId);
        return (FragmentVersion) query.getResultList();
    }

    @Override
    public List<String> getContainingFragments(final List<String> nodes) {
        // TODO: Implement as it is used...or should be
        return null;
    }
    
    @Override
	public List<FragmentVersion> getFragmentsOfProcess(String processName,
			int minSize) {
    	Query query = em.createNamedQuery(NamedQueries.GET_FRAGMENTS_OF_PROCESS_NAME);
        query.setParameter("processName", processName);
        query.setParameter("minSize", minSize);
        return (List<FragmentVersion>) query.getResultList();
	}

	/**
     * @see org.apromore.dao.FragmentVersionDao#getContainedProcessModels(int)
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getContainedProcessModels(final int fragmentId) {
        Query query = em.createNamedQuery(NamedQueries.GET_CONTAINED_PROCESS_MODEL);
        query.setParameter("fragVersionId", fragmentId);
        return (List<Integer>) query.getResultList();
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#getUsedFragmentIds(String)
     * {@inheritDoc}
     */
    @Override
    public List<String> getUsedFragmentIds(final String matchingContentId) {
        Query query = em.createNamedQuery(NamedQueries.GET_USED_FRAGMENT_IDS);
        query.setParameter("contentId", matchingContentId);
        return (List<String>) query.getResultList();
    }


    /**
     * @see org.apromore.dao.FragmentVersionDao#getSimilarFragmentsBySize(int, int)
     * {@inheritDoc}
     */
    @Override
    public List<FragmentVersion> getSimilarFragmentsBySize(final int minSize, final int maxSize) {
        Query query = em.createNamedQuery(NamedQueries.GET_SIMILAR_FRAGMENTS_BY_SIZE);
        query.setParameter("min", minSize);
        query.setParameter("max", maxSize);
        return (List<FragmentVersion>) query.getResultList();
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#getSimilarFragmentsBySizeAndType(int, int, String)
     * {@inheritDoc}
     */
    @Override
    public List<FragmentVersion> getSimilarFragmentsBySizeAndType(final int minSize, final int maxSize, final String type) {
        Query query = em.createNamedQuery(NamedQueries.GET_SIMILAR_FRAGMENTS_BY_SIZE_AND_TYPE);
        query.setParameter("min", minSize);
        query.setParameter("max", maxSize);
        query.setParameter("type", type);
        return (List<FragmentVersion>) query.getResultList();
    }




    /**
     * @see org.apromore.dao.FragmentVersionDao#save(org.apromore.dao.model.FragmentVersion)
     * {@inheritDoc}
     */
    @Override
    public void save(final FragmentVersion fragVersion) {
        em.persist(fragVersion);
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#update(org.apromore.dao.model.FragmentVersion)
     * {@inheritDoc}
     */
    @Override
    public FragmentVersion update(final FragmentVersion fragVersion) {
        return em.merge(fragVersion);
    }

    /**
     * @see org.apromore.dao.FragmentVersionDao#delete(org.apromore.dao.model.FragmentVersion)
     * {@inheritDoc}
     */
    @Override
    public void delete(final FragmentVersion fragVersion) {
        em.remove(fragVersion);
    }
    
    @Override
	public List<String> getSharedFragmentIds(int minSharings, int minFragmentSize) {
    	Query query = em.createNamedQuery(NamedQueries.GET_SHARED_FRAGMENT_IDS);
        query.setParameter("minSharings", (long) minSharings);
        query.setParameter("minFragmentSize", minFragmentSize);
        return (List<String>) query.getResultList();
	}
    
    @Override
	public List<String> getProcessNamesOfFragment(String fragmentId) {
    	Query query = em.createNamedQuery(NamedQueries.GET_PROCESS_NAMES_OF_FRAGMENT);
        query.setParameter("fragmentId", fragmentId);
        return (List<String>) query.getResultList();
	}

	/**
     * Sets the Entity Manager. No way around this to get Unit Testing working
     * @param em the entitymanager
     */
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
