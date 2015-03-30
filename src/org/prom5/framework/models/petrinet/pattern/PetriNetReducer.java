package org.prom5.framework.models.petrinet.pattern;

import org.prom5.framework.models.petrinet.Transition;

public interface PetriNetReducer<A extends Object> {

	public A createAnnotation(Transition t);

	
	
}
