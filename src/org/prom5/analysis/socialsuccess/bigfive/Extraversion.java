package org.prom5.analysis.socialsuccess.bigfive;

import org.prom5.analysis.socialsuccess.PersonalityData;
import org.prom5.analysis.socialsuccess.bigfive.extraversion.ModeratingGroups;
import org.prom5.analysis.socialsuccess.bigfive.extraversion.PrivatePerson;
import org.prom5.analysis.socialsuccess.bigfive.extraversion.StartConversations;

public class Extraversion extends Trait {

	public Extraversion(PersonalityData inp) {
		super(inp);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void loadBehaviour() {
		b[0] = new ModeratingGroups(this);
		b[1] = new PrivatePerson(this);
		b[2] = new StartConversations(this);
	}

}
