package org.prom6.models.heuristics;

import java.util.Collection;
import java.util.Set;

import org.processmining.models.graphbased.directed.DirectedGraph;
import org.prom6.models.heuristics.elements.Activity;
import org.prom6.models.heuristics.elements.Flow;
import org.prom6.models.heuristics.elements.Gateway;
import org.prom6.models.heuristics.elements.HNEdge;
import org.prom6.models.heuristics.elements.HNNode;
import org.prom6.models.heuristics.elements.Gateway.GatewayType;

public interface HeuristicsNetDiagram extends DirectedGraph<HNNode, HNEdge<? extends HNNode, ? extends HNNode>> {

	String getLabel();

	//Activities
	Activity addActivity(String label, boolean bLooped, boolean bAdhoc, boolean bCompensation, boolean bMultiinstance,
			boolean bCollapsed);

	Activity removeActivity(Activity activity);

	Collection<Activity> getActivities();

	//Gateways
	Gateway addGateway(String label, GatewayType gatewayType);

	Gateway removeGateway(Gateway gateway);

	Collection<Gateway> getGateways();

	//Flows
	Flow addFlow(HNNode source, HNNode target, String label);

	Set<Flow> getFlows();
}
