package org.processmining.plugins.epc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.epc.EPCEdge;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.processmining.models.graphbased.directed.epc.elements.Connector;
import org.processmining.models.graphbased.directed.epc.elements.Event;
import org.processmining.models.graphbased.directed.epc.elements.Function;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;

public class EPCConversion {

	private ConfigurableEPC epc;
	private Petrinet pn;
	private Map<EPCEdge<?, ?>, Place> sourceMap;
	private Map<EPCEdge<?, ?>, Place> targetMap;

	private void init(ConfigurableEPC epc) {
		this.epc = epc;
		pn = PetrinetFactory.newPetrinet("Petri net converted from "
				+ epc.getLabel());
		sourceMap = new HashMap<EPCEdge<?, ?>, Place>();
		targetMap = new HashMap<EPCEdge<?, ?>, Place>();
	}

	public Petrinet convertToPN(ConfigurableEPC epc) {
		init(epc);
		
		for (Function function : epc.getFunctions()) {
			generateAndNode(function, false);
		}

		for (Event event : epc.getEvents()) {
			generateXorNode(event, true);
		}

		for (Connector connector : epc.getConnectors()) {
			switch (connector.getType()) {
			case AND: {
				generateAndNode(connector, true);
				break;
			}
			case XOR: {
				generateXorNode(connector, true);
				break;
			}
			case OR: {
				generateOrNode(connector, true);
				break;
			}
			default:
			}
		}

		for (EPCEdge<?, ?> edge : epc.getEdges()) {
			generateEdge(edge, true);
		}

		return pn;
	}

	private void generateAndNode(EPCNode node, boolean isInvisible) {
		Transition transition = pn.addTransition(node.getLabel());
		transition.setInvisible(isInvisible);
		for (EPCEdge<?, ?> edge : epc.getInEdges(node)) {
			Place inputPlace = pn.addPlace(edge.getLabel());
			pn.addArc(inputPlace, transition);
			targetMap.put(edge, inputPlace);
		}
		for (EPCEdge<?, ?> edge : epc.getOutEdges(node)) {
			Place outputPlace = pn.addPlace(edge.getLabel());
			pn.addArc(transition, outputPlace);
			sourceMap.put(edge, outputPlace);
		}
	}

	private void generateXorNode(EPCNode node, boolean isInvisible) {
		Place place = pn.addPlace(node.getLabel());
		for (EPCEdge<?, ?> edge : epc.getInEdges(node)) {
			Transition transition = pn.addTransition(edge.getLabel());
			transition.setInvisible(isInvisible);
			Place inputPlace = pn.addPlace(edge.getLabel());
			pn.addArc(inputPlace, transition);
			pn.addArc(transition, place);
			targetMap.put(edge, inputPlace);
		}
		for (EPCEdge<?, ?> edge : epc.getOutEdges(node)) {
			Transition transition = pn.addTransition(edge.getLabel());
			transition.setInvisible(isInvisible);
			Place outputPlace = pn.addPlace(edge.getLabel());
			pn.addArc(place, transition);
			pn.addArc(transition, outputPlace);
			sourceMap.put(edge, outputPlace);
		}
	}

	private void generateOrNode(EPCNode node, boolean isInvisible) {
		Place place = pn.addPlace(node.getLabel());
		for (EPCEdge<?, ?> inputEdge : epc.getInEdges(node)) {
			Place inputPlace = pn.addPlace(inputEdge.getLabel());
			targetMap.put(inputEdge, inputPlace);
		}
		for (EPCEdge<?, ?> outputEdge : epc.getOutEdges(node)) {
			Place outputPlace = pn.addPlace(outputEdge.getLabel());
			sourceMap.put(outputEdge, outputPlace);
		}
		Set<EPCEdge<?, ?>> inputEdges = new HashSet<EPCEdge<?, ?>>(epc
				.getInEdges(node));
		generateOrNodeInput(node, inputEdges, new HashSet<EPCEdge<?, ?>>(),
				isInvisible, place);
		Set<EPCEdge<?, ?>> outputEdges = new HashSet<EPCEdge<?, ?>>(epc
				.getOutEdges(node));
		generateOrNodeOutput(node, outputEdges, new HashSet<EPCEdge<?, ?>>(),
				isInvisible, place);
	}

	private void generateOrNodeInput(EPCNode node,
			Set<EPCEdge<?, ?>> candidateEdges, Set<EPCEdge<?, ?>> edges,
			boolean isInvisible, Place place) {
		if (candidateEdges.isEmpty()) {
			if (!edges.isEmpty()) {
				Transition transition = pn.addTransition(node.getLabel());
				transition.setInvisible(isInvisible);
				for (EPCEdge<?, ?> edge : edges) {
					pn.addArc(targetMap.get(edge), transition);
				}
				pn.addArc(transition, place);
			}
		} else {
			EPCEdge<?, ?> edge = candidateEdges.iterator().next();
			candidateEdges.remove(edge);
			generateOrNodeInput(node, candidateEdges, edges, isInvisible, place);
			edges.add(edge);
			generateOrNodeInput(node, candidateEdges, edges, isInvisible, place);
			edges.remove(edge);
			candidateEdges.add(edge);
		}
	}

	private void generateOrNodeOutput(EPCNode node,
			Set<EPCEdge<?, ?>> candidateEdges, Set<EPCEdge<?, ?>> edges,
			boolean isInvisible, Place place) {
		if (candidateEdges.isEmpty()) {
			if (!edges.isEmpty()) {
				Transition transition = pn.addTransition(node.getLabel());
				transition.setInvisible(isInvisible);
				pn.addArc(place, transition);
				for (EPCEdge<?, ?> edge : edges) {
					pn.addArc(transition, sourceMap.get(edge));
				}
			}
		} else {
			EPCEdge<?, ?> edge = candidateEdges.iterator().next();
			candidateEdges.remove(edge);
			generateOrNodeOutput(node, candidateEdges, edges, isInvisible,
					place);
			edges.add(edge);
			generateOrNodeOutput(node, candidateEdges, edges, isInvisible,
					place);
			edges.remove(edge);
			candidateEdges.add(edge);
		}
	}

	private void generateEdge(EPCEdge<?, ?> edge, boolean isInvisible) {
		Place sourcePlace = sourceMap.get(edge);
		Place targetPlace = targetMap.get(edge);
		if (sourcePlace != null && targetPlace != null) {
			Transition transition = pn.addTransition(edge.getLabel());
			transition.setInvisible(isInvisible);
			pn.addArc(sourcePlace, transition);
			pn.addArc(transition, targetPlace);
		}
	}
}
