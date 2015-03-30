/**
 * 
 */
package org.apromore.mining.utils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * @author Chathura C. Ekanayake
 *
 */
public class LogExtractor1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new LogExtractor1().work1();
	}
	
	public void work1() {
		
		String logFilePath = "/media/work/data/sc/simple_mining/log_original.csv";
		String extractedFilePath = "/media/work/data/sc/simple_mining/interval_2011_6_1_to_8_30/log.csv";
		
		try {
			List<String> lines = FileUtils.readLines(new File(logFilePath));
			int i = 0;
			int k = 0;
			Calendar gc = GregorianCalendar.getInstance();
			gc.set(2011, 6, 1);
			Date limit1 = gc.getTime();
			System.out.println(limit1.toString());
			Calendar gc2 = GregorianCalendar.getInstance();
			gc2.set(2011, 8, 30);
			Date limit2 = gc2.getTime();
			
			int eventID = 1;
			
			StringBuffer buf = new StringBuffer();
			buf.append("Case ID;Event ID;dd-MM-yyyy HH:mm;Activity\n");
//			buf.append("Case ID;Event ID;dd-MM-yyyy:HH.mm;Activity\n");
			for (String l : lines) {
				i++;
				
				if (i == 1) {
					continue;
				}
				
				String[] cols = l.split(",");
				
				if (cols.length < 3) {
					continue;
				}
				
				String claimID = cols[0];
				String time = cols[2];
				String event = cols[3];
				event = event.replaceAll("\"", " ");
				
				if (claimID.equals("1363526000")) {
//					System.out.println(l);
				}
				
				
				try {
//					DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DAY_OF_YEAR_FIELD, DateFormat.HOUR_OF_DAY0_FIELD);
//					19JAN2012:13:23:11.881000
					DateFormat df = new SimpleDateFormat("ddMMMyyyy:kk:mm:ss.SSSSSS");
					Date dtime = df.parse(time);
					if (dtime.after(limit1) && dtime.before(limit2)) {
						
//						dtime.get
						
						Calendar cal = Calendar.getInstance();
						cal.setTime(dtime);
						String timestamp = ft(cal.get(Calendar.DAY_OF_MONTH)) + "-" + 
											ft((cal.get(Calendar.MONTH) + 1)) + "-" +
											cal.get(Calendar.YEAR) + " " +
											ft(cal.get(Calendar.HOUR_OF_DAY)) + ":" +
											ft(cal.get(Calendar.MINUTE));
						
						String record = claimID + ";" + eventID + ";" + timestamp + ";" + event;
						eventID++;
						System.out.println(record);
						buf.append(record + "\n");
						k++;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
//				if (i == 100) {
//					break;
//				}
			}
			
			File ef = new File(extractedFilePath);
			ef.delete();
			ef.createNewFile();
			FileUtils.writeStringToFile(ef, buf.toString());
			
			System.out.println("Total records: " + i);
			System.out.println("Matched records: " + k);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String ft(int value) {
		String item = Integer.toString(value);
		if (item.length() < 2) {
			item = "0" + item;
		}
		return item;
	}

}
