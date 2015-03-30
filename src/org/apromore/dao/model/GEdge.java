package org.apromore.dao.model;

public class GEdge {
	
	private int edgeId;
	private int sourceVId;
	private int targetVId;
	private String contentId;
	
	public int getEdgeId() {
		return edgeId;
	}
	
	public void setEdgeId(int edgeId) {
		this.edgeId = edgeId;
	}

	public int getSourceVId() {
		return sourceVId;
	}

	public void setSourceVId(int sourceVId) {
		this.sourceVId = sourceVId;
	}

	public int getTargetVId() {
		return targetVId;
	}

	public void setTargetVId(int targetVId) {
		this.targetVId = targetVId;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
}
