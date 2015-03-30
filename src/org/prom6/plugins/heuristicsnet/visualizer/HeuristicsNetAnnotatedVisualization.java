package org.prom6.plugins.heuristicsnet.visualizer;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.prom6.models.heuristics.HeuristicsNet;
import org.prom6.models.heuristics.HeuristicsNetGraph;
import org.prom6.plugins.heuristicsnet.miner.heuristics.miner.gui.HeuristicsNetVisualizer;
import org.prom6.plugins.heuristicsnet.visualizer.annotatedvisualization.AnnotatedVisualizationGenerator;
import org.prom6.plugins.heuristicsnet.visualizer.annotatedvisualization.AnnotatedVisualizationSettings;

@Plugin(name = "Visualize HeuristicsNet with Annotations", parameterLabels = { "HeuristicsNet" }, returnLabels = { "HN Annotated Visualization - No Semantics" }, returnTypes = { JComponent.class })
@Visualizer
public class HeuristicsNetAnnotatedVisualization {

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, HeuristicsNet net) {
			
		AnnotatedVisualizationGenerator generator = new AnnotatedVisualizationGenerator();
		
		AnnotatedVisualizationSettings settings = new AnnotatedVisualizationSettings();
		HeuristicsNetGraph graph = generator.generate(net, settings);
		
		return HeuristicsNetVisualizer.visualizeGraph(graph, net, settings, context.getProgress());
	}
}
