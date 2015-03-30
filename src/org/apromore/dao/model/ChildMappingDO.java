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
@Table(name = "fragment_version_dag")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Configurable("childMappingDO")
public class ChildMappingDO implements Serializable {
	
	private FragmentVersionDagId id;

    public ChildMappingDO(FragmentVersionDagId id) {
    	this.id = id;
    }

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "fragmentVersionId", column = @Column(name = "fragment_version_id", nullable = false, length = 40)),
            @AttributeOverride(name = "childFragmentVersionId", column = @Column(name = "child_fragment_version_id", nullable = false, length = 40)),
            @AttributeOverride(name = "pocketId", column = @Column(name = "pocket_id", nullable = false, length = 40))})
    public FragmentVersionDagId getId() {
        return this.id;
    }

    public void setId(final FragmentVersionDagId newId) {
        this.id = newId;
    }
}
