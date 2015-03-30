/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.prom5.mining.instancemining;

import org.prom5.framework.log.ProcessInstance;
import org.prom5.framework.models.ModelGraph;

/**
 * @author Peter van den Brand
 * @version 1.0
 */
public interface InstanceNetBuilder {
	public ModelGraph getCompleteGraph();

	public ModelGraph build(ProcessInstance instance);
}
