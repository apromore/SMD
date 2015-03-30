package org.prom5.analysis.socialsuccess.bigfive;

import org.prom5.analysis.socialsuccess.PersonalityData;
import org.prom5.analysis.socialsuccess.bigfive.conscientiousness.OrderAndRegularity;
import org.prom5.analysis.socialsuccess.bigfive.conscientiousness.PayAttention;
import org.prom5.analysis.socialsuccess.bigfive.conscientiousness.TidyUp;

public class Conscientiousness extends Trait {

	public Conscientiousness(PersonalityData inp) {
		super(inp);
	}

	@Override
	protected void loadBehaviour() {
		b[0] = new OrderAndRegularity(this);
		b[1] = new PayAttention(this);
		b[2] = new TidyUp(this);
	}

}
