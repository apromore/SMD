/**
 * 
 */
package org.apromore.common;

/**
 * @author Chathura Ekanayake
 *
 */
public class FSConstants {

//	public static final String DEFAULT_INDEX_FILE = "/home/cn/projects/processSpace/index/psindex";
	public static final String DEFAULT_DATABASE_URL = "jdbc:mysql://localhost:3306/testdb2";
	public static final String DEFAULT_INDEX_FOLDER = "/home/cn/projects/processBase_svn1/index/";

	public static final boolean INDEX_CONNECTORS = false;
	public static final String TRUNK_NAME = "MAIN";
	public static final String TYPE = "type";
	public static final String CONNECTOR = "Connector";
	public static final String FUNCTION = "Function";
	public static final String EVENT = "Event";
	public static final String POCKET = "Pocket";
	public static final String PROCESS_NAME = "ProcessName";
	public static final String BRANCH_NAME = "BranchName";
	public static final String BRANCH_ID = "BranchID";
	public static final String VERSION_NUMBER = "VersionNumber";
	public static final String PROCESS_MODEL_VERSION_ID = "PMVID";
	public static final String ROOT_FRAGMENT_ID = "RootFragmentId";
	public static final String ORIGINAL_FRAGMENT_ID = "OriginalFragmentId";
	public static final String LOCK_STATUS = "LockStatus";
	public static final String LOCKED = "Locked";
	public static final String UNLOCKED = "Unlocked";
	public static final String PHASE1 = "Phase_1";
	public static final String PHASE2 = "Phase_2";
	
	public static final int NO_LOCK = 0;
	public static final int INDIRECT_LOCK = 1;
	public static final int DIRECT_LOCK = 2;
	
	public static final int MAXIMUM_PROCESS_LOCK_ATTEMPTS = 10;
	public static final int PROCESS_LOCKING_WAITING_TIME = 5000;
	
	public static final int ROUND_OFF_AMOUNT = 1000000;
	
	public static final String DBSCAN = "DBSCAN";
	public static final String HAC = "HAC";
}

