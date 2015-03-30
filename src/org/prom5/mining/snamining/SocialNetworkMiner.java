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

package org.prom5.mining.snamining;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.prom5.analysis.originator.OTMatrix2DTableModel;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.LogSummary;
import org.prom5.framework.models.orgmodel.algorithms.OmmlReader;
import org.prom5.framework.ui.Message;
import org.prom5.framework.ui.Progress;
import org.prom5.mining.MiningPlugin;
import org.prom5.mining.MiningResult;
import org.prom5.mining.snamining.miningoperation.BasicOperation;
import org.prom5.mining.snamining.miningoperation.OperationFactory;
import org.prom5.mining.snamining.model.OriginatorsModel;
import org.prom5.mining.snamining.model.SocialNetworkMatrix;

/**
 * @author Minseok
 * @version 1.0
 */

public class SocialNetworkMiner implements MiningPlugin {

        private SocialNetworkOptions ui = null;
		private BasicOperation baseOprtation = null;
		private OriginatorsModel originatorsModel = null;

        public SocialNetworkMiner() {
        }

        public String getName() {
                return "Social network miner";
        }

        public String getHtmlDescription()
        {
			return "http://prom.win.tue.nl/research/wiki/snminer";
        }

        public JPanel getOptionsPanel(LogSummary summary) {
                if (ui == null) {
					    originatorsModel = new OriginatorsModel(summary);
                        ui = new SocialNetworkOptions(originatorsModel);
                }
                return ui;
        }

        public MiningResult mine(LogReader log) {

                LogSummary summary = log.getLogSummary();

                if (summary.getOriginators().length == 0) {
                        JOptionPane.showMessageDialog(null,
                                        "This log file does not have originator information, so it cannot be mined.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                        return null;
                }

				int indexType = 0;

				switch (ui.getPanelTabbed().getSelectedMetrics()) {
					case SocialNetworkOptions.WORKING_TOGETHER:
						indexType = SocialNetworkOptions.WORKING_TOGETHER +
									ui.getPanelTabbed().getWorkingTogetherSetting();
						break;
					case SocialNetworkOptions.SIMILAR_TASK:
						indexType = SocialNetworkOptions.SIMILAR_TASK + ui.getPanelTabbed().getSimilarTaskSetting();
						break;
					case SocialNetworkOptions.REASSIGNMENT:
						indexType = SocialNetworkOptions.REASSIGNMENT +
									ui.getPanelTabbed().getReassignmentSetting();
						break;
					default: indexType = ui.getPanelTabbed().getSelectedMetrics() + ((ui.getPanelTabbed().getConsiderCausality()==true)?1:0) * 100 + ((ui.getPanelTabbed().getOnlyDirectSuccession()==true)?1:0)*10 +((ui.getPanelTabbed().getConsiderMultipleTransfers()==true)?1:0);
				}

	            baseOprtation = OperationFactory.getOperation(indexType, summary, log);

	            if (baseOprtation!=null)
					originatorsModel.setMatrix(baseOprtation.calculation(ui.getPanelTabbed().getBeta(), ui.getPanelTabbed().getDepthOfCalculation()));

				if(originatorsModel.getMatrix() == null) {
                        JOptionPane.showMessageDialog(null,
                                        "This type of social network mining is not implemented yet.");
                        return null;
                }

				// to make OTMatrix
				OTMatrix2DTableModel otMatrix;
				otMatrix = new OTMatrix2DTableModel(log);

				SocialNetworkMatrix snMatrix = new SocialNetworkMatrix(originatorsModel.getSelectedOriginators(),originatorsModel.filterResultMatrix());
				snMatrix.setOTMatrix(otMatrix.getFilteredOTMatrix(originatorsModel.getSelectedOriginators()));

				if (ui.getPanelOriginators().isAssignOrgModelChecked()) {
					originatorsModel.setOrgModel(OmmlReader.read(ui.getPanelOriginators().getOrgModelFileName()));
					snMatrix.setOrgUnitMatrix(originatorsModel.getOrgUnitAssignmentMatrix());
					snMatrix.setRoleMatrix(originatorsModel.getRoleAssignmentMatrix());
					snMatrix.setOrgUnitName(originatorsModel.getOrgUnitList());
					snMatrix.setRoleName(originatorsModel.getRoleList());
				}

				return new SocialNetworkResults(log, snMatrix, otMatrix);
        }

         private void message(String msg, int stage, Progress progress) {
                Message.add(msg, Message.DEBUG);
                if (progress != null) {
                        progress.setNote(msg);
                        progress.setProgress(stage);
                }
	     }
}
