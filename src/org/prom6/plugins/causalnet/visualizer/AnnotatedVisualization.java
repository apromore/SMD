package org.prom6.plugins.causalnet.visualizer;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.flexiblemodel.Flex;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.jgraph.ProMGraphModel;
import org.processmining.models.jgraph.ProMJGraph;
import org.prom6.models.causalnet.CausalNetAnnotations;
import org.prom6.models.causalnet.CausalNetAnnotationsConnection;
import org.prom6.plugins.causalnet.miner.gui.ParametersPanel;
import org.prom6.plugins.causalnet.miner.settings.HeuristicsMinerSettings;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

@Plugin(name = "Visualize Causal Net with Annotations", parameterLabels = { "CausalNet" }, returnLabels = { "aC-Net Visualization" }, returnTypes = { JComponent.class })
@Visualizer
public class AnnotatedVisualization {

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, Flex net) {
		
		CausalNetAnnotations annotations = null;
		try {
			CausalNetAnnotationsConnection conn = context.getConnectionManager().getFirstConnection(CausalNetAnnotationsConnection.class, context, net);
			annotations = (CausalNetAnnotations) conn.getObjectWithRole(CausalNetAnnotationsConnection.ANNOTATIONS);
		} catch (ConnectionCannotBeObtained e) {
			//no annotations
		}
		
		ProMJGraph graph = buildJGraph(net);
		AnnotatedScalableView view = new AnnotatedScalableView(graph, annotations);
		
		PIPPanel pip = new PIPPanel(view);
		view.addViewPanel(pip, "PIP", "View", true);
		
		ZoomPanel zoom = new ZoomPanel(view, AnnotatedScalableView.MAX_ZOOM);
		zoom.setHeight(200);
		view.addViewPanel(zoom, "Zoom", "View", true);
		
		SetupPanel setup = new SetupPanel(new AnnotatedVisualizationSettings(), view);
		view.addViewPanel(setup, "Setup", "View", true);
		
		PatternsPanel joins = new PatternsPanel(null, null, null, "No event selected");
		joins.setSize(300,300);
		view.addViewPanel(joins, "Joins", "Patterns", true);
		
		PatternsPanel splits = new PatternsPanel(null, null, null, "No event selected");
		splits.setSize(300,300);
		view.addViewPanel(splits, "Splits", "Patterns", true);
		
		HeuristicsMinerSettings settings = (HeuristicsMinerSettings) annotations.getExecutionInfo("Parameters");
		if(settings != null) {
			
			ParametersPanel parameters = new ParametersPanel();
			parameters.copySettings(settings);
			parameters.setEnabled(false);
			parameters.resize(false);
			parameters.setPreferredSize(new java.awt.Dimension(315, 370));
			
			view.addViewPanel(parameters, "Parameters", "Info", false);
		}
		
		ExportPanel export = new ExportPanel(view);
		view.addViewPanel(export, "Options", "Info", false);
		
		view.addInteractionViewports(pip, zoom);
		view.addPatternsViewports(joins, splits);
				
		return view;
	}
	
	private static ProMJGraph buildJGraph(DirectedGraph<?, ?> causalNet){
		
		ViewSpecificAttributeMap map = new ViewSpecificAttributeMap();
		GraphLayoutConnection layoutConnection = new GraphLayoutConnection(causalNet);
		
		ProMGraphModel model = new ProMGraphModel(causalNet);
		ProMJGraph jGraph = new ProMJGraph(model, map, layoutConnection);
		
		JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
		layout.setDeterministic(false);
		layout.setCompactLayout(false);
		layout.setFineTuning(true);
		layout.setParallelEdgeSpacing(15);
		layout.setFixRoots(false);

		layout.setOrientation(map.get(causalNet, AttributeMap.PREF_ORIENTATION, SwingConstants.SOUTH));

		if(!layoutConnection.isLayedOut()){
		
			JGraphFacade facade = new JGraphFacade(jGraph);
	
			facade.setOrdered(false);
			facade.setEdgePromotion(true);
			facade.setIgnoresCellsInGroups(false);
			facade.setIgnoresHiddenCells(false);
			facade.setIgnoresUnconnectedCells(false);
			facade.setDirected(true);
			facade.resetControlPoints();
			facade.run(layout, true);
	
			java.util.Map<?, ?> nested = facade.createNestedMap(true, true);
	
			jGraph.getGraphLayoutCache().edit(nested);
			layoutConnection.setLayedOut(true);
		}
		
		jGraph.setUpdateLayout(layout);
		
		layoutConnection.updated();
		
		return jGraph;
	}
}

