package org.prom5.analysis.performance.fsmevaluator;

import javax.swing.JComponent;

import org.prom5.analysis.Analyzer;
import org.prom5.analysis.performance.fsmanalysis.FSMStatistics;

public class FSMEvaluationAnalysisPlugin {
	@Analyzer(name = "FSM Evaluator", names = {"Log", "FSM Statistics"})
	public JComponent analyze(FSMStatistics fsmStat) {
		return new FSMEvaluationMenuUI(fsmStat);
	}
}
