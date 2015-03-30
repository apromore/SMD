package test;

import java.util.LinkedList;

import common.EPCModelParser;

import merge.MergeModels;
import graph.Graph;

public class TestMergeVideoPP {

	public static void main(String[] args) {
//		testVideoPostProduction();
		testVideoPostProductionAll();
//		testVideoPostProduction2();
	}

	public static void testVideoPostProduction() {
		String model_prefix = "models/video post production/";
		String result_prefix = "models/video post production wordnet/";
		
		String fileExt = ".epml";
		String file = "vpp_";	
		
//		int i = 4;
//		int j = 2;
//		int k = 1;
//		int m = 3;

		int i = 4;
		int j = 3;
		

		Graph.cleanGraphIDs();
		
		Graph g1 = EPCModelParser.readModels(model_prefix + file + i + fileExt, false).get(0);
		g1.reorganizeIDs();
		
		System.out.println("*******************************************");
		
		Graph g2 = EPCModelParser.readModels(model_prefix + file + j + fileExt, false).get(0);
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();

		Graph merged = new MergeModels().mergeModels(g1, g2);
		EPCModelParser.writeModel(result_prefix +""+file + i+"_"+file + j+"_MERGED.epml", merged);	

//		Graph g3 = EPCModelParser.readModels(model_prefix + file + k + fileExt, false).get(0);
//		g3.reorganizeIDs();
//		
//		Graph g4 = EPCModelParser.readModels(model_prefix + file + m + fileExt, false).get(0);
//		g4.reorganizeIDs();
//
//		g3.addLabelsToUnNamedEdges();
//		g4.addLabelsToUnNamedEdges();
//
//		merged = MergeModels.mergeModels(merged, g3);
//		
//		EPCModelParser.writeModel(result_prefix +""+file + i+"_"+file + j+"_"+file + k+"_MERGED.epml", merged);	

//		System.out.println("*******************************************");

//		merged = MergeModels.mergeModels(merged, g4);
//		EPCModelParser.writeModel(result_prefix +""+file + i+"_"+file + j+"_"+file + k+ "_"+file + m+"_MERGED.epml", merged);	
				
	}
	
	public static void testVideoPostProduction2() {
		String model_prefix = "models/video post production/";
		String result_prefix = "models/video post production/";
		
		String fileExt = ".epml";
		String file = "vpp_";	
		String add = "_merged";//"_merged";
		
		int i = 421;
		int k = 3;

		Graph.cleanGraphIDs();

		Graph g1 = EPCModelParser.readModels(model_prefix + file + i + add + fileExt, false).get(0);
		g1.reorganizeIDs();
		
		Graph g3 = EPCModelParser.readModels(model_prefix + file + k + fileExt, false).get(0);
		g3.reorganizeIDs();

		g1.addLabelsToUnNamedEdges();
		g3.addLabelsToUnNamedEdges();

		Graph merged = new MergeModels().mergeModels(g1, g3);
		EPCModelParser.writeModel(result_prefix +""+file + i+"_"+file + k+"_MERGEDX.epml", merged);	
				
	}

	
	public static void testVideoPostProductionAll() {
		String model_prefix = "models/video post production/";
		String result_prefix = "models/video post production/";
		
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

				Graph merged = new MergeModels().mergeModels(g1, g2);
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
	
					Graph merged = new MergeModels().mergeModels(g1, g2);
					
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
		
						Graph merged = new MergeModels().mergeModels(g1, g2);
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
}
