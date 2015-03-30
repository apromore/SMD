package test;

import java.util.LinkedList;

import common.EPCModelParser;
import common.Settings;
import common.VertexPair;
import common.algos.GraphEditDistanceGreedy;
import common.similarity.AssingmentProblem;

import merge.MergingPaper;
import graph.Graph;

public class TestMergeVideoPP_Paper {

	public static void main(String[] args) {
//		testVideoPostProduction();
//		testVideoPostProductionAll();
//		testVideoPostProduction2();
		testAllModelsSimilarity();
	}

	public static void testVideoPostProduction() {
		String model_prefix = "models/video post production/";
		String result_prefix = "models/video post production _paper/";
		
		String fileExt = ".epml";
		String file = "vpp_";	
		
//		int i = 4;
//		int j = 2;
//		int k = 1;
//		int m = 3;

		int i = 1;
		int j = 2;
		

		Graph.cleanGraphIDs();
		
		Graph g1 = EPCModelParser.readModels(model_prefix + file + i + fileExt, false).get(0);
		g1.reorganizeIDs();
		
		System.out.println("*******************************************");
		
		Graph g2 = EPCModelParser.readModels(model_prefix + file + j + fileExt, false).get(0);
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();

		Graph merged = new MergingPaper().mergeModels(g1, g2);
		EPCModelParser.writeModel(result_prefix +""+file + i+"_"+file + j+"_MERGEDY.epml", merged);	

//		Graph g3 = EPCModelParser.readModels(model_prefix + file + k + fileExt, false).get(0);
//		g3.reorganizeIDs();
//		
//		Graph g4 = EPCModelParser.readModels(model_prefix + file + m + fileExt, false).get(0);
//		g4.reorganizeIDs();
//
//		g3.addLabelsToUnNamedEdges();
//		g4.addLabelsToUnNamedEdges();
//
//		merged = Merging.mergeModels(merged, g3);
//		
//		EPCModelParser.writeModel(result_prefix +""+file + i+"_"+file + j+"_"+file + k+"_MERGED.epml", merged);	

//		System.out.println("*******************************************");

//		merged = Merging.mergeModels(merged, g4);
//		EPCModelParser.writeModel(result_prefix +""+file + i+"_"+file + j+"_"+file + k+ "_"+file + m+"_MERGED.epml", merged);	
				
	}
	
