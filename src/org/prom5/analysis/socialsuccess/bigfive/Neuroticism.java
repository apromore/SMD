/**
 * 
 */
package org.prom5.analysis.socialsuccess.bigfive;

import org.prom5.analysis.socialsuccess.PersonalityData;
import org.prom5.analysis.socialsuccess.bigfive.neuroticism.IritatedEasily;
import org.prom5.analysis.socialsuccess.bigfive.neuroticism.OverwhelmedByEmotions;

/**
 * @author MvanWingerden
 *
 */
public class Neuroticism extends Trait {

	/**
	 * @param inp
	 */
	public Neuroticism(PersonalityData inp) {
		super(inp);
		// TODO Auto-generated constructor stub
	}

	/** (non-Javadoc)
	 * @see org.prom5.analysis.socialsuccess.bigfive.Trait#loadBehaviour()
	 */
	@Override
	protected void loadBehaviour() {
		b[0] = new IritatedEasily(this);
		b[1] = new OverwhelmedByEmotions(this);
	}

}
