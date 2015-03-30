//Om Ganesayanamaha
package org.prom6.plugins.guidetreeminer;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.prom6.plugins.guidetreeminer.tree.GuideTree;
import org.prom6.plugins.guidetreeminer.ui.GuideTreeMinerUI;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 July 2009
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */
@Plugin(name = "Guide Tree Miner", parameterLabels = {"Log", "GuideTreeMiner Input"}, returnLabels = {"Guide Tree", "Cluster Log Output"}, returnTypes = {GuideTree.class, ClusterLogOutput.class}, userAccessible = true)
public class GuideTreeMinerPlugin {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "R.P. Jagadeesh Chandra 'JC' Bose", email = "j.c.b.rantham.prabhakara@tue.nl")
	@PluginVariant(variantLabel = "Select options to use", requiredParameterLabels = { 0 })
	public static Object[] main(UIPluginContext context, final XLog log) {
		GuideTreeMinerUI guideTreeMinerUI = new GuideTreeMinerUI(context);
		GuideTreeMinerInput input = guideTreeMinerUI.mineTree(log);
		return main(context, log, input);
	}
	
	@PluginVariant(variantLabel = "Select options to use", requiredParameterLabels = {0, 1})
	public static Object[] main(UIPluginContext context, XLog log, GuideTreeMinerInput input) {
		MineGuideTree mineGuideTree = new MineGuideTree();
		Object[] returnObjects = mineGuideTree.mine(context, input, log);
		return new Object[] {returnObjects[0], returnObjects[1]};
	}
}
