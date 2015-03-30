package pcf;

import java.util.HashMap;
import java.util.LinkedList;

public class ClassificationTree {
	

	HashMap<LinkedList<String>, ClassificationTreeNode> stringNodeMap = new HashMap<LinkedList<String>, ClassificationTreeNode>();
	ClassificationTreeNode rootnode = new ClassificationTreeNode("_root_", -1);
	
	public ClassificationTreeNode getRootNode() {
		return rootnode;
	}
	
	public String toString() {
		String toReturn = "";
		
		for (ClassificationTreeNode ch : rootnode.children) {
			toReturn += ch.toString();
		}
		return toReturn;
	}
}
