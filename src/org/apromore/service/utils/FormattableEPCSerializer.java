package org.apromore.service.utils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apromore.common.FSConstants;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.MiningConfig;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Chathura Ekanayake
 *
 */
public class FormattableEPCSerializer {
	
	private static final int FUNTION_WIDTH = 150;
	private static final int EVENT_WIDTH = FUNTION_WIDTH;

	private static Logger log = LoggerFactory.getLogger(FormattableEPCSerializer.class);

	private Random random;
	
	private int currentId;
	private Map<FlowNode, Integer> psToEpc;

	public FormattableEPCSerializer() {
		random = new Random();
		psToEpc = new HashMap<FlowNode, Integer>();
		currentId = 0;
	}
	
	private int getNextId() {
		currentId++;
		return currentId;
	}

//	public void serializeAllContents() {
//
//		Connection con = DBManager.getDBManager().getConnection();
//		try {
//			List<String> contentIDs = GraphDAO.getContentIDs(con);
//			for (String contentID : contentIDs) {
//				ProcessModelGraph graph = GraphDAO.getGraph(contentID, con);
//				String fragmentFileName = "e:/tests/fragmentstore3/data/fragments/content_" + contentID + ".epml";
//				log.debug("============= Content " + contentID + "=================");
//				FragmentUtil.printGraph(graph);
//				serialize(graph, fragmentFileName);
//			}
//
//		} catch (Exception e) {
//			String msg = "Failed to serialize all contents.";
//			log.error(msg, e);
//			try {
//				con.rollback();
//			} catch (SQLException e1) {
//				String msg1 = "Failed to roll back.";
//				log.error(msg1, e1);
//			}
//		} finally {
//			if (con != null) {
//				try {
//					con.close();
//				} catch (SQLException e) {
//					String msg = "Failed to close the database connection.";
//					log.error(msg, e);
//				}
//			}
//		}
//	}
	
	public String serializeToString(CPF g) {
		Document epml = serializeToEPML(g);
		String epmlString = XMLUtils.xmlToString(epml);
		return epmlString;
	}
	
	public Document serializeToEPML(CPF g) {

		log.debug("Serializing process model to EPML ...");
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

			FormattedGraph fg = new FormattedGraph(g, FUNTION_WIDTH);
			Collection<FlowNode> vertices = fg.getVertices();
			for (FlowNode v : vertices) {
//				String label = v.getName().replaceAll("\n", " ");
				String label = formatLabel(v.getName());
				String type = fg.getVertexProperty(v.getId(), FSConstants.TYPE);
				
				int vid = getNextId();
				psToEpc.put(v, vid);

				if ("Function".equals(type)) {
					addFunction(vid, label, fg.getCoordinates(v), epcElement, s);
				} else if ("Event".equals(type)) {
					addEvent(vid, label, fg.getCoordinates(v), epcElement, s);
				} else if ("Connector".equals(type)) {
					addConnector(vid, label, fg.getCoordinates(v), epcElement, s);
				} else if ("Pocket".equals(type)) {
					addFunction(vid, label, fg.getCoordinates(v), epcElement, s);
				} else {
					addFunction(vid, label, fg.getCoordinates(v), epcElement, s);
				}
			}

			Collection<ControlFlow<FlowNode>> edges = fg.getEdges();
			for (ControlFlow<FlowNode> edge : edges) {
				int edgeID = getNextId();
				if (psToEpc.get(edge.getSource()) == null || psToEpc.get(edge.getTarget()) == null) {
					continue;
				}
				
				int source = psToEpc.get(edge.getSource());
				int target = psToEpc.get(edge.getTarget());
				addEdge(edgeID, source, target, epcElement, s);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} 
		return s;
	}

