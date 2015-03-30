package org.prom5.analysis.socialsuccess;

import javax.swing.JComponent;

import org.prom5.analysis.Analyzer;
import org.prom5.analysis.socialsuccess.ui.UIPersonalityAnalyzer;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.orgmodel.OrgModel;
import org.prom5.framework.ui.Message;

public class PersonalityAnalyzerPlugin {
	@Analyzer(name = "PAN (Personality Analyzer)", names = { "Log", "Log", "Log", "Log", "Org Model" }, connected = false)
	public static JComponent analyze(LogReader log, LogReader log1, LogReader log2, LogReader log3, OrgModel model) {
		Message.add("Personality Analyzer Plugin started");
		PersonalityData data = new PersonalityData(log, log1, log2, log3, model);
		Message.add("SSA: opening GUI");
		return new UIPersonalityAnalyzer(data);
	}

}
