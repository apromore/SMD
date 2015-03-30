package org.apromore.dao.model;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.beans.factory.annotation.Configurable;

@Entity
@Table(name = "fs_geds")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Configurable("fs_geds")
public class GED implements Serializable {
	
	private GEDId gedId;
	private double ged;
	
	@EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "fid1", column = @Column(name = "fs_fid1", nullable = false, length = 40)),
            @AttributeOverride(name = "fid2", column = @Column(name = "fs_fid2", nullable = false, length = 40))})
	public GEDId getGedId() {
		return gedId;
	}

	public void setGedId(GEDId gedId) {
		this.gedId = gedId;
	}

	@Column(name = "fs_ged")
	public double getGed() {
		return ged;
	}

	public void setGed(double ged) {
		this.ged = ged;
	}
}
