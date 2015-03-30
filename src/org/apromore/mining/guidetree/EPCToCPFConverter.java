package org.apromore.mining.guidetree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apromore.common.FSConstants;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfAndGateway;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.graph.JBPT.CpfGateway;
import org.apromore.graph.JBPT.CpfOrGateway;
import org.apromore.graph.JBPT.CpfTask;
import org.apromore.graph.JBPT.CpfXorGateway;
import org.jbpt.pm.FlowNode;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.epc.EPCEdge;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.processmining.models.graphbased.directed.epc.elements.Connector;
import org.processmining.models.graphbased.directed.epc.elements.Connector.ConnectorType;
import org.processmining.models.graphbased.directed.epc.elements.Event;
import org.processmining.models.graphbased.directed.epc.elements.Function;

public class EPCToCPFConverter {

	public static CPF convert(ConfigurableEPC epc) {
		
		CPF cpf = new CPF();
		
		Map<EPCNode, FlowNode> vmap = new HashMap<EPCNode, FlowNode>();
		
		Collection<Function> functions = epc.getFunctions();
		for (Function f : functions) {
			addFunction(f, cpf, vmap);
		}
		
		Collection<Event> events = epc.getEvents();
		for (Event e : events) {
			addEvent(e, cpf, vmap);
		}
		
		Collection<Connector> connectors = epc.getConnectors();
		for (Connector c : connectors) {
			addGateway(c, cpf, vmap);
		}
		
		Set<EPCEdge<? extends EPCNode,? extends EPCNode>> edges = epc.getEdges();
		for (EPCEdge<? extends EPCNode, ? extends EPCNode> edge : edges) {
			addEdge(edge, cpf, vmap);
		}
		
		return cpf;
	}
	
	private static void addGateway(Connector c, CPF cpf, Map<EPCNode, FlowNode> vmap) {
		String label = c.getLabel();
		CpfGateway gateway = null;
		
		if (ConnectorType.XOR.equals(c.getType())) {
			gateway = new CpfXorGateway("XOR");
		} else if (ConnectorType.OR.equals(c.getType())) {
			gateway = new CpfOrGateway("OR");
		} else if (ConnectorType.AND.equals(c.getType())) {
			gateway = new CpfAndGateway("AND");
		}
		
		cpf.addVertex(gateway);
		cpf.setVertexProperty(gateway.getId(), FSConstants.TYPE, FSConstants.CONNECTOR);
		
		vmap.put(c, gateway);
	}

	private static void addEvent(Event e, CPF cpf, Map<EPCNode, FlowNode> vmap) {
		String label = e.getLabel();
		CpfEvent event = new CpfEvent(label);
		cpf.addVertex(event);
		cpf.setVertexProperty(event.getId(), FSConstants.TYPE, FSConstants.EVENT);
		
		vmap.put(e, event);
	}

	private static void addFunction(Function f, CPF cpf, Map<EPCNode, FlowNode> vmap) {
		
		String label = f.getLabel();
		CpfTask task = new CpfTask(label);
		cpf.addVertex(task);
		cpf.setVertexProperty(task.getId(), FSConstants.TYPE, FSConstants.FUNCTION);
		
		vmap.put(f, task);
		
	}
	
	private static void addEdge(EPCEdge<? extends EPCNode, ? extends EPCNode> edge, CPF cpf, Map<EPCNode, FlowNode> vmap) {
		
		EPCNode epcSource = edge.getSource();
		FlowNode cpfSource = vmap.get(epcSource);
		
		EPCNode epcTarget = edge.getTarget();
		FlowNode cpfTarget = vmap.get(epcTarget);
		
		cpf.addEdge(cpfSource, cpfTarget);
	}
}
