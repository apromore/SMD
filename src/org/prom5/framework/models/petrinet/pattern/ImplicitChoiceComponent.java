package org.prom5.framework.models.petrinet.pattern;

import org.prom5.framework.models.petrinet.PetriNet;

public class ImplicitChoiceComponent extends ChoiceComponent {

	public ImplicitChoiceComponent(PetriNet wfnet) {
		super(wfnet);
	}
	
	@Override
	public Component cloneComponent() {
		return new ImplicitChoiceComponent(getWfnet());
	}

	@Override
	public String toString() {
		return "Implicit choice";
	}

}
