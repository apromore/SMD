package org.prom5.framework.models.petrinet.pattern;

import org.prom5.framework.models.petrinet.PetriNet;

public class WhileComponent extends Component {

	public WhileComponent(PetriNet wfnet) {
		super(wfnet);
	}

	@Override
	public Component cloneComponent() {
		return new WhileComponent(getWfnet());
	}

	@Override
	public String toString() {
		return "While";
	}

}
