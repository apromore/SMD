package org.apromore.service.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.utils.MiningUtils;
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
public class SMD2 {
	
	private static Logger log = null;

	public static void main2(String[] args) {
		log = LoggerFactory.getLogger(SMD2.class);
		smdAll();
	}
	
    public static void main(String[] args) {

    	String programLogPath = "/home/chathura/projects/qut/mining/apromore_mining_ws2/smd/logs";
    	
//        String logFile = "/home/chathura/projects/qut/mining/apromore-mining/tests/t_smd/logs/new/bpi_log.mxml";
        String logFile = "/home/chathura/projects/qut/mining/apromore-mining/tests/t_smd/logs/1568.mxml";
        String outPath = "/home/chathura/projects/qut/mining/apromore-mining/tests/t_smd/run_test2/smd_b";
        
        if (args == null || args.length != 4) {
        	args = new String[4];
            args[0] = "B";
            args[1] = "50";
            args[2] = logFile;
            args[3] = outPath;
        } else {
        	outPath = args[3];
        }
        
        initializePaths(outPath);
        
        File outFolder = new File(outPath);
        if (outFolder.exists()) {
        	try {
				FileUtils.cleanDirectory(outFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
        } else {
        	outFolder.mkdirs();
        }

        Logger log = LoggerFactory.getLogger(SMD2.class);
        log.info("Starting the SMD method with parameters: {}", args);

        ApplicationContext ctx = new FileSystemXmlApplicationContext("resources/META-INF/spring/applicationContext-WORK.xml");
        SimplifiedProcessMiner miner = (SimplifiedProcessMiner) ctx.getBean("simplifiedProcessMiner");
        miner.mineCollection(args);

        log.info("Completed the SMD method.");
        
        File programLogsFolder = new File(programLogPath);
        try {
        	if (programLogsFolder.exists()) {
				FileUtils.copyDirectory(programLogsFolder, outFolder);
				FileUtils.cleanDirectory(programLogsFolder);
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        deleteTemp(outPath);
    }
    
    public static void smdAll() {
    	
    	String outBase = "/home/chathura/projects/qut/mining/apromore-mining/tests/t3/test3";
    	
    	String motor_log = "/home/chathura/projects/qut/mining/apromore-mining/tests/t3/logs/motor_log.mxml";
    	String com_log = "/home/chathura/projects/qut/mining/apromore-mining/tests/t3/logs/com_log.mxml";
    	String bpi_log = "/home/chathura/projects/qut/mining/apromore-mining/tests/t3/logs/bpi_log.mxml";
    	
    	ApplicationContext ctx = new FileSystemXmlApplicationContext("resources/META-INF/spring/applicationContext-WORK.xml");
        SimplifiedProcessMiner miner = (SimplifiedProcessMiner) ctx.getBean("simplifiedProcessMiner");
    	
    	// BPI log
//    	testAllTechniques(bpi_log, "56", outBase, "bpi", miner);
    	
    	// Com log
//    	testAllTechniques(com_log, "34", outBase, "com", miner);
    	
    	// Motor log
    	testAllTechniques(motor_log, "37", outBase, "motor", miner);
        
        log.info("All SMD tests are complete.");
    }
    
    private static void testAllTechniques(String currentLog, String currentThreshold, String outBase, String outPrefix, SimplifiedProcessMiner miner) {
    	
    	log.info("Testing all techniques on the {} log.", currentLog);
    	
    	String[] args = new String[4];
    	
    	log.info("Starting SMD_S with Motor log available at " + currentLog);
    	args[0] = "SMD_S";
        args[1] = currentThreshold;
        args[2] = currentLog;
        args[3] = getOutPath(outBase, outPrefix + "_smd_s");
        miner.mineCollection(args);
        
        log.info("Starting S with Motor log available at " + currentLog);
    	args[0] = "S";
        args[1] = currentThreshold;
        args[2] = currentLog;
        args[3] = getOutPath(outBase, outPrefix + "_s");
        miner.mineCollection(args);
        
        log.info("Starting SMD_B with Motor log available at " + currentLog);
    	args[0] = "SMD_B";
        args[1] = currentThreshold;
        args[2] = currentLog;
        args[3] = getOutPath(outBase, outPrefix + "_smd_b");
        miner.mineCollection(args);
        
        log.info("Starting B with Motor log available at " + currentLog);
    	args[0] = "B";
        args[1] = currentThreshold;
        args[2] = currentLog;
        args[3] = getOutPath(outBase, outPrefix + "_b");
        miner.mineCollection(args);
        
        log.info("Starting SMD_M with Motor log available at " + currentLog);
    	args[0] = "SMD_M";
        args[1] = currentThreshold;
        args[2] = currentLog;
        args[3] = getOutPath(outBase, outPrefix + "_smd_m");
        miner.mineCollection(args);
        
        log.info("Starting M with Motor log available at " + currentLog);
    	args[0] = "M";
        args[1] = currentThreshold;
        args[2] = currentLog;
        args[3] = getOutPath(outBase, outPrefix + "_m");
        miner.mineCollection(args);
    }
    
    private static String getOutPath(String base, String name) {
    	File outFolder = new File(base, name);
    	if (outFolder.exists()) {
    		try {
				FileUtils.cleanDirectory(outFolder);
			} catch (IOException e) {
				log.error("Failed to clean the output folder: " + outFolder.getAbsolutePath());
			}
    	} else {
    		outFolder.mkdir();
    	}
    	return outFolder.getAbsolutePath();
    }
    
    private static void initializePaths(String outPath) {
		
		File tempFolder = new File(outPath, "temp");
		if (tempFolder.exists()) {
			try {
				FileUtils.cleanDirectory(tempFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			tempFolder.mkdir();
		}
		
		File f1 = new File(tempFolder, "f1");
		f1.mkdir();
		MiningConfig.PCM_EVAL_FILE_PATH = f1.getAbsolutePath();
		
		File f2 = new File(tempFolder, "f2");
		f2.mkdir();
		MiningUtils.logsFolder = f2.getAbsolutePath();
		
		File f3 = new File(tempFolder, "f3");
		f3.mkdir();
		MiningConfig.TEMP_LOG_FILE_PATH = f3.getAbsolutePath();
		
		File merged = new File(tempFolder, "merged");
		merged.mkdir();
		MiningConfig.MERGED_MODELS_PATH = merged.getAbsolutePath();
		
	}
    
    private static void deleteTemp(String outPath) {
    	File tempFolder = new File(outPath, "temp");
		if (tempFolder.exists()) {
			try {
				FileUtils.deleteDirectory(tempFolder);
			} catch (IOException e1) {
				try {
					FileUtils.cleanDirectory(tempFolder);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} 
    }
}
