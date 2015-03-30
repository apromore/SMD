package org.prom5.converting.wfnet2bpel.pattern;

import org.prom5.framework.models.petrinet.PetriNet;
import org.prom5.framework.models.petrinet.pattern.ChoiceComponent;
import org.prom5.framework.models.petrinet.pattern.Component;

public class PickComponent extends ChoiceComponent {

	public PickComponent(PetriNet wfnet) {
		super(wfnet);
	}
	
	/**
	 * @see org.prom5.framework.models.petrinet.pattern.Component#toString()
	 */
	@Override
	public String toString() {
		return "Pick";
	}

	/**
	 * @see org.prom5.framework.models.petrinet.pattern.ChoiceComponent#cloneComponent()
	 */
	@Override
	public Component cloneComponent() {
		return new PickComponent(getWfnet());
	}

}
