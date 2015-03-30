package org.apromore.mining.complexity;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class TempFilterUtil {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			new TempFilterUtil().filter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void filter() throws Exception {

		String subprocessesPath = "/media/work/qut/process-mining/eval/merged/t4/pcm/subprocesses";
		String subprocessIdsPath = "/media/work/qut/process-mining/eval/merged/t4/pcm/subprocesses.txt";
		File sf = new File(subprocessIdsPath);
		
		Set<String> sids = new HashSet<String>();
		List<String> lines = FileUtils.readLines(sf);
		for (String l : lines) {
			int i1 = l.indexOf("F");
			int i2 = l.lastIndexOf(":");
			if (i1 > 0 && i2 > i1) {
				String sid = l.substring(i1, i2);
				sid = sid + ".epml";
				sids.add(sid);
				System.out.println(sid);
			}
		}
		
		int retained = 0;
		File sfolder = new File(subprocessesPath);
		File[] sfiles = sfolder.listFiles();
		for (File sfile : sfiles) {
			String sname = sfile.getName();
			if (sids.contains(sname)) {
				retained++;
			} else {
				sfile.delete();
			}
		}
		System.out.println(retained + " files retained.");
	}

}
