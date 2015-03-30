package org.prom6.plugins.guidetreeminer.tree;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 June 2010
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */

public class GuideTreeNode {
	String encodedTrace;
	int level;
	int step;
	
	GuideTreeNode parent;
	GuideTreeNode left;
	GuideTreeNode right;
	

	public GuideTreeNode(String encodedTrace, int level){
		this.encodedTrace = encodedTrace;
		this.level = level;
		this.left = this.right = null;
	}
	
	public GuideTreeNode(String encodedTrace){
		this.encodedTrace = encodedTrace;
		this.left = this.right = null;
	}

	public void setParent(GuideTreeNode parent) {
		this.parent = parent;
	}

	public void setChildren(GuideTreeNode left, GuideTreeNode right) {
		this.left = left;
		this.right = right;
		
		this.level = Math.max(left.level, right.level)+1;
		left.setParent(this);
		right.setParent(this);
	}
	
	public void setStep(int step){
		this.step = step;
	}

	public int getStep(){
		return this.step;
	}
	
	public int getNoChildren(){
		GuideTreeNode node = this;
		Queue<GuideTreeNode> queue = new ConcurrentLinkedQueue<GuideTreeNode>();
		if(this.right == null && this.left == null)
			return 0;
		
		queue.add(node);
		int noChildren = 0;
        while(!queue.isEmpty()){
        	 node = queue.remove();
        	 if(node.right == null && node.left == null)
        		 noChildren++;
        	 if(node.right != null)
        		 queue.add(node.right);
        	 if(node.left != null)
        		 queue.add(node.left);
         }
        return noChildren;
     }

	public String getEncodedTrace() {
		return encodedTrace;
	}

	public int getLevel() {
		return level;
	}

	public GuideTreeNode getParent() {
		return parent;
	}

	public GuideTreeNode getLeft() {
		return left;
	}

	public GuideTreeNode getRight() {
		return right;
	}
}
