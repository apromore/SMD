package clustering.dendogram;

import java.util.LinkedList;
import java.util.List;

public class LeafNode implements Node {
	private Integer index;
	private String value;

	protected List<String> children;

	public LeafNode(int index) {
		this.index = index;
		this.children = new LinkedList<String>();
		this.value = null;
	}

	public LeafNode(int index, String value) {
		this.index = index;
		this.children = new LinkedList<String>();
		this.children.add(value);
		this.value = value;
	}
	
	public int getIndex() {
		return index;
	}
		
	public Node getFirst() {
		return null;
	}
	
	public Node getSecond() {
		return null;
	}
	
	public String toString() {
		return String.format("Leaf (%s): %s", value, children);
	}

	public List<String> getChildren() {
		return children;
	}
}
