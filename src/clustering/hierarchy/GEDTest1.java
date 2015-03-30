/**
 * 
 */
package clustering.hierarchy;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.TreeMultiset;

import nl.tue.tm.is.epc.Connector;
import nl.tue.tm.is.epc.EPC;
import nl.tue.tm.is.epc.Node;
import nl.tue.tm.is.graph.SimpleGraph;
import clustering.containment.ContainmentRelationImpl;
import clustering.dissimilarity.measure.GEDDissimCalc;

/**
 * @author Chathura C. Ekanayake
 *
 */
public class GEDTest1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new GEDTest1().compueGED();
//		new GEDTest1().containmentTest();
	}
	
	public void containmentTest() {
		try {
			ContainmentRelationImpl crel = new ContainmentRelationImpl();
			crel.setMinSize(4);
			crel.initialize();
			
			System.out.println(crel.areInContainmentRelation(crel.getFragmentIndex("put valid id"), crel.getFragmentIndex("put valid id")));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void compueGED() {
		
//		Missing pair: 1259 - 3852
//		Missing pair: 8364 - 7923
//		Missing pair: 1259 - 3851
//		Missing pair: 2389 - 2721
//		Missing pair: 1193 - 3911
//		Missing pair: 4117 - 4530
//		Missing pair: 4118 - 4530
//		Missing pair: 4117 - 4532
//		Missing pair: 4119 - 4530
//		Missing pair: 8361 - 7923
//		Missing pair: 7974 - 8344
//		Missing pair: 7997 - 8323
//		Missing pair: 7997 - 8324
//		Missing pair: 2469 - 2630
//		Missing pair: 7997 - 8325
//		Missing pair: 7976 - 8346
//		Missing pair: 7975 - 8348
//		Missing pair: 7976 - 8347
//		Missing pair: 4172 - 4503
//		Missing pair: 4076 - 46022
//		Missing pair: 2468 - 2623
//		Missing pair: 2469 - 2624
		
		EPC epc1 = EPC.loadEPML("/home/cn/temp/data/sap/Fragment_2469.epml");
		formatConnectorLabel(epc1);
		SimpleGraph g1 = new SimpleGraph(epc1);
		System.out.println(g1.getVertices().size() + " : " + g1.getEdges().size());
		
		EPC epc2 = EPC.loadEPML("/home/cn/temp/data/sap/Fragment_2624.epml");
		formatConnectorLabel(epc2);
		SimpleGraph g2 = new SimpleGraph(epc2);
		System.out.println(g2.getVertices().size() + " : " + g2.getEdges().size());
		
		GEDDissimCalc c = new GEDDissimCalc(0.45, 0.4);
		double ged = c.compute(g1, g2);
		System.out.println(ged);
	}
	
	private void formatConnectorLabel(EPC epc) {
		Map<Connector, String> labels = new HashMap<Connector, String>();
		
		for (Connector c: epc.getConnectors()) {
			String label = c.getName();
			TreeMultiset<String> mset = TreeMultiset.create();
			
			for (Node n: epc.getPre(c))
				if (n != null && n.getName() != null)
					mset.add(n.getName());
			label += mset.toString();
			mset.clear();
			
			for (Node n: epc.getPost(c))
				if (n != null && n.getName() != null)
					mset.add(n.getName());
			label += mset.toString();
			
			labels.put(c, label);
		}
		
		for (Connector c: labels.keySet()) {
			c.setName(labels.get(c));
//			System.out.println(c.getName());
		}
	}
}
