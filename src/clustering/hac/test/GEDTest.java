package clustering.hac.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.TreeMultiset;

import nl.tue.tm.is.epc.Connector;
import nl.tue.tm.is.epc.EPC;
import nl.tue.tm.is.epc.Node;
import nl.tue.tm.is.graph.SimpleGraph;
import clustering.dissimilarity.measure.GEDDissimCalc;

public class GEDTest {
	static String prefix = "sap";

	public static SimpleGraph getSimpleGraph(int frag) {
		String fname = String.format(prefix + "/Fragment_%d.epml", frag);
		EPC epc = EPC.loadEPML(fname);
		formatConnectorLabel(epc);
		SimpleGraph graph = new SimpleGraph(epc);

//		hideConnectors(epc);
//		SimpleGraph graph = new SimpleGraph(epc);
//		abstractConnectors(graph);
		return graph;
	}

	private static void abstractConnectors(SimpleGraph graph) {
		Set<Integer> silentVertices = new HashSet<Integer>();
		for (Integer v: graph.getVertices()){
			if (graph.getLabel(v).length() == 0)
				silentVertices.add(v);
		}
		graph = graph.removeVertices(silentVertices);
	}

	private static void hideConnectors(EPC epc) {
		for (Connector c: epc.getConnectors())
			c.setName("");
	}

	private static void formatConnectorLabel(EPC epc) {
		Map<Connector, String> labels = new HashMap<Connector, String>();
		
		for (Connector c: epc.getConnectors()) {
			String label = c.getName();
			TreeMultiset<String> mset = TreeMultiset.create();
			
			for (Node n: epc.getPre(c))
				mset.add(n.getName());
			label += mset.toString();
			mset.clear();
			
			for (Node n: epc.getPost(c))
				mset.add(n.getName());
			label += mset.toString();
			
			labels.put(c, label);
		}
		
		for (Connector c: labels.keySet()) {
			c.setName(labels.get(c));
			System.out.println(c.getName());
		}
	}	

	public static void main(String args[]) throws Exception {
		SimpleGraph graph1 = getSimpleGraph(7901);
		SimpleGraph graph2 = getSimpleGraph(7976);

		GEDDissimCalc gedcalc = new GEDDissimCalc(0.4,0.4);
		
		System.out.println(gedcalc.compute(graph2, graph1));
		
		System.out.println("Deterministic: " + gedcalc.isDeterministicGED());
		
		System.out.println("done");

	}
}
