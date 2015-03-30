package org.apromore.mining.conformance.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfAndGateway;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.graph.JBPT.CpfOrGateway;
import org.apromore.graph.JBPT.CpfTask;
import org.apromore.graph.JBPT.CpfXorGateway;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.service.utils.EPCDeserializer;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.hypergraph.abs.Vertex;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.epc.EPCFactory;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.processmining.models.graphbased.directed.epc.elements.Connector;
import org.processmining.models.graphbased.directed.epc.elements.Connector.ConnectorType;
import org.processmining.models.graphbased.directed.epc.elements.Event;
import org.processmining.models.graphbased.directed.epc.elements.Function;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.epc.EPCConversion;
import org.processmining.plugins.epml.Epml;
import org.processmining.plugins.pnml.Pnml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class FitnessTest2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		FitnessTest2 ft2 = new FitnessTest2();
		try {
			ft2.runTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void runTest() throws Exception {
//		String epmlPath = "";
//		String pnmlPath = "";
		
		String epmlPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/1An_ka9y.epml";
		String pnmlPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/pn1.pnml";
		
		File epmlFile = new File(epmlPath);
		InputStream in = FileUtils.openInputStream(epmlFile);
		ConfigurableEPC epc = importEPC2(in, epmlFile.getName());
		System.out.println("Functions: " + epc.getFunctions().size());
		System.out.println("Events: " + epc.getEvents().size());
		System.out.println("Edges: " + epc.getEdges().size());
		
		EPCConversion epcConversion = new EPCConversion();
		Petrinet pn = epcConversion.convertToPN(epc);
		System.out.println("Converted to petri net.");
		
		File pnmlFile = new File(pnmlPath);
		exportPNML(pn, pnmlFile);
		System.out.println("Exported pnml to file.");
		
//		in.close();
	}
	
	public void exportPNML(Petrinet net, File file) {
		Marking marking = new Marking();
		GraphLayoutConnection layout = new GraphLayoutConnection(net);
		HashMap<PetrinetGraph, Marking> markedNets = new HashMap<PetrinetGraph, Marking>();
		markedNets.put(net, marking);
		Pnml pnml = new Pnml().convertFromNet(markedNets, layout);
		pnml.setType(Pnml.PnmlType.PNML);
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + pnml.exportElement(pnml);

		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			bw.write(text);
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ConfigurableEPC importEPC2(InputStream input, String filename) throws Exception {
		
		EPCDeserializer deserializer = new EPCDeserializer();
		CPF cpf = deserializer.deserializeInputStream(input);
		
		Map<FlowNode, EPCNode> vmap = new HashMap<FlowNode, EPCNode>();
		
		String label = filename;
		ConfigurableEPC epc = EPCFactory.newConfigurableEPC(label);
		Collection<FlowNode> vs = cpf.getFlowNodes();
		for (FlowNode cv : vs) {
			if (cv instanceof CpfTask) {
				Function function = epc.addFunction(cv.getLabel());
				vmap.put(cv, function);
				
			} else if (cv instanceof CpfEvent) {
				Event event = epc.addEvent(cv.getLabel());
				vmap.put(cv, event);
				
			} else if (cv instanceof CpfXorGateway) {
				Connector connector = epc.addConnector(cv.getLabel(), ConnectorType.XOR);
				vmap.put(cv, connector);
				
			} else if (cv instanceof CpfAndGateway) {
				Connector connector = epc.addConnector(cv.getLabel(), ConnectorType.AND);
				vmap.put(cv, connector);
				
			} else if (cv instanceof CpfOrGateway) {
				Connector connector = epc.addConnector(cv.getLabel(), ConnectorType.OR);
				vmap.put(cv, connector);
				
			}
		}
		
		Collection<ControlFlow<FlowNode>> edges = cpf.getEdges();
		for (ControlFlow<FlowNode> e : edges) {
			EPCNode v1 = vmap.get(e.getSource());
			EPCNode v2 = vmap.get(e.getTarget());
			epc.addArc(v1, v2);
		}
		
		return epc;
	}
	
	public ConfigurableEPC importEPC(InputStream input, String filename) throws Exception {
		
//		protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
//				throws Exception {
			/*
			 * Get an XML pull parser.
			 */
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			/*
			 * Initialize the parser on the provided input.
			 */
			xpp.setInput(input, null);
			/*
			 * Get the first event type.
			 */
			int eventType = xpp.getEventType();
			Epml epml = new Epml();

			/*
			 * Skip whatever we find until we've found a start tag.
			 */
			while (eventType != XmlPullParser.START_TAG) {
				eventType = xpp.next();
			}
			/*
			 * Check whether start tag corresponds to PNML start tag.
			 */
			if (xpp.getName().equals(Epml.TAG)) {
				/*
				 * Yes it does. Import the PNML element.
				 */
				epml.importElement(xpp, epml);
			} else {
				/*
				 * No it does not. Return null to signal failure.
				 */
				epml.log(Epml.TAG, xpp.getLineNumber(), "Expected epml");
			}
			if (epml.hasErrors()) {
//				context.getProvidedObjectManager().createProvidedObject("Log of EPML import", epml.getLog(), XLog.class,
//						context);
				return null;
			}

			ConfigurableEPC cEpc = EPCFactory.newConfigurableEPC("C-EPC imported from " + filename);
			/*
			 * Set the label of the Petri net.
			 */
//			context.getFutureResult(0).setLabel("C-EPC imported from " + filename);

			GraphLayoutConnection layout = new GraphLayoutConnection(cEpc);
			epml.convertToCEpc(cEpc, layout);
			return cEpc;
		}
//	}

}
