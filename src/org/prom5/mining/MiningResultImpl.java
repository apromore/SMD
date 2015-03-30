package org.prom5.mining;

import javax.swing.JComponent;

import org.prom5.framework.log.LogReader;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.framework.plugin.Provider;

public class MiningResultImpl implements MiningResult, Provider {
	
	private LogReader log;
	private JComponent component;

	public MiningResultImpl(LogReader log, JComponent component) {
		this.log = log;
		this.component = component;
	}

	public LogReader getLogReader() {
		return log;
	}

	public JComponent getVisualization() {
		return component;
	}

	public ProvidedObject[] getProvidedObjects() {
		if (component != null && component instanceof Provider) {
			return ((Provider) component).getProvidedObjects();
		}
		return new ProvidedObject[0];
	}
}
