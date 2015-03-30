package org.apromore.dao.jpa;

import org.apromore.common.Constants;
import org.apromore.dao.NamedQueries;
import org.apromore.dao.ProcessDao;
import org.apromore.dao.model.Process;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hibernate implementation of the org.apromore.dao.ProcessDao interface.
 *
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 * @since 1.0
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class ProcessDaoJpa implements ProcessDao {

    @PersistenceContext
    private EntityManager em;
    
    private JdbcTemplate jdbcTemplate;
	
	public void setDataSource(DataSource dataSource) {
    	this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public static final String GET_ALL_PROCESSES = "SELECT p FROM Process p ";
    public static final String GET_ALL_PRO_SORT = " ORDER by p.processId";

    @Override
    public Collection<Integer> getBranches(String processName) {
    	
    	String sql = "select b.branch_id from process p, process_branch b " +
    			"where p.processId=b.process_id and p.name=?";
    	
    	Collection<Integer> branchIds = this.jdbcTemplate.query(
    		    sql,
    		    new Object[] { processName },
    		    new RowMapper<Integer>() {
    		        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
    		            int branchId = rs.getInt("branch_id");
    		            return new Integer(branchId);
    		        }
    		    });
    	
    	return branchIds;
    }
    
    @Override
    public String getRootFragmentId(String processName) {
    	
    	String sql = "select v.root_fragment_version_id from process p, process_branch b, process_model_version v " +
    			"where p.processId=b.process_id and b.branch_id=v.branch_id and p.name=? and b.branch_name=?";
    	
    	List<String> rootIds = this.jdbcTemplate.query(
    		    sql,
    		    new Object[] { processName, Constants.TRUNK_NAME },
    		    new RowMapper<String>() {
    		        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    		            String rootId = rs.getString("root_fragment_version_id");
    		            return rootId;
    		        }
    		    });
    	
    	if (rootIds.size() != 1) {
    		String msg = "No root fragment Id for the process " + processName;
    		return null;
    	}
    	return rootIds.get(0);
    }
    
    @Override
    public void deleteProcess(String processName) {
    	String sql = "delete from process where name = ?";
    	jdbcTemplate.update(sql, processName);
    }

    /**
     * @see org.apromore.dao.ProcessDao#findProcess(String)
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Process findProcess(String processId) {
        return em.find(Process.class, processId);
    }

    @Override
    public List<Process> getProcessesJDBC() {
    	String sql = "select processId, name from process";
    	
    	List<Process> processes = this.jdbcTemplate.query(
    	        sql,
    	        new Object[] {},
    	        new RowMapper<Process>() {
    	            public Process mapRow(ResultSet rs, int rowNum) throws SQLException {
    	            	Process p = new Process();
    	            	p.setProcessId(rs.getInt("processId"));
    	            	p.setName(rs.getString("name"));
    	                return p;
    	            }
    	        });
    	
    	return processes;
    }

    /**
     * @return the list of processes.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Process> getProcesses() {
        Query query = em.createNamedQuery(NamedQueries.GET_All_PROCESSES);
        return query.getResultList();
    }

    /**
     * @see org.apromore.dao.ProcessDao#getAllProcesses(String)
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Process> getAllProcesses(final String conditions) {
        StringBuilder strQry = new StringBuilder(0);
        strQry.append(GET_ALL_PROCESSES);
        if (conditions != null && !conditions.isEmpty()) {
            strQry.append(conditions);
        }
        strQry.append(GET_ALL_PRO_SORT);

        Query query = em.createQuery(strQry.toString());
        return query.getResultList();
    }

    /**
     * @see org.apromore.dao.ProcessDao#getAllDomains()
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<String> getAllDomains() {
        Query query = em.createNamedQuery(NamedQueries.GET_All_DOMAINS);
        return query.getResultList();
    }


    /**
     * @see org.apromore.dao.ProcessDao#getProcess(int)
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Process getProcess(final int processId) {
        Query query = em.createNamedQuery(NamedQueries.GET_PROCESS_BY_ID);
        query.setParameter("id", processId);
        List<Process> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * @see org.apromore.dao.ProcessDao#getProcess(String)
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Process getProcess(final String processName) {
        Query query = em.createNamedQuery(NamedQueries.GET_PROCESS_BY_NAME);
        query.setParameter("name", processName);
        List<Process> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * @see org.apromore.dao.ProcessDao#getRootFragmentVersionId(Integer)
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public String getRootFragmentVersionId(final Integer processModelVersionId) {
        Query query = em.createNamedQuery(NamedQueries.GET_ROOT_FRAGMENT_PROCESS_MODEL);
        query.setParameter("id", processModelVersionId);
        return (String) query.getSingleResult();
    }


    /**
     * @see org.apromore.dao.ProcessDao#getCurrentProcessModels()
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<Integer, int[]> getCurrentProcessModels() {
        Map<Integer, int[]> results = new HashMap<Integer, int[]>(0);

        Query query = em.createNamedQuery(NamedQueries.GET_CURRENT_PROCESS_MODELS);
        List<Object[]> res = (List<Object[]>) query.getResultList();
        for (Object[] obj : res) {
            int[] versionInfo = new int[2];
            versionInfo[0] = (Integer) obj[1];
            versionInfo[1] = (Integer) obj[2];
            results.put((Integer) obj[0], versionInfo);
        }
        return results;
    }




    /**
     * @see org.apromore.dao.ProcessDao#delete(Process)
     * {@inheritDoc}
     */
    @Override
    public void save(Process process) {
        em.persist(process);
    }

    /**
     * @see org.apromore.dao.ProcessDao#delete(Process)
     * {@inheritDoc}
     */
    @Override
    public Process update(Process process) {
        return em.merge(process);
    }

    /**
     * @see org.apromore.dao.ProcessDao#delete(Process)
     * {@inheritDoc}
     */
    @Override
    public void delete(Process process) {
        em.remove(process);
    }


    /**
     * Sets the Entity Manager. No way around this to get Unit Testing working
     * @param em the entitymanager
     */
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
