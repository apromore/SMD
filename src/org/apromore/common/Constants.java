package org.apromore.common;

public class Constants {

	// For the DB JNDI Lookup
    public static final String CONTEXT = "java:comp/env/jdbc/ApromoreDB";

	public static final String INITIAL_ANNOTATION = "Initial";
	public static final String ANNOTATIONS = "Annotations";
	public static final String CANONICAL = "Canonical";

    public static final boolean INDEX_CONNECTORS = false;
    public static final String TRUNK_NAME = "1.0";
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

    public static final String ANF_CONTEXT = "org.apromore.anf";
    public static final String CPF_CONTEXT = "org.apromore.cpf";
    public static final String XPDL2_CONTEXT = "org.wfmc._2008.xpdl2";
    public static final String EPML_CONTEXT = "de.epml";
    public static final String PNML_CONTEXT = "org.apromore.pnml";

    public static final String XPDL_2_1 = "XPDL 2.1";
    public static final String EPML_2_0 = "EPML 2.0";
    public static final String PNML_1_3_2 = "PNML 1.3.2";

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public static final int NO_LOCK = 0;
    public static final int INDIRECT_LOCK = 1;
    public static final int DIRECT_LOCK = 2;

    public static final int MAXIMUM_PROCESS_LOCK_ATTEMPTS = 10;
    public static final int PROCESS_LOCKING_WAITING_TIME = 5000;
}
