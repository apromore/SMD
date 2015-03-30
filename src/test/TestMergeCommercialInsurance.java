package test;

import common.EPCModelParser;

import graph.Graph;
import merge.MergeModels;

public class TestMergeCommercialInsurance {

	static String model_prefix = "models/commercial insurance/";
	static String result_prefix = "models/commercial insurance wordnet/";
//	static String model_prefix = "models/suncorp_new/";
//	static String result_prefix = "models/suncorp_new/";


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testCommercialInsurance("MC_claim_initiation.epml", "PC_claim_initiation.epml");
		testCommercialInsurance("MC_claim_lodgement.epml", "PC_claim_lodgement.epml");
		testCommercialInsurance("MC_invoice_received.epml", "PC_invoice_received.epml");
//		testCommercialInsurance("PC-1.01a_Claim_Lodgement_v0.2.epml", "Property-MC-1.01a_Claim_Lodgement_v0.4.epml");
	}

	public static void testCommercialInsurance(String m1, String m2) {


		Graph g1 = EPCModelParser.readModels(model_prefix + m1, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
//		EPCModelParser.writeModel(result_prefix +""+g1.name+"_changed.epml", g1);	
		int g1size = g1.getVertices().size();
		
		Graph g2 = EPCModelParser.readModels(model_prefix + m2, false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();
//		EPCModelParser.writeModel(result_prefix +""+g2.name+"_changed.epml", g2);	
		int g2size = g2.getVertices().size();

		Graph merged = new MergeModels().mergeModels(g1, g2);
//		int[] gwInf = merged.getNrOfConfigGWs(); 
//		System.out.println(m1+"\t"+g1.getVertices().size()+"\t"+m2+"\t"+g2.getVertices().size()+"\t"+
//				merged.getVertices().size()+"\t"+ gwInf[0]+"\t"+gwInf[1]+"\t"+gwInf[2]+"\t"+gwInf[3] +"\t"+merged.mergetime+"\t"+merged.cleanTime + "\t"+merged.beforeReduction);

		g1size = g1.getVertices().size();
		g2size = g2.getVertices().size();
		
		System.out.println(m1+"\t"+g1size+"\t"+m2+"\t"+g2size+"\t"+
				merged.mergetime+"\t"+merged.beforeReduction+"\t"+((double)merged.beforeReduction/(double)(g1size+g2size))
				+"\t"+merged.getVertices().size()+"\t"+((double)merged.getVertices().size()/(double)(g1size+g2size)));

		EPCModelParser.writeModel(result_prefix +""+g1.name+"_"+g2.name+"_merged_wordnet.epml", merged);	
	}
	
}
