package org.apromore.service.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apromore.common.FSConstants;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfAndGateway;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.graph.JBPT.CpfOrGateway;
import org.apromore.graph.JBPT.CpfTask;
import org.apromore.graph.JBPT.CpfXorGateway;
import org.jbpt.pm.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.hpi.bpt.hypergraph.abs.Vertex;

//import fragstore.FSConstants;
//import fragstore.database.dao.GraphDAO;
//import fragstore.graph.ProcessModelGraph;
//import fragstore.repository.IDGenerator;
//import fragstore.util.XMLUtils;

/**
 * @author Chathura Ekanayake
 *
 */
public class EPCDeserializer {
	
	private static Logger log = LoggerFactory.getLogger(EPCDeserializer.class);

	private static final String FUNCTION = "function";
	private static final String EVENT = "event";
	private static final String XOR = "xor";
	private static final String OR = "or";
	private static final String AND = "and";
	private static final String ARC = "arc";
	private static final String PROPERTY = "property";
	
	public CPF deserializeInputStream(InputStream stream) {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(stream);
			CPF g = deserialize(doc);
			return g;
		} catch (Exception e) {
			log.error("Failed to parse EPML from input stream", e);
			e.printStackTrace();
			return null;
		} 
	}

	public CPF deserializeFile(String fileName) {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File(fileName));
			CPF g = deserialize(doc);
			return g;
		} catch (Exception e) {
			log.error("Failed to parse file: " + fileName, e);
			e.printStackTrace();
			return null;
		} 
	}
	
	public CPF deserialize(Document doc) {

		CPF g = new CPF();
		
		try {
			Map<String, String> vmap = new HashMap<String, String>();

			Element docE = doc.getDocumentElement();

			Element epcE = XMLUtils.getChildElement("epc", docE);
			NodeList propNodes = epcE.getElementsByTagName(PROPERTY);
			for (int i = 0; i < propNodes.getLength(); i++) {
				Element p = (Element) propNodes.item(i);
				addProperty(p, g);
			}
			
			NodeList fl = epcE.getElementsByTagName(FUNCTION);
			for (int i = 0; i < fl.getLength(); i++) {
				Element f = (Element) fl.item(i);
				addFunction(f, g, vmap);
			}
			
			NodeList el = epcE.getElementsByTagName(EVENT);
			for (int i = 0; i < el.getLength(); i++) {
				Element ev = (Element) el.item(i);
				addEvent(ev, g, vmap);
			}

			NodeList xorl = epcE.getElementsByTagName(XOR);
			for (int i = 0; i < xorl.getLength(); i++) {
				Element xor = (Element) xorl.item(i);
				addXOR(xor, g, vmap);
			}
			
			NodeList orl = epcE.getElementsByTagName(OR);
			for (int i = 0; i < orl.getLength(); i++) {
				Element or = (Element) orl.item(i);
				addOR(or, g, vmap);
			}
			
			NodeList andl = epcE.getElementsByTagName(AND);
			for (int i = 0; i < andl.getLength(); i++) {
				Element and = (Element) andl.item(i);
				addAND(and, g, vmap);
			}
			
			NodeList arcl = epcE.getElementsByTagName(ARC);
			for (int i = 0; i < arcl.getLength(); i++) {
				Element arc = (Element) arcl.item(i);
				addArc(arc, g, vmap);
			}
			
//			removeUnconnectedNodes(g);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return g;
	}
	
	private void addProperty(Element p, CPF g) {
		String name = p.getAttribute("name");
		String value = p.getAttribute("value");
		g.setProperty(name, value);
	}

	private void removeUnconnectedNodes(CPF g) {
		
		g.removeVertices(g.getDisconnectedVertices());
		
//		Set<Integer> unconnectedNodes = new HashSet<Integer>();
//		Set<Integer> nodes = g.getVertices();
//		for (int node: nodes) {
//			if (g.getPredecessorsOfVertex(node).size() == 0 && g.getSuccessorsOfVertex(node).size() == 0) {
//				unconnectedNodes.add(node);
//			}
//		}
//		nodes.removeAll(unconnectedNodes);
	}
	
	private void addArc(Element arcE, CPF g, Map<String, String> vmap) {

		Element flowE = XMLUtils.getChildElement("flow", arcE);
		String s = flowE.getAttribute("source");
		String t = flowE.getAttribute("target");
		
		log.debug("Adding arc: " + s + " -> " + t);
		
		Set<String> vkeys = vmap.keySet();
		if (vkeys.contains(s) && vkeys.contains(t)) {
			String es = vmap.get(s);
			String et = vmap.get(t);
			g.addEdge(g.getVertex(es), g.getVertex(et));
		} else {
			log.debug("Edge between non-existent vertices. Skipped...");
		}
	}

	private void addFunction(Element f, CPF g, Map<String, String> vmap) {

		String fid = f.getAttribute("id");
		Element nameE = XMLUtils.getChildElement("name", f);
		String label = nameE.getTextContent();
		if (label != null && label.length() > 0) {
			label = label.replaceAll("\n", " ");
		}

//		int vid = GraphDAO.getNextAvailableVertexID();
//		int vid = IDGenerator.getNextVertexId();
		FlowNode v = new CpfTask(label);
//		v.setId(uuid());
		g.addVertex(v);
		String vid = v.getId();
		g.setVertexProperty(vid, FSConstants.TYPE, FSConstants.FUNCTION);
		vmap.put(fid, vid);
	}

	private void addEvent(Element ev, CPF g, Map<String, String> vmap) {

		String eid = ev.getAttribute("id");
		Element nameE = XMLUtils.getChildElement("name", ev);
		String label = nameE.getTextContent();
		if (label != null && label.length() > 0) {
			label = label.replaceAll("\n", " ");
		}
		
		FlowNode v = new CpfEvent(label);
//		v.setId(uuid());
		g.addVertex(v);
		String vid = v.getId();
		g.setVertexProperty(vid, FSConstants.TYPE, FSConstants.EVENT);
		vmap.put(eid, vid);
	}

	private void addXOR(Element e, CPF g, Map<String, String> vmap) {
		String eid = e.getAttribute("id");
		FlowNode v = new CpfXorGateway("XOR");
		String vid = v.getId();
		g.addVertex(v);
		g.setVertexProperty(vid, FSConstants.TYPE, FSConstants.CONNECTOR);
		vmap.put(eid, vid);
	}
	
	private void addOR(Element e, CPF g, Map<String, String> vmap) {
		String eid = e.getAttribute("id");
		FlowNode v = new CpfOrGateway("OR");
		String vid = v.getId();
		g.addVertex(v);
		g.setVertexProperty(vid, FSConstants.TYPE, FSConstants.CONNECTOR);
		vmap.put(eid, vid);
	}
	
	private void addAND(Element e, CPF g, Map<String, String> vmap) {
		String eid = e.getAttribute("id");
		FlowNode v = new CpfAndGateway("AND");
		String vid = v.getId();
		g.addVertex(v);
		g.setVertexProperty(vid, FSConstants.TYPE, FSConstants.CONNECTOR);
		vmap.put(eid, vid);
	}
	
	private String uuid() {
		return UUID.randomUUID().toString();
	}
}
