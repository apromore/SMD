package tools;

import java.io.File;

public class FileCounter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String resultsPath = "/home/chathura/projects/qut/mining/apromore-mining/tests/t_smd/run_105_ws";
		File file = new File(resultsPath);
		File[] results = file.listFiles();
		
		StringBuffer b = new StringBuffer();
		b.append("folder,processes,logs,match\n");
		
		for (File result : results) {
			if (!result.isDirectory()) {
				continue;
			}
			File processes = null;
			File logs = null;
			File[] subs = result.listFiles();
			for (File sub : subs) {
				if (sub.isDirectory() && sub.getName().equals("processes")) {
					processes = sub;
				}
				if (sub.isDirectory() && sub.getName().equals("logs")) {
					logs = sub;
				}
			}
			
			int numProcesses = (processes == null)? -1 : processes.list().length;
			int numLogs = (logs == null)? -1 : logs.list().length;
			String match = (numProcesses == numLogs)? "Yes" : "No";
			b.append(result.getName() + "," + numProcesses + "," + numLogs + "," + match + "\n");
		}
		System.out.println(b);
	}

}
