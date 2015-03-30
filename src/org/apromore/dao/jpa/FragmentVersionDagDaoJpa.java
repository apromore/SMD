package org.apromore.dao.jpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.apromore.dao.FragmentVersionDagDao;
import org.apromore.dao.NamedQueries;
import org.apromore.dao.model.FragmentVersion;
import org.apromore.dao.model.FragmentVersionDag;
import org.apromore.dao.model.FragmentVersionDagId;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate implementation of the org.apromore.dao.FragmentVersionDagDao interface.
 *
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 * @since 1.0
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class FragmentVersionDagDaoJpa implements FragmentVersionDagDao {

    @PersistenceContext
    private EntityManager em;
    
    private JdbcTemplate jdbcTemplate;
    
    public void setDataSource(DataSource dataSource) {
    	this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    /**
     * @see org.apromore.dao.FragmentVersionDagDao#findFragmentVersionDag(String)
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public FragmentVersionDag findFragmentVersionDag(String vertexId) {
        return em.find(FragmentVersionDag.class, vertexId);
    }
    
    @Override
    public void replaceChildren(String replacement, String occurance) {
    	
    	String sql = "delete from fragment_version_dag where fragment_version_id=?";
    	jdbcTemplate.update(sql, occurance);
    	
//    	String sql = "update fragment_version_dag " +
//    			"set fragment_version_id=? " +
//    			"where fragment_version_id=?";
//    	
//    	try {
//			jdbcTemplate.update(sql, replacement, occurance);
//		} catch (DuplicateKeyException e) {
////			System.out.println("Duplicate ");
//		}
    }
    
    @Override
    public void replaceParents(String replacement, String occurance) {

    	String sql = "update fragment_version_dag " +
    			"set child_fragment_version_id=? " +
    			"where child_fragment_version_id=?";
    	
    	try {
			jdbcTemplate.update(sql, replacement, occurance);
		} catch (DuplicateKeyException e) {
			System.out.println("Duplicate parent links");
		}
    }
    
    @Override
	public Map<String, List<String>> getAllParentChildMappings() {
    	
    	Query query = em.createNamedQuery(NamedQueries.GET_ALL_PARENT_CHILD_MAPPINGS);
		List<FragmentVersionDag> mappings = (List<FragmentVersionDag>) query.getResultList();
    	
    	Map<String, List<String>> parentChildMap = new HashMap<String, List<String>>();
		for (FragmentVersionDag mapping : mappings) {
			String pid = mapping.getId().getFragmentVersionId();
			String cid = mapping.getId().getChildFragmentVersionId();
			if (parentChildMap.containsKey(pid)) {
				parentChildMap.get(pid).add(cid);
			} else {
				List<String> childIds = new ArrayList<String>();
				childIds.add(cid);
				parentChildMap.put(pid, childIds);
			}
		}
		return parentChildMap;
    }

	@Override
	public Map<String, List<String>> getAllChildParentMappings() {
		
		Query query = em.createNamedQuery(NamedQueries.GET_ALL_PARENT_CHILD_MAPPINGS);
		List<FragmentVersionDag> mappings = (List<FragmentVersionDag>) query.getResultList();
		
		Map<String, List<String>> childParentMap = new HashMap<String, List<String>>();
		for (FragmentVersionDag mapping : mappings) {
			String pid = mapping.getId().getFragmentVersionId();
			String cid = mapping.getId().getChildFragmentVersionId();
			if (childParentMap.containsKey(cid)) {
				childParentMap.get(cid).add(pid);
			} else {
				List<String> parentIds = new ArrayList<String>();
				parentIds.add(pid);
				childParentMap.put(cid, parentIds);
			}
		}
		return childParentMap;
	}

    /**
     * @see org.apromore.dao.FragmentVersionDagDao#getChildMappings(String)
     * {@inheritDoc}
     */
    @Override
    public List<FragmentVersionDagId> getChildMappings(final String fragmentId) {
    	
    	List<FragmentVersionDagId> cmaps = this.jdbcTemplate.query(
    	        "select fragment_version_id, child_fragment_version_id, pocket_id from fragment_version_dag where fragment_version_id=?",
    	        new Object[] {fragmentId},
    	        new RowMapper<FragmentVersionDagId>() {
    	            public FragmentVersionDagId mapRow(ResultSet rs, int rowNum) throws SQLException {
    	            	FragmentVersionDagId cmap = new FragmentVersionDagId();
    	            	cmap.setFragmentVersionId(rs.getString("fragment_version_id"));
    	            	cmap.setChildFragmentVersionId(rs.getString("child_fragment_version_id"));
    	            	cmap.setPocketId(rs.getString("pocket_id"));
    	                return cmap;
    	            }
    	        });
    	
    	return cmaps;
    	
//        Query query = em.createNamedQuery(NamedQueries.GET_CHILD_MAPPINGS);
//        query.setParameter("fragVersionId", fragmentId);
//        return (List<FragmentVersionDagId>) query.getResultList();
    }
    
    @Override
	public List<FragmentVersionDag> getAllDAGEntries(int minimumChildFragmentSize) {
    	Query query = em.createNamedQuery(NamedQueries.GET_ALL_DAGS_WITH_SIZE);
        query.setParameter("minSize", minimumChildFragmentSize);
        return (List<FragmentVersionDag>) query.getResultList();
	}
    
    public List<FragmentVersionDag> getAllDAGEntriesWithinSize(int minimumChildFragmentSize) {
    	Query query = em.createNamedQuery(NamedQueries.GET_ALL_DAGS_WITHIN_SIZE);
        query.setParameter("minSize", minimumChildFragmentSize);
        query.setParameter("maxSize", minimumChildFragmentSize);
        return (List<FragmentVersionDag>) query.getResultList();
	}

	/**
     * @see org.apromore.dao.FragmentVersionDagDao#getChildFragmentsByFragmentVersion(String)
     * {@inheritDoc}
     */
    @Override
    public List<FragmentVersion> getChildFragmentsByFragmentVersion(final String fragmentVersionId) {
        Query query = em.createNamedQuery(NamedQueries.GET_CHILD_FRAGMENTS_BY_FRAGMENT_VERSION);
        query.setParameter("fragVersionId", fragmentVersionId);
        return (List<FragmentVersion>) query.getResultList();
    }


    /**
     * @see org.apromore.dao.FragmentVersionDagDao#save(org.apromore.dao.model.FragmentVersionDag)
     * {@inheritDoc}
     */
    @Override
    public void save(final FragmentVersionDag fragmentVersionDag) {
        em.persist(fragmentVersionDag);
    }

    /**
     * @see org.apromore.dao.FragmentVersionDagDao#update(org.apromore.dao.model.FragmentVersionDag)
     * {@inheritDoc}
     */
    @Override
    public FragmentVersionDag update(final FragmentVersionDag fragmentVersionDag) {
        return em.merge(fragmentVersionDag);
    }

    /**
     * @see org.apromore.dao.FragmentVersionDagDao#delete(org.apromore.dao.model.FragmentVersionDag)
     * {@inheritDoc}
     */
    @Override
    public void delete(final FragmentVersionDag fragmentVersionDag) {
        em.remove(fragmentVersionDag);
    }



    /**
     * Sets the Entity Manager. No way around this to get Unit Testing working
     * @param em the entitymanager
     */
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
