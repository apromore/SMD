/**
 * 
 */
package org.apromore.dao.model;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Chathura C. Ekanayake
 *
 */
@Entity
@Table(name = "fs_cluster_assignments")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Configurable("fs_cluster_assignments")
public class ClusterAssignment implements Serializable {
	
	private ClusterAssignmentId id;
	private FragmentVersion fragment;
	
	@EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "fragmentId", column = @Column(name = "fs_fragment_version_id", nullable = false, length = 40)),
            @AttributeOverride(name = "clusterId", column = @Column(name = "fs_cluster_id", nullable = false, length = 40))})
    public ClusterAssignmentId getId() {
        return this.id;
    }
	
	public void setId(ClusterAssignmentId id) {
		this.id = id;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fs_fragment_version_id", nullable = false, insertable = false, updatable = false)
    public FragmentVersion getFragment() {
        return this.fragment;
    }

    public void setFragment(final FragmentVersion fragment) {
        this.fragment = fragment;
    }
}
