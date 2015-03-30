package clustering.dissimilarity;

import java.util.List;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apromore.dao.ClusteringDao;
import org.apromore.dao.model.FragmentDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import clustering.containment.ContainmentRelation;

public class DissimilarityMatrixReader implements DissimilarityMatrix {
	
	@Autowired @Qualifier("ClusteringDao")
	private ClusteringDao clusteringDao;
	
	private ContainmentRelation crel;
	
	MultiKeyMap dissimmap = new MultiKeyMap();
	
	public void initialize(double threshold, ContainmentRelation crel) {
		this.crel = crel;
		
		List<FragmentDistance> geds = clusteringDao.getRawDistances(threshold);
		for (FragmentDistance ged : geds) {
			String fid1 = ged.getId().getFragmentId1();
			String fid2 = ged.getId().getFragmentId2();
			double value = ged.getDistance();
			dissimmap.put(crel.getFragmentIndex(fid1), crel.getFragmentIndex(fid2), value);
		}
	}

	public Double getDissimilarity(Integer frag1, Integer frag2) {
		Double result = (Double)dissimmap.get(frag1, frag2);
		if (result == null)
			result = (Double)dissimmap.get(frag2, frag1);
		return result;
	}
}
