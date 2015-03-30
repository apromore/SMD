package org.prom5.analysis.sltl;

import java.util.List;

import org.prom5.analysis.AnalysisInputItem;
import org.prom5.analysis.ltlchecker.LTLChecker;
import org.prom5.analysis.ltlchecker.ParamData;
import org.prom5.analysis.ltlchecker.ParamTable;
import org.prom5.analysis.ltlchecker.TemplateGui;
import org.prom5.analysis.ltlchecker.parser.FormulaParameter;
import org.prom5.analysis.ltlchecker.parser.LTLParser;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.ontology.OntologyCollection;

public class SLTLTemplateGui extends TemplateGui {

	private static final long serialVersionUID = -507559432832372820L;
	private OntologyCollection semanticLog;
	
	public SLTLTemplateGui(OntologyCollection semanticLog, LogReader log, LTLParser parser, LTLChecker checker, AnalysisInputItem[] inputs) {
		super();
		this.semanticLog = semanticLog;
		init(log, parser, checker, inputs);
	}

	@Override
    protected ParamData createDataModel(List<FormulaParameter> items) {
    	return new ParamData(items, semanticLog);
    }

    protected ParamTable createParamTable() {
		return new SemanticParamTable();
	}
    
	protected int getParamPaneHeight() {
		return 150;
	}
}
