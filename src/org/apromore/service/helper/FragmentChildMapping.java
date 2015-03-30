package org.apromore.service.helper;

import org.apromore.dao.model.FragmentVersionDag;
import org.apromore.dao.model.FragmentVersionDagId;

import java.util.List;

/**
 * @author Chathura Ekanayake
 */
public class FragmentChildMapping {

	private String fragmentId;
	
	//private Map<String, String> childMapping;
    private List<FragmentVersionDagId> childMapping;

	public String getFragmentId() {
		return fragmentId;
	}

	public void setFragmentId(String fragmentId) {
		this.fragmentId = fragmentId;
	}

	public List<FragmentVersionDagId>  getChildMapping() {
		return childMapping;
	}

	public void setChildMapping(List<FragmentVersionDagId> childMapping) {
		this.childMapping = childMapping;
	}
}
