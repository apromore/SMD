package org.apromore.service.model;

import java.util.List;

public class FragmentAssociation {
	
	private String fragmentId;
	
	private List<String> processNames;
	
	private int fragmentSize;

	public String getFragmentId() {
		return fragmentId;
	}

	public void setFragmentId(String fragmentId) {
		this.fragmentId = fragmentId;
	}

	public List<String> getProcessNames() {
		return processNames;
	}

	public void setProcessNames(List<String> processNames) {
		this.processNames = processNames;
	}

	public int getFragmentSize() {
		return fragmentSize;
	}

	public void setFragmentSize(int fragmentSize) {
		this.fragmentSize = fragmentSize;
	}
}
