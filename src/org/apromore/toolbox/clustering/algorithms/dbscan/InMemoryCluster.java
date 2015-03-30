/**
 * 
 */
package org.apromore.toolbox.clustering.algorithms.dbscan;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chathura Ekanayake
 *
 */
public class InMemoryCluster {
	
	private String clusterId;
	private String phase;
	private List<FragmentDataObject> fragments = new ArrayList<FragmentDataObject>();
	
	public InMemoryCluster(String clusterId, String phase) {
		this.clusterId = clusterId;
		this.phase = phase;
	}

	public String getClusterId() {
		return clusterId;
	}
	
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	
	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public void addFragment(FragmentDataObject f) {
		f.addClusterId(clusterId);
		fragments.add(f);
	}
	
	public FragmentDataObject removeFirstFragment() {
		if (fragments != null && !fragments.isEmpty()) {
			FragmentDataObject f = fragments.remove(0);
			f.removeClusterId(clusterId);
			return f;
		} else {
			return null;
		}
	}
	
	public List<FragmentDataObject> getFragments() {
		return fragments;
	}
	
	public void setFragments(List<FragmentDataObject> fragments) {
		this.fragments = fragments;
		for (FragmentDataObject f : this.fragments) {
			f.addClusterId(clusterId);
		}
	}
	
	public boolean isEmpty() {
		return fragments == null || fragments.isEmpty();
	}
}
