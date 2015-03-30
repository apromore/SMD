package org.prom5.mining;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;

import org.prom5.framework.log.LogReader;
import org.prom5.importing.LogReaderConnection;

public class MiningResultWithLogConnectionImpl extends MiningResultImpl implements LogReaderConnection {
	
	private LogReaderConnection connection;

	public MiningResultWithLogConnectionImpl(LogReader log, JComponent component, LogReaderConnection connection) {
		super(log, component);
		this.connection = connection;
	}

	public void connectWith(LogReader newLog, HashMap eventsMapping) {
		if (connection != null) {
			connection.connectWith(newLog, eventsMapping);
		}
	}

	public ArrayList getConnectableObjects() {
		return connection == null ? new ArrayList() : connection.getConnectableObjects();
	}
}
