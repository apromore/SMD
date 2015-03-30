package org.processmining.plugins.epc;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.epc.EPC;
import org.processmining.models.graphbased.directed.epc.InstanceEPC;
import org.processmining.models.jgraph.ProMJGraphVisualizer;

@Plugin(name = "Visualize EPC", parameterLabels = { "EPC" }, returnLabels = { "EPC Visualization" }, returnTypes = { JComponent.class })
@Visualizer
public class EPCVisualization {

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, InstanceEPC net) {
		return ProMJGraphVisualizer.instance().visualizeGraph(context, net);
	}

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, EPC net) {
		return ProMJGraphVisualizer.instance().visualizeGraph(context, net);
	}

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, ConfigurableEPC net) {
		return ProMJGraphVisualizer.instance().visualizeGraph(context, net);
	}

}
