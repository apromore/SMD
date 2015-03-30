package org.prom5.analysis.socialsuccess.bigfive;

import org.prom5.analysis.socialsuccess.PersonalityData;
import org.prom5.analysis.socialsuccess.bigfive.openness.ExcellentIdeas;
import org.prom5.analysis.socialsuccess.bigfive.openness.FullOfIdeas;
import org.prom5.analysis.socialsuccess.bigfive.openness.HaveRichVocubalary;

public class Openness extends Trait {
	
	public Openness(PersonalityData ssd) {
		super(ssd);
	}

	@Override
	protected void loadBehaviour() {
		b[0] = new HaveRichVocubalary(this);
		b[1] = new FullOfIdeas(this);
		b[2] = new ExcellentIdeas(this); 
	}
}
