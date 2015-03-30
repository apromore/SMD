package org.prom6.models.causalnet;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.flexiblemodel.Flex;

//import org.processmining.framework.connections.impl.AbstractConnection;
//import org.processmining.models.flexiblemodel.Flex;

public class CausalNetAnnotationsConnection extends AbstractConnection {

	public final static String FLEX = "CausalNet";
	public final static String ANNOTATIONS = "CausalNetAnnotations";
	
	public CausalNetAnnotationsConnection(String label, Flex flex, CausalNetAnnotations annotations) {
		super(label);
		put(FLEX, flex);
		put(ANNOTATIONS, annotations);
	}
}
