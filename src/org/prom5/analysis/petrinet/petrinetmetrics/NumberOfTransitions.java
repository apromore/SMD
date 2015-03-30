package org.prom5.analysis.petrinet.petrinetmetrics;

import org.prom5.framework.models.petrinet.PetriNet;

public class NumberOfTransitions implements ICalculator {
	private PetriNet net;
	private final static String TYPE = "Size";
	private final static String NAME = "Transitions";


	public NumberOfTransitions(PetriNet net) {
		super();
		this.net = net;
	}


	public String Calculate() {
		int result = net.getTransitions().size();
		String output = ""+result;
		return output;
	}

	public String getName() {
		return this.NAME;
	}

	public String getType() {
		return this.TYPE;
	}

	public String VerifyBasicRequirements() {
		return ".";
	}

}
