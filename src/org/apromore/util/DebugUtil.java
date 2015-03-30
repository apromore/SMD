package org.apromore.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apromore.graph.JBPT.CPF;
import org.apromore.service.utils.FormattableEPCSerializer;

public class DebugUtil {
	
	private static String outPath = null;
	private static FormattableEPCSerializer serializer = new FormattableEPCSerializer(); 
	
	public static int invalidModelsCount = 0;
	
	private static int modelNumber = 0;
	
	public static void initOutPath(String baseOutPath) {
		outPath = new File(baseOutPath, "debug").getAbsolutePath();
		File outFolder = new File(outPath);
		try {
			if (outFolder.exists()) {
				FileUtils.cleanDirectory(new File(outPath));
			} else {
				outFolder.mkdir();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeModel(String prefix, CPF model) {
		if (outPath == null) return;
		modelNumber++;
		File file = new File(outPath, prefix + "_" + modelNumber + ".epml");
		serializer.serialize(model, file.getAbsolutePath());
	}
	
	public static void writeModelToLocation(String path, CPF model) {
		modelNumber++;
		File file = new File(path);
		serializer.serialize(model, file.getAbsolutePath());
	}
	
	public static String getAsString(Collection<String> ss) {
		StringBuffer b = new StringBuffer();
		for (String s : ss) {
			b.append(s + ", ");
		}
		return b.toString();
	}
}
