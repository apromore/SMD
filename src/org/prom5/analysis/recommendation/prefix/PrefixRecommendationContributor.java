package org.prom5.analysis.recommendation.prefix;

import org.prom5.analysis.recommendation.contrib.LogBasedContributor;
import org.prom5.framework.models.logabstraction.PrefixAbstractionFactory;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class PrefixRecommendationContributor extends LogBasedContributor {

	public PrefixRecommendationContributor(){
		super();
		logAbstractionFactory = new PrefixAbstractionFactory();
	};

	public String getName() {
		return "Prefix Abstraction";
	}

	public String getHtmlDescription() {
		return "Calculate the prefix abstraction of a process instance";
	}
}
