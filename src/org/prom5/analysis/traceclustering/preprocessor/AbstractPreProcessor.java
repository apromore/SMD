package org.prom5.analysis.traceclustering.preprocessor;

import org.prom5.analysis.traceclustering.profile.AbstractProfile;
import org.prom5.analysis.traceclustering.profile.AggregateProfile;
import org.prom5.framework.log.LogReader;

public abstract class AbstractPreProcessor extends AbstractProfile {

	public AbstractPreProcessor(String aName, String aDescription, LogReader aLog) {
		super(aName, aDescription,aLog);
	}

	public abstract void buildProfile(AggregateProfile aggregateProfile);
	
	public abstract void buildProfile(AggregateProfile aggregateProfile, int dim);
}