	public static void testVideoPostProduction2() {
		String model_prefix = "models/video post production/";
		String model_prefix1 = "models/video post production _paper/";
		String result_prefix = "models/video post production _paper/";
		
		String fileExt = ".epml";
		String file = "vpp_";	
		String add = "_merged";//"_merged";
		
		int i = 14;
		int k = 3;

		Graph.cleanGraphIDs();

		Graph g1 = EPCModelParser.readModels((i < 10 ? model_prefix : model_prefix1)+ file + i + add + fileExt, false).get(0);
		g1.reorganizeIDs();
		
		Graph g3 = EPCModelParser.readModels((k < 10 ? model_prefix : model_prefix1) + file + k + fileExt, false).get(0);
		g3.reorganizeIDs();

		g1.addLabelsToUnNamedEdges();
		g3.addLabelsToUnNamedEdges();

		Graph merged = new MergingPaper().mergeModels(g1, g3);
		EPCModelParser.writeModel(result_prefix +""+file + i+"_"+file + k+"_MERGEDX.epml", merged);	
				
	}

	
	public static void testVideoPostProductionAll() {
		String model_prefix = "models/video post production/";
		String result_prefix = "models/video post production _paper/";
		
		LinkedList<String> modelInf = new LinkedList<String>();
		
		String fileExt = ".epml";
		String file = "vpp_";	

		// 2 models :
		for (int i = 1; i <= 4; i++) {
			for (int j = 1; j <= 4; j++) {
				
				if (i == j ) {
					continue;
				}

				Graph.cleanGraphIDs();
				Graph g1 = EPCModelParser.readModels(model_prefix + file + i + fileExt, false).get(0);
				g1.reorganizeIDs();
				
				Graph g2 = EPCModelParser.readModels(model_prefix + file + j + fileExt, false).get(0);
				g2.reorganizeIDs();
				
				g1.addLabelsToUnNamedEdges();
				g2.addLabelsToUnNamedEdges();

				Graph merged = new MergingPaper().mergeModels(g1, g2);
				EPCModelParser.writeModel(result_prefix +""+file + i+ j+"_merged.epml", merged);	
				
				modelInf.add(file + i+"\t"+g1.getVertices().size()+"\t"+ file + j+"\t"+g2.getVertices().size()+"\t"+
						merged.mergetime+"\t"+merged.beforeReduction+"\t"+((double)merged.beforeReduction/(double)(g1.getVertices().size()+g2.getVertices().size()))
						+"\t"+merged.getVertices().size()+"\t"+((double)merged.getVertices().size()/(double)(g1.getVertices().size()+g2.getVertices().size())));

			}
		}

		// 3 models:
		for (int i = 1; i <= 4; i++) {
			for (int j = 1; j <= 4; j++) {
				for (int k = 1; k <= 4; k++) {
				
					if (i == j || i == k || j == k) {
						continue;
					}
					
					Graph.cleanGraphIDs();
					
					Graph g1 = EPCModelParser.readModels(result_prefix +""+file + i+ j+"_merged" + fileExt, false).get(0);
					g1.reorganizeIDs();
					
					Graph g2 = EPCModelParser.readModels(model_prefix + file + k + fileExt, false).get(0);
					g2.reorganizeIDs();
					
					g1.addLabelsToUnNamedEdges();
					g2.addLabelsToUnNamedEdges();
	
					Graph merged = new MergingPaper().mergeModels(g1, g2);
					
					EPCModelParser.writeModel(result_prefix +""+file + i+ j+k+"_merged.epml", merged);	
					modelInf.add(file + i+ j+"\t"+g1.getVertices().size()+"\t"+file + k+"\t"+g2.getVertices().size()+"\t"+
							merged.mergetime+"\t"+merged.beforeReduction+"\t"+((double)merged.beforeReduction/(double)(g1.getVertices().size()+g2.getVertices().size()))
							+"\t"+merged.getVertices().size()+"\t"+((double)merged.getVertices().size()/(double)(g1.getVertices().size()+g2.getVertices().size())));
				}
			}
		}

		// 4 models:
		for (int i = 1; i <= 4; i++) {
			for (int j = 1; j <= 4; j++) {
				for (int k = 1; k <= 4; k++) {
					for (int m = 1; m <= 4; m++) {
				
						if (i == j || i == k || i == m ||
								j == k || j == m || k == m) {
							continue;
						}
						Graph.cleanGraphIDs();
						
						Graph g1 = EPCModelParser.readModels(result_prefix +""+file + i + j+ k +"_merged" + fileExt, false).get(0);
						g1.reorganizeIDs();
						
						Graph g2 = EPCModelParser.readModels(model_prefix + file + m + fileExt, false).get(0);
						g2.reorganizeIDs();
						
						g1.addLabelsToUnNamedEdges();
						g2.addLabelsToUnNamedEdges();
		
						Graph merged = new MergingPaper().mergeModels(g1, g2);
						EPCModelParser.writeModel(result_prefix +""+file + i + j + k + m +"_merged.epml", merged);	

						modelInf.add(file + i + j+ k+"\t"+g1.getVertices().size()+"\t"+file + m+"\t"+g2.getVertices().size()+"\t"+
								merged.mergetime+"\t"+merged.beforeReduction+"\t"+((double)merged.beforeReduction/(double)(g1.getVertices().size()+g2.getVertices().size()))
								+"\t"+merged.getVertices().size()+"\t"+((double)merged.getVertices().size()/(double)(g1.getVertices().size()+g2.getVertices().size())));

					}
				}
			}
		}
		
		for (String inf : modelInf) {
			System.out.println(inf);
		}
	}
	
	private static void testAllModelsSimilarity() {
		String model_prefix = "models/video post production/";
		
		String[] processModels = new String[]{
				"vpp_1.epml",
				"vpp_2.epml",
				"vpp_3.epml",
				"vpp_4.epml"
		};
		
		int method = 2;
		
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
				
				if (method == 1) {
					GraphEditDistanceGreedy gedepc = new GraphEditDistanceGreedy();
					Object weights[] = {"vweight", Settings.vweight, 
										"sweight", Settings.sweight, 
										"eweight", Settings.eweight};
					
					gedepc.setWeight(weights);
					double weight = gedepc.computeGED(g1, g2, false);
						System.out.println(processModels[i] + "("+size1+ ")\t"+ processModels[j] + "("+size2 + ")\t" + (1 - (weight < 0.0000001 ? 0: (weight > 1 ? 1 : weight))));
			
				} else if (method == 2) {
					double weight = 0;
					LinkedList<VertexPair> mapping = AssingmentProblem.getMappingsVetrexUsingNodeMapping(g1, g2, Settings.MERGE_THRESHOLD, Settings.MERGE_CONTEXT_THRESHOLD);
					int matched = 0;
					for (VertexPair vp : mapping) {
//						System.out.println(vp.getLeft() + " <> "+ vp.getRight() + " " + vp.getWeight());
						weight += vp.getWeight();
						matched++;
					}
					System.out.println(processModels[i] + "("+size1+ ")\t"+ processModels[j] + "\t"+size2 + "\t" + (weight/Math.max(size1, size2))+"\t"+matched);
				}
			}
		}
	}
}
