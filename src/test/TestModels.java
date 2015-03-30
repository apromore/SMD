package test;

import graph.Edge;
import graph.Graph;
import graph.Vertex;

import java.util.Set;

import merge.MergingPaper;
import nl.tue.tm.is.epc.EPC;
import nl.tue.tm.is.epc.EPCHelper;
import nl.tue.tm.is.epc.RPSDag;

import common.EPCModelParser;
import common.Settings;
import common.algos.TwoVertices;

public class TestModels {
	static String prefix = "models/journal/";
	
	public static void main(String[] args) {

//		testDigestInfo();
		testMergedModels();
//		printStructurenessScore(prefix + "test.epml");
//		testMerge();
	}

	private static void testDigestInfo() {
//		String m1 = "MC-1.01b Claim Initiation v2,PC-1.01b Claim Initiation v2_digest_ent_removed.epml";
//		String m1 = "PC-1.01a Claim Lodgement v2,MC-1.01a Claim Lodgement  v2_digest_ent_removed.epml";
//		String m1 = "PC-1.04 Invoice Received v2,MC-1.03 Invoice Received v2_digest_ent_removed.epml";
		String m1 = "WA,SA_digest_ent_removed.epml";
		printInfo(m1);
	}
	
	public static void testMergedModels() {
		String m1 = "MC-1.01b Claim Initiation v2,PC-1.01b Claim Initiation v2_merged.epml";
		printModelComplexityInfo(m1);
		m1 = "MC-1.01b Claim Initiation v2,PC-1.01b Claim Initiation v2_merged_ent_removed.epml";
		printModelComplexityInfo(m1);
		
		m1 = "PC-1.01a Claim Lodgement v2,MC-1.01a Claim Lodgement  v2_merged.epml";
		printModelComplexityInfo(m1);
		m1 = "PC-1.01a Claim Lodgement v2,MC-1.01a Claim Lodgement  v2_merged_ent_removed.epml";
		printModelComplexityInfo(m1);

		m1 = "PC-1.04 Invoice Received v2,MC-1.03 Invoice Received v2_merged.epml";
		printModelComplexityInfo(m1);
		m1 = "PC-1.04 Invoice Received v2,MC-1.03 Invoice Received v2_merged_ent_removed.epml";
		printModelComplexityInfo(m1);

		m1 = "WA,SA_merged.epml";
		printModelComplexityInfo(m1);
		m1 = "WA,SA_merged_ent_removed.epml";
		printModelComplexityInfo(m1);

		m1 = "WAfragment,SAfragment_merged.epml";
		printModelComplexityInfo(m1);
		m1 = "WAfragment,SAfragment_merged_ent_removed.epml";
		printModelComplexityInfo(m1);
	}
	
	public static void printModelComplexityInfo(String modelname) {
		System.out.println(modelname);
		printDensity(modelname);
		printAvgConnDegree(modelname);
		printStructurenessScore(modelname);
		printSequentiality(modelname);
		System.out.println();
	}
	
