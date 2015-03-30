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

package org.prom5.importing.heuristicsnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.prom5.framework.log.LogEvent;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.importing.LogReaderConnection;
import org.prom5.mining.heuristicsmining.HeuristicsNetResult;

/**
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class HeuristicsNetResultWithLogReader extends HeuristicsNetResult implements
		LogReaderConnection {

	public HeuristicsNetResultWithLogReader(HeuristicsNet net, LogReader log) {
		super(net, log, false);
	}

	public ArrayList getConnectableObjects() {
		return net.getLogEvents();
	}

	public void connectWith(LogReader newLog, HashMap eventsMapping) {
		log = newLog;
		if (eventsMapping != null) {
			Iterator it = net.getLogEvents().iterator();

			while (it.hasNext()) {
				LogEvent t = (LogEvent) it.next();
				Object[] mapped = (Object[]) eventsMapping.get(t);

				t = ((LogEvent) mapped[0]);
				// For now, nothing is done with the label included in the mapping...
				// String label = (String) mapped[1];

				//t = (LogEvent) eventsMapping.get(t);
			}
		}
	}
}
