package org.apromore.service.impl;

import org.apromore.service.mining.SimplifiedProcessMiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * SMD <method> <complexity threshold> <input log file> <output folder>
 * 
 * method: S, SMD_S, B, SMD_B, M, SMD_M
 * complexity threshold: Maximum expected size (e.g. 38)
 * input log file: A process log in MXML format
 * output folder: Folder to write outputs (processes and subprocesses folders will be created in this folder)
 * 
 * @author cn
 */
public class WorkRunnerMain {
	
	public static void main(String[] args) {
		
		Logger log = LoggerFactory.getLogger(WorkRunnerMain.class);
		log.info("Starting the cluster evaluator...");
		
		ApplicationContext ctx = new FileSystemXmlApplicationContext("resources/META-INF/spring/applicationContext-WORK.xml");
		WorkRunner workRunner = (WorkRunner) ctx.getBean("workRunner");
		workRunner.testClusters();
		
		log.info("Completed the cluster evaluator.");
	}
}
