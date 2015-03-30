package org.prom6.plugins.heuristicsnet.miner.heuristics.miner;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.prom6.models.heuristics.HeuristicsNet;
import org.prom6.plugins.heuristicsnet.miner.heuristics.miner.gui.ParametersPanel;
import org.prom6.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;


@Plugin(name = "Mine for a Heuristics Net using Heuristics Miner",
		parameterLabels = {"Log", "Settings", "Log Info"},
		returnLabels = {"Mined Models"},
		returnTypes = {HeuristicsNet.class},
		userAccessible = true,
		help = "Flexible Heuristics Miner")
public class FlexibleHeuristicsMinerPlugin {
	//TODO - Add documentation
	
	//TODO - Add a help
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "A.J.M.M. Weijters", email = "a.j.m.m.weijters@tue.nl", website = "http://is.tm.tue.nl/staff/aweijters")
	@PluginVariant(variantLabel = "User-defined settings", requiredParameterLabels = { 0 })
	public static HeuristicsNet run(UIPluginContext context, XLog log) {
		//TODO - Build different plug-ins that already receive log infos!!!
		//Note that, the default classifier are been used at the moment
		
		ParametersPanel parameters = new ParametersPanel();
		parameters.removeAndThreshold();
	
		InteractionResult result = context.showConfiguration("Heuristics Miner Parameters", parameters);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
		}
		return run(context, log, parameters.getSettings());
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "A.J.M.M. Weijters", email = "a.j.m.m.weijters@tue.nl", website = "http://is.tm.tue.nl/staff/aweijters")
	@PluginVariant(variantLabel = "User-defined settings", requiredParameterLabels = { 0, 2 })
	public static HeuristicsNet run(UIPluginContext context, XLog log, XLogInfo logInfo) {
		//TODO - Build different plug-ins that already receive log infos!!!
		//Note that, the default classifier are been used at the moment
		
		ParametersPanel parameters = new ParametersPanel();
		parameters.removeAndThreshold();
	
		InteractionResult result = context.showConfiguration("Heuristics Miner Parameters", parameters);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
		}
		return run(context, log, parameters.getSettings(), logInfo);
	}
	
	@PluginVariant(variantLabel = "User-defined settings", requiredParameterLabels = { 0 })
	public static HeuristicsNet run(PluginContext context, XLog log) {

		//XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
		FlexibleHeuristicsMiner fhm = new FlexibleHeuristicsMiner(context, log, new HeuristicsMinerSettings());
		return fhm.mine();
	}
	
	@PluginVariant(variantLabel = "User-defined settings", requiredParameterLabels = { 0, 2 })
	public static HeuristicsNet run(PluginContext context, XLog log, XLogInfo logInfo) {

		FlexibleHeuristicsMiner fhm = new FlexibleHeuristicsMiner(context, log, logInfo);
		return fhm.mine();
	}
	
	@PluginVariant(variantLabel = "User-defined settings", requiredParameterLabels = { 0, 1 })
	public static HeuristicsNet run(PluginContext context, XLog log, HeuristicsMinerSettings settings) {

		//XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
		FlexibleHeuristicsMiner fhm = new FlexibleHeuristicsMiner(context, log, settings);
		return fhm.mine();
	}
	
	@PluginVariant(variantLabel = "User-defined settings", requiredParameterLabels = { 0, 1, 2 })
	public static HeuristicsNet run(PluginContext context, XLog log, HeuristicsMinerSettings settings, XLogInfo logInfo) {

		//XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
		FlexibleHeuristicsMiner fhm = new FlexibleHeuristicsMiner(context, log, logInfo, settings);
		return fhm.mine();
	}
}