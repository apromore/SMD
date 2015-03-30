/**
 * 
 */
package org.apromore.service.model;

import java.util.ArrayList;
import java.util.List;

import org.apromore.dao.model.ClusterInfo;

/**
 * @author Chathura C. Ekanayake
 *
 */
public class Cluster {
	
	private ClusterInfo clusterInfo;
	private List<MemberFragment> fragments = new ArrayList<MemberFragment>();
	
	public ClusterInfo getClusterInfo() {
		return clusterInfo;
	}
	
	public void setClusterInfo(ClusterInfo clusterInfo) {
		this.clusterInfo = clusterInfo;
	}
	
	public void addFragment(MemberFragment fragment) {
		fragments.add(fragment);
	}
	
	public List<MemberFragment> getFragments() {
		return fragments;
	}
	
	public void setFragments(List<MemberFragment> fragments) {
		this.fragments = fragments;
	}
}
