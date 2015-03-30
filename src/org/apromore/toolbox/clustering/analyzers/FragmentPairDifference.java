package org.apromore.toolbox.clustering.analyzers;

import java.util.List;

public class FragmentPairDifference {
	
	private String fid1;
	private String fid2;
	
	private List<String> f1UnmatchedNodes;
	private List<String> f2UnmatchedNodes;
	private List<String> exactlyMatchedNodes;
	private List<PartiallyMappedPair> pairtiallyMatchedNodes;

	public String getFid1() {
		return fid1;
	}

	public void setFid1(String fid1) {
		this.fid1 = fid1;
	}

	public String getFid2() {
		return fid2;
	}

	public void setFid2(String fid2) {
		this.fid2 = fid2;
	}

	public List<String> getF1UnmatchedNodes() {
		return f1UnmatchedNodes;
	}

	public void setF1UnmatchedNodes(List<String> f1UnmatchedNodes) {
		this.f1UnmatchedNodes = f1UnmatchedNodes;
	}

	public List<String> getF2UnmatchedNodes() {
		return f2UnmatchedNodes;
	}

	public void setF2UnmatchedNodes(List<String> f2UnmatchedNodes) {
		this.f2UnmatchedNodes = f2UnmatchedNodes;
	}

	public List<String> getExactlyMatchedNodes() {
		return exactlyMatchedNodes;
	}

	public void setExactlyMatchedNodes(List<String> exactlyMatchedNodes) {
		this.exactlyMatchedNodes = exactlyMatchedNodes;
	}

	public List<PartiallyMappedPair> getPairtiallyMatchedNodes() {
		return pairtiallyMatchedNodes;
	}

	public void setPairtiallyMatchedNodes(
			List<PartiallyMappedPair> pairtiallyMatchedNodes) {
		this.pairtiallyMatchedNodes = pairtiallyMatchedNodes;
	}
}
