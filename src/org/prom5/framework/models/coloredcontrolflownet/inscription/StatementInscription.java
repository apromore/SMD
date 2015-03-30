package org.prom5.framework.models.coloredcontrolflownet.inscription;

import org.prom5.framework.models.coloredcontrolflownet.statement.Statement;

public class StatementInscription implements ArcInscription {

	public final Statement statement;

	public StatementInscription(Statement statement) {
		this.statement = statement;
	}

	@Override
	public String toString() {
		return "(pid," + statement + ")";
	}

}
