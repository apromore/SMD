package org.prom6.plugins.guidetreeminer.encoding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 June 2009
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */
public class EncodeTraces {
	private List<String> charStreamList;
	
	public EncodeTraces(Map<String, String> activityCharMap, XLog log) throws EncodingNotFoundException{
		charStreamList = new ArrayList<String>();

		StringBuilder charStreamBuilder = new StringBuilder();
		StringBuilder activityBuilder = new StringBuilder();
	
		XAttributeMap attributeMap;
		boolean ignoreTrace;
		for(XTrace trace : log){
			charStreamBuilder.setLength(0);
			ignoreTrace = false;
			for(XEvent event : trace){
				attributeMap = event.getAttributes();
				if(!attributeMap.containsKey("concept:name")){
					ignoreTrace = true;
					break;
				}

				activityBuilder.setLength(0);
				activityBuilder.append(attributeMap.get("concept:name").toString()).append("-").append(attributeMap.get("lifecycle:transition").toString());
			
				if(activityCharMap.containsKey(activityBuilder.toString())){
					charStreamBuilder.append(activityCharMap.get(activityBuilder.toString()));
				}else{
					throw new EncodingNotFoundException(activityBuilder.toString());
				}
				
			}
			if(!ignoreTrace)
				charStreamList.add(charStreamBuilder.toString());
		}
		System.out.println("No.Char Streams: "+charStreamList.size());
	}

	public List<String> getCharStreamList() {
		return charStreamList;
	}
}
