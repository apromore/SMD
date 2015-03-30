package org.prom5.converting.wfnet2bpel.pattern;

import org.prom5.framework.models.petrinet.PetriNet;
import org.prom5.framework.models.petrinet.pattern.Component;
import org.prom5.framework.models.petrinet.pattern.WellStructuredGraphComponent;

public class FlowComponent extends WellStructuredGraphComponent {

	public FlowComponent(PetriNet wfnet) {
		super(wfnet);
	}

	/**
	 * @see org.prom5.framework.models.petrinet.pattern.Component#toString()
	 */
	@Override
	public String toString() {
		return "Flow";
	}

	/**
	 * @see org.prom5.framework.models.petrinet.pattern.WellStructuredGraphComponent#cloneComponent()
	 */
	@Override
	public Component cloneComponent() {
		return new FlowComponent(getWfnet());
	}

}
