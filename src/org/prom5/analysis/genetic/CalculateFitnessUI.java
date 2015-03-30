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

package org.prom5.analysis.genetic;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.framework.plugin.Provider;
import org.prom5.framework.ui.MainUI;
import org.prom5.framework.ui.Message;
import org.prom5.framework.util.ToolTipComboBox;
import org.prom5.mining.geneticmining.fitness.Fitness;
import org.prom5.mining.geneticmining.fitness.FitnessFactory;
import org.prom5.mining.geneticmining.fitness.duplicates.DTFitnessFactory;
import org.prom5.mining.heuristicsmining.HeuristicsNetResult;

public class CalculateFitnessUI extends JPanel implements Provider {

	private HeuristicsNet net = null;
	private LogReader log = null;

	private HeuristicsNetResult netResult = null;
	private JPanel fitnessSpace = null;

	private ToolTipComboBox fitnessType = null;
	private JButton calculate = null;

	CalculateFitnessUI panelObject = null;

	public CalculateFitnessUI(LogReader log, HeuristicsNet net) {
		this.net = net;
		this.log = log;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void jbInit() throws Exception {

		JLabel ftLabel = new JLabel("Type of Fitness ");
		netResult = new HeuristicsNetResult(net, log);
		fitnessSpace = new JPanel();

		fitnessType = new ToolTipComboBox(FitnessFactory.getAllFitnessTypes());
		calculate = new JButton("Calculate");
		fitnessSpace.add(ftLabel);
		fitnessSpace.add(fitnessType);
		fitnessSpace.add(calculate);

		panelObject = this;

		calculate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Fitness fitness = FitnessFactory.getFitness(fitnessType.getSelectedIndex(), log,
								  DTFitnessFactory.ALL_FITNESS_PARAMETERS);
				HeuristicsNet[] result = fitness.calculate(new
										 HeuristicsNet[] {net});

				JOptionPane.showMessageDialog(MainUI.getInstance(),
						"The fitness  for this individual is " + result[0].getFitness(),
						"Fitness - " +
						FitnessFactory.getAllFitnessTypes()[fitnessType.getSelectedIndex()],
						JOptionPane.INFORMATION_MESSAGE);
				Message.add("<Fitness " +
						FitnessFactory.getAllFitnessTypes()[fitnessType.getSelectedIndex()] + "=\"" +
						result[0].getFitness() + "\">", Message.TEST);

				panelObject.removeAll();
				netResult = new HeuristicsNetResult(net, log, netResult.getShowSplitJoinSemantics());
				panelObject.add(netResult.getVisualization(), BorderLayout.CENTER);
				panelObject.add(fitnessSpace, BorderLayout.SOUTH);
				panelObject.repaint();
				panelObject.validate();
			}
		});

		this.setLayout(new BorderLayout());
		this.add(netResult.getVisualization(), BorderLayout.CENTER);
		this.add(fitnessSpace, BorderLayout.SOUTH);

	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[] {new ProvidedObject("Heuristics Net",
								   new Object[] {net, log})};
		return objects;
	}

}
