package org.prom5.exporting.fsm;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.prom5.framework.ui.MainUI;
import org.prom5.framework.util.CenterOnScreen;

/**
 * <p>Title: LogExportUI</p>
 *
 * <p>Description: Dialog for Log Export</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: TU/e</p>
 *
 * @author Eric Verbeek
 * @version 1.0
 *
 * Code rating: Red
 *
 * Review rating: Red
 */
public class LogExportUI extends JDialog implements ActionListener {
	LogExportParameters parameters;
	JSpinner spinner;
	JButton button;

	public LogExportUI(LogExportParameters parameters) {
		super(MainUI.getInstance(), "Select the number of process instances...", true);
		this.parameters = parameters;
		try {
			setUndecorated(false);
			jbInit();
			pack();
			CenterOnScreen.center(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		panel.setLayout(layout);
		panel.setPreferredSize(new Dimension(300, 20));

		spinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		layout.setConstraints(spinner, constraints);
		panel.add(spinner);

		button = new JButton("Done");
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 1;
		constraints.gridy = 0;
		layout.setConstraints(button, constraints);
		panel.add(button);
		button.addActionListener(this);

		this.add(panel);
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == button) {
			String value = spinner.getValue().toString();
			parameters.nofInstances = Integer.valueOf(value);
			dispose();
		}
	}
}
