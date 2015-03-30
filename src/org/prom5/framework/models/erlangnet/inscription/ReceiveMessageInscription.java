package org.prom5.framework.models.erlangnet.inscription;

import org.prom5.framework.models.erlangnet.expression.Expression;

public class ReceiveMessageInscription implements ArcInscription {

	public final Expression expression;

	public ReceiveMessageInscription(Expression expression) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		return "(sender,pid," + expression + ")";
	}

}
