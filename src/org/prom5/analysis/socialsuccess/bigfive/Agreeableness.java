/**
 * 
 */
package org.prom5.analysis.socialsuccess.bigfive;

import org.prom5.analysis.socialsuccess.PersonalityData;
import org.prom5.analysis.socialsuccess.bigfive.agreeableness.InterestedInOthers;
import org.prom5.analysis.socialsuccess.bigfive.agreeableness.OnGoodTerms;
import org.prom5.analysis.socialsuccess.bigfive.agreeableness.ShowGratitude;

/**
 * @author MvanWingerden
 *
 */
public class Agreeableness extends Trait {

	public Agreeableness(PersonalityData inp) {
		super(inp);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void loadBehaviour() {
		b[0] = new InterestedInOthers(this);
		b[1] = new OnGoodTerms(this);
		b[2] = new ShowGratitude(this);
	}

}
