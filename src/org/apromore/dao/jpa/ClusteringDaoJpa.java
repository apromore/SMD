/**
 * 
 */
package org.apromore.dao.jpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apromore.dao.ClusteringDao;
import org.apromore.dao.NamedQueries;
import org.apromore.dao.model.ClusterAssignment;
import org.apromore.dao.model.ClusterAssignment2;
import org.apromore.dao.model.ClusterAssignmentId;
import org.apromore.dao.model.ClusterInfo;
import org.apromore.dao.model.ClusteringSummary;
import org.apromore.dao.model.FragmentDistance;
import org.apromore.dao.model.FragmentVersion;
import org.apromore.dao.model.FragmentVersionDagId;
import org.apromore.dao.model.GED;
import org.apromore.dao.model.GEDId;
import org.apromore.service.model.ClusterFilter;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentDataObject;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentPair;
import org.apromore.toolbox.clustering.algorithms.dbscan.InMemoryCluster;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.JpaTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Chathura C. Ekanayake
 *
 */
public class ClusteringDaoJpa extends JpaTemplate implements ClusteringDao {
	
	@PersistenceContext
	private EntityManager em;
	
	private JdbcTemplate jdbcTemplate;
	
	public void setDataSource(DataSource dataSource) {
    	this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
	
	@Override
	public void deleteDistances(Collection<String> fids) {
		for (String fid : fids) {
			String sql = "delete from fs_geds where fs_fid1=? or fs_fid2=?";
			jdbcTemplate.update(sql, fid, fid);
		}
	}
	
	@Override
    public List<String> getClustersOfFragment(final String fragmentId) {
		
		String sql = "select fs_cluster_id from fs_cluster_assignments where fs_fragment_version_id=?";
    	
    	List<String> clusterIds = this.jdbcTemplate.query(
    	        sql,
    	        new Object[] {fragmentId},
    	        new RowMapper<String>() {
    	            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    	            	String clusterId = rs.getString("fs_cluster_id");
    	                return clusterId;
    	            }
    	        });
		
//		List<String> clusterIds = this.jdbcTemplate.queryForList(sql, String.class);
		
    	return clusterIds;
    }
	
    public double getDistanceJDBC(String fid1, String fid2) {
		
		String sql = "select fs_ged from fs_geds where (fs_fid1=? and fs_fid2=?) or (fs_fid1=? and fs_fid2=?)";
    	
    	List<Double> geds = this.jdbcTemplate.query(
    	        sql,
    	        new Object[] {fid1, fid2, fid2, fid1},
    	        new RowMapper<Double>() {
    	            public Double mapRow(ResultSet rs, int rowNum) throws SQLException {
    	            	double ged = rs.getDouble("fs_ged");
    	                return ged;
    	            }
    	        });
		
    	if (geds.isEmpty()) {
    		return 1;
    	} else {
    		return geds.get(0);
    	}
    }
	
	@Override
	public void clearDistances() {
		Query q = em.createQuery("DELETE FROM GED");
		q.executeUpdate();
	}
	
	@Override
	public void insertDistancesJDBC(MultiKeyMap dissimmap) {
		
		MapIterator mi = dissimmap.mapIterator();
		while (mi.hasNext()) {
			Object k = mi.next();
			Object v = mi.getValue();

			MultiKey fids = (MultiKey) k;
			String fid1 = (String) fids.getKey(0);
			String fid2 = (String) fids.getKey(1);
			Double gedValue = (Double) v;
			
			if (getDistanceJDBC(fid1, fid2) < 1) {
				continue;
			}
			
			String sql = "insert into fs_geds (fs_fid1, fs_fid2, fs_ged) values (?,?,?)";
			jdbcTemplate.update(sql, fid1, fid2, gedValue);
		}
	}
	
	@Override
	public void insertDistances(MultiKeyMap dissimmap) {

		MapIterator mi = dissimmap.mapIterator();
		while (mi.hasNext()) {
			Object k = mi.next();
			Object v = mi.getValue();

			MultiKey fids = (MultiKey) k;
			String fid1 = (String) fids.getKey(0);
			String fid2 = (String) fids.getKey(1);
			Double gedValue = (Double) v;
			
			GEDId gidForward = new GEDId();
			gidForward.setFid1(fid1);
			gidForward.setFid2(fid2);
			
			GEDId gidReverse = new GEDId();
			gidReverse.setFid1(fid2);
			gidReverse.setFid2(fid1);
			
			if (em.find(GED.class, gidForward) != null || em.find(GED.class, gidReverse) != null) {
				continue;
			}
			
			GED ged = new GED();
			ged.setGedId(gidForward);
			ged.setGed(gedValue);
			em.persist(ged);
		}
	}
	
