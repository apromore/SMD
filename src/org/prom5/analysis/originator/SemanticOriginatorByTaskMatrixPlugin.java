package org.prom5.analysis.originator;

import org.prom5.analysis.Analyzer;
import org.prom5.framework.log.LogReader;

public class SemanticOriginatorByTaskMatrixPlugin {
	@Analyzer(name = "Semantic Originator by Task Matrix", names = { "Log file" })
	public static OriginatorUI analyse(LogReader log) {
		return new OriginatorUI(log, new SemanticOTMatrix2DTableModel(log));
	}
}
