/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.prom5.framework.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JInternalFrame;

import org.prom5.framework.log.LogReader;
import org.prom5.framework.plugin.DoNotCreateNewInstance;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.framework.ui.MainUI;
import org.prom5.framework.ui.Message;
import org.prom5.framework.ui.MiningSettings;
import org.prom5.framework.ui.SwingWorker;
import org.prom5.framework.ui.WaitDialog;
import org.prom5.framework.util.RuntimeUtils;
import org.prom5.mining.MiningPlugin;

/**
 * @author christian
 *
 */
public class MineAction extends AbstractAction {

	private static final long serialVersionUID = -6646466342638716990L;
	
	private MiningPlugin algorithm;
	private LogReader log;
	private String name;

	public MineAction(MiningPlugin algorithm, ProvidedObject object) {
		super(RuntimeUtils.stripHtmlForOsx("<html>"
				/* + object.getName() + " using<br>&nbsp;&nbsp;&nbsp;"*/
				+
				algorithm.getName() +
				"</html>"));
		this.name = object.getName();
		this.algorithm = algorithm;
		for (int k = 0; k < object.getObjects().length; k++) {
			if (object.getObjects()[k] instanceof LogReader) {
				log = (LogReader) object.getObjects()[k];
				k = object.getObjects().length;
			}
		}
	}
	
	public String toString() {
		return this.algorithm.getName();
	}
	
	public MiningPlugin getPlugin() {
		return algorithm;
	}

	public void actionPerformed(ActionEvent e) {
		// copy and filter log in asynchronous swing worker thread
		SwingWorker worker = new SwingWorker() {

			protected JInternalFrame frame = null;
			protected WaitDialog dialog = null;

			public Object construct() {
				// show wait teaser dialog
				dialog = new WaitDialog(MainUI.getInstance(), "Filtering log...",
						 "Please wait, \nfiltering the log...");
				dialog.setVisible(true);
				try {
					if (algorithm instanceof DoNotCreateNewInstance) {
						frame = new MiningSettings(log, name, algorithm);
					} else {
						frame = new MiningSettings(log, name, (MiningPlugin) algorithm.getClass().newInstance());
					}
				} catch (IllegalAccessException ex) {
					Message.add("No new instantiation of " + algorithm.getName() +
							" could be made, using" +
							" old instance instead", Message.ERROR);
					frame = new MiningSettings(log, name, algorithm);
				} catch (InstantiationException ex) {
					Message.add("No new instantiation of " + algorithm.getName() +
							" could be made, using" +
							" old instance instead", Message.ERROR);
					frame = new MiningSettings(log, name, algorithm);
				}
				return null;
			}

			public void finished() {
				// close dialog
				dialog.setVisible(false);
				if (frame.isVisible()) {
					MainUI.getInstance().getDesktop().add(frame, algorithm);
				}
			}

		};
		worker.start();
	}
}
