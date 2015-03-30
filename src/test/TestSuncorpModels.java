package test;

import java.util.LinkedList;

import merge.MergingPaper;

import graph.Graph;

import common.EPCModelParser;
import common.Settings;
import common.VertexPair;
import common.algos.GraphEditDistanceGreedy;
import common.similarity.AssingmentProblem;
import digest.Digest;

public class TestSuncorpModels {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		testAllModelsSimilarity();
//		testAllModelsSimilarityGreedy();
//		testLoadModels();		
//		testMergeDigest();
		testMerge();
//		testSimilarity();
//		testSimilarityPairs();
//		testMergeDigestMulti();
	}

	private static void testAllModelsSimilarity() {
		String model_prefix = "models/suncorp_new2/";
		
		String[] processModels = models;
		for (int i = 0; i < processModels.length - 1 ;i++) {
			for (int j = i+1; j < processModels.length; j++) {
			
				Graph g1 = EPCModelParser.readModels(model_prefix + processModels[i], false).get(0);
				g1.removeEmptyNodes();
				g1.reorganizeIDs();
				int size1 = g1.getVertices().size();
				
				Graph g2 = EPCModelParser.readModels(model_prefix + processModels[j], false).get(0);
				g2.removeEmptyNodes();
				g2.reorganizeIDs();
				int size2 = g2.getVertices().size();
				
				g1.addLabelsToUnNamedEdges();
				g2.addLabelsToUnNamedEdges();
				if (g1.getVertices().size() == 0 || g2.getVertices().size() == 0) {
					System.out.println(processModels[i] + "\t"+size1+ "\t"+ processModels[j] + "\t"+size2 + "\t0\t0\t-ONE OF THE MODELS HAD SIze == 0");
					continue;
				}
				try {
					double weight = 0;
					LinkedList<VertexPair> mapping = AssingmentProblem.getMappingsVetrexUsingNodeMapping(g1, g2, Settings.MERGE_THRESHOLD, Settings.MERGE_CONTEXT_THRESHOLD);
					int matched = 0;
					for (VertexPair vp : mapping) {
//						System.out.println("\t" +vp.getLeft() + "\t"+ vp.getRight() + "\t" + vp.getWeight());
						weight += vp.getWeight();
						matched++;
					}
					System.out.println(processModels[i] + "\t"+size1+ "\t"+ processModels[j] + "\t"+size2 + "\t" + (weight/Math.max(size1, size2))+"\t"+matched);
				} catch (Exception e) {
					System.out.println("Failed to process modelpairs " + processModels[i] + "\t"+ processModels[j]);
				}
			}
		}
	}
	
	private static void testAllModelsSimilarityGreedy() {
		String model_prefix = "models/suncorp_new2/";
		
		String[] processModels = lvl4Models;
		for (int i = 0; i < processModels.length - 1 ;i++) {
			for (int j = i+1; j < processModels.length; j++) {
			
				Graph g1 = EPCModelParser.readModels(model_prefix + processModels[i], false).get(0);
				g1.removeEmptyNodes();
				g1.reorganizeIDs();
				int size1 = g1.getVertices().size();
				
				Graph g2 = EPCModelParser.readModels(model_prefix + processModels[j], false).get(0);
				g2.removeEmptyNodes();
				g2.reorganizeIDs();
				int size2 = g2.getVertices().size();
				
				g1.addLabelsToUnNamedEdges();
				g2.addLabelsToUnNamedEdges();
				
				if (g1.getVertices().size() == 0 || g2.getVertices().size() == 0) {
//					System.out.println(processModels[i] + "\t"+size1+ "\t"+ processModels[j] + "\t"+size2 + "\t0\t0\t-ONE OF THE MODELS HAD SIze == 0");
					continue;
				}
				
				try {
					GraphEditDistanceGreedy gedepc = new GraphEditDistanceGreedy();
					Object weights[] = {"vweight", Settings.vweight, 
										"sweight", Settings.sweight, 
										"eweight", Settings.eweight};
					
					gedepc.setWeight(weights);
					double weight = gedepc.computeGED(g1, g2);
//					System.out.println(processModels[i] + "\t"+size1+ "\t"+ processModels[j] + "\t"+size2 + "\t" + (1 - (weight < 0.0000001 ? 0: (weight > 1 ? 1 : weight)))+"\t"+gedepc.nrSubstitudedVertices);
//					System.out.println(models[i] + "("+size1+ ")\t"+ models[j] + "("+size2 + ")\t" + (1 - (weight < 0.0000001 ? 0: (weight > 1 ? 1 : weight))));
				} catch (Exception e) {
					System.out.println("Failed to process modelpairs " + processModels[i] + "\t"+ processModels[j]);
				}
			}
		}
		
		for (String j: Settings.jura) {
			System.out.println(j);
		}
	}

	private static void testSimilarityPairs() {
		String model_prefix = "models/suncorp_new2/";
		String result_prefix = "models/suncorp_new2/merged/";
		String[] modelpairs = new String[] {
				"01.01 HC. Validate customer policy holding - lvl 4.epml", "01.02 MC. Obtain policy details and validate caller - lvl 4.epml",
				"01.02 HC. Determine caller relationship - lvl 4.epml", "01.02 MC. Obtain policy details and validate caller - lvl 4.epml",
				"01.01 HC. Validate customer policy holding - lvl 4.epml", "01.03 MC. Validate Policy - lvl 4.epml",
				"02.03 HC. Itemise the Scope of Work - lvl 4.epml", "01.09 MC. Assess pathing - lvl 4.epml",
				"02.01 HC. Allocate Onsite Assessor - lvl 4.epml", "02.09 MC. Review customer's quotes - lvl 4.epml",
				"04.02 HC. Finalise claim - lvl 4.epml", "S. 07 MC. Greet caller and determine reason for call - lvl 4.epml",
				"02.01 HC. Allocate Onsite Assessor - lvl 4.epml", "S. 00b MC. Client Management_Ensure customer attendance at CSC - repairer - lvl 4.epml"
		};
	
		for (int i = 0; i < modelpairs.length - 1; i += 2) {
			String model1 = modelpairs[i];
			String model2 = modelpairs[i+1];
			
			Graph g1 = EPCModelParser.readModels(model_prefix + model1, false).get(0);
			g1.removeEmptyNodes();
			g1.reorganizeIDs();
			
			Graph g2 = EPCModelParser.readModels(model_prefix + model2, false).get(0);
			g2.removeEmptyNodes();
			g2.reorganizeIDs();
			
			g1.addLabelsToUnNamedEdges();
			g2.addLabelsToUnNamedEdges();
			
			GraphEditDistanceGreedy gedepc = new GraphEditDistanceGreedy();
			Object weights[] = {"vweight", Settings.vweight, 
								"sweight", Settings.sweight, 
								"eweight", Settings.eweight};
			
			gedepc.setWeight(weights);
			
			Graph merged = new MergingPaper().mergeModels(g1, g2);

			EPCModelParser.writeModel(result_prefix +""+g1.name+"_"+g2.name+"_merged.epml", merged);	

			System.out.println(model1+"\t"+g1.getVertices().size()+"\t"+model2+"\t"+g2.getVertices().size()+"\t"+
					merged.getVertices().size());
		}
	}
	
	private static void testSimilarity() {
		String model_prefix = "models/suncorp_new2/";
		String model1 = "01.00 HC. Lodge Claim - lvl 3.epml";
		String model2 = "01.01 HC. Validate customer policy holding - lvl 4.epml";
		int method = 2;
	
		Graph g1 = EPCModelParser.readModels(model_prefix + model1, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		int size1 = g1.getVertices().size();
		
		Graph g2 = EPCModelParser.readModels(model_prefix + model2, false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();
		int size2 = g2.getVertices().size();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();
		
		if (method == 1) {
			GraphEditDistanceGreedy gedepc = new GraphEditDistanceGreedy();
			Object weights[] = {"vweight", Settings.vweight, 
								"sweight", Settings.sweight, 
								"eweight", Settings.eweight};
			
			gedepc.setWeight(weights);
			double weight = gedepc.computeGED(g1, g2, true);
			System.out.println(model1 + "\t"+size1+ "\t"+ model2 + "\t"+size2 + "\t" + (1 - (weight < 0.0000001 ? 0: (weight > 1 ? 1 : weight)))+"\t"+gedepc.nrSubstitudedVertices);
	//					System.out.println(models[i] + "("+size1+ ")\t"+ models[j] + "("+size2 + ")\t" + (1 - (weight < 0.0000001 ? 0: (weight > 1 ? 1 : weight))));
	
		} else if (method == 2) {
			double weight = 0;
			LinkedList<VertexPair> mapping = AssingmentProblem.getMappingsVetrexUsingNodeMapping(g1, g2, Settings.MERGE_THRESHOLD, Settings.MERGE_CONTEXT_THRESHOLD);
			int matched = 0;
			for (VertexPair vp : mapping) {
				System.out.println(vp.getLeft() + " <> "+ vp.getRight() + " " + vp.getWeight());
				weight += vp.getWeight();
				matched++;
			}
			System.out.println(model1 + "\t"+size1+ "\t"+ model2 + "\t"+size2 + "\t" + (weight/Math.max(size1, size2))+"\t"+matched);
		}
	}
	
	private static void testMerge() {
		String model_prefix = "models/suncorp_new2/";
		String result_prefix = "models/suncorp_new2/merged/";
		String model1 = "01.01 HC. Validate customer policy holding - lvl 4.epml";
		String model2 = "01.02 MC. Obtain policy details and validate caller - lvl 4.epml";

		Graph g1 = EPCModelParser.readModels(model_prefix + model1, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		Graph g2 = EPCModelParser.readModels(model_prefix + model2, false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();
		
		Graph merged = new MergingPaper().mergeModels(g1, g2);

		EPCModelParser.writeModel(result_prefix +""+g1.name+"_"+g2.name+"_merged.epml", merged);	

		System.out.println(model1+"\t"+g1.getVertices().size()+"\t"+model2+"\t"+g2.getVertices().size()+"\t"+
				merged.getVertices().size());

	}
	
	private static void testMergeDigestMulti() {
		String model_prefix = "models/suncorp_new2/";
		String result_prefix = "models/suncorp_new2/merged/";
		String[] models = new String[] {
				"01.01 HC. Validate customer policy holding - lvl 4.epml", 
				"01.02 MC. Obtain policy details and validate caller - lvl 4.epml",
				"01.02 HC. Determine caller relationship - lvl 4.epml",
				"01.03 MC. Validate Policy - lvl 4.epml"
		};
		
		Graph g1 = EPCModelParser.readModels(model_prefix + models[0], false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		Graph g2 = EPCModelParser.readModels(model_prefix + models[1], false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();
		
		Graph merged = new MergingPaper().mergeModels(g1, g2);
		String name = g1.name+"_"+g2.name;
		
		for (int i = 2; i < models.length; i++) {
			Graph g3 = EPCModelParser.readModels(model_prefix + models[i], false).get(0);
			g3.removeEmptyNodes();
			g3.reorganizeIDs();
			g3.addLabelsToUnNamedEdges();
			
			merged = new MergingPaper().mergeModels(merged, g3);
			name += "_" + g3.name;

		}
		
		EPCModelParser.writeModel(result_prefix +""+name+"_merged.epml", merged);	

		Graph digest2 = Digest.digest(merged, 2);
		EPCModelParser.writeModel(result_prefix +""+g1.name+"_"+g2.name+"_digest2.epml", digest2);	
		Graph digest3 = Digest.digest(merged, 3);
		EPCModelParser.writeModel(result_prefix +""+g1.name+"_"+g2.name+"_digest3.epml", digest3);	
		
		Graph digest4 = Digest.digest(merged, 4);
		EPCModelParser.writeModel(result_prefix +""+g1.name+"_"+g2.name+"_digest4.epml", digest4);	

	}
	
	private static void testMergeDigest() {
		String model_prefix = "models/suncorp_new2/";
		String result_prefix = "models/suncorp_new2/merged/";
		String model1 = "02.03 HC. Itemise the Scope of Work - lvl 4.epml";
		String model2  = "02.04 HC. Additional information required from customer - lvl 4.epml";

		Graph g1 = EPCModelParser.readModels(model_prefix + model1, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		Graph g2 = EPCModelParser.readModels(model_prefix + model2, false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();
		
		Graph merged = new MergingPaper().mergeModels(g1, g2);
		EPCModelParser.writeModel(result_prefix +""+g1.name+"_"+g2.name+"_merged.epml", merged);	

		Graph digest = Digest.digest(merged, 2);
		
		EPCModelParser.writeModel(result_prefix +""+g1.name+"_"+g2.name+"_digest.epml", digest);	
	}
	
	private static void testLoadModels() {
		String model_prefix = "models/suncorp_new2/";
		String modelname = "04.06b MC. Issue Payment to Third Party - lvl 4.epml";
		
		Graph g1 = EPCModelParser.readModels(model_prefix + modelname, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		int size1 = g1.getVertices().size();
		System.out.println("g1 " + size1);
		
	}
	
	private static String[] models = new String[]{
		"01.00 HC. Lodge Claim - lvl 3.epml",
		"01.00 MC. Lodgement - lvl 3.epml",
		"01.01 HC. Validate customer policy holding - lvl 4.epml",
		"01.02 HC. Determine caller relationship - lvl 4.epml",
		"01.02 MC. Obtain policy details and validate caller - lvl 4.epml",
		"01.03 HC. Determine policy status - lvl 4.epml",
		"01.03 MC. Validate Policy - lvl 4.epml",
		"01.04 HC. Confirm event coverage - lvl 4.epml",
		"01.04 MC. Capture scenario - situational details - lvl 4.epml",
		"01.05 HC. Validate event against the PDS.epml",
		"01.05 MC. Capture insured incidents and details - lvl 4.epml",
		"01.06 HC. Record schedule of loss - lvl 4.epml",
		"01.06 MC. Capture third party incidents and details - lvl 4.epml",
		"01.07 HC. Determine claim type and claim allocation - lvl 4.epml",
		"01.07 MC. Capture additional parties - lvl 4.epml",
		"01.08 MC. Make claim decisions and record impacts - lvl 4.epml",
		"01.09 MC. Assess pathing - lvl 4.epml",
		"01.09 MC. Book and-or allocate assesing resources, suppliers, repairers - lvl 4.epml",
		"01.10 MC. Lodge claim and make final updates - lvl 4.epml",
		"01.11 MC. Arrange services - lvl 4.epml",
		"01.14 MC. Finalise claim lodgement - lvl 4.epml",
		"01.15 MC. Create report only claim - lvl 4.epml",
		"02.00 HC. Assess Claim - lvl 3.epml",
		"02.00 MC. Assessment - lvl 3.epml",
		"02.00a MC. Repairer Assessment On Site - lvl 3.epml",
		"02.01 HC. Allocate Onsite Assessor - lvl 4.epml",
		"02.01 MC. Prepare for assessment.epml",
		"02.02 HC. Perform Internal Onsite Assessment - lvl 4.epml",
		"02.02 MC. Co-ordinate assessment - lvl 4.epml",
		"02.03 HC. Itemise the Scope of Work - lvl 4.epml",
		"02.03 MC. Customer intake on site (Greeting) - lvl 4.epml",
		"02.04 HC. Additional information required from customer - lvl 4.epml",
		"02.04 MC. Receive vehicle at centre - lvl 4.epml",
		"02.05 HC. Finalise the Scope of Work - lvl 4.epml",
		"02.05 MC. Intake customer at repairer - lvl 4.epml",
		"02.06 HC. Perform External Onsite Assessment.epml",
		"02.06 MC. Intake customer at contract shop - lvl 4.epml",
		"02.07 MC. Allocation and Co-ordination - lvl 4.epml",
		"02.08 MC. Obtain competative quotes - lvl 4.epml",
		"02.09 MC. Review customer's quotes - lvl 4.epml",
		"02.10 MC. Create and Submit Quote and Photos - lvl 4.epml",
		"02.11 MC. Obtain contract shop quotes - lvl 4.epml",
		"02.14 MC. Negotiate and action cash settlement - lvl 4.epml",
		"02.15 MC. Assess supplementary quote.epml",
		"02.16 MC. Assess sublet quote - lvl 4.epml",
		"03.   HC. Review Report - lvl 4.epml",
		"03.00 HC. Fulfil Claim - lvl 3.epml",
		"03.02 MC. Repair Vehicle.epml",
		"03.04 HC. Manage quotations - lvl 4.epml",
		"03.04 MC. Invoice for repair - lvl 4.epml",
		"03.05 HC. Review quotes and select vendor - lvl 4.epml",
		"03.06 Assess replacement vehicle qualification and obtain quotes - To Be - lvl 4.epml",
		"03.06 HC. Send authority to repairer - supplier - lvl 4.epml",
		"03.08 HC. Request report - SOW - lvl 4.epml",
		"04.00 HC. Recovery and Settlement - lvl 3.epml",
		"04.00b MC. Third Party Settlement -  lvl 3.epml",
		"04.00c MC. Mercantile and Legal - lvl 3.epml",
		"04.01 HC. Payment - lvl 4.epml",
		"04.01a MC. Review Liability - lvl 4.epml",
		"04.01b MC. Review Liability - lvl 4.epml",
		"04.02 HC. Finalise claim - lvl 4.epml",
		"04.02a MC. Confirm Adequate Third Party Details and Excess Conditions - lvl 4.epml",
		"04.02b MC. Contact Third Party and Confirm Intentions - lvl 4.epml",
		"04.02c MC. Action Recovery - lvl 4.epml",
		"04.03b MC. Contributory Negligence - lvl 4.epml",
		"04.03c MC. Track Recovery Progress - lvl 4.epml",
		"04.04a MC. Path Recovery - lvl 4.epml",
		"04.04b MC. Await Demands - lvl 4.epml",
		"04.04c MC. Review Recovery Outcomes - lvl 4.epml",
		"04.05a MC. Determine if Claim is Ready to Recover - lvl 4.epml",
		"04.05b MC. Review Demands- lvl 4.epml",
		"04.06a MC. Review Agreements and Quantum - lvl 4.epml",
		"04.06b MC. Issue Payment to Third Party - lvl 4.epml",
		"04.07a MC. Create Recovery Proposal and Issue Demand - lvl 4.epml",
		"04.10a MC. Check Debt Recovery Against Claim - lvl 4.epml",
		"04.12 MC. Review X-ref Claim - lvl 4.epml",
		"04.13a MC. Door Knock - lvl 4.epml",
		"09.00 HC. Reject Claim - lvl 3.epml",
		"10.00 HC. Withdraw Claim - lvl 3.epml",
		"HC Accidental Loss or Damage.epml",
		"HC Allocate internal Assessment.epml",
		"HC Authorise payment.epml",
		"HC Bursting and Leaking Loss Cause Question Tree.epml",
		"HC Contact customer.epml",
		"HC Damage by Animals Question Tree.epml",
		"HC Discuss coverage requirements with customer.epml",
		"HC Discuss PoO-PoL requirements with customer.epml",
		"HC Electric Motor Burnout Question Tree.epml",
		"HC Fire Question Tree.epml",
		"HC Food Spoilage Question Tree.epml",
		"HC Impact (non storm) Question Tree.epml",
		"HC Investigate fulfilment decision.epml",
		"HC Manage authorisation failure.epml",
		"HC Pet Cover Question Tree.epml",
		"HC Record appointment date - time against assessment request in web portal.epml",
		"HC Search available assessors and select.epml",
		"HC Submit assessment.epml",
		"HC Theft Question Tree.epml",
		"PC 1.3.1 Inform Customer on Claim Rejection - To Be Lvl 4 eEPC.epml",
		"PC 4.1 Claim Payments - To Be Lvl2 eEPC.epml",
		"S. 00b MC. Client Management_Assessment call.epml",
		"S. 00b MC. Client Management_Customer intro - lvl 4.epml",
		"S. 00b MC. Client Management_Ensure customer attendance at CSC - repairer - lvl 4.epml",
		"S. 00b MC. Client Management_Outstanding documentation.epml",
		"S. 01 MC. Payment - lvl 4 (draft).epml",
		"S. 05 MC. Finalisation - lvl 4 (draft).epml",
		"S. 06 MC. Receipting - lvl 4 (draft).epml",
		"S. 07 MC. Greet caller and determine reason for call - lvl 4.epml",
		"S. 29b Investigations - lvl 3.epml"
	};
	
	// level 2 models
	private static String[] lvl2Models = new String[] {
		"01.05 HC. Validate event against the PDS.epml",
		"02.06 HC. Perform External Onsite Assessment.epml",
		"HC Accidental Loss or Damage.epml",
		"HC Allocate internal Assessment.epml",
		"HC Authorise payment.epml",
		"HC Bursting and Leaking Loss Cause Question Tree.epml",
		"HC Contact customer.epml",
		"HC Damage by Animals Question Tree.epml",
		"HC Discuss coverage requirements with customer.epml",
		"HC Discuss PoO-PoL requirements with customer.epml",
		"HC Electric Motor Burnout Question Tree.epml",
		"HC Fire Question Tree.epml",
		"HC Food Spoilage Question Tree.epml",
		"HC Impact (non storm) Question Tree.epml",
		"HC Investigate fulfilment decision.epml",
		"HC Manage authorisation failure.epml",
		"HC Pet Cover Question Tree.epml",
		"HC Record appointment date - time against assessment request in web portal.epml",
		"HC Search available assessors and select.epml",
		"HC Submit assessment.epml",
		"HC Theft Question Tree.epml",
		"PC 4.1 Claim Payments - To Be Lvl2 eEPC.epml",
		"02.01 MC. Prepare for assessment.epml",
		"02.15 MC. Assess supplementary quote.epml",
		"03.02 MC. Repair Vehicle.epml",
		"S. 00b MC. Client Management_Assessment call.epml",
		"S. 00b MC. Client Management_Outstanding documentation.epml"
	};
	
	private static String[] lvl3Models = new String[] {
		"01.00 HC. Lodge Claim - lvl 3.epml",
		"02.00 HC. Assess Claim - lvl 3.epml",
		"03.00 HC. Fulfil Claim - lvl 3.epml",
		"04.00 HC. Recovery and Settlement - lvl 3.epml",
		"09.00 HC. Reject Claim - lvl 3.epml",
		"10.00 HC. Withdraw Claim - lvl 3.epml",
		"01.00 MC. Lodgement - lvl 3.epml",
		"02.00 MC. Assessment - lvl 3.epml",
		"02.00a MC. Repairer Assessment On Site - lvl 3.epml",
		"04.00b MC. Third Party Settlement -  lvl 3.epml",
		"04.00c MC. Mercantile and Legal - lvl 3.epml"
	};
	
	private static String[] lvl4Models = new String[] {
		"01.01 HC. Validate customer policy holding - lvl 4.epml",
		"01.02 HC. Determine caller relationship - lvl 4.epml",
		"01.03 HC. Determine policy status - lvl 4.epml",
		"01.04 HC. Confirm event coverage - lvl 4.epml",
		"01.06 HC. Record schedule of loss - lvl 4.epml",
		"01.07 HC. Determine claim type and claim allocation - lvl 4.epml",
		"02.01 HC. Allocate Onsite Assessor - lvl 4.epml",
		"02.02 HC. Perform Internal Onsite Assessment - lvl 4.epml",
		"02.03 HC. Itemise the Scope of Work - lvl 4.epml",
		"02.04 HC. Additional information required from customer - lvl 4.epml",
		"02.05 HC. Finalise the Scope of Work - lvl 4.epml",
		"03.   HC. Review Report - lvl 4.epml",
		"03.04 HC. Manage quotations - lvl 4.epml",
		"03.05 HC. Review quotes and select vendor - lvl 4.epml",
		"03.06 HC. Send authority to repairer - supplier - lvl 4.epml",
		"03.08 HC. Request report - SOW - lvl 4.epml",
		"04.01 HC. Payment - lvl 4.epml",
		"04.02 HC. Finalise claim - lvl 4.epml",
		"PC 1.3.1 Inform Customer on Claim Rejection - To Be Lvl 4 eEPC.epml",
		"01.02 MC. Obtain policy details and validate caller - lvl 4.epml",
		"01.03 MC. Validate Policy - lvl 4.epml",
		"01.04 MC. Capture scenario - situational details - lvl 4.epml",
		"01.05 MC. Capture insured incidents and details - lvl 4.epml",
		"01.06 MC. Capture third party incidents and details - lvl 4.epml",
		"01.07 MC. Capture additional parties - lvl 4.epml",
		"01.08 MC. Make claim decisions and record impacts - lvl 4.epml",
		"01.09 MC. Assess pathing - lvl 4.epml",
		"01.09 MC. Book and-or allocate assesing resources, suppliers, repairers - lvl 4.epml",
		"01.10 MC. Lodge claim and make final updates - lvl 4.epml",
		"01.11 MC. Arrange services - lvl 4.epml",
		"01.14 MC. Finalise claim lodgement - lvl 4.epml",
		"01.15 MC. Create report only claim - lvl 4.epml",
		"02.02 MC. Co-ordinate assessment - lvl 4.epml",
		"02.03 MC. Customer intake on site (Greeting) - lvl 4.epml",
		"02.04 MC. Receive vehicle at centre - lvl 4.epml",
		"02.05 MC. Intake customer at repairer - lvl 4.epml",
		"02.06 MC. Intake customer at contract shop - lvl 4.epml",
		"02.07 MC. Allocation and Co-ordination - lvl 4.epml",
		"02.08 MC. Obtain competative quotes - lvl 4.epml",
		"02.09 MC. Review customer's quotes - lvl 4.epml",
		"02.10 MC. Create and Submit Quote and Photos - lvl 4.epml",
		"02.11 MC. Obtain contract shop quotes - lvl 4.epml",
		"02.14 MC. Negotiate and action cash settlement - lvl 4.epml",
		"02.16 MC. Assess sublet quote - lvl 4.epml",
		"03.04 MC. Invoice for repair - lvl 4.epml",
		"03.06 Assess replacement vehicle qualification and obtain quotes - To Be - lvl 4.epml",
		"04.01a MC. Review Liability - lvl 4.epml",
		"04.01b MC. Review Liability - lvl 4.epml",
		"04.02a MC. Confirm Adequate Third Party Details and Excess Conditions - lvl 4.epml",
		"04.02b MC. Contact Third Party and Confirm Intentions - lvl 4.epml",
		"04.02c MC. Action Recovery - lvl 4.epml",
		"04.03b MC. Contributory Negligence - lvl 4.epml",
		"04.03c MC. Track Recovery Progress - lvl 4.epml",
		"04.04a MC. Path Recovery - lvl 4.epml",
		"04.04b MC. Await Demands - lvl 4.epml",
		"04.04c MC. Review Recovery Outcomes - lvl 4.epml",
		"04.05a MC. Determine if Claim is Ready to Recover - lvl 4.epml",
		"04.05b MC. Review Demands- lvl 4.epml",
		"04.06a MC. Review Agreements and Quantum - lvl 4.epml",
		"04.06b MC. Issue Payment to Third Party - lvl 4.epml",
		"04.07a MC. Create Recovery Proposal and Issue Demand - lvl 4.epml",
		"04.10a MC. Check Debt Recovery Against Claim - lvl 4.epml",
		"04.12 MC. Review X-ref Claim - lvl 4.epml",
		"04.13a MC. Door Knock - lvl 4.epml",
		"S. 00b MC. Client Management_Customer intro - lvl 4.epml",
		"S. 00b MC. Client Management_Ensure customer attendance at CSC - repairer - lvl 4.epml",
		"S. 01 MC. Payment - lvl 4 (draft).epml",
		"S. 05 MC. Finalisation - lvl 4 (draft).epml",
		"S. 06 MC. Receipting - lvl 4 (draft).epml",
		"S. 07 MC. Greet caller and determine reason for call - lvl 4.epml"
	};
}
