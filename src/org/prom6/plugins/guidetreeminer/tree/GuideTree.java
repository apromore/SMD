package org.prom6.plugins.guidetreeminer.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.deckfour.xes.model.XLog;

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

public class GuideTree {
	GuideTreeNode root;
	XLog log;
	Map<String, TreeSet<Integer>> encodedTraceIdenticalIndicesMap;
	Map<String, String> charActivityMap, activityCharMap;
	int encodingLength;
	List<String> encodedTraceList;
	int noElements;
	int noClusters;
	List<GuideTreeNode> clusterNodeList;

	public GuideTree(GuideTreeNode root, int noElements){
		this.root = root;
		this.noElements = noElements;
	}
	
	public void levelOrderTraversal(){
		Map<Integer, Set<GuideTreeNode>> levelNodeSetMap = new HashMap<Integer, Set<GuideTreeNode>>();
		Set<GuideTreeNode> levelNodeSet;
		Queue<GuideTreeNode> queue = new ConcurrentLinkedQueue<GuideTreeNode>();
		 
		GuideTreeNode rt = root;
        if(rt != null)
        {
            queue.add(rt);
            while(!queue.isEmpty())
            {
                rt = (GuideTreeNode)queue.remove();
                if(levelNodeSetMap.containsKey(rt.level)){
                	levelNodeSet = levelNodeSetMap.get(rt.level);
                }else{
                	levelNodeSet = new HashSet<GuideTreeNode>();
                }
                levelNodeSet.add(rt);
                levelNodeSetMap.put(rt.level, levelNodeSet);
                
                if(rt.right != null)
                    queue.add(rt.right);
                if(rt.left != null)
                    queue.add(rt.left);
            }
        }
        
        TreeSet<Integer> sortedLevelSet = new TreeSet<Integer>();
        sortedLevelSet.addAll(levelNodeSetMap.keySet());
        int noLevels = sortedLevelSet.size();
        int[] sortedLevelArray = new int[noLevels];
        
        int index = 0;
        for(Integer level : sortedLevelSet)
        	sortedLevelArray[index++] = level;
        
        sortedLevelSet = null;
	}
	
	public void printClusters(int noClusters){
		GuideTreeNode node = root;
		Queue<GuideTreeNode> queue = new ConcurrentLinkedQueue<GuideTreeNode>();
		queue.add(node);
		
		while(!queue.isEmpty()){
			node = queue.remove();
			if(node.step >= noClusters){
//				System.out.println("L: "+node.encodedTrace);
			}else{
				if(node.right != null && node.right.step != noElements)
					queue.add(node.right);
				else if(node.right != null && node.right.step == noElements);
//					System.out.println(node.right.encodedTrace);
				if(node.left != null && node.left.step != noElements)
					queue.add(node.left);
				else if(node.left != null && node.left.step == noElements);
//					System.out.println(node.left.encodedTrace);
			}
		}
		
	}
	
	public List<List<String>> getClusters(int noClusters){
		List<List<String>> clusterEncodedTraceList = new ArrayList<List<String>>();
		this.noClusters = noClusters;
		clusterNodeList = new ArrayList<GuideTreeNode>();
		
		if(noClusters == 0)
			return clusterEncodedTraceList;
		
		GuideTreeNode node = root;
		Queue<GuideTreeNode> queue = new ConcurrentLinkedQueue<GuideTreeNode>();
		queue.add(node);
		
		List<String> encodedTraceList;
		while(!queue.isEmpty()){
			node = queue.remove();
			if(node.step >= noClusters){
				clusterNodeList.add(node);
				encodedTraceList = getLeavesEncodedTraceList(node);
				clusterEncodedTraceList.add(encodedTraceList);
			}else{
				if(node.right != null && node.right.step != noElements)
					queue.add(node.right);
				else if(node.right != null && node.right.step == noElements){
					encodedTraceList = new ArrayList<String>();
					encodedTraceList.add(node.right.encodedTrace);
					clusterNodeList.add(node.right);
					clusterEncodedTraceList.add(encodedTraceList);
				}
				if(node.left != null && node.left.step != noElements)
					queue.add(node.left);
				else if(node.left != null && node.left.step == noElements){
					encodedTraceList = new ArrayList<String>();
					encodedTraceList.add(node.left.encodedTrace);
					clusterNodeList.add(node.left);
					clusterEncodedTraceList.add(encodedTraceList);
				}
			}
		}
		
		return clusterEncodedTraceList;
	}
	
	
	private List<String> getLeavesEncodedTraceList(GuideTreeNode node){
		List<String> encodedTraceList = new ArrayList<String>();
		Queue<GuideTreeNode> queue = new ConcurrentLinkedQueue<GuideTreeNode>();
		queue.add(node);
		
		while(!queue.isEmpty()){
			node = queue.remove();
			if(node.right != null){
				queue.add(node.right);
			}
			if(node.left != null){
				queue.add(node.left);
			}
			
			if(node.right == null && node.left == null)
				encodedTraceList.add(node.encodedTrace);
		}
		
		return encodedTraceList;
	}
	
	public void setLog(XLog log){
		this.log = log;
	}
	
	public void setEncodingLength(int encodingLength){
		this.encodingLength = encodingLength;
	}
	
	public void setEncodedTraceIdenticalIndicesSetMap(Map<String, TreeSet<Integer>> encodedTraceIdenticalIndicesMap){
		this.encodedTraceIdenticalIndicesMap = encodedTraceIdenticalIndicesMap;
	}
	
	public void setCharActivityMap(Map<String, String> charActivityMap){
		this.charActivityMap = charActivityMap;
	}
	
	public void setActivityCharMap(Map<String, String> activityCharMap){
		this.activityCharMap = activityCharMap;
	}
	
	public int getEncodingLength(){
		return this.encodingLength;
	}
	
	public Map<String, String> getCharActivityMap(){
		return this.charActivityMap;
	}
	
	public Map<String, String> getActivityCharMap(){
		return this.activityCharMap;
	}
	
	public XLog getLog(){
		return this.log;
	}

	public List<String> getEncodedTraceList() {
		return encodedTraceList;
	}

	public void setEncodedTraceList(List<String> encodedTraceList) {
		this.encodedTraceList = new ArrayList<String>();
		this.encodedTraceList.addAll(encodedTraceList);
	}
	
	public GuideTreeNode getRoot(){
		return root;
	}

	public Map<String, TreeSet<Integer>> getEncodedTraceIdenticalIndicesMap() {
		return encodedTraceIdenticalIndicesMap;
	}

	public int getNoElements() {
		return noElements;
	}
}
