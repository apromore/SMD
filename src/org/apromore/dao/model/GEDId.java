package org.apromore.dao.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class GEDId implements Serializable {
	
	private String fid1;
	private String fid2;
	
	@Column(name = "fs_fid1", nullable = false, length = 40)
	public String getFid1() {
		return fid1;
	}
	
	public void setFid1(String fid1) {
		this.fid1 = fid1;
	}
	
	@Column(name = "fs_fid2", nullable = false, length = 40)
	public String getFid2() {
		return fid2;
	}

	public void setFid2(String fid2) {
		this.fid2 = fid2;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj == null || !(obj instanceof GEDId)) {
			return false;
		}
		
		GEDId otherId = (GEDId) obj;
		if (fid1.equals(otherId.getFid1()) && fid2.equals(otherId.getFid2())) {
			return true;
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return fid1.hashCode() + fid2.hashCode();
	}
}
