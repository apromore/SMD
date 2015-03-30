package org.prom6.plugins.guidetreeminer;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.prom6.plugins.guidetreeminer.tree.GuideTree;
import org.prom6.plugins.guidetreeminer.tree.PTree;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 July 2010
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */

@Plugin(name = "Guide Tree Visualization", 
		returnLabels = { "Visualizion of Guide Tree" }, 
		returnTypes = { JComponent.class }, 
		parameterLabels = {"GuideTree"},
		userAccessible = false)
@Visualizer
public class GuideTreeVisualization {
	@PluginVariant(requiredParameterLabels = {0})
	public JComponent visualize(PluginContext context, 
			GuideTree guideTree){
		JInternalFrame frame = new JInternalFrame();
		PTree pTree = new PTree(guideTree);
		JScrollPane scrollPane = new JScrollPane(pTree.getPanel());
		frame.getContentPane().add(scrollPane);
		frame.pack();
		frame.show();
		return frame;
	}
}
