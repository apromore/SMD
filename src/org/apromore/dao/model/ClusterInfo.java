/**
 * 
 */
package org.apromore.dao.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Chathura C. Ekanayake
 *
 */
@Entity
@Table(name = "fs_clusters")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Configurable("fs_clusters")
public class ClusterInfo implements Serializable {
	
	private static final long serialVersionUID = -2353656404638485586L;
	
	private String clusterId;
	private int size = 0;
	private float avgFragmentSize = 0;
	private String medoidId = null;
	private double standardizingEffort = 0;
	private double BCR = 0;
	private int refactoringGain = 0;
	
	public ClusterInfo() {}
	
	public ClusterInfo(String clusterId, int size, float avgFragmentSize, String medoidId, double BCR) {
		this.clusterId = clusterId;
		this.size = size;
		this.avgFragmentSize = avgFragmentSize;
		this.medoidId = medoidId;
		this.BCR = BCR;
	}

	@Id
	@Column(name = "fs_cluster_id")
	public String getClusterId() {
		return clusterId;
	}
	
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	
	@Column(name = "fs_size")
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	@Column(name = "fs_avg_fragment_size")
	public float getAvgFragmentSize() {
		return avgFragmentSize;
	}
	
	public void setAvgFragmentSize(float avgFragmentSize) {
		this.avgFragmentSize = avgFragmentSize;
	}
	
	@Column(name = "fs_medoid_id")
	public String getMedoidId() {
		return medoidId;
	}
	
	public void setMedoidId(String medoidId) {
		this.medoidId = medoidId;
	}
	
	@Column(name = "fs_benifit_cost_ratio")
	public double getBCR() {
		return BCR;
	}
	
	public void setBCR(double BCR) {
		this.BCR = BCR;
	}
	
	@Column(name = "fs_std_effort")
	public double getStandardizingEffort() {
		return standardizingEffort;
	}

	public void setStandardizingEffort(double standardizingEffort) {
		this.standardizingEffort = standardizingEffort;
	}

	@Column(name = "fs_refactoring_gain")
	public int getRefactoringGain() {
		return refactoringGain;
	}

	public void setRefactoringGain(int refactoringGain) {
		this.refactoringGain = refactoringGain;
	}

	@Override
	public String toString() {
		String s = clusterId + " | " + size + " | " + avgFragmentSize + " | " + BCR;
		return s;
	}
}
