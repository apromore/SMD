package org.prom5.framework.models.coloredcontrolflownet.inscription;

import java.util.List;

import org.prom5.framework.models.coloredcontrolflownet.statement.AssignStatement;

public class InitialConfigurationInscription implements ArcInscription {

	public final List<AssignStatement> assignments;

	public InitialConfigurationInscription(List<AssignStatement> assignments) {
		this.assignments = assignments;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("(pid,[");
		for (AssignStatement as : assignments) {
			builder.append("(\"" + as.var + "\"," + as.expression + ")");
		}
		builder.append("])");
		return builder.toString();
	}

}
