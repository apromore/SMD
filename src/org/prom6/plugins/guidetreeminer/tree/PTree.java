package org.prom6.plugins.guidetreeminer.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tree;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.util.FontLib;
import prefuse.visual.VisualItem;

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

public class PTree {
	private Tree pTree = null;
	private Table pNodes = null;
	private Node pActiveNode = null;
	
	Map<GuideTreeNode, Node> guideTreePTreeNodeMap = new HashMap<GuideTreeNode, Node>();

	JPanel panel;
	JPanel detailPanel;
	JLabel nameLabel, noIdenticalTracesLabel, traceLengthLabel;
	JLabel encodedTraceLabel;
	
	Map<String, TreeSet<Integer>> encodedTraceIdenticalTraceIndicesSetMap;
	XLog log;
	int encodingLength;

	@SuppressWarnings("rawtypes")
	public PTree(GuideTree guideTree){
		this.log = guideTree.log;
		this.encodedTraceIdenticalTraceIndicesSetMap = guideTree.encodedTraceIdenticalIndicesMap;
		this.encodingLength = guideTree.encodingLength;
		
		nameLabel = SlickerFactory.instance().createLabel(" ");
		noIdenticalTracesLabel = SlickerFactory.instance().createLabel(" ");
		traceLengthLabel = SlickerFactory.instance().createLabel("");
		encodedTraceLabel = SlickerFactory.instance().createLabel("");
		pTree = new Tree();
        pNodes = pTree.getNodeTable();	
        pNodes.addColumn("Level", String.class);
        pNodes.addColumn("GuideTreeNode", GuideTreeNode.class);
		 
        pActiveNode = pTree.addRoot();

        pActiveNode.set("Level", "Level "+guideTree.root.level+" Node "+guideTree.root.encodedTrace);
        pActiveNode.set("GuideTreeNode", guideTree.root);

        
        guideTreePTreeNodeMap.put(guideTree.root, pActiveNode);
        
        Queue<GuideTreeNode> queue = new ConcurrentLinkedQueue<GuideTreeNode>();
		 
		GuideTreeNode rt = guideTree.root;
		queue.add(rt);
		
		GuideTreeNode node;
		int traceIndex;
		String traceName;
		while (!queue.isEmpty()) {
			rt = queue.remove();

			if (rt.right != null) {
				pActiveNode = pTree.addChild(guideTreePTreeNodeMap.get(rt));
				pActiveNode.set("Level", "Level "+rt.right.level+" Node "+rt.right.encodedTrace);
				pActiveNode.set("GuideTreeNode", rt.right);

				guideTreePTreeNodeMap.put(rt.right, pActiveNode);
				queue.add(rt.right);
			}else{
				pActiveNode = guideTreePTreeNodeMap.get(rt);
				node = ((GuideTreeNode)pActiveNode.get("GuideTreeNode"));
				traceIndex = encodedTraceIdenticalTraceIndicesSetMap.get(node.encodedTrace).first();
				traceName = log.get(traceIndex).getAttributes().get("concept:name").toString();
				pActiveNode.set("Level", "Trace Name: "+traceName);
			}
			
			if (rt.left != null) {
				pActiveNode = pTree.addChild(guideTreePTreeNodeMap.get(rt));
				pActiveNode.set("Level", "Level "+rt.left.level+" Node "+rt.left.encodedTrace);
				pActiveNode.set("GuideTreeNode", rt.left);
				guideTreePTreeNodeMap.put(rt.left, pActiveNode);
				queue.add(rt.left);
			}else{
				pActiveNode = guideTreePTreeNodeMap.get(rt);
				node = ((GuideTreeNode)pActiveNode.get("GuideTreeNode"));
				traceIndex = encodedTraceIdenticalTraceIndicesSetMap.get(node.encodedTrace).first();
				traceName = log.get(traceIndex).getAttributes().get("concept:name").toString();
				pActiveNode.set("Level", "Trace Name: "+traceName);
			}
		}

		// create a new treemap
		Color BACKGROUND = Color.WHITE;
        Color FOREGROUND = Color.BLACK;
        final String label = "Level";
        final TreeView tview = new TreeView(pTree, label);
        tview.setBackground(BACKGROUND);
        tview.setForeground(FOREGROUND);
       
   
        final List<String> clusterNodeLabelList = new ArrayList<String>();
        for(GuideTreeNode clusterNode : guideTree.clusterNodeList)
        	if(clusterNode.getNoChildren() > 0)
        		clusterNodeLabelList.add(clusterNode.encodedTrace);
        	else{
        		traceIndex = encodedTraceIdenticalTraceIndicesSetMap.get(clusterNode.encodedTrace).first();
        		traceName = log.get(traceIndex).getAttributes().get("concept:name").toString();
        		clusterNodeLabelList.add(traceName);
        	}
        
        tview.addControlListener(new ControlAdapter() {
            public void itemEntered(VisualItem item, MouseEvent e) {
//                if ( item.canGetString(label) )
            	
            }
            
            public void itemExited(VisualItem item, MouseEvent e) {
                
            }
            
            public void itemClicked(VisualItem item, MouseEvent e){
            	if(item.canGet("GuideTreeNode", GuideTreeNode.class)){
            		GuideTreeNode node = (GuideTreeNode)item.get("GuideTreeNode");
            		if(clusterNodeLabelList.contains(node.encodedTrace))
            			item.setHighlighted(true);
            	}
            		
            	GuideTreeNode node = (GuideTreeNode)item.get("GuideTreeNode");
            	if(node.right == null && node.left == null){
	            	int traceIndex = encodedTraceIdenticalTraceIndicesSetMap.get(node.encodedTrace).first();
					String traceName = log.get(traceIndex).getAttributes().get("concept:name").toString(); 
	            	nameLabel.setText("Trace Name: "+traceName);
	            	noIdenticalTracesLabel.setText("No. Identical Traces: "+encodedTraceIdenticalTraceIndicesSetMap.get(node.encodedTrace).size());
	            	encodedTraceLabel.setText("Encoded Trace: "+node.encodedTrace);
	            	traceLengthLabel.setText("No. Events in Trace: "+node.encodedTrace.length()/encodingLength);
	            	traceLengthLabel.setVisible(true);
	            	encodedTraceLabel.setVisible(true);
            	}else{
            		nameLabel.setText("Cluster Node at Level: "+node.level);
            		noIdenticalTracesLabel.setText("No. Leaves: "+node.getNoChildren());
//            		traceLengthLabel.setText(text)
            		traceLengthLabel.setVisible(false);
            		encodedTraceLabel.setVisible(false);
            	}
            }
        });
        
        final Visualization m_vis = tview.getVisualization();
        
//        System.out.println("Cluster Node Label List: "+clusterNodeLabelList);
        Iterator items = m_vis.items();
        VisualItem item;
        DefaultTupleSet tupleSet = new DefaultTupleSet();
        m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, tupleSet);
        while(items.hasNext()){
        	 item = (VisualItem)items.next();
        	 if(item.canGet("GuideTreeNode", GuideTreeNode.class)){
	        	 node = (GuideTreeNode)item.get("GuideTreeNode");
	        	 if(clusterNodeLabelList.contains(node.encodedTrace)){
	        		 item.setHighlighted(true);
	        		 tupleSet.addTuple(item);
	        	 }
        	 }
        }
//        System.out.println(tupleSet.toArray().length);
        tupleSet.addTupleSetListener(new TupleSetListener() {
            public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
//            	System.out.println("Here");
                m_vis.cancel("animatePaint");
                m_vis.run("fullPaint");
                m_vis.run("animatePaint");
            }
        });
        
       
        JLabel clusterNodeLabel = SlickerFactory.instance().createLabel("<HTML> No. Clusters: "+clusterNodeLabelList.size()+"<BR>Cluster Node Label List: "+clusterNodeLabelList+"</HTML>");
        Box box = new Box(BoxLayout.Y_AXIS);
        box.add(Box.createHorizontalStrut(10));
        box.add(clusterNodeLabel);
        box.add(Box.createVerticalGlue());
        box.add(Box.createVerticalStrut(3));
        box.add(nameLabel);
        box.add(Box.createVerticalGlue());
        box.add(Box.createVerticalStrut(3));
        box.add(noIdenticalTracesLabel);
        box.add(Box.createVerticalGlue());
        box.add(Box.createVerticalStrut(3));
        box.add(encodedTraceLabel);
        box.add(Box.createVerticalGlue());
        box.add(Box.createVerticalStrut(3));
        box.add(traceLengthLabel);
        box.add(Box.createVerticalStrut(3));
        box.setBackground(BACKGROUND);
		
        panel = new JPanel(new BorderLayout());
        panel.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));
        panel.setBackground(BACKGROUND);
        panel.setForeground(FOREGROUND);
        panel.add(tview, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);
	}
	
	public JPanel getPanel(){
		return panel;
	}
}
