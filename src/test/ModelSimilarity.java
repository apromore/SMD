package test;

import graph.Graph;

import java.util.HashSet;
import java.util.LinkedList;

import merge.MergingPaper;

import common.EPCModelParser;
import common.Settings;

public class ModelSimilarity {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length == 0 || (args.length == 1 && "-help".equals(args[0]))){
			printHelp();
		} else {
			HashSet<String> containedParams = new HashSet<String>();
			LinkedList<String> modelnames = new LinkedList<String>();
			for (int i = 0; i < args.length; i++) {
				String param = args[i];
				boolean paramEnd = false;
				// this is a parameter
				if (!paramEnd && param.startsWith("-")) {
					int separator = param.indexOf("=");
					if (separator == -1) {
						System.out.println("Invalid parameters. Parameters and values must be separated by = sign.");
						printHelp();
						return;
					}
					String paramHead = param.substring(1, separator);
					String value = param.substring(separator + 1);
					if (containedParams.contains(paramHead)) {
						System.out.println("Parameter "+paramHead+ " occurred twice.");
						printHelp();
						return;
					} else {
						processParameters(paramHead, value);
						containedParams.add(paramHead);
					}
				} else {
					paramEnd = true;
					// other ones should me model names
					modelnames.add(param);
				}
			}
			
			// parameters processed
			if (modelnames.size() != 2) {
				System.out.println("Give 2 modelnames.");
				printHelp();
				return;
			}
			testMerge(modelnames);
		}
	}
	
	private static void processParameters(String parameter, String value) {
		
		if ("mt".equals(parameter) || "ct".equals(parameter)) {
			Double v = 0.0;
			try {
				v = Double.parseDouble(value);
				if (v < 0 || v > 1) {
					throw new Exception();
				}
			} catch (Exception e) {
				System.out.println("Invalid parameter value for threshold - "+ value +".\nParameter value for merge and context treshold must be double number in range [0, 1].");
				printHelp();
				System.exit(0);
			}
			// now the parameter should be also ok
			if ("mt".equals(parameter)) {
				Settings.MERGE_THRESHOLD = v;
			} else {
				Settings.MERGE_CONTEXT_THRESHOLD = v;
			}
		} else if ("ent".equals(parameter)) {
			if ("true".equals(value)) {
				Settings.REMOVE_ENTANGLEMENT = true;
			} else if ("false".equals(value)) {
				Settings.REMOVE_ENTANGLEMENT = false;
			} else {
				System.out.println("Invalid parameter value for entanglement - "+ value+".\nEntanglement parameter value can be only true or false.");
				printHelp();
				System.exit(0);
			}
		} else if ("alg".equals(parameter)) {
			if ("h".equals(value)) {
				Settings.COMPARISON_METHOD = Settings.ComparisonMethod.Hungarian;
			} else if ("g".equals(value)) {
				Settings.COMPARISON_METHOD = Settings.ComparisonMethod.Greedy;
			} else {
				System.out.println("Invalid parameter value for agorithm - "+ value +".\nAlgorithm parameter should be h for hungarian and g for greedy.");
				printHelp();
				System.exit(0);
			}
		} else {
			System.out.println("Unknown parameter - "+ parameter);
			printHelp();
			System.exit(0);
		}
	}

	private static void printHelp() {
		System.out.println("USAGE: \n" +
				"ProcessSimilarity [-mt=mt_value] [-ct=ct_value] [-ent=[true|false]] " +
				" [-alg=[g|h]] model1 model2\n" +
				"-mt - threshold for non connector nodes, if similarity for nodes is >= threshold, " +
				"then 2 nodes are expected to be merged, default value=0.5\n" + 
				"-ct - threshold for connector nodes, if similarity for connectors is >= threshold, " +
				"then 2 connectors are expected to be merged, default value=0.75\n" + 
				"-ent - defines if mapped pairs that entangle should be removed or not, \n" +
				"if true, then entanglement pairs should be removed, default value=true\n" +
				"-alg - algorithm that is used for similarity calculation for merge,\n h - hungarian, g - greedy, default is greedy.\n"
				);
	}

	private static void testMerge(LinkedList<String> modelnames) {

		Graph g1 = EPCModelParser.readModels(modelnames.get(0), false).get(0);
		g1.removeEmptyNodes();

		g1.reorganizeIDs();
		
		Graph g2 = EPCModelParser.readModels(modelnames.get(1), false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();

		double similarity = new MergingPaper().getModelSimilarity(g1, g2);
		
		System.out.println(modelnames.get(0) + " " + modelnames.get(1) + " similarity is: " + similarity);
	}
}
