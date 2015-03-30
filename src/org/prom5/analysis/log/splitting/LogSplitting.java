package org.prom5.analysis.log.splitting;

import javax.swing.JComponent;

import org.prom5.analysis.AnalysisInputItem;
import org.prom5.analysis.AnalysisPlugin;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.framework.plugin.ProvidedObject;

public class LogSplitting implements AnalysisPlugin {

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		HeuristicsNet net = null;
		LogReader log = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
				break;
			} 
		}

		return new LogSplittingUI(log);
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = {
				new AnalysisInputItem("Event Log") {
					public boolean accepts(ProvidedObject object) {
						Object[] o = object.getObjects();
						boolean hasLog = false;

						for (int i = 0; i < o.length; i++) {
							if (o[i] instanceof LogReader) {
								hasLog = true;
							}
						}
						return hasLog;
					}
				}
		} ;
		return items;

	}

	public String getHtmlDescription() {
		return "<p> <b>Plug-in: Log Splitting </b><p>This plug-in splits "+
		"each process instance in the input log into multiple (potentially much smaller) " +
		"new process instances. The splitting is based on the input parameters.";
	}

	public String getName() {
		return "Log Splitting";
	}

}
