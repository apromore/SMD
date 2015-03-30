package org.apromore.mining.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;

import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.guidetree.DummyContext;
import org.apromore.mining.guidetree.Prom6Miner;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.util.progress.XMonitoredInputStream;
import org.deckfour.xes.util.progress.XProgressListener;
import org.springframework.beans.factory.annotation.Autowired;

public class MiningTest1 {
	
	private String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp/miningtests/log";
	private String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp/miningtests/model_p6.epml";
	
	@Autowired
	private Prom6Miner prom6Miner;
	
	public void mine() throws Exception {
		
		File logFolder = new File(logPath);
		String logFilePath = logFolder.listFiles()[0].getAbsolutePath();
		
		XLog log = getLog(logFilePath);
		DummyContext context = new DummyContext();
		CPF model = prom6Miner.mineMode(log, context);
		
		FormattableEPCSerializer epcSerializer = new FormattableEPCSerializer();
		epcSerializer.serialize(model, outPath);
	}
	
	private XLog getLog(String logPath) throws FileNotFoundException {
		File logFile = new File(logPath);
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(logFile));
		long fileSizeInBytes = logFile.length();
		
		XFactory factory = new XFactoryNaiveImpl();
		XParser parser = new XMxmlParser(factory);
		Collection<XLog> logs = null;
		try {
			logs = (new XMxmlParser()).parse(new XMonitoredInputStream(input, fileSizeInBytes,
					new XProgressListener() {

						public boolean isAborted() {
							return false;
						}

						public void updateProgress(int arg0, int arg1) {

						}
					}));
		} catch (Exception e) {
			logs = null;
		}
		
		XLog log = null;
		if (logs != null) {
			log = logs.iterator().next();
		}
		return log;
	}

}
