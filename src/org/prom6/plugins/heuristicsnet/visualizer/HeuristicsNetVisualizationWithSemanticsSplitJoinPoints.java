package org.prom6.plugins.heuristicsnet.visualizer;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.prom6.models.heuristics.HeuristicsNet;
import org.prom6.models.heuristics.HeuristicsNetGraph;

@Plugin(name = "Visualize HeuristicsNet with Semantics Split/Joing Points", parameterLabels = { "HeuristicsNet" }, returnLabels = { "HN Visualization - With Semantics" }, returnTypes = { JComponent.class })
@Visualizer
public class HeuristicsNetVisualizationWithSemanticsSplitJoinPoints {

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, HeuristicsNet[] population) {
		return ProMJGraphVisualizer.instance().visualizeGraph(context, new HeuristicsNetGraph(population[population.length - 1],
				"Heuristics Net", true));
	}

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, HeuristicsNet population) {
		return ProMJGraphVisualizer.instance().visualizeGraph(context, new HeuristicsNetGraph(population,
				"Heuristics Net", true));
	}

}
