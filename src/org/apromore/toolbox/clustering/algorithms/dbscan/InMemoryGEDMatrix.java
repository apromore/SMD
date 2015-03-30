/**
 * 
 */
package org.apromore.toolbox.clustering.algorithms.dbscan;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apromore.dao.ClusteringDao;
import org.apromore.exception.RepositoryException;
import org.apromore.service.model.ClusterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Chathura Ekanayake
 *
 */
public class InMemoryGEDMatrix {
	
	private static final Logger log = LoggerFactory.getLogger(InMemoryGEDMatrix.class);
	
	int cacheSize = 12502500;
	
	@Autowired
	private ClusteringDao clusteringDao;
	
	private NeighbourhoodCache neighborhoodCache;
	
	private Map<FragmentPair, Double> inMemoryGEDs;
	private boolean useInMemoryGEDs = false;
	
	private Map<String, InMemoryCluster> clusters;
	private List<FragmentDataObject> noise;
	private List<FragmentDataObject> unprocessedFragments;
	
	private ClusterSettings settings;
	
	public InMemoryGEDMatrix() {
		neighborhoodCache = new NeighbourhoodCache();
	}
	
	public void initialize(ClusterSettings settings,
			Map<String, InMemoryCluster> clusters, 
			List<FragmentDataObject> noise, 
			List<FragmentDataObject> unprocessedFragments) throws RepositoryException {
		this.settings = settings;
		this.clusters = clusters;
		this.noise = noise;
		this.unprocessedFragments = unprocessedFragments;
		
		loadInMemoryGEDs();
	}
	
	public void setClusteringDao(ClusteringDao clusteringDao) {
		this.clusteringDao = clusteringDao;
	}

	private void loadInMemoryGEDs() throws RepositoryException {
		double maxGED = settings.getMaxNeighborGraphEditDistance();
		log.debug("Loading GEDs to memory...");
		inMemoryGEDs = clusteringDao.getDistances(maxGED);
		useInMemoryGEDs = true;
		log.debug("GEDs have been successfully loaded to memory.");
	}
	
	public void loadCache() {
//		geds = GEDDAO.loadGEDs(0, cacheSize);
	}
	
	public double getGED(String fid1, String fid2) throws RepositoryException {
		
		if (fid1.equals(fid2)) {
			return 0;
		}
		
		double gedValue = 1;
		FragmentPair pair = new FragmentPair(fid1, fid2);
		if (inMemoryGEDs.containsKey(pair)) {
			gedValue = inMemoryGEDs.get(pair);
		}
		
		return gedValue;
	}

	public List<FragmentDataObject> getUnsharedCoreObjectNeighborhood(FragmentDataObject o, String sharableClusterId,
			List<String> allowedIds) throws RepositoryException {
		
		List<FragmentDataObject> nb = getCoreObjectNeighborhood(o, allowedIds);
		if (nb == null) {
			return null;
		}
		
		List<FragmentDataObject> unsharedNB = new ArrayList<FragmentDataObject>();
		for (FragmentDataObject fo : nb) {
			
			boolean containedInSharableCluster = false;
			InMemoryCluster sharableCluster = clusters.get(sharableClusterId);
			if (sharableCluster != null) {
				containedInSharableCluster = sharableCluster.getFragments().contains(fo);
			}
			
			if (unprocessedFragments.contains(fo) || noise.contains(fo) || containedInSharableCluster) {
				unsharedNB.add(fo);
			}
		}
		
		if (!unsharedNB.contains(o)) {
			unsharedNB.add(o);
		}
		
		if (unsharedNB.size() < settings.getMinPoints()) {
			return null;
		} else {
			return unsharedNB;
		}
	}
	
	public List<FragmentDataObject> getCoreObjectNeighborhood(FragmentDataObject o, List<String> allowedIds) 
			throws RepositoryException {
		
		List<FragmentDataObject> nb = neighborhoodCache.getNeighborhood(o.getFragmentId());
		
		if (nb == null) {
			nb = getNeighbourhood(o);
			if (!nb.contains(o)) nb.add(o);
//			neighborhoodCache.add(o.getFragmentId(), nb);
		}
		
		if (allowedIds != null) {
			List<FragmentDataObject> toBeRemoved = new ArrayList<FragmentDataObject>();
			for (FragmentDataObject nf : nb) {
				if (!allowedIds.contains(nf.getFragmentId())) {
					toBeRemoved.add(nf);
				}
			}
			nb.removeAll(toBeRemoved);
		}
		
		if (nb.size() >= settings.getMinPoints()) {
			return nb;
		} else {
			return null;
		}
	}

	/**
	 * @param o
	 * @return
	 */
	private List<FragmentDataObject> getNeighbourhood(FragmentDataObject o) {
		
		String oid = o.getFragmentId();
		List<FragmentDataObject> nb = new ArrayList<FragmentDataObject>();
		Set<FragmentPair> pairs = inMemoryGEDs.keySet();
		for (FragmentPair pair : pairs) {
			if (pair.getFid1().equals(oid)) {
				nb.add(new FragmentDataObject(pair.getFid2()));
			} else if (pair.getFid2().equals(oid)) {
				nb.add(new FragmentDataObject(pair.getFid1()));
			}
		}
		if (!nb.contains(o)) {
			nb.add(o);
		}
		return nb;
	}

	/* (non-Javadoc)
	 * @see fragstore.repository.clustering.INeighbourhoodFinder#getCoreObjectNeighborhood(fragstore.repository.clustering.fragments.FragmentDataObject, java.util.List, java.sql.Connection)
	 */
	public List<FragmentDataObject> getCoreObjectNeighborhood(FragmentDataObject o, List<String> allowedIds,
			Connection con) throws RepositoryException {
		
		List<FragmentDataObject> nb = neighborhoodCache.getNeighborhood(o.getFragmentId());
		
		if (nb == null) {
			nb = getNeighbourhood(o);
			if (!nb.contains(o)) nb.add(o);
			neighborhoodCache.add(o.getFragmentId(), nb);
		}
		
		if (allowedIds != null) {
			nb.retainAll(allowedIds);
		}
		
		if (nb.size() >= settings.getMinPoints()) {
			return nb;
		} else {
			return null;
		}
	}
}
