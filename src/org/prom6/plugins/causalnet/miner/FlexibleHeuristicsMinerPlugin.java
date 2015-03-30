package org.prom6.plugins.causalnet.miner;

import java.util.LinkedList;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.flexiblemodel.FlexEndTaskNodeConnection;
import org.processmining.models.connections.flexiblemodel.FlexStartTaskNodeConnection;
import org.processmining.models.flexiblemodel.EndTaskNodesSet;
import org.processmining.models.flexiblemodel.Flex;
import org.processmining.models.flexiblemodel.StartTaskNodesSet;
import org.prom6.models.causalnet.CausalNetAnnotations;
import org.prom6.models.causalnet.CausalNetAnnotationsConnection;
import org.prom6.plugins.causalnet.miner.gui.ParametersPanel;
import org.prom6.plugins.causalnet.miner.settings.HeuristicsMinerSettings;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;
import org.prom6.plugins.causalnet.temp.cube.EventCube;
import org.prom6.plugins.causalnet.temp.elements.Dimension;
import org.prom6.plugins.causalnet.temp.elements.Perspective;
import org.prom6.plugins.causalnet.temp.index.InvertedIndex;
import org.prom6.plugins.causalnet.temp.measures.EventDirectDependencyMeasure;
import org.prom6.plugins.causalnet.temp.measures.EventDirectSuccessor;
import org.prom6.plugins.causalnet.temp.measures.EventEnd;
import org.prom6.plugins.causalnet.temp.measures.EventEntry;
import org.prom6.plugins.causalnet.temp.measures.EventIndirectSuccessor;
import org.prom6.plugins.causalnet.temp.measures.EventLenghtTwoDependencyMeasure;
import org.prom6.plugins.causalnet.temp.measures.EventLengthTwoLoop;
import org.prom6.plugins.causalnet.temp.measures.EventLongDistanceDependencyMeasure;
import org.prom6.plugins.causalnet.temp.measures.EventStart;
import org.prom6.plugins.causalnet.temp.measures.InstanceEntry;
import org.prom6.plugins.causalnet.temp.measures.Measure;

@Plugin(name = "Mine for a Causal Net using Heuristics Miner", 
		parameterLabels = {"Log", "Settings"},
		returnLabels = {"CausalNet", "StartTaskNodesSet", "EndTaskNodesSet", "CausalNetAnnotations"},
		returnTypes = {Flex.class, StartTaskNodesSet.class, EndTaskNodesSet.class, CausalNetAnnotations.class},
		userAccessible = true,
		help = "Flexible Heuristics Miner")
public class FlexibleHeuristicsMinerPlugin {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "A.J.M.M. Weijters", email = "a.j.m.m.weijters@tue.nl", website = "http://is.tm.tue.nl/staff/aweijters")
	@PluginVariant(variantLabel = "User-defined settings", requiredParameterLabels = { 0 })
	public static Object[] MDHPanel(UIPluginContext context, XLog log) {
		
		ParametersPanel parameters = new ParametersPanel();
		InteractionResult result = context.showConfiguration("Heuristics Miner Parameters", parameters);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
			context.getFutureResult(1).cancel(true);
			context.getFutureResult(2).cancel(true);
			context.getFutureResult(3).cancel(true);
			return null;
		}
		return runFlexibleHeuristicsMiner(context, log, parameters.getSettings());
	}
	
	@PluginVariant(variantLabel = "User-defined settings", requiredParameterLabels = { 0 })
	public static Object[] MDHPanel(PluginContext context, XLog log) {
		return runFlexibleHeuristicsMiner(context, log, new HeuristicsMinerSettings());
	}

	@PluginVariant(variantLabel = "User-defined settings", requiredParameterLabels = { 0 })
	public static Object[] runFlexibleHeuristicsMiner(PluginContext context, XLog log, HeuristicsMinerSettings settings) {
		
		//BUILDING INVERTED INDEX
		InvertedIndex index;
		if(!context.getProgress().isCancelled()){
			
			index = new InvertedIndex();
			index.createCompleteIndex(log);
		}
		else return null;
		
		//CREATING DEFAULT PERSPECTIVE
		Perspective perspective;
		if(!context.getProgress().isCancelled()){
			
			LinkedList<Dimension> modelDimensions = new LinkedList<Dimension>();
			LinkedList<Pair<Measure, AggregationFunction<?>>> measures = new LinkedList<Pair<Measure, AggregationFunction<?>>>();
			
			Dimension mainDim = index.getDimension("Event:Name");
			Dimension d1 = mainDim.instance();
			d1.addValues(mainDim.getValues());
			modelDimensions.add(d1);
			
			Dimension secondDim = index.getDimension("Event:Type");
			if(secondDim.getCardinality() > 1){
				
				Dimension d2 = secondDim.instance();
				d2.addValues(secondDim.getValues());
				modelDimensions.add(d2);
			}
			
			measures.add(new Pair<Measure, AggregationFunction<?>>(new EventEntry(), null));
			measures.add(new Pair<Measure, AggregationFunction<?>>(new InstanceEntry(), null));
			measures.add(new Pair<Measure, AggregationFunction<?>>(new EventStart(), null));
			measures.add(new Pair<Measure, AggregationFunction<?>>(new EventEnd(), null));
			measures.add(new Pair<Measure, AggregationFunction<?>>(new EventDirectSuccessor(), null));
			measures.add(new Pair<Measure, AggregationFunction<?>>(new EventIndirectSuccessor(), null));
			measures.add(new Pair<Measure, AggregationFunction<?>>(new EventDirectDependencyMeasure(), null));
			measures.add(new Pair<Measure, AggregationFunction<?>>(new EventLenghtTwoDependencyMeasure(), null));
			measures.add(new Pair<Measure, AggregationFunction<?>>(new EventLongDistanceDependencyMeasure(), null));
			measures.add(new Pair<Measure, AggregationFunction<?>>(new EventLengthTwoLoop(), null));
			
			perspective = 
				new Perspective(modelDimensions, new LinkedList<Dimension>(), measures);
		}
		else return null;
		
		//BUILDING EVENT CUBE
		EventCube cube;
		if(!context.getProgress().isCancelled()){
			
			cube = new EventCube(index, perspective.getFirstSpace(), perspective.getMeasures());
			cube.processValues(perspective);
		}
		else return null;
		
		//COMPUTING CAUSAL NET
		if(!context.getProgress().isCancelled()){
			
			String logID = XConceptExtension.instance().extractName(log);
			
			Object[] cnet = cube.computeCausalNet(logID, perspective, settings);
			Flex flexDiagram = (Flex) cnet[0];
			
			context.getFutureResult(0).setLabel(flexDiagram.getLabel());
			context.getFutureResult(1).setLabel("Start tasks node of " + flexDiagram.getLabel());
			context.getFutureResult(2).setLabel("End tasks node of " + flexDiagram.getLabel());
			context.getFutureResult(3).setLabel("Annotations of " + flexDiagram.getLabel());
			
			context.addConnection(new FlexStartTaskNodeConnection("Start tasks node of " + flexDiagram.getLabel() + " connection", flexDiagram, (StartTaskNodesSet) cnet[1]));
			context.addConnection(new FlexEndTaskNodeConnection("End tasks node of " + flexDiagram.getLabel() + " connection", flexDiagram, (EndTaskNodesSet) cnet[2]));
			context.addConnection(new CausalNetAnnotationsConnection("Annotations of " + flexDiagram.getLabel()  + " connection", flexDiagram, (CausalNetAnnotations) cnet[3]));
			
			return cnet;
		}
		else return null;
	}
}
