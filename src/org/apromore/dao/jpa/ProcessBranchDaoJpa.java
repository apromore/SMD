package org.apromore.dao.jpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apromore.dao.NamedQueries;
import org.apromore.dao.ProcessBranchDao;
import org.apromore.dao.model.ProcessBranch;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;

/**
 * Hibernate implementation of the org.apromore.dao.ProcessBranchDao interface.
 *
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 * @since 1.0
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class ProcessBranchDaoJpa implements ProcessBranchDao {

    @PersistenceContext
    private EntityManager em;
    
    private JdbcTemplate jdbcTemplate;
	
	public void setDataSource(DataSource dataSource) {
    	this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
	
	@Override
    public Collection<Integer> getProcessModelVersions(Integer branchId) {
    	
    	String sql = "select process_model_version_id from process_model_version where branch_id=?";
    	
    	Collection<Integer> pmvIds = this.jdbcTemplate.query(
    		    sql,
    		    new Object[] { branchId },
    		    new RowMapper<Integer>() {
    		        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
    		            int branchId = rs.getInt("process_model_version_id");
    		            return new Integer(branchId);
    		        }
    		    });
    	
    	return pmvIds;
    }
    
    @Override
    public void deleteBranch(Integer processId) {
    	String sql = "delete from process_branch where branch_id = ?";
    	jdbcTemplate.update(sql, processId);
    }
	
    /**
     * @see org.apromore.dao.ProcessBranchDao#findProcessBranch(String)
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ProcessBranch findProcessBranch(String branchId) {
        return em.find(ProcessBranch.class, branchId);
    }


    /**
     * @see org.apromore.dao.ProcessBranchDao#getProcessBranchByProcessBranchName(String, String)
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ProcessBranch getProcessBranchByProcessBranchName(final String processId, final String branchName) {
        Query query = em.createNamedQuery(NamedQueries.GET_BRANCH_BY_PROCESS_BRANCH_NAME);
        query.setParameter("processId", processId);
        query.setParameter("name", branchName);
        return (ProcessBranch) query.getSingleResult();
    }
    
    

    /**
     * @see org.apromore.dao.ProcessBranchDao#save(org.apromore.dao.model.ProcessBranch)
     * {@inheritDoc}
     */
    @Override
    public void save(final ProcessBranch branch) {
        em.persist(branch);
    }

    /**
     * @see org.apromore.dao.ProcessBranchDao#update(org.apromore.dao.model.ProcessBranch)
     * {@inheritDoc}
     */
    @Override
    public ProcessBranch update(final ProcessBranch branch) {
        return em.merge(branch);
    }

    /**
     * @see org.apromore.dao.ProcessBranchDao#delete(org.apromore.dao.model.ProcessBranch)
     * {@inheritDoc}
     */
    @Override
    public void delete(final ProcessBranch branch) {
        em.remove(branch);
    }


    /**
     * Sets the Entity Manager. No way around this to get Unit Testing working
     * @param em the entitymanager
     */
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
