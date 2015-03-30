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
public class SMD {
	
	public static void main(String[] args) {
		
		Logger log = LoggerFactory.getLogger(SMD.class);
		log.info("Starting the SMD method with parameters: {}", args);
		
		ApplicationContext ctx = new FileSystemXmlApplicationContext("resources/META-INF/spring/applicationContext-WORK.xml");
		SimplifiedProcessMiner miner = (SimplifiedProcessMiner) ctx.getBean("simplifiedProcessMiner");
		miner.mineCollection(args);
		
		log.info("Completed the SMD method.");
	}
}
