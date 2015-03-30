package org.apromore.mining.standardize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StandardizedCluster {

	private String clusterId;
	
	/**
	 * All fragments in the cluster.
	 */
	private List<String> fragmentIds;

	/**
	 * Fragments that will be removed from the repository due to the standardization.
	 */
	private Collection<String> removedFragmentIds;
	
	/**
	 * Fragments which will be simplified below the complexity thresholds.
	 */
	private List<String> standardFragmentIds = new ArrayList<String>();

	/**
	 * Fragment to be used for replacing all occurrences of other fragments in this cluster.
	 */
	private String representativeFragmentId = null;
	
	private int repFragmentSize = 0;
	
	public StandardizedCluster(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	
	public String getRepresentativeFragmentId() {
		return representativeFragmentId;
	}

	public void setRepresentativeFragmentId(String representativeFragmentIds) {
		this.representativeFragmentId = representativeFragmentIds;
	}
	
	public int getRepFragmentSize() {
		return repFragmentSize;
	}

	public void setRepFragmentSize(int repFragmentSize) {
		this.repFragmentSize = repFragmentSize;
	}

	public List<String> getFragmentIds() {
		return fragmentIds;
	}

	public void setFragmentIds(List<String> fragmentIds) {
		this.fragmentIds = fragmentIds;
	}

	public List<String> getStandardFragmentIds() {
		return standardFragmentIds;
	}

	public void setStandardFragmentIds(List<String> standardFragmentIds) {
		this.standardFragmentIds = standardFragmentIds;
	}
	
	public void addStandardFragment(String fid) {
		this.standardFragmentIds.add(fid);
	}

	public Collection<String> getRemovedFragmentIds() {
		return removedFragmentIds;
	}

	public void setRemovedFragmentIds(Collection<String> removedFragmentIds) {
		this.removedFragmentIds = removedFragmentIds;
	}
}
