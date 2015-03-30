package clustering.hac;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apromore.common.FSConstants;
import org.apromore.dao.ClusteringDao;
import org.apromore.dao.model.ClusterInfo;
import org.apromore.exception.RepositoryException;
import org.apromore.service.model.ClusterSettings;
import org.apromore.toolbox.clustering.algorithms.dbscan.ClusterIdGenerator;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentDataObject;
import org.apromore.toolbox.clustering.algorithms.dbscan.InMemoryCluster;
import org.apromore.toolbox.clustering.algorithms.dbscan.InMemoryGEDMatrix;
import org.apromore.toolbox.clustering.analyzers.ClusterAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import clustering.containment.ContainmentRelationImpl;
import clustering.dendogram.InternalNode;
import clustering.dissimilarity.DissimilarityMatrixReader;

public class HACClusterer {
	
	private static final Logger log = LoggerFactory.getLogger(HACClusterer.class);
	
	@Autowired @Qualifier("ContainmentRelation")
	private ContainmentRelationImpl crel;
	
	@Autowired @Qualifier("DissimilarityMatrixReader")
	private DissimilarityMatrixReader dmatrixReader;
	
	@Autowired
	private ClusterAnalyzer clusterAnalyzer;
	
	@Autowired
	private ClusteringDao clusteringDao;
	
	@Autowired
	private InMemoryGEDMatrix gedMatrix;

	public void clusterRepository(ClusterSettings settings) throws RepositoryException {
		
		try {
			double maxDistance = settings.getMaxNeighborGraphEditDistance();
			crel.setMinSize(settings.getMinClusteringFragmentSize());
			crel.initialize();
			dmatrixReader.initialize(settings.getMaxNeighborGraphEditDistance(), crel);
			
			HierarchicalAgglomerativeClustering clusterer = new CompleteLinkage(crel, dmatrixReader);
			clusterer.setDiameterThreshold(maxDistance);
			SortedSet<InternalNode> sources2 = clusterer.cluster();
			
			// now convert clusters into InMemoryCluster objects so that we can analyse them
			List<InMemoryCluster> clusters = new ArrayList<InMemoryCluster>();
			for (InternalNode inode : sources2) {
				String clusterId = ClusterIdGenerator.getStringId();
				InMemoryCluster c = new InMemoryCluster(clusterId, FSConstants.PHASE1);
				
				for (String fid : inode.getChildren()) {
					FragmentDataObject fd = new FragmentDataObject(fid);
					c.addFragment(fd);
				}
				clusters.add(c);
			}
			
			// analyse clusters, which gives persistance bean containing cluster analysis for each cluster
			long pt1 = System.currentTimeMillis();
			log.debug("Analyzing and persisting " + clusters.size() + " clusters in the database...");
			gedMatrix.initialize(settings, null, null, null);
			clusterAnalyzer.loadFragmentSizes();
			List<ClusterInfo> cds = new ArrayList<ClusterInfo>();
			for (InMemoryCluster cluster : clusters) {
				ClusterInfo cd = clusterAnalyzer.analyzeCluster(cluster, settings);
				cds.add(cd);
			}
			
			// if there are exact clones, remove them if the configuration says so
//			if (settings.isIgnoreClustersWithExactClones()) {
//				Set<ClusterInfo> toBeRemovedCDs = new HashSet<ClusterInfo>();
//				for (ClusterInfo cd : cds) {
//					if (cd.getStandardizingEffort() == 0) {
//						// this is a cluster with exact clones (i.e. inter-fragment distances and std effort are zero)
//						toBeRemovedCDs.add(cd);
//						clusters.remove(cd.getClusterId());
//						log.debug("Removed cluster: " + cd.getClusterId() + 
//								" from results as it only contains identical fragments (i.e. exact clones)");
//					}
//				}
//				cds.removeAll(toBeRemovedCDs);
//			}
			
			// nor persist clusters and cluster-fragment associations
			clusteringDao.persistClusters(cds);
			clusteringDao.persistClusterAssignments(clusters);
			long pt2 = System.currentTimeMillis();
			long pduration = pt2 - pt1;
			log.debug("Time for persisting clusters: " + pduration);
			
			log.debug("Cluster persistance completed.");
			
		} catch (Exception e) {
			String msg = "Failed to create clusters using the HAC algorithm.";
			log.error(msg, e);
			throw new RepositoryException(msg, e);
		}
	}
}