	public static void printInfo(String modelname) {
		Graph g1 = EPCModelParser.readModels(prefix + modelname, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		System.out.println("model size " + g1.getVertices().size());
	}
	
	public static void printDensity(String modelname) {
		Graph g1 = EPCModelParser.readModels(prefix + modelname, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		int[] vertexInfo = g1.getNrOfVertices();
		double n = vertexInfo[0];
		double f = vertexInfo[1];
		double e = vertexInfo[2];
		double c = vertexInfo[3];
		double a = g1.getEdges().size();
				
		double a_min = n - 1;
		double c_maxeven = c > 1 ? Math.sqrt(c/2 +1) : 1;
		double c_maxodd = c > 1 ? Math.sqrt((c-1)/2 + 1) + (c-1)/2 + 1 : 1;
		
		if (c <= 1) {
			System.out.println("density: 0");
		} if (c % 2 == 0) {
			// in case of even number of connectors
			double d_even = (a - a_min) / (c_maxeven + 2*(e+f) - a_min);
			System.out.println("density: " + d_even);
		} else {
			// in case odd number of connectors
			double d_odd = (a - a_min) / (c_maxodd + 2*(e+f) - a_min);
			System.out.println("density: " + d_odd);
		}
	}
	
	public static void printAvgConnDegree(String modelname) {
		Graph g1 = EPCModelParser.readModels(prefix + modelname, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		double nrgws = 0;
		double total_degree = 0;
		int max_degree = 0;
		
		for (Vertex v : g1.getConnectors()) {
			int degree = v.getParents().size() + v.getChildren().size();
			nrgws++;
			total_degree += degree;
			if (degree > max_degree) {
				max_degree = degree;
			}
		}
		System.out.println("avg connector degree: " + (nrgws > 0 ? total_degree/nrgws : 0));
		System.out.println("max connector degree: " + max_degree);
	}
	
	
	public static void printStructurenessScore(String modelname) {
		
		Graph g1 = EPCModelParser.readModels(prefix + modelname, false).get(0);
		g1.removeEmptyNodes();

		RPSDag engine = new RPSDag();
		EPC epcmodel = EPC.loadEPML(prefix + modelname);
		epcmodel.cleanEPC();
		try {
			engine.addProcessModel(new EPCHelper(epcmodel, modelname));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Structureness: " + ((double) engine.nrBonds * 2.0/ (double)g1.getNrOfVertices()[3]));

	}
	
	public static void printSequentiality(String modelname) {
		Graph g1 = EPCModelParser.readModels(prefix + modelname, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();

		double onlyfnevents = 0;
		for (Edge e : g1.getEdges()) {
			if (!g1.getVertexMap().get(e.getFromVertex()).getType().equals(Vertex.Type.gateway) &&
					!g1.getVertexMap().get(e.getToVertex()).getType().equals(Vertex.Type.gateway)) {
				onlyfnevents++;
			}
		}
		
		System.out.println("Sequentiality: " + onlyfnevents/(double)g1.getEdges().size());
	}
	
	/*********************TEST MERGE***********************/
	
	
	public static void testMerge() {
		testMerge("MC_claim_initiation.epml", "PC_claim_initiation.epml");
		testMerge("MC_claim_lodgement.epml", "PC_claim_lodgement.epml");
		testMerge("MC_invoice_received.epml", "PC_invoice_received.epml");
		testMerge("WA_2.epml", "SA_2.epml");
		testMerge("WA_2_fragment.epml", "SA_2_fragment.epml");
	}
	
	private static void testMerge(String model1, String model2) {
		System.out.println("merge "+ model1 + " " + model2);
		
		Graph.cleanGraphIDs();
		Graph g1 = EPCModelParser.readModels(prefix + model1, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		Graph g2 = EPCModelParser.readModels(prefix + model2, false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();

		MergingPaper mp = new MergingPaper();
		Set<TwoVertices> mapping = mp.getMappingIDs(g1, g2);
		
		//******************************************
//		Graph.cleanGraphIDs();
//		g1 = EPCModelParser.readModels(prefix + model1, false).get(0);
//		g1.removeEmptyNodes();
//		g1.reorganizeIDs();
//		
//		g2 = EPCModelParser.readModels(prefix + model2, false).get(0);
//		g2.removeEmptyNodes();
//		g2.reorganizeIDs();
//		
//		g1.addLabelsToUnNamedEdges();
//		g2.addLabelsToUnNamedEdges();

		Settings.REMOVE_ENTANGLEMENT = false;
		Graph merged = new MergingPaper().mergeModels(g1, g2, mp.getMapping(g1, g2, mapping));
		EPCModelParser.writeModel(prefix + merged.name +"_merged.epml", merged);
		
		//******************************************
		Graph.cleanGraphIDs();
		g1 = EPCModelParser.readModels(prefix + model1, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		g2 = EPCModelParser.readModels(prefix + model2, false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();

		Settings.REMOVE_ENTANGLEMENT = true;
		merged = new MergingPaper().mergeModels(g1, g2, mp.getMapping(g1, g2, mapping));
		EPCModelParser.writeModel(prefix + merged.name + "_merged_ent_removed.epml", merged);

//		System.out.println(modelnames.get(0) + " " + modelnames.get(1) + " similarity is: " + similarity);
	}
}
