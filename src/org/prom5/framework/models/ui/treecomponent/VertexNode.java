package org.prom5.framework.models.ui.treecomponent;

import org.prom5.framework.models.ModelGraphVertex;

public class VertexNode extends SelectableNode {

	private static final long serialVersionUID = -7438327407115913249L;

	private ModelGraphVertex vertex;

	public VertexNode(ModelGraphVertex vertex, String shortName) {
		super(shortName);
		this.vertex = vertex;
	}
	
	public ModelGraphVertex getVertex() {
		return vertex;
	}

	public boolean equals(Object other) {
		if (!(other instanceof VertexNode)) {
			return false;
		}
		VertexNode otherNode = (VertexNode) other;

		return vertex.getName().equals(otherNode.vertex.getName());
	}
}

