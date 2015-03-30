package common.tools;

import java.util.HashMap;
import java.util.LinkedList;

import planarGraphMathing.PlanarGraphMathing;
import planarGraphMathing.PlanarGraphMathing.MappingRegions;

import common.EPCModelParser;
import common.Settings;
import common.VertexPair;

import graph.Graph;
import graph.Vertex;
import graph.Edge;

public class GraphM {

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		addGraphicsToTestReductionRules();
//		addGraphicsToVPP();
//		addGraphicsToLandDevelopment();
//		addGraphicsModels();
		addGraphicsToAnything();
	}

	private static void addGraphicsToVPP(){
		
		HashMap<String, String> nodeIdMap = new HashMap<String, String>();
		
//		String prefix = "models/video post production/";
		String prefix = "models/video post production _paper/";
		String fileExt = ".epml";
		
		LinkedList<String> modelNames = new LinkedList<String>();
		
		for (int i = 1; i <= 4; i++) {
			for (int j = 1; j <= 4; j++) {
				for (int k = 1; k <= 4; k++) {
					for (int l = 1; l <= 4; l++) {
				
						if (i == j || i == k || i == l ||
								j == k || j == l ||
								k == l) {
							continue;
						}
						modelNames.add("vpp_"+i+""+j+"_merged");
						modelNames.add("vpp_"+i+""+j+""+k+"_merged");
						modelNames.add("vpp_"+i+""+j+""+k+""+l+"_merged");
					}
				}
			}
		}
		
		for (String s : modelNames) {
			String model1 = prefix + s + fileExt;
			String model2 = prefix + s + "_1" + fileExt;
			
//			System.out.println(model1);
			Graph g1 = EPCModelParser.readModels(model1, false).get(0);
//			g1.removeEmptyNodes();
	//		System.out.println("**************************************");
			Graph g2 = EPCModelParser.readModels(model2, false, true).get(0);
//			g2.removeEmptyNodes();
			
			getIDMap(nodeIdMap, g1, g2);
			
			addGraphics(nodeIdMap, g1, g2);
			
	
			EPCModelParser.writeModel(model1, g1);	
		}

	}

	private static void addGraphicsToAnything(){
		
		HashMap<String, String> nodeIdMap = new HashMap<String, String>();
		
//		String prefix = "models/video post production _paper/";
		String prefix = "models/test reduction rules/";
		String fileExt = ".epml";
		
		String modelName = "rule4_left_rule4_right_merged";
		
		String model1 = prefix + modelName + fileExt;
		String model2 = prefix + modelName + "_1" + fileExt;
		
//			System.out.println(model1);
		Graph g1 = EPCModelParser.readModels(model1, false).get(0);
//			g1.removeEmptyNodes();
//		System.out.println("**************************************");
		Graph g2 = EPCModelParser.readModels(model2, false, true).get(0);
//			g2.removeEmptyNodes();
		
		getIDMap(nodeIdMap, g1, g2);
		
		addGraphics(nodeIdMap, g1, g2);
		

		EPCModelParser.writeModel(model1, g1);	

	}
	
	private static void addGraphicsToLandDevelopment(){
		
		HashMap<String, String> nodeIdMap = new HashMap<String, String>();
		
		String prefix = "models/land develpment/";
		String fileExt = ".epml";
		
		String modelName = "SA_WA_merged";
		
		String model1 = prefix + modelName + fileExt;
		String model2 = prefix + modelName + "_1" + fileExt;
		
//			System.out.println(model1);
		Graph g1 = EPCModelParser.readModels(model1, false).get(0);
//			g1.removeEmptyNodes();
//		System.out.println("**************************************");
		Graph g2 = EPCModelParser.readModels(model2, false, true).get(0);
//			g2.removeEmptyNodes();
		
		getIDMap(nodeIdMap, g1, g2);
		
		addGraphics(nodeIdMap, g1, g2);
		

		EPCModelParser.writeModel(model1, g1);	

	}

	private static void addGraphicsModels(){
		
		HashMap<String, String> nodeIdMap = new HashMap<String, String>();
		
		String prefix = "models/video post production/";
		//String prefix =  "models/test reduction rules/";
		String fileExt = ".epml";
		
		String[] modelNames = new String[] {
//				"rule3_left_rule3_right_merged"
				"vpp_12_vpp_3_MERGED"
		};
		
		for (String s : modelNames) {
			String model1 = prefix + s + fileExt;
			String model2 = prefix + s + "_1" + fileExt;
			
//			System.out.println(model1);
			Graph g1 = EPCModelParser.readModels(model1, false).get(0);
//			g1.removeEmptyNodes();
	//		System.out.println("**************************************");
			Graph g2 = EPCModelParser.readModels(model2, false, true).get(0);
//			g2.removeEmptyNodes();
			
			getIDMap(nodeIdMap, g1, g2);
			
			addGraphics(nodeIdMap, g1, g2);
			
	
			EPCModelParser.writeModel(model1, g1);	
		}

	}

	private static void addGraphicsToTestReductionRules(){
		
		HashMap<String, String> nodeIdMap = new HashMap<String, String>();
		
		String prefix = "models/test reduction rules/";
		String fileExt = ".epml";
		
		String[] modelNames = new String[] {
				"rule1_left_rule1_right_merged",
				"rule2_left_rule2_right_merged",
				"rule3_left_rule3_right_merged"
		};
		
		for (String s : modelNames) {
			String model1 = prefix + s + fileExt;
			String model2 = prefix + s + "_1" + fileExt;
			
//			System.out.println(model1);
			Graph g1 = EPCModelParser.readModels(model1, false).get(0);
//			g1.removeEmptyNodes();
	//		System.out.println("**************************************");
			Graph g2 = EPCModelParser.readModels(model2, false, true).get(0);
//			g2.removeEmptyNodes();
			
			getIDMap(nodeIdMap, g1, g2);
			
			addGraphics(nodeIdMap, g1, g2);
			
	
			EPCModelParser.writeModel(model1, g1);	
		}

	}
	
	private static void getIDMap(HashMap<String, String> nodeIdMap, Graph g1, Graph g2) {

		MappingRegions mappings = PlanarGraphMathing.findMatchWithGW(g1, g2, Settings.MERGE_THRESHOLD, true);

		for (LinkedList<VertexPair> regions : mappings.getRegions()) {
			for (VertexPair region : regions) {
				nodeIdMap.put(region.getLeft().getID(), region.getRight().getID());
			}
		}
	}

	private static void addGraphics(HashMap<String, String> nodeIdMap, Graph g1, Graph g2) {
//		System.out.println("ADD GRAPHICS");
		for (Vertex v : g1.getVertices()) {
			String matchID = nodeIdMap.get(v.getID());
			for (Vertex v1 : g2.getVertices()) {
				if(matchID.equals(v1.getID())) {
					v.setGraphics(v1.getGraphics());
					break;
				}
			}
		}
		for (Edge e : g1.getEdges()) {
			for (Edge e1 : g2.getEdges()) {
				String matchFrom = nodeIdMap.get(e.getFromVertex());
				String matchTo = nodeIdMap.get(e.getToVertex());
//				System.out.println(e.getFromVertex()+" "+e1.getFromVertex() + 
//						" "+ e.getToVertex()+" "+e1.getToVertex());
				if(matchFrom.equals(e1.getFromVertex()) 
						&& matchTo.equals(e1.getToVertex())) {
//					System.out.println("EEEQ*ALS "+e1.getFromG()+" "+e1.getToG());
					e.setFromG(e1.getFromG());
					e.setToG(e1.getToG());
					break;
				}
			}
		}

	}
}
