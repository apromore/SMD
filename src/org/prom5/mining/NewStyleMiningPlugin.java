package org.prom5.mining;

import javax.swing.JPanel;

import org.prom5.framework.log.LogReader;

public interface NewStyleMiningPlugin {
	public MiningResult mine(LogReader log, JPanel optionsPanel);
}
