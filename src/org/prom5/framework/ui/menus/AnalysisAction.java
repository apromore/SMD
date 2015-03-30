package org.prom5.framework.ui.menus;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;

import org.prom5.analysis.AnalysisInputItem;
import org.prom5.analysis.AnalysisPlugin;
import org.prom5.framework.log.LogStateMachine;
import org.prom5.framework.plugin.DoNotCreateNewInstance;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.framework.plugin.Provider;
import org.prom5.framework.ui.MainUI;
import org.prom5.framework.ui.Message;
import org.prom5.framework.ui.SwingWorker;
import org.prom5.framework.ui.UISettings;
import org.prom5.framework.util.RuntimeUtils;

public class AnalysisAction extends AbstractAction {

	private static final long serialVersionUID = -1681699131525292889L;
	
	private AnalysisPlugin algorithm;
	private ProvidedObject object;

	public AnalysisAction(AnalysisPlugin algorithm, ProvidedObject object) {
		super(RuntimeUtils.stripHtmlForOsx("<html>" +
				algorithm.getName() +
				"</html>"));
		this.algorithm = algorithm;
		this.object = object;
	}
	
	public AnalysisPlugin getPlugin() {
		return algorithm;
	}
	
	public String toString() {
		return this.algorithm.getName();
	}

	public void actionPerformed(ActionEvent e) {
		final AnalysisInputItem[] input = algorithm.getInputItems();

		input[0].setProvidedObjects(new ProvidedObject[] {object});

		MainUI.getInstance().addAction(algorithm, LogStateMachine.START, new Object[] {object});
		UISettings.getInstance().setLastUsedAnalysis(algorithm.getName());

		SwingWorker w = new SwingWorker() {
			private JComponent result;
			public Object construct() {

				try {
					if (algorithm instanceof DoNotCreateNewInstance) {
						result = algorithm.analyse(input);
					} else {
						result = ((AnalysisPlugin) algorithm.getClass().newInstance()).analyse(input);
					}
				} catch (IllegalAccessException ex) {
					Message.add("No new instantiation of " + algorithm.getName() +
							" could be made, using" +
							" old instance instead", Message.ERROR);
					result = algorithm.analyse(input);
				} catch (InstantiationException ex) {
					Message.add("No new instantiation of " + algorithm.getName() +
							" could be made, using" +
							" old instance instead", Message.ERROR);
					result = algorithm.analyse(input);
				}
				return null;
			}

			public void finished() {
				MainUI.getInstance().addAction(algorithm, LogStateMachine.COMPLETE,
						(result instanceof Provider) ? ((Provider) result).getProvidedObjects() : null);

				MainUI.getInstance().createAnalysisResultFrame(algorithm, input, result);
			}
		};
		w.start();
	}
}