	@Override
	public List<ClusterInfo> getAllClusters() {
		
		String sql = "select fs_cluster_id, fs_size, fs_avg_fragment_size, fs_medoid_id, fs_benifit_cost_ratio, fs_std_effort, fs_refactoring_gain " +
				"from fs_clusters";
    	
    	List<ClusterInfo> cinfos = this.jdbcTemplate.query(
    	        sql,
    	        new Object[] {},
    	        new RowMapper<ClusterInfo>() {
    	            public ClusterInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
    	            	ClusterInfo cinfo = new ClusterInfo();
    	            	cinfo.setClusterId(rs.getString("fs_cluster_id"));
    	            	cinfo.setSize(rs.getInt("fs_size"));
    	            	cinfo.setAvgFragmentSize(rs.getFloat("fs_avg_fragment_size"));
    	            	cinfo.setMedoidId(rs.getString("fs_medoid_id"));
    	            	cinfo.setBCR(rs.getDouble("fs_benifit_cost_ratio"));
    	            	cinfo.setStandardizingEffort(rs.getDouble("fs_std_effort"));
    	            	cinfo.setRefactoringGain(rs.getInt("fs_refactoring_gain"));
    	            	return cinfo;
    	            }
    	        });
    	return cinfos;
    }
	
    public List<ClusterInfo> getAllClustersJPA() {
        return execute(new JpaCallback<List<ClusterInfo>>() {

            @SuppressWarnings("unchecked")
            public List<ClusterInfo> doInJpa(EntityManager em) {
                Query query = em.createNamedQuery(NamedQueries.GET_ALL_CLUSTERS);
                return query.getResultList();
            }
        });
    }
	
	@Override
	public void clearClusters() {
		
		String deleteClustersSQL = "delete from fs_clusters";
		jdbcTemplate.update(deleteClustersSQL);
		
		String deleteClusterAssignmentsSQL = "delete from fs_cluster_assignments";
		jdbcTemplate.update(deleteClusterAssignmentsSQL);
		
//		execute(new JpaCallback<Integer>() {
//            @SuppressWarnings("unchecked")
//            public Integer doInJpa(EntityManager em) {
//                Query query = em.createNamedQuery(NamedQueries.DELETE_ALL_CLUSTERS);
//                return query.executeUpdate();
//            }
//        });
//		
//		execute(new JpaCallback<Integer>() {
//            @SuppressWarnings("unchecked")
//            public Integer doInJpa(EntityManager em) {
//                Query query = em.createNamedQuery(NamedQueries.DELETE_ALL_CLUSTER_ASSIGNMENTS);
//                return query.executeUpdate();
//            }
//        });		
	}

	@Override
	public ClusterInfo getClusterSummary(final String clusterId) {
		
		List<ClusterInfo> cs = execute(new JpaCallback<List<ClusterInfo>>() {
            @SuppressWarnings("unchecked")
            public List<ClusterInfo> doInJpa(EntityManager em) {
                Query query = em.createNamedQuery(NamedQueries.GET_CLUSTER_BY_ID);
                query.setParameter("clusterId", clusterId);
                return query.getResultList();
            }
        });
		
		if (cs.isEmpty()) {
			return null;
		} else {
			return cs.get(0);
		}
	}

	/* (non-Javadoc)
	 * @see org.apromore.dao.ClusteringDao#getFilteredClusters(org.apromore.service.model.ClusterFilter)
	 */
	@Override
	public List<ClusterInfo> getFilteredClusters(final ClusterFilter filter) {
		return execute(new JpaCallback<List<ClusterInfo>>() {

            @SuppressWarnings("unchecked")
            public List<ClusterInfo> doInJpa(EntityManager em) {
                Query query = em.createNamedQuery(NamedQueries.GET_FILTERED_CLUSTERS);
                query.setParameter("minClusterSize", filter.getMinClusterSize());
                query.setParameter("maxClusterSize", filter.getMaxClusterSize());
                query.setParameter("minAvgFragmentSize", filter.getMinAverageFragmentSize());
                query.setParameter("maxAvgFragmentSize", filter.getMaxAverageFragmentSize());
                query.setParameter("minBCR", filter.getMinBCR());
                query.setParameter("maxBCR", filter.getMaxBCR());
                return query.getResultList();
            }
        });
	}
	
	@Override
	public ClusteringSummary getClusteringSummary() {
		return execute(new JpaCallback<ClusteringSummary>() {
            @SuppressWarnings("unchecked")
            public ClusteringSummary doInJpa(EntityManager em) {
                Query query = em.createNamedQuery(NamedQueries.GET_CLUSTERING_SUMMARY);
                List results = query.getResultList();
                if (results == null || results.isEmpty()) {
                	return null;
                } else {
                	return (ClusteringSummary) results.get(0);
                }
            }
        });
	}

	@Override
	public double getDistance(String fragmentId1, String fragmentId2) {
		double distance = getOrderedDistance(fragmentId1, fragmentId2);
		if (distance < 0) {
			distance = getOrderedDistance(fragmentId2, fragmentId1);
		}
		return distance;
	}

	public double getOrderedDistance(final String fragmentId1, final String fragmentId2) {
		return execute(new JpaCallback<Double>() {

            @SuppressWarnings("unchecked")
            public Double doInJpa(EntityManager em) {
                Query query = em.createNamedQuery(NamedQueries.GET_FRAGMENT_DISTANCE);
                query.setParameter("fragmentId1", fragmentId1);
                query.setParameter("fragmentId2", fragmentId2);
                List results = query.getResultList();
                if (results == null || results.isEmpty()) {
                	return -1d;
                } else {
                	return (Double) results.get(0);
                }
            }
        });
	}
	
	@Override
	public List<FragmentDistance> getRawDistances(final double threshold) {
		Query query = em.createNamedQuery(NamedQueries.GET_DISTANCES_BELOW_THRESHOLD);
        query.setParameter("threshold", threshold);
        return (List<FragmentDistance>) query.getResultList();
	}
	
	@Override
	public void deleteDistance(double distance) {
		String sql = "delete from fs_geds where fs_ged=?";
		jdbcTemplate.update(sql, distance);
	}
	
	@Override
	public void deleteDistancesWithFragment(String fragmentId) {
		String sql = "delete from fs_geds where fs_fid1=? or fs_fid2=?";
		jdbcTemplate.update(sql, fragmentId, fragmentId);
	}
	
	@Override
	public List<String> getSharedFragmentIds() {
		
		String sql = "select fs_fragment_version_id, count(*) as c " +
				"from fs_cluster_assignments group by fs_fragment_version_id having c > 1 order by c desc";
		List<String> cmaps = this.jdbcTemplate.query(
    	        sql,
    	        new Object[] {},
    	        new RowMapper<String>() {
    	            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    	            	String fid = rs.getString("fs_fragment_version_id");
    	                return fid;
    	            }
    	        });
    	
    	return cmaps;
	}
	
	@Override
	public Map<String, List<String>> getAllFragmentClusterMappings() {
		
		final String sql = "select fs_fragment_version_id, fs_cluster_id from fs_cluster_assignments";
		
		List<String[]> assignments = this.jdbcTemplate.query(
		        sql,
		        new RowMapper<String[]>() {
		            public String[] mapRow(ResultSet rs, int rowNum) throws SQLException {
		                String[] assignment = new String[2];
		                assignment[0] = rs.getString("fs_fragment_version_id");
		                assignment[1] = rs.getString("fs_cluster_id");
		                return assignment;
		            }
		        });
		
		Map<String, List<String>> fragmentClusterMap = new HashMap<String, List<String>>();
		for (String[] assignment : assignments) {
			List<String> cids = fragmentClusterMap.get(assignment[0]);
			if (cids == null) {
				cids = new ArrayList<String>();
				fragmentClusterMap.put(assignment[0], cids);
			}
			cids.add(assignment[1]);
		}
		
		return fragmentClusterMap;
	}

	@Override
	public Map<FragmentPair, Double> getFragmentsWithDistance(final double distance) {
		
		final String sql = "SELECT fd FROM FragmentDistance fd WHERE fd.distance = :distance";
		
		List<FragmentDistance> distances = execute(new JpaCallback<List<FragmentDistance>>() {
            @SuppressWarnings("unchecked")
            public List<FragmentDistance> doInJpa(EntityManager em) {
                Query query = em.createQuery(sql);
                query.setParameter("distance", distance);
                return query.getResultList();
            }
        });
		
		Map<FragmentPair, Double> fragmentDistances = new HashMap<FragmentPair, Double>();
		for (FragmentDistance d : distances) {
			FragmentPair pair = new FragmentPair(d.getId().getFragmentId1(), d.getId().getFragmentId2());
			fragmentDistances.put(pair, d.getDistance());
		}
		return fragmentDistances;
	}
	
	@Override
	public Map<FragmentPair, Double> getDistances(final double threshold) {
		List<FragmentDistance> distances = execute(new JpaCallback<List<FragmentDistance>>() {
            @SuppressWarnings("unchecked")
            public List<FragmentDistance> doInJpa(EntityManager em) {
                Query query = em.createNamedQuery(NamedQueries.GET_DISTANCES_BELOW_THRESHOLD);
                query.setParameter("threshold", threshold);
                return query.getResultList();
            }
        });
		
		Map<FragmentPair, Double> fragmentDistances = new HashMap<FragmentPair, Double>();
		for (FragmentDistance d : distances) {
			FragmentPair pair = new FragmentPair(d.getId().getFragmentId1(), d.getId().getFragmentId2());
			fragmentDistances.put(pair, d.getDistance());
		}
		return fragmentDistances;
	}

	@Override
	public List<String> getFragmentIds(final String clusterId) {
		String sql = "select fs_fragment_version_id from fs_cluster_assignments where fs_cluster_id=?";
    	
    	List<String> fids = this.jdbcTemplate.query(
    	        sql,
    	        new Object[] {clusterId},
    	        new RowMapper<String>() {
    	            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    	            	String fid = rs.getString("fs_fragment_version_id");
    	            	return fid;
    	            }
    	        });
    	return fids;
	}
	
	public List<String> getFragmentIdsJPA(final String clusterId) {
		return execute(new JpaCallback<List<String>>() {
            @SuppressWarnings("unchecked")
            public List<String> doInJpa(EntityManager em) {
                Query query = em.createNamedQuery(NamedQueries.GET_FRAGMENTIDS_OF_CLUSTER);
                query.setParameter("clusterId", clusterId);
                return query.getResultList();
            }
        });
	}
	
	@Override
	public List<FragmentVersion> getFragments(final String clusterId) {
		return execute(new JpaCallback<List<FragmentVersion>>() {
            @SuppressWarnings("unchecked")
            public List<FragmentVersion> doInJpa(EntityManager em) {
                Query query = em.createNamedQuery(NamedQueries.GET_FRAGMENTS_OF_CLUSTER);
                query.setParameter("clusterId", clusterId);
                return query.getResultList();
            }
        });
	}

	@Override
	public List<FragmentDataObject> getUnprocessedFragments() {
		List<FragmentDataObject> fragments = new ArrayList<FragmentDataObject>();
		List<FragmentVersion> fvs = getAllFragments();
		for (FragmentVersion fv : fvs) {
			FragmentDataObject fragment = new FragmentDataObject();
			fragment.setFragmentId(fv.getFragmentVersionId());
			fragment.setSize(fv.getFragmentSize());
			fragments.add(fragment);
		}
		return fragments;
	}

	private List<FragmentVersion> getAllFragments() {
		return execute(new JpaCallback<List<FragmentVersion>>() {
            @SuppressWarnings("unchecked")
            public List<FragmentVersion> doInJpa(EntityManager em) {
                Query query = em.createNamedQuery(NamedQueries.GET_UNPROCESSED_FRAGMENTS);
                return query.getResultList();
            }
        });
	}
	
	@Override
	public List<FragmentDataObject> getUnprocessedFragmentsOfProcesses(List<Integer> processIds) {
		List<FragmentDataObject> fragments = new ArrayList<FragmentDataObject>();
		List<FragmentVersion> fvs = getFragmentsOfProcesses(processIds);
		for (FragmentVersion fv : fvs) {
			FragmentDataObject fragment = new FragmentDataObject();
			fragment.setFragmentId(fv.getFragmentVersionId());
			fragment.setSize(fv.getFragmentSize());
			fragments.add(fragment);
		}
		return fragments;
	}

	private List<FragmentVersion> getFragmentsOfProcesses(final List<Integer> processIds) {
		return execute(new JpaCallback<List<FragmentVersion>>() {
            @SuppressWarnings("unchecked")
            public List<FragmentVersion> doInJpa(EntityManager em) {
                Query query = em.createNamedQuery(NamedQueries.GET_UNPROCESSED_FRAGMENTS_OF_PROCESSES);
                query.setParameter("processIds", processIds);
                return query.getResultList();
            }
        });
	}

	public void createClusters(Collection<InMemoryCluster> clusters) {
		List<ClusterInfo> cs = new ArrayList<ClusterInfo>();
		for (InMemoryCluster cluster: clusters) {
			ClusterInfo c = new ClusterInfo();
			c.setClusterId(cluster.getClusterId());
			cs.add(c);
		}
		persistClusters(cs);
	}
	
	public void persistClusterAssignmentsOld(Collection<InMemoryCluster> clusters) {
		List<ClusterAssignment> cas = new ArrayList<ClusterAssignment>();
		for (InMemoryCluster cluster : clusters) {
			Collection<FragmentDataObject> fs = cluster.getFragments();
			for (FragmentDataObject f : fs) {
				ClusterAssignment ca = new ClusterAssignment();
				ClusterAssignmentId caid = new ClusterAssignmentId();
				caid.setClusterId(cluster.getClusterId());
				caid.setFragmentId(f.getFragmentId());
				ca.setId(caid);
				cas.add(ca);
			}
		}
		persistClusterFragmentMappings(cas);
	}
	
	@Override
	public void persistClusterAssignments(Collection<InMemoryCluster> clusters) {
		List<ClusterAssignment2> cas = new ArrayList<ClusterAssignment2>();
		for (InMemoryCluster cluster : clusters) {
			Collection<FragmentDataObject> fs = cluster.getFragments();
			for (FragmentDataObject f : fs) {
				ClusterAssignment2 ca = new ClusterAssignment2();
				ca.setClusterId(cluster.getClusterId());
				ca.setFragmentId(f.getFragmentId());
				cas.add(ca);
			}
		}
		persistClusterFragmentMappingsInBatch(cas);
	}

	private void persistClusterFragmentMappings(List<ClusterAssignment> cas) {
		for (ClusterAssignment ca : cas) {
			persistClusterAssignment(ca);
//			persist(ca);
		}
	}
	
	private void persistClusterFragmentMappingsInBatch(final List<ClusterAssignment2> cas) {
		
		String sql = "insert into fs_cluster_assignments (fs_cluster_id, fs_fragment_version_id) values (?, ?)";
		
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ClusterAssignment2 ca = cas.get(i);
				ps.setString(1, ca.getClusterId());
				ps.setString(2, ca.getFragmentId());
				
			}
			
			@Override
			public int getBatchSize() {
				return cas.size();
			}
		});
	}
	
	private void persistClusterAssignment(ClusterAssignment ca) {
		String sql = "insert into fs_cluster_assignments (fs_cluster_id, fs_fragment_version_id) values (?, ?)";
		jdbcTemplate.update(sql, ca.getId().getClusterId(), ca.getId().getFragmentId());
	}

	@Override
	public void persistClusters(Collection<ClusterInfo> cs) {
		for (ClusterInfo c : cs) {
			persistJDBC(c);
		}
	}

	private void persistJDBC(ClusterInfo c) {
		String sql = "insert into fs_clusters (fs_cluster_id, fs_size, fs_avg_fragment_size, fs_medoid_id, fs_benifit_cost_ratio, fs_std_effort, fs_refactoring_gain) values (?,?,?,?,?,?,?)";
		jdbcTemplate.update(sql, c.getClusterId(), c.getSize(), c.getAvgFragmentSize(), c.getMedoidId(), c.getBCR(), c.getStandardizingEffort(), c.getRefactoringGain());
	}
}
