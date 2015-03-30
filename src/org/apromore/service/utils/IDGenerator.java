package org.apromore.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IDGenerator {
	
	private static final Logger logger = LoggerFactory.getLogger(IDGenerator.class);
	
	private static int currentProcessID = 880;
	private static int currentFragmentID = 207400;
	private static int currentExactCloneID = 0;
	
	private static int currentLogCustererID = 0;
	
	/**
	 * Used only for GuideTree based process collection mining.
	 */
	private static int currentGTNodeID = 1;
	
	public static String generateProcessID() {
		currentProcessID++;
		String id = "p" + currentProcessID;
		return id;
	}
	
	public static String generateFragmentID() {
		currentFragmentID++;
		String id = "F" + currentFragmentID;
		return id;
	}
	
	public static String generateExactCloneID() {
		currentExactCloneID++;
		String id = "E" + currentExactCloneID;
		return id;
	}
	
	public static String generateLogClustererID() {
		String id = "L" + currentLogCustererID;
		currentLogCustererID++;
		return id;
	}
	
	public static int generateGTNodeID() {
		currentGTNodeID++;
		return currentGTNodeID;
	}
}
