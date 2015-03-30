package org.processmining.plugins.petrinet.epcconversion;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.epc.EPCFactory;
import org.processmining.models.graphbased.directed.epc.elements.Connector;
import org.processmining.models.graphbased.directed.epc.elements.Event;
import org.processmining.models.graphbased.directed.epc.elements.Function;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class EpcConversion {

	public static ConfigurableEPC convert(Petrinet net) {
		ConfigurableEPC epc = EPCFactory.newConfigurableEPC("C-EPC converted from " + net.getLabel());
		Map<PetrinetNode, Connector> pointsOfEntry = new HashMap<PetrinetNode, Connector>();
		Map<PetrinetNode, Connector> pointsOfExit = new HashMap<PetrinetNode, Connector>();
		Collection<Place> places = new HashSet<Place>();
		places.addAll(net.getPlaces());
		Collection<Transition> transitions = new HashSet<Transition>();
		transitions.addAll(net.getTransitions());
		Map<Transition, Collection<PetrinetNode>> joinSpheres = new HashMap<Transition, Collection<PetrinetNode>>();
		Map<Transition, Collection<PetrinetNode>> splitSpheres = new HashMap<Transition, Collection<PetrinetNode>>();

		// Map all non-silent transitions.
		for (Transition transition : net.getTransitions()) {
			if (!transition.isInvisible()) {
				Collection<PetrinetNode> joinSphere = getSphere(net, transition, true, false);
				Collection<PetrinetNode> splitSphere = getSphere(net, transition, false, false);
				joinSpheres.put(transition, joinSphere);
				splitSpheres.put(transition, splitSphere);
				places.removeAll(joinSphere);
				places.removeAll(splitSphere);
				transitions.removeAll(joinSphere);
				transitions.removeAll(splitSphere);

				Function function = epc.addFunction(transition.getLabel());

				Event event = epc.addEvent(transition.getLabel() + " [event]");
				epc.addArc(event, function);

				Connector.ConnectorType joinType = getType(net, transition, joinSphere, true);
				Connector joinConnector = epc.addConnector(transition.getLabel() + " [join]", joinType);
				;
				epc.addArc(joinConnector, event);

				Connector.ConnectorType splitType = getType(net, transition, splitSphere, false);
				Connector splitConnector = epc.addConnector(transition.getLabel() + " [split]", splitType);
				;
				epc.addArc(function, splitConnector);

				pointsOfEntry.put(transition, joinConnector);
				if (net.getOutEdges(transition).isEmpty()) {
					Event completedEvent = epc.addEvent(transition.getLabel() + " [complete]");
					epc.addArc(splitConnector, completedEvent);
				} else {
					pointsOfExit.put(transition, splitConnector);
				}
			}
		}

		// Second, remaining silent transitions are mapped
		while (!transitions.isEmpty()) {
			Transition maxTransition = null;
			int size, maxSize = -1;
			Collection<PetrinetNode> joinSphere, splitSphere;
			for (Transition transition : transitions) {
				joinSphere = getSphere(net, transition, true, false);
				splitSphere = getSphere(net, transition, false, false);
				size = joinSphere.size() + splitSphere.size();
				if (size > maxSize) {
					maxTransition = transition;
					maxSize = size;
				}
			}
			Transition transition = maxTransition;
			joinSphere = getSphere(net, transition, true, false);
			splitSphere = getSphere(net, transition, false, false);
			joinSpheres.put(transition, joinSphere);
			splitSpheres.put(transition, splitSphere);
			places.removeAll(joinSphere);
			places.removeAll(splitSphere);
			transitions.removeAll(joinSphere);
			transitions.removeAll(splitSphere);

			Connector.ConnectorType joinType = getType(net, transition, joinSphere, true);
			Connector joinConnector = epc.addConnector(transition.getLabel() + " [join]", joinType);

			Connector.ConnectorType splitType = getType(net, transition, splitSphere, false);
			Connector splitConnector = epc.addConnector(transition.getLabel() + " [split]", splitType);

			epc.addArc(joinConnector, splitConnector);

			pointsOfEntry.put(transition, joinConnector);
			pointsOfExit.put(transition, splitConnector);
		}

		// Third, add all mapped transitions as YAWL tasks (has been done now in previous steps

		// Fourth, add all remaining places as YAWL conditions
		for (Place place : places) {
			if (net.getInEdges(place).isEmpty()) {
				boolean needStartEvent = true;
				if (net.getOutEdges(place).size() == 1) {
					Transition transition = (Transition) net.getOutEdges(place).iterator().next().getTarget();
					if (!transition.isInvisible()) {
						needStartEvent = false; // Don't introduce if the only successor is a visible task.
					}
				} else if (net.getOutEdges(place).size() == 0) {
					epc.addEvent(place.getLabel());
					needStartEvent = false;
				}
				if (needStartEvent) {
					Function function = epc.addFunction("[start function]", false);
					Event event = epc.addEvent("[start event]");
					epc.addArc(event, function);
					Connector splitConnector = epc.addConnector("[start split]", Connector.ConnectorType.XOR);
					epc.addArc(function, splitConnector);
					pointsOfExit.put(place, splitConnector);
				}
			} else if (net.getOutEdges(place).isEmpty()) {
				Connector joinConnector = epc.addConnector(place.getLabel() + " [join]", Connector.ConnectorType.XOR);
				Event event = epc.addEvent(place.getLabel());
				epc.addArc(joinConnector, event);
				pointsOfEntry.put(place, joinConnector);
			} else {
				// A YAWL condition is broken down into an XOR join connector and an XOR split connector.
				Connector joinConnector = epc.addConnector(place.getLabel() + " [join]", Connector.ConnectorType.XOR);
				Connector splitConnector = epc.addConnector(place.getLabel() + " [split]", Connector.ConnectorType.XOR);
				epc.addArc(joinConnector, splitConnector);
				pointsOfEntry.put(place, joinConnector);
				pointsOfExit.put(place, splitConnector);
			}
		}

		// Fifth, add edges between YAWL tasks
		for (Object joinTransition : joinSpheres.keySet()) {
			for (Object splitTransition : splitSpheres.keySet()) {
				Collection<PetrinetNode> joinSphere = joinSpheres.get(joinTransition);
				Collection<PetrinetNode> splitSphere = splitSpheres.get(splitTransition);
				if (joinTransition == splitTransition) {
					joinSphere.remove(joinTransition);
					splitSphere.remove(splitTransition);
				}
				if (connect(net, splitSphere, joinSphere)) {
					Connector pointOfExit = pointsOfExit.get(splitTransition);
					Connector pointOfEntry = pointsOfEntry.get(joinTransition);
					if ((pointOfExit != null) && (pointOfEntry != null)) {
						pointsOfExit.remove(splitTransition);
						pointsOfEntry.remove(joinTransition);
						epc.addArc(pointOfExit, pointOfEntry);
						pointsOfExit.put((PetrinetNode) splitTransition, pointOfExit);
						pointsOfEntry.put((PetrinetNode) joinTransition, pointOfEntry);
					}
				}
				if (joinTransition == splitTransition) {
					joinSphere.add((PetrinetNode) joinTransition);
					splitSphere.add((PetrinetNode) splitTransition);
				}
			}
		}

		// Sixth, add edges from YAWL task to YAWL conditions and v.v.
		for (Object transition : splitSpheres.keySet()) {
			for (Place place : places) {
				Collection<PetrinetNode> placeSphere = new HashSet<PetrinetNode>();
				placeSphere.add(place);
				Collection<PetrinetNode> joinSphere = joinSpheres.get(transition);
				Collection<PetrinetNode> splitSphere = splitSpheres.get(transition);
				if (connect(net, splitSphere, placeSphere)) {
					Connector pointOfExit = pointsOfExit.get(transition);
					Connector pointOfEntry = pointsOfEntry.get(place);
					if ((pointOfExit != null) && (pointOfEntry != null)) {
						pointsOfExit.remove(transition);
						pointsOfEntry.remove(place);
						epc.addArc(pointOfExit, pointOfEntry);
						pointsOfExit.put((PetrinetNode) transition, pointOfExit);
						pointsOfEntry.put(place, pointOfEntry);
					}
				}
				if (connect(net, placeSphere, joinSphere)) {
					Connector pointOfExit = pointsOfExit.get(place);
					Connector pointOfEntry = pointsOfEntry.get(transition);
					if ((pointOfExit != null) && (pointOfEntry != null)) {
						pointsOfExit.remove(place);
						pointsOfEntry.remove(transition);
						epc.addArc(pointOfExit, pointOfEntry);
						pointsOfExit.put(place, pointOfExit);
						pointsOfEntry.put((PetrinetNode) transition, pointOfEntry);
					}
				}
			}
		}

		// Remove any source connect. because we did not add a new start event (and function), 
		// any original start event will generate a source connector.
		Iterator<Connector> it = epc.getConnectors().iterator();
		while (it.hasNext()) {
			Connector connector = it.next();
			if (epc.getInEdges(connector).isEmpty()) {
				epc.removeConnector(connector);
				it = epc.getConnectors().iterator();
			}
		}

		// Finally, reduce any connector that has one input and one output.
		it = epc.getConnectors().iterator();
		while (it.hasNext()) {
			Connector connector = it.next();
			if ((epc.getInEdges(connector).size() == 1) && (epc.getOutEdges(connector).size() == 1)) {
				epc.addArc(epc.getInEdges(connector).iterator().next().getSource(), epc.getOutEdges(connector)
						.iterator().next().getTarget());
				epc.removeEdge(epc.getInEdges(connector).iterator().next());
				epc.removeEdge(epc.getOutEdges(connector).iterator().next());
				epc.removeConnector(connector);
				it = epc.getConnectors().iterator();
			}
		}

		return epc;
	}

	public static Collection<PetrinetNode> getSphere(Petrinet net, Transition labeledTransition, boolean isJoin,
			boolean isSimple) {
		Collection<PetrinetNode> sphere = new HashSet<PetrinetNode>();
		sphere.add(labeledTransition);
		boolean done = false, inSphere;
		Place place;
		Transition transition;
		Iterator<Place> itPlace;
		Iterator<Transition> itTransition;
		Iterator<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> itEdge;

		while (!done) {
			done = true; // No place has been added yet.
			// First, find and add places for which all output (input) transitions are already part of the sphere.
			itPlace = net.getPlaces().iterator();
			while (itPlace.hasNext()) {
				place = itPlace.next();
				if (!sphere.contains(place) && (net.getOutEdges(place).size() > 0)
						&& (net.getInEdges(place).size() > 0)) {
					if (isJoin) {
						itEdge = net.getOutEdges(place).iterator();
					} else {
						itEdge = net.getInEdges(place).iterator();
					}
					// Check whether all output (input) transitions are already in sphere.
					inSphere = true;
					while (inSphere && itEdge.hasNext()) {
						if (sphere.contains(isJoin ? itEdge.next().getTarget() : itEdge.next().getSource())) {
						} else {
							inSphere = false;
						}
					}
					if (inSphere) { // Yes, they are. Add this place.
						sphere.add(place);
						done = false; // A place has been added.
					}
				}
			}
			if (!done) { // Only if some places have been added to the sphere.
				done = true; // No transition has been added yet.
				// Second, find and add transitions for which all output (input) places are already part of the sphere.
				itTransition = net.getTransitions().iterator();
				while (itTransition.hasNext() && (!isSimple || !labeledTransition.isInvisible())) {
					transition = itTransition.next();
					if (!sphere.contains(transition) && transition.isInvisible()) {
						if (isJoin) {
							itEdge = net.getOutEdges(transition).iterator();
						} else {
							itEdge = net.getInEdges(transition).iterator();
						}
						// Check whether all output (input) places are already in sphere.
						inSphere = true;
						while (inSphere && itEdge.hasNext()) {
							if (sphere.contains(isJoin ? itEdge.next().getTarget() : itEdge.next().getSource())) {
							} else {
								inSphere = false;
							}
						}
						if (inSphere) { // Yes, they are. Add this transition.
							sphere.add(transition);
							done = isSimple; // A transition has been added.
						}
					}
				}
			}
		}
		return sphere;
	}

	private static Connector.ConnectorType getType(Petrinet net, Transition sphereTransition,
			Collection<PetrinetNode> sphere, boolean isJoin) {
		boolean isNone = true, isAnd = true, isXor = true;
		Iterator<PetrinetNode> it = sphere.iterator();
		while (it.hasNext() && (isNone || isAnd || isXor)) {
			Object object = it.next();
			if (object instanceof Place) {
				Place place = (Place) object;
				if (net.getInEdges(place).size() != 1) { // If place with multiple inputs
					// Keep only isXor and only if join sphere.
					isNone = false;
					isAnd = false;
					if (!isJoin) {
						isXor = false;
					}
				}
				if (net.getOutEdges(place).size() != 1) { // If place has multiple outputs
					// Keep only isXor and only if split sphere.
					isNone = false;
					isAnd = false;
					if (isJoin) {
						isXor = false;
					}
				}
			} else {
				Transition transition = (Transition) object;
				if (((transition != sphereTransition) || isJoin) && // Ignore inputs of sphere transition if join sphere
						(net.getInEdges(transition).size() != 1)) { // If transition has multiple inputs
					// Keep only isAnd and only if join sphere.
					isNone = false;
					isXor = false;
					if (!isJoin) {
						isAnd = false;
					}
				}
				if (((transition != sphereTransition) || !isJoin) && // Ignore outputs of sphere transition if split sphere
						(net.getOutEdges(transition).size() != 1)) { // If transition has multiple outputs
					// Keep only isAnd and only if split sphere
					isNone = false;
					isXor = false;
					if (isJoin) {
						isAnd = false;
					}
				}
			}
		}
		if (isNone) {
			return Connector.ConnectorType.AND;
		} else if (isAnd) {
			return Connector.ConnectorType.AND;
		} else if (isXor) {
			return Connector.ConnectorType.XOR;
		} else {
			return Connector.ConnectorType.OR;
		}
	}

	private static boolean connect(Petrinet net, Collection<PetrinetNode> splitSphere,
			Collection<PetrinetNode> joinSphere) {
		boolean connect = false;
		Iterator<PetrinetNode> it = splitSphere.iterator();
		while (!connect && it.hasNext()) {
			Object object = it.next();
			if (joinSphere.contains(object)) {
				connect = true;
			} else {
				if (object instanceof Place) {
					Place place = (Place) object;
					Iterator<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> it2 = net.getOutEdges(place)
							.iterator();
					while (!connect && it2.hasNext()) {
						Transition transition = (Transition) it2.next().getTarget();
						if (joinSphere.contains(transition)) {
							connect = true;
						}
					}
				} else {
					Transition transition = (Transition) object;
					Iterator<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> it2 = net.getOutEdges(
							transition).iterator();
					while (!connect && it2.hasNext()) {
						Place place = (Place) it2.next().getTarget();
						if (joinSphere.contains(place)) {
							connect = true;
						}
					}
				}
			}
		}
		return connect;
	}
}
