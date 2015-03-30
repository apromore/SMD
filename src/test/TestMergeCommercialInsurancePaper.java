package test;

import common.EPCModelParser;

import graph.Graph;
import merge.MergeModels;
import merge.MergingPaper;

public class TestMergeCommercialInsurancePaper {

	static String model_prefix = "models/commercial insurance/";
	static String result_prefix = "models/commercial insurance paper/";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testCommercialInsurance("MC_claim_initiation.epml", "PC_claim_initiation.epml");
		testCommercialInsurance("MC_claim_lodgement.epml", "PC_claim_lodgement.epml");
		testCommercialInsurance("MC_invoice_received.epml", "PC_invoice_received.epml");
	}

	public static void testCommercialInsurance(String m1, String m2) {
		long mergetime = 0;
		long sizemergedbefore = 0;
		long sizemergedafterred = 0;
		double redfactorbefore = 0;
		double redfactorafter = 0;
		int g1size = 0;
		int g2size = 0;
		int times = 1;
		
		for (int i = 0; i < times; i++) {
			Graph g1 = EPCModelParser.readModels(model_prefix + m1, false).get(0);
			g1.removeEmptyNodes();
			g1.reorganizeIDs();
	//		EPCModelParser.writeModel(result_prefix +""+g1.name+"_changed.epml", g1);	
			g1size = g1.getVertices().size();
			
			Graph g2 = EPCModelParser.readModels(model_prefix + m2, false).get(0);
			g2.removeEmptyNodes();
			g2.reorganizeIDs();
			
			g1.addLabelsToUnNamedEdges();
			g2.addLabelsToUnNamedEdges();
	//		EPCModelParser.writeModel(result_prefix +""+g2.name+"_changed.epml", g2);	
			g2size = g2.getVertices().size();
	
			Graph merged = new MergingPaper().mergeModels(g1, g2);
	//		Graph merged = new MergeModels().mergeModels(g1, g2);
			
	//		int[] gwInf = merged.getNrOfConfigGWs(); 
	//		System.out.println(m1+"\t"+g1.getVertices().size()+"\t"+m2+"\t"+g2.getVertices().size()+"\t"+
	//				merged.getVertices().size()+"\t"+ gwInf[0]+"\t"+gwInf[1]+"\t"+gwInf[2]+"\t"+gwInf[3] +"\t"+merged.mergetime+"\t"+merged.cleanTime + "\t"+merged.beforeReduction);
			
			EPCModelParser.writeModel(result_prefix +""+g1.name+"_"+g2.name+"_merged_paper.epml", merged);	
			mergetime += merged.mergetime;
			sizemergedbefore += merged.beforeReduction;
			redfactorbefore += ((double)merged.beforeReduction/(double)(g1size+g2size));
			sizemergedafterred += merged.getVertices().size();
			redfactorafter += ((double)merged.getVertices().size()/(double)(g1size+g2size));

		}
		
		System.out.println(m1+"\t"+g1size+"\t"+m2+"\t"+g2size+"\t"+
				(mergetime/times)+"\t"+(sizemergedbefore/times)+"\t"+(redfactorbefore/times)
				+"\t"+(sizemergedafterred/times)+"\t"+(redfactorafter/times));

	}
	
}
