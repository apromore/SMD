
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
package org.prom5.analysis.heuristics;

import org.prom5.analysis.AnalysisInputItem;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.framework.plugin.ProvidedObject;

/**
 * <p>Accepts objects that containd a <code>HeuristicsNet<code> and a <code>LogReader<code>.</p>
 *
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class HeuristicsNetLogInputItem extends AnalysisInputItem {

	public HeuristicsNetLogInputItem(String label) {
		super(label);
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();
		boolean hasLog = false, hasNet = false;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				hasLog = true;
			}
			if (o[i] instanceof HeuristicsNet) {
				hasNet = true;
			}
		}
		return hasLog && hasNet;
	}
}
