package org.prom5.converting.erlangnet2erlang;

import java.util.LinkedHashSet;
import java.util.Set;

import org.prom5.framework.models.erlang.Function;
import org.prom5.framework.models.petrinet.AnnotatedPetriNet;
import org.prom5.framework.models.petrinet.Choice;

public class ErlangWorkflowNet extends
		AnnotatedPetriNet<Choice, Function, Object, Object> {

	public final Set<String> variables = new LinkedHashSet<String>();

}
