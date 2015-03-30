package test;

import common.EPCModelParser;
import digest.Digest;

import graph.Graph;
import merge.MergingPaper;

public class TestDigest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testSAP();
	}

	public static void testSAP() {
		String model_prefix = "models/digest/";
		String result_prefix = "models/digest/";

		String m1 = "SAP_1.epml";	
		String m2 = "SAP_2.epml";

		Graph g1 = EPCModelParser.readModels(model_prefix + m1, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		Graph g2 = EPCModelParser.readModels(model_prefix + m2, false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();

		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();
		
		Graph merged = new MergingPaper().mergeModels(g1, g2);

		Graph digested = Digest.digest(merged, 2);
		
		EPCModelParser.writeModel(result_prefix +""+g1.name+"_"+g2.name+"_digest.epml", digested);	
	}
}
