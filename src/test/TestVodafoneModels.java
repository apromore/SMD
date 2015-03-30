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

public class TestVodafoneModels {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		testAllModelsSimilarity();
		testAllModelsSimilarityGreedy();
//		testLoadModels();		
//		testMergeDigest();
//		testMerge();
//		testSimilarity();
//		testSimilarityPairs();
//		testMergeDigestMulti();
	}

	private static void testAllModelsSimilarity() {
		String model_prefix = "models/vodafone/";
		
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
		String model_prefix = "models/vodafone/";
		
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
					System.out.println(models[i] + "("+size1+ ")\t"+ models[j] + "("+size2 + ")\t" + (1 - (weight < 0.0000001 ? 0: (weight > 1 ? 1 : weight))));
				} catch (Exception e) {
					System.out.println("Failed to process modelpairs " + processModels[i] + "\t"+ processModels[j]);
				}
			}
		}
		
//		for (String j: Settings.jura) {
//			System.out.println(j);
//		}
	}

	private static void testSimilarity() {
		String model_prefix = "models/vodafone/";
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
		String model_prefix = "models/vodafone/";
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
		String model_prefix = "models/vodafone/";
		String result_prefix = "models/vodafone/merged/";
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
		String model_prefix = "models/vodafone/";
		String result_prefix = "models/vodafone/merged/";
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
		String model_prefix = "models/vodafone/";
		String modelname = "04.06b MC. Issue Payment to Third Party - lvl 4.epml";
		
		Graph g1 = EPCModelParser.readModels(model_prefix + modelname, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		int size1 = g1.getVertices().size();
		System.out.println("g1 " + size1);
		
	}
	
	private static String[] models = new String[]{		
		"edited _mod-168154.epml",
		"edited _mod-168781.epml",
		"edited _mod-168806.epml",
		"edited _mod-168841.epml",
		"edited _mod-169010.epml",
		"edited _mod-169175.epml",
		"edited _mod-169315.epml",
		"edited _mod-169352.epml",
		"edited _mod-169441.epml",
		"edited _mod-169526.epml",
		"edited _mod-169588.epml",
		"edited _mod-169657.epml",
		"edited _mod-169736.epml",
		"edited _mod-169822.epml",
		"edited _mod-169879.epml",
		"edited _mod-169910.epml",
		"edited _mod-169936.epml",
		"edited _mod-170164.epml",
		"edited _mod-170295.epml",
		"edited _mod-170333.epml",
		"edited _mod-170379.epml",
		"edited _mod-170404.epml",
		"edited _mod-170445.epml",
		"edited _mod-170927.epml",
		"edited _mod-172632.epml",
		"edited _mod-173456.epml",
		"edited _mod-173577.epml",
		"edited _mod-173991.epml",
		"edited _mod-174255.epml",
		"edited _mod-174324.epml",
		"edited _mod-174419.epml",
		"edited _mod-174527.epml",
		"edited _mod-174631.epml",
		"edited _mod-174766.epml",
		"edited _mod-174867.epml",
		"edited _mod-174964.epml",
		"edited _mod-175075.epml",
		"edited _mod-175216.epml",
		"edited _mod-175344.epml",
		"edited _mod-175373.epml",
		"edited _mod-175422.epml",
		"edited _mod-175775.epml",
		"edited _mod-177973.epml",
		"edited _mod-178319.epml",
		"edited _mod-178615.epml",
		"edited _mod-178777.epml",
		"edited _mod-179530.epml",
		"edited _mod-180210.epml",
		"edited _mod-180258.epml",
		"edited _mod-180306.epml",
		"edited _mod-180387.epml",
		"edited _mod-180449.epml",
		"edited _mod-180502.epml",
		"edited _mod-180572.epml",
		"edited _mod-180650.epml",
		"edited _mod-180745.epml",
		"edited _mod-180827.epml",
		"edited _mod-180958.epml",
		"edited _mod-181001.epml",
		"edited _mod-181092.epml",
		"edited _mod-181132.epml",
		"edited _mod-181538.epml",
		"edited _mod-181624.epml",
		"edited _mod-181772.epml",
		"edited _mod-181855.epml",
		"edited _mod-182198.epml",
		"edited _mod-183274.epml",
		"edited _mod-183484.epml",
		"edited _mod-183964.epml",
		"edited _mod-184220.epml",
		"edited _mod-185426.epml",
		"edited _mod-185547.epml",
		"edited _mod-185879.epml",
		"edited _mod-186131.epml",
		"edited _mod-186209.epml",
		"edited _mod-186306.epml",
		"edited _mod-186439.epml",
		"edited _mod-186665.epml",
		"edited _mod-186977.epml",
		"edited _mod-187097.epml",
		"edited _mod-187220.epml",
		"edited _mod-187241.epml",
		"edited _mod-187285.epml",
		"edited _mod-187301.epml",
		"edited _mod-187349.epml",
		"edited _mod-187386.epml",
		"edited _mod-187657.epml",
		"edited _mod-187757.epml",
		"edited _mod-187956.epml",
		"edited _mod-188001.epml",
		"edited _mod-188077.epml",
		"edited _mod-188218.epml",
		"edited _mod-188362.epml",
		"edited _mod-188407.epml",
		"edited _mod-188473.epml",
		"edited _mod-188486.epml",
		"edited _mod-188577.epml",
		"edited _mod-188687.epml",
		"edited _mod-188859.epml",
		"edited _mod-188896.epml",
		"edited _mod-188953.epml",
		"edited _mod-188984.epml",
		"edited _mod-189028.epml",
		"edited _mod-189095.epml",
		"edited _mod-189325.epml",
		"edited _mod-189428.epml",
		"edited _mod-189482.epml",
		"edited _mod-189495.epml",
		"edited _mod-189609.epml",
		"edited _mod-189731.epml",
		"edited _mod-189828.epml",
		"edited _mod-189991.epml",
		"edited _mod-190249.epml",
		"edited _mod-190325.epml",
		"edited _mod-190405.epml",
		"edited _mod-190437.epml",
		"edited _mod-190469.epml",
		"edited _mod-190501.epml",
		"edited _mod-190526.epml",
		"edited _mod-195540.epml",
		"edited _mod-196159.epml",
		"edited _mod-197249.epml",
		"edited _mod-197300.epml",
		"edited _mod-197386.epml",
		"edited _mod-197512.epml", 
		"edited _mod-197595.epml",
		"edited _mod-197828.epml",
		"edited _mod-197923.epml",
		"edited _mod-197944.epml", 
		"edited _mod-198091.epml",
		"edited _mod-198207.epml",
		"edited _mod-202571.epml",
		"edited _mod-202767.epml",
		"edited _mod-202851.epml",
		"edited _mod-202873.epml",
		"edited _mod-202890.epml",
		"edited _mod-202911.epml",
		"edited _mod-202959.epml",
		"edited _mod-203205.epml",
		"edited _mod-203272.epml",
		"edited _mod-203486.epml",
		"edited _mod-205035.epml",
		"edited _mod-205252.epml",
		"edited _mod-205265.epml",
		"edited _mod-205315.epml",
		"edited _mod-205377.epml",
		"edited _mod-205446.epml",
		"edited _mod-207446.epml",
		"edited _mod-208119.epml",
		"edited _mod-208567.epml",
		"edited _mod-208732.epml",
		"edited _mod-209112.epml",
		"edited _mod-209253.epml",
		"edited _mod-209700.epml",
		"edited _mod-210027.epml",
		"edited _mod-210035.epml",
		"edited _mod-210465.epml",
		"edited _mod-210564.epml",
		"edited _mod-211102.epml",
		"edited _mod-211269.epml",
		"edited _mod-211496.epml",
		"edited _mod-211640.epml",
		"edited _mod-211823.epml",
		"edited _mod-212432.epml",
		"edited _mod-213312.epml",
		"edited _mod-213444.epml",
		"edited _mod-213457.epml",
		"edited _mod-213598.epml",
		"edited _mod-214205.epml",
		"edited _mod-214248.epml",
		"edited _mod-214268.epml",
		"edited _mod-214281.epml",
		"edited _mod-214751.epml",
		"edited _mod-215124.epml",
		"edited _mod-215215.epml",
		"edited _mod-215298.epml",
		"edited _mod-215384.epml", 
		"edited _mod-215456.epml",
		"edited _mod-215514.epml",
		"edited _mod-215570.epml",
		"edited _mod-215638.epml",
		"edited _mod-215692.epml",
		"edited _mod-215854.epml",
		"edited _mod-215897.epml",
		"edited _mod-215943.epml",
		"edited _mod-215986.epml",
		"edited _mod-216029.epml",
		"edited _mod-216080.epml",
		"edited _mod-216120.epml",
		"edited _mod-216166.epml",
		"edited _mod-216213.epml",
		"edited _mod-216253.epml",
		"edited _mod-216306.epml",
		"edited _mod-216434.epml",
		"edited _mod-216495.epml",
		"edited _mod-216535.epml",
		"edited _mod-216579.epml",
		"edited _mod-216615.epml",
		"edited _mod-216655.epml",
		"edited _mod-216772.epml",
		"edited _mod-216814.epml",
		"edited _mod-216902.epml",
		"edited _mod-216965.epml",
		"edited _mod-217002.epml",
		"edited _mod-217041.epml",
		"edited _mod-217085.epml",
		"edited _mod-217105.epml",
		"edited _mod-217147.epml",
		"edited _mod-217254.epml",
		"edited _mod-217289.epml",
		"edited _mod-217364.epml",
		"edited _mod-217933.epml",
		"edited _mod-218018.epml",
		"edited _mod-218364.epml",
		"edited _mod-218679.epml",
		"edited _mod-218735.epml",
		"edited _mod-218797.epml",
		"edited _mod-218842.epml",
		"edited _mod-218907.epml",
		"edited _mod-218952.epml",
		"edited _mod-219037.epml",
		"edited _mod-219088.epml",
		"edited _mod-219176.epml",
		"edited _mod-219211.epml",
		"edited _mod-219257.epml",
		"edited _mod-219318.epml",
		"edited _mod-219353.epml",
		"edited _mod-219414.epml",
		"edited _mod-219456.epml",
		"edited _mod-219491.epml",
		"edited _mod-219545.epml",
		"edited _mod-219596.epml",
		"edited _mod-219923.epml",
		"edited _mod-220114.epml",
		"edited _mod-221538.epml",
		"edited _mod-221898.epml",
		"edited _mod-222028.epml",
		"edited _mod-223637.epml",
		"edited _mod-223738.epml",
		"edited _mod-223792.epml",
		"edited _mod-223959.epml",
		"edited _mod-224114.epml",
		"edited _mod-224186.epml",
		"edited _mod-225350.epml",
		"edited _mod-229043.epml",
		"edited _mod-229582.epml",
		"edited _mod-229710.epml",
		"edited _mod-250771.epml",
		"edited _mod-253807.epml",
		"edited _mod-253846.epml",
		"edited _mod-253899.epml",
		"edited _mod-254041.epml",
		"edited _mod-254098.epml",
		"edited _mod-254147.epml",
		"edited _mod-254290.epml",
		"edited _mod-254356.epml",
		"edited _mod-254672.epml",
		"edited _mod-254711.epml",
		"edited _mod-254815.epml",
		"edited _mod-255417.epml",
		"edited _mod-255967.epml",
		"edited _mod-258016.epml",
		"edited _mod-266293.epml",
		"edited _mod-268777.epml",
		"edited _mod-273010.epml",
		"edited _mod-274007.epml",
		"edited _mod-274025.epml",
		"edited _mod-275537.epml",
		"edited _mod-275590.epml",
		"edited _mod-275636.epml",
		"edited _mod-275731.epml",
		"edited _mod-275774.epml",
		"edited _mod-275825.epml",
		"edited _mod-277274.epml",
		"edited _mod-277374.epml",
		"edited _mod-281602.epml",
		"edited _mod-282002.epml",
		"edited _mod-282100.epml",
		"edited _mod-284421.epml",
		"edited _mod-285482.epml",
		"edited _mod-285488.epml",
		"edited _mod-285498.epml",
		"edited _mod-287074.epml",
		"edited _mod-296811.epml",
		"edited _mod-297317.epml", 
		"edited _mod-297323.epml",
		"edited _mod-297329.epml",
		"edited _mod-302618.epml",
		"edited _mod-305103.epml",
		"edited _mod-305109.epml",
		"edited _mod-305121.epml",
		"edited _mod-305127.epml",
		"edited _mod-305133.epml",
		"edited _mod-305495.epml",
		"edited _mod-333228.epml",
		"edited _mod-333545.epml",
		"edited _mod-334018.epml",
		"edited _mod-334371.epml",
		"edited _mod-334651.epml",
		"edited _mod-334903.epml",
		"edited _mod-335447.epml",
		"edited _mod-335627.epml",
		"edited _mod-335815.epml",
		"edited _mod-335944.epml",
		"edited _mod-336123.epml",
		"edited _mod-336422.epml",
		"edited _mod-336603.epml",
		"edited _mod-336833.epml",
		"edited _mod-337520.epml",
		"edited _mod-338078.epml",
		"edited _mod-338279.epml",
		"edited _mod-338514.epml",
		"edited _mod-338648.epml",
		"edited _mod-338911.epml",
		"edited _mod-339399.epml",
		"edited _mod-340043.epml",
		"edited _mod-340446.epml",
		"edited _mod-340704.epml",
		"edited _mod-340885.epml",
		"edited _mod-341095.epml",
		"edited _mod-341225.epml",
		"edited _mod-341417.epml",
		"edited _mod-356861.epml",
		"edited _mod-357809.epml",
		"edited _mod-359943.epml",
		"edited _mod-360294.epml",
		"edited _mod-360652.epml",
		"edited _mod-361003.epml",
		"edited _mod-362455.epml",
		"edited _mod-376516.epml",
		"edited _mod-376541.epml",
		"edited _mod-377285.epml",
		"edited _mod-377421.epml",
		"edited _mod-377541.epml",
		"edited _mod-377612.epml",
		"edited _mod-378034.epml",
		"edited _mod-378138.epml",
		"edited _mod-378193.epml",
		"edited _mod-378239.epml",
		"edited _mod-378294.epml",
		"edited _mod-378371.epml",
		"edited _mod-378418.epml",
		"edited _mod-378752.epml",
		"edited _mod-379242.epml",
		"edited _mod-379282.epml",
		"edited _mod-379559.epml",
		"edited _mod-380505.epml",
		"edited _mod-380549.epml",
		"edited _mod-380835.epml",
		"edited _mod-380985.epml",
		"edited _mod-381483.epml",
		"edited _mod-381759.epml",
		"edited _mod-381881.epml",
		"edited _mod-382662.epml",
		"edited _mod-386888.epml",
		"edited _mod-389049.epml",
		"edited _mod-398202.epml",
		"edited _mod-398250.epml",
		"edited _mod-398602.epml",
		"edited _mod-398650.epml",
		"edited _mod-398830.epml",
		"edited _mod-398927.epml",
		"edited _mod-399116.epml",
		"edited _mod-399440.epml",
		"edited _mod-399593.epml",
		"edited _mod-399730.epml",
		"edited _mod-399776.epml",
		"edited _mod-399823.epml",
		"edited _mod-399956.epml",
		"edited _mod-400150.epml",
		"edited _mod-400260.epml",
		"edited _mod-400300.epml",
		"edited _mod-400373.epml",
		"edited _mod-400431.epml",
		"edited _mod-400532.epml",
		"edited _mod-400622.epml",
		"edited _mod-400678.epml",
		"edited _mod-400771.epml",
		"edited _mod-417317.epml",
		"edited _mod-418425.epml",
		"edited _mod-419581.epml",
		"edited _mod-419692.epml",
		"edited _mod-420282.epml",
		"edited _mod-420530.epml",
		"edited _mod-421609.epml",
		"edited _mod-422402.epml",
		"edited _mod-422904.epml",
		"edited _mod-423021.epml"
	};
}
