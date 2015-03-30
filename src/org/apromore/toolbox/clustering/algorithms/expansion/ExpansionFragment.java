package org.apromore.toolbox.clustering.algorithms.expansion;

public class ExpansionFragment {
	
	private String fragmentId;
	private int fragmentSize;
	private double sizeRatio;
	private int numClusters;
	
	private String memberFragmentId;
	private double distanceToMemberFragment;
	
	private String farthestMemberFragment;
	private String distanceToFarthestMemberFragment;
	
	public String getFragmentId() {
		return fragmentId;
	}
	
	public void setFragmentId(String fragmentId) {
		this.fragmentId = fragmentId;
	}
	
	public int getFragmentSize() {
		return fragmentSize;
	}

	public void setFragmentSize(int fragmentSize) {
		this.fragmentSize = fragmentSize;
	}
	
	public double getSizeRatio() {
		return sizeRatio;
	}

	public void setSizeRatio(double sizeRatio) {
		this.sizeRatio = sizeRatio;
	}

	public int getNumClusters() {
		return numClusters;
	}

	public void setNumClusters(int numClusters) {
		this.numClusters = numClusters;
	}

	public String getMemberFragmentId() {
		return memberFragmentId;
	}

	public void setMemberFragmentId(String memberFragmentId) {
		this.memberFragmentId = memberFragmentId;
	}

	public double getDistanceToMemberFragment() {
		return distanceToMemberFragment;
	}

	public void setDistanceToMemberFragment(double distanceToMemberFragment) {
		this.distanceToMemberFragment = distanceToMemberFragment;
	}

	public String getFarthestMemberFragment() {
		return farthestMemberFragment;
	}

	public void setFarthestMemberFragment(String farthestMemberFragment) {
		this.farthestMemberFragment = farthestMemberFragment;
	}

	public String getDistanceToFarthestMemberFragment() {
		return distanceToFarthestMemberFragment;
	}

	public void setDistanceToFarthestMemberFragment(
			String distanceToFarthestMemberFragment) {
		this.distanceToFarthestMemberFragment = distanceToFarthestMemberFragment;
	}

	@Override
	public int hashCode() {
		return fragmentId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		ExpansionFragment ef = (ExpansionFragment) obj;
		return fragmentId.equals(ef.getFragmentId());
	}
}
