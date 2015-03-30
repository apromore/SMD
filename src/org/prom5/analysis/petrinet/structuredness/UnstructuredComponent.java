package org.prom5.analysis.petrinet.structuredness;

import org.prom5.framework.models.petrinet.PetriNet;
import org.prom5.framework.models.petrinet.pattern.Component;

public class UnstructuredComponent extends Component {

	public UnstructuredComponent(PetriNet wfnet) {
		super(wfnet);
	}

	@Override
	public Component cloneComponent() {
		return new UnstructuredComponent(getWfnet());
	}

	@Override
	public String toString() {
		return "Unstructured";
	}

}
