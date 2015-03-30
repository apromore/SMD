package pcf;

import java.util.LinkedList;

public class ClassificationTreeNode {
	String label; 
	ClassificationTreeNode parent;
	int level; 
	LinkedList<ClassificationTreeNode> children = new LinkedList<ClassificationTreeNode>();
	
	ClassificationTreeNode(String label, int level) {
		this.label = label;
		this.level = level;
	}
	
	public void addChild(ClassificationTreeNode child) {
		children.add(child);
		child.addParent(this);
	}
	
	public void addParent(ClassificationTreeNode parent) {
		this.parent = parent;
	}
	
	public String toString() {
		String toReturn = "";
		for (int i = 0; i < level; i++) {
			toReturn += "\t";
		}
		toReturn += label +"\n";
		
		for (ClassificationTreeNode ch : children) {
			toReturn += ch.toString();
		}
		return toReturn;
	}
}