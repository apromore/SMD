package test;

import common.EPCModelParser;
import common.Settings;

import graph.Graph;
import merge.MergeModels;

public class MergeProcessModels {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			testSAWA(args[0], args[1]);
		} else if (args.length == 4 && "-mt".equals(args[0])) {
			double newMergeThreshold = Double.parseDouble(args[1]);
			if (newMergeThreshold >=0 && newMergeThreshold <= 1) {
				Settings.MERGE_THRESHOLD = Double.parseDouble(args[1]);
			} else {
				System.out.println("Merge threshold not in valid range, leaving it to default 0.5 value");
			}
			testSAWA(args[2], args[3]);
		} else {
			System.out.println("USAGE: \n" +
					"ProcessMerger [mt mt_value] model1 model2\n" +
					"-mt - custom merge threshold - if similarity for nodes is > threshold, then 2 nodes are merged\n" +
					"If mt and ct are not specified, the default value of 0.5 is used for both.");
		}
	}

	public static void testSAWA(String m1, String m2) {

		Graph g1 = EPCModelParser.readModels(m1, false).get(0);
		g1.reorganizeIDs();
		
		Graph g2 = EPCModelParser.readModels(m2, false).get(0);
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();


		Graph merged = new MergeModels().mergeModels(g1, g2);
		
		EPCModelParser.writeModel(m1.substring(0, m1.length()-5) + "_"+m2.substring(0, m2.length()-5) + ".epml", merged);	
	}

}
