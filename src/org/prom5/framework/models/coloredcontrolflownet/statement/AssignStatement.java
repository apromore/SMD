package org.prom5.framework.models.coloredcontrolflownet.statement;

import org.prom5.framework.models.coloredcontrolflownet.expression.Expression;

public class AssignStatement implements Statement {

	public final String var;
	public final Expression expression;

	public AssignStatement(String var, Expression expression) {
		this.var = var;
		this.expression = expression;
	}

	@Override
	public String toString() {
		return "assign(\"" + var + "\"," + expression + ",confs)";
	}

}
