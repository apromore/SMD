package org.apromore.mining.test;

import java.io.File;

import org.apromore.graph.JBPT.CPF;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.FormattableEPCSerializer;

public class ProcessFormatter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ProcessFormatter().format();
	}
	
	public void format() {
		
		String inPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/sap_good";
		String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/sap_good_formatted";
		
		EPCDeserializer deserializer = new EPCDeserializer();
		FormattableEPCSerializer serializer = new FormattableEPCSerializer();
		
		File inFolder = new File(inPath);
		File[] files = inFolder.listFiles();
		for (File file : files) {
			CPF cpf = deserializer.deserializeFile(file.getAbsolutePath());
			if (cpf.getVertices().size() >= 40) {
				String outFilePath = new File(outPath, file.getName()).getAbsolutePath();
				serializer.serialize(cpf, outFilePath);
			}
		}
	}

}
