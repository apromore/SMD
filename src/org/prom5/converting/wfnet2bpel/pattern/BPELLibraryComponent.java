package org.prom5.converting.wfnet2bpel.pattern;

import java.util.Map;

import org.prom5.framework.models.bpel.BPELActivity;
import org.prom5.framework.models.petrinet.PetriNet;
import org.prom5.framework.models.petrinet.pattern.LibraryComponent;

import att.grappa.Node;

public class BPELLibraryComponent extends LibraryComponent {

	private final BPELActivity activity;

	public BPELLibraryComponent(PetriNet wfnet, String path,
			Map<Node, Node> isomorphism, BPELActivity activity) {
		super(wfnet, path, isomorphism);
		this.activity = activity;
	}

	/**
	 * @return the activity
	 */
	public BPELActivity getActivity() {
		return activity;
	}
}