	public void serialize(CPF g, String fileName) {

		log.debug("Serializing process model to file: " + fileName);
		
		try {
			
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = fac.newDocumentBuilder();
			Document s = docBuilder.newDocument();
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

			FormattedGraph fg = null;
			try {
				fg = new FormattedGraph(g, FUNTION_WIDTH);
			} catch (Exception e3) {
				fg = new FormattedGraph(g, false);
			}
			Collection<FlowNode> vertices = fg.getVertices();
			for (FlowNode v : vertices) {
//				String label = v.getName().replaceAll("\n", " ");
				String label = formatLabel(v.getName());
				String type = fg.getVertexProperty(v.getId(), FSConstants.TYPE);
				
				int vid = getNextId();
				psToEpc.put(v, vid);

				if ("Function".equals(type)) {
					addFunction(vid, label, fg.getCoordinates(v), epcElement, s);
				} else if ("Event".equals(type)) {
					addEvent(vid, label, fg.getCoordinates(v), epcElement, s);
				} else if ("Connector".equals(type)) {
					addConnector(vid, label, fg.getCoordinates(v), epcElement, s);
				} else if ("Pocket".equals(type)) {
					addFunction(vid, label, fg.getCoordinates(v), epcElement, s);
				} else {
					addFunction(vid, label, fg.getCoordinates(v), epcElement, s);
				}
			}

			Collection<ControlFlow<FlowNode>> edges = fg.getEdges();
			for (ControlFlow<FlowNode> edge : edges) {
				try {
					int edgeID = getNextId();
					int source = psToEpc.get(edge.getSource());
					int target = psToEpc.get(edge.getTarget());
					addEdge(edgeID, source, target, epcElement, s);
				} catch (Exception e2) {
					
				}
			}

			XMLUtils.xmlToFile(s, new File(fileName));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} 
	}

	private String formatLabel(String label) {
		if (label == null) {
			return null;
		}
		
		String f = label.trim();
		f = f.replaceAll("\n", " ");
//		f = f.replaceAll("'", "&apos;");
		
		if (!MiningConfig.ADD_LINE_BREAKS_WHEN_FORMATTING_LABELS) {
			return f;
		}
		
		int l = f.length();
		if (l <= FormattedGraph.MAX_CHARACTORS_PER_LINE) {
			return label;
		}
		
		f = new StringBuffer(f).insert(FormattedGraph.MAX_CHARACTORS_PER_LINE, "\n").toString();
		
		if (l < 2 * FormattedGraph.MAX_CHARACTORS_PER_LINE) {
			return f;
		}
		
		f = new StringBuffer(f).insert(2 * FormattedGraph.MAX_CHARACTORS_PER_LINE, "\n").toString();
		
		if (l >= 3 * FormattedGraph.MAX_CHARACTORS_PER_LINE) {
			f = f.substring(0, (3 * FormattedGraph.MAX_CHARACTORS_PER_LINE) - 3);
			f += "...";
		}
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

	private void addFunction(Integer id, String label, Point point, Element epcElement, Document doc) {

		Element functionElement = XMLUtils.addElement(epcElement, "function", "id", id.toString(), doc);
		XMLUtils.addElement(functionElement, "name", label, doc);
		Element graphicsElement = XMLUtils.addElement(functionElement, "graphics", doc);
		Element positionElement = XMLUtils.addElement(graphicsElement, "position", doc);
		positionElement.setAttribute("height", Integer.toString(FormattedGraph.ACTIVITY_HEIGHT));
		positionElement.setAttribute("width", Integer.toString(FUNTION_WIDTH));
		positionElement.setAttribute("x", Integer.toString(point.x));
		positionElement.setAttribute("y", Integer.toString(point.y));
	}

	private void addEvent(Integer id, String label, Point point, Element epcElement, Document doc) {

		Element eventElement = XMLUtils.addElement(epcElement, "event", "id", id.toString(), doc);
		XMLUtils.addElement(eventElement, "name", label, doc);
		Element graphicsElement = XMLUtils.addElement(eventElement, "graphics", doc);
		Element positionElement = XMLUtils.addElement(graphicsElement, "position", doc);
		positionElement.setAttribute("height", Integer.toString(FormattedGraph.EVENT_HEIGHT));
		positionElement.setAttribute("width", Integer.toString(EVENT_WIDTH));
		positionElement.setAttribute("x", Integer.toString(point.x));
		positionElement.setAttribute("y", Integer.toString(point.y));
	}

	private void addConnector(Integer id, String label, Point point, Element epcElement, Document doc) {

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
		positionElement.setAttribute("x", Integer.toString(point.x));
		positionElement.setAttribute("y", Integer.toString(point.y));
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
