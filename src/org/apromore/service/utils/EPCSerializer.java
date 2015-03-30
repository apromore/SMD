package org.apromore.service.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apromore.common.FSConstants;
import org.apromore.graph.JBPT.CPF;
import org.apromore.util.FragmentUtil;
import org.apromore.util.XMLUtils;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hpi.bpt.graph.DirectedEdge;
import de.hpi.bpt.hypergraph.abs.Vertex;

/**
 * @author Chathura Ekanayake
 */
public class EPCSerializer {
	
	private static final int FUNTION_WIDTH = 100;
	private static final int EVENT_WIDTH = FUNTION_WIDTH;

	private static Logger LOGGER = LoggerFactory.getLogger(EPCSerializer.class);

	private Random random;
	
	private int currentId;
	private Map<FlowNode, Integer> psToEpc;

	public EPCSerializer() {
		random = new Random();
		psToEpc = new HashMap<FlowNode, Integer>();
		currentId = 0;
	}
	
	private int getNextId() {
		currentId++;
		return currentId;
	}
	
	public String serializeToString(CPF g) {
		Document epml = serializeToEPML(g);
		String epmlString = XMLUtils.xmlToString(epml);
		epmlString = epmlString.replace("xmlns=\"http://www.epml.de\"", "").replace("<epml ", "<epml:epml ").replace("</epml>", "</epml:epml>");
		return epmlString;
	}
	
	public void serializeToFile(CPF g, String fileName) {
		Document epml = serializeToEPML(g);
		XMLUtils.xmlToFile(epml, new File(fileName));
	}
	
	public Document serializeToEPML(CPF g) {

		LOGGER.debug("Serializing process model to EPML ...");
		Document s = null;
		try {
			
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = fac.newDocumentBuilder();
			s = docBuilder.newDocument();
			Element epmlElement = s.createElementNS("http://www.epml.de", "epml");
			epmlElement.setAttribute("xmlns:epml", "http://www.epml.de");
			epmlElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			epmlElement.setAttribute("xsi:schemaLocation", "http://www.epml.de EPML_2.0.xsd");
			s.appendChild(epmlElement);

			Element epcElement = XMLUtils.addElement(epmlElement, "epc", s);
			epcElement.setAttribute("epcId", "1");
			epcElement.setAttribute("name", "EPC");
			
			Map<String, String> properties = g.getProperties();
			Set<String> propNames = properties.keySet();
			for (String propName : propNames) {
				String propValue = properties.get(propName);
				Element propElement = XMLUtils.addElement(epcElement, "property", s);
				propElement.setAttribute("name", propName);
				propElement.setAttribute("value", propValue);
			}

//			FormattedGraph fg = new FormattedGraph(g, FUNTION_WIDTH);
			Collection<FlowNode> vertices = g.getFlowNodes();
			for (FlowNode v : vertices) {
//				String label = v.getName().replaceAll("\n", " ");
				String label = formatLabel(v.getName());
				String type = g.getVertexProperty(v.getId(), FSConstants.TYPE);
				
				int vid = getNextId();
				psToEpc.put(v, vid);

				if ("Function".equals(type)) {
					addFunction(vid, label, epcElement, s);
				} else if ("Event".equals(type)) {
					addEvent(vid, label, epcElement, s);
				} else if ("Connector".equals(type)) {
					addConnector(vid, label, epcElement, s);
				} else if ("Pocket".equals(type)) {
					addFunction(vid, label, epcElement, s);
				} else {
					addFunction(vid, label, epcElement, s);
				}
			}

			Collection<ControlFlow<FlowNode>> edges = g.getEdges();
			for (ControlFlow<FlowNode> edge : edges) {
				int edgeID = getNextId();
				int source = psToEpc.get(edge.getSource());
				int target = psToEpc.get(edge.getTarget());
				addEdge(edgeID, source, target, epcElement, s);
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			e.printStackTrace();
		} 
		return s;
	}
	
	private String formatLabel(String label) {
		if (label == null) {
			return null;
		}
		
		String f = label.trim();
		f = f.replaceAll("\n", " ");
		f = f.replaceAll("'", " ");
		return f;
	}

	private void addEdge(Integer id, Integer source, Integer target, Element epcElement, Document doc) {

		Element arcElement = XMLUtils.addElement(epcElement, "arc", "id", id.toString(), doc);
		Element flowElement = XMLUtils.addElement(arcElement, "flow", doc);
		flowElement.setAttribute("source", source.toString());
		flowElement.setAttribute("target", target.toString());
		Element graphicsElement = XMLUtils.addElement(arcElement, "graphics", doc);
		Element position1Element = XMLUtils.addElement(graphicsElement, "position", doc);
		position1Element.setAttribute("x", "200");
		position1Element.setAttribute("y", "100");
		Element position2Element = XMLUtils.addElement(graphicsElement, "position", doc);
		position2Element.setAttribute("x", "200");
		position2Element.setAttribute("y", "100");
	}

	private void addFunction(Integer id, String label, Element epcElement, Document doc) {

		Element functionElement = XMLUtils.addElement(epcElement, "function", "id", id.toString(), doc);
		XMLUtils.addElement(functionElement, "name", label, doc);
		Element graphicsElement = XMLUtils.addElement(functionElement, "graphics", doc);
		Element positionElement = XMLUtils.addElement(graphicsElement, "position", doc);
		positionElement.setAttribute("height", "60");
		positionElement.setAttribute("width", Integer.toString(FUNTION_WIDTH));
		positionElement.setAttribute("x", Integer.toString(0));
		positionElement.setAttribute("y", Integer.toString(0));
	}

	private void addEvent(Integer id, String label, Element epcElement, Document doc) {

		Element eventElement = XMLUtils.addElement(epcElement, "event", "id", id.toString(), doc);
		XMLUtils.addElement(eventElement, "name", label, doc);
		Element graphicsElement = XMLUtils.addElement(eventElement, "graphics", doc);
		Element positionElement = XMLUtils.addElement(graphicsElement, "position", doc);
		positionElement.setAttribute("height", "60");
		positionElement.setAttribute("width", Integer.toString(EVENT_WIDTH));
		positionElement.setAttribute("x", Integer.toString(0));
		positionElement.setAttribute("y", Integer.toString(0));
	}

	private void addConnector(Integer id, String label, Element epcElement, Document doc) {

		String elementName = "xor";
		if ("XOR".equals(label)) {
			elementName = "xor";
		} else if ("OR".equals(label)) {
			elementName = "or";
		} else if ("AND".equals(label)) {
			elementName = "and";
		}

		Element connectorElement = XMLUtils.addElement(epcElement, elementName, "id", id.toString(), doc);
		Element graphicsElement = XMLUtils.addElement(connectorElement, "graphics", doc);
		Element positionElement = XMLUtils.addElement(graphicsElement, "position", doc);
		positionElement.setAttribute("height", "30");
		positionElement.setAttribute("width", "30");
		positionElement.setAttribute("x", Integer.toString(0));
		positionElement.setAttribute("y", Integer.toString(0));
	}

	private String getX() {
		int x = random.nextInt(600) + 440;
		return String.valueOf(x);
	}

	private String getY() {
		int y = random.nextInt(540) + 40;
		return String.valueOf(y);
	}
}
