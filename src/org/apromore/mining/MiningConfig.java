package org.apromore.mining;

public class MiningConfig {

	public static int COMPLEXITY_MATRIC_N = 36; // 30 | 56
	public static int N_PREPROCESSING_BUFFER = 5; // 5
	
	public static double GED_THRESHOLD = 0.4d; // 0.4d | 0.1d for bpi log as it similarities are high.
	
	// Maximum size of a process model after trace clustering (before giving as the input to the subprocess extraction + trace clustering).
	public static int INITIAL_PROCESS_SIZE = 100; // 50 | 80 for 56 threshold
	
	public static int MAX_GED_FRAGMENT_SIZE = 30; // 25 | 25 gives better results
	public static int MIN_GED_FRAGMENT_SIZE = 4; // 4
	public static int MIN_EXACT_CLONE_FRAGMENT_SIZE = 10; // 5 | 10 for restricting the reduction of approximate clones 

	public static String CLUSTERING_ALGORITHM = "DBSCAN"; // DBSCAN
	
	public static boolean REMOVE_EVENTS = true; // false (this is not supported in gt pcm)
	public static boolean IDENTIFY_EXACT_CLONES_SEPARATELY = true; // true;
	
	public static int SELECTTION_1_LOWERBOUND = 20; // 15 , 20
	public static int SELECTTION_1_UPPERBOUND = 25; // 20 , 25
	
	public static int SELECTTION_2_LOWERBOUND = 15; // 10 , 15
	public static int SELECTTION_2_UPPERBOUND = 20; // 15 , 20
	
	public static int SELECTTION_3_LOWERBOUND = 10; // 10 , 10 not tested
	public static int SELECTTION_3_UPPERBOUND = 15; // 15 , 15 not tested

	public static boolean ADD_COMMONG_START_END_EVENTS_BEFORE_MERGING = false; // true
	public static boolean DWS_USE_ALTERNATE_CLUSTER_DIVIDING = true; // true
	
	public static String PCM_EVAL_FILE_PATH = "/home/chathura/projects/qut/temp/f1";
	public static String MERGED_MODELS_PATH = "/home/chathura/projects/qut/temp/merged";
	public static String TEMP_LOG_FILE_PATH = "/home/chathura/projects/qut/temp/f3";
	public static final boolean SERIALIZE_NONMULTITERMINAL_LOGS = true;
	public static final boolean WRITE_EVAL_DATA = false;
	
	public static int LOG_NOISE_FOR_TC = 1; // 2
	public static int MIN_SPLITTABLE_LOG_SIZE = 1; // 1, 2
	
	// whether we want to serialize the logs of the final process models.
	// WARNING: if we mine large number of process models, this could take large space (> 4 GB) and time.
	public static boolean SERIALIZE_LOGS = true; // false
	public static boolean SERIALIZE_INVALID_MODELS = false; // false
	public static boolean WRITE_ADDITIONAL_DATA = true; // false
	
	public static boolean PURE_MODELS = false;
	public static boolean ADD_LINE_BREAKS_WHEN_FORMATTING_LABELS = false; // set this to true to add line breaks to labels of serialized models in order to increase readability

	public static String getConfig() {
		StringBuffer b = new StringBuffer();
		b.append("MiningConfig\n");
		b.append("Complexity metric N," + COMPLEXITY_MATRIC_N + "\n");
		b.append("N preprocessing buffer," + N_PREPROCESSING_BUFFER + "\n");
		b.append("GED threshold," + GED_THRESHOLD + "\n");
		b.append("Initial process size," + INITIAL_PROCESS_SIZE + "\n");
		b.append("Max GED fragment size," + MAX_GED_FRAGMENT_SIZE + "\n");
		b.append("Min GED fragment size," + MIN_GED_FRAGMENT_SIZE + "\n");
		b.append("Min exact clone fragment size," + MIN_EXACT_CLONE_FRAGMENT_SIZE + "\n");
		b.append("Clustering algorithm," + CLUSTERING_ALGORITHM + "\n");
		b.append("DWS use alternate cluster dividing," + (DWS_USE_ALTERNATE_CLUSTER_DIVIDING? "true" : "false") + "\n");
		b.append("Remove events," + (REMOVE_EVENTS? "true" : "false") + "\n");
		b.append("Serialize logs," + (SERIALIZE_LOGS? "true" : "false") + "\n");
		b.append("Serialize invalid models," + (SERIALIZE_INVALID_MODELS? "true" : "false") + "\n");
		b.append("Identify exact clones separately," + (IDENTIFY_EXACT_CLONES_SEPARATELY? "true" : "false") + "\n");
		b.append("Selection 1 lowerbound," + SELECTTION_1_LOWERBOUND + "\n");
		b.append("Selection 1 upperbound," + SELECTTION_1_UPPERBOUND + "\n");
		b.append("Selection 2 lowerbound," + SELECTTION_2_LOWERBOUND + "\n");
		b.append("Selection 2 upperbound," + SELECTTION_2_UPPERBOUND + "\n");
		b.append("Selection 3 lowerbound," + SELECTTION_3_LOWERBOUND + "\n");
		b.append("Selection 3 upperbound," + SELECTTION_3_UPPERBOUND + "\n");
		b.append("Log noise for TC," + LOG_NOISE_FOR_TC + "\n");
		return b.toString();
	}
}
