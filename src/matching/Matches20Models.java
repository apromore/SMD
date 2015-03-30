package matching;

public class Matches20Models {
	/**
	 * File names of the model pairs.
	 */
	public static String mp[] = {"_pair01","_pair02","_pair03","_pair04","_pair05","_pair06","_pair07","_pair08","_pair09","_pair10","_pair11","_pair12","_pair13","_pair14","_pair15","_pair16","_pair17","_pair18","_pair19","_pair20"};

	/**
	 * Directory prefix to identify the filenames.
	 */
	public static String prefix = "models/20modelpairs/";
	
	/**
	 * indicates if we need english or dutch stemmer, 0 - english, 1 dutch
	 */
	public static int[] englishDutch = new int [] {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
	
	
	/**
	 * Models that have 1 to 1 mapping.
	 */
	private static int[] oneMappingModels = new int[]{13, 15, 14, 7, 5, 20};

}
