package org.prom6.plugins.causalnet.temp.index;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.elements.Dimension;

/**
 * Inverted index.
 * 
 * @author jribeiro
 * @email j.t.s.ribeiro@tue.nl
 * @version May 12, 2011
 */
public class InvertedIndex {

	private HashMap<String, Dimension> dimensions;
	
	private HashMap<String, ArrayList<Pair<Integer, Integer>>> index;
	private HashMap<String, XTrace> data;
	
	public InvertedIndex(){
		
		this.index = new HashMap<String, ArrayList<Pair<Integer, Integer>>>();
		this.data = new HashMap<String, XTrace>();
		this.dimensions = new HashMap<String, Dimension>();
	}
	
	/**
	 * Creates the complete inverted index of a given event log.
	 * All the information in the event log will be indexed.
	 * 
	 * @param log the event log to be indexed
	 */
	public void createCompleteIndex(XLog log){
		
		for(XTrace trace : log) this.insertTrace(trace, true);
		
//		for(Dimension dim : this.dimensions.values()){
//			
//			System.out.println(dim.getCategory()+"/"+dim.getName()+"="+dim.getCardinality());
//		}
	}
	
	/**
	 * Creates the partial inverted index of a given event log.
	 * Only the information about the event name and type will be indexed.
	 * 
	 * @param log the event log to be indexed
	 */
	public void createPartialIndex(XLog log){
		
		for(XTrace trace : log) this.insertTrace(trace, false);
		
//		for(Dimension dim : this.dimensions.values()){
//			
//			System.out.println(dim.getCategory()+"/"+dim.getName()+"="+dim.getCardinality());
//		}
	}
	
	
	/**
	 * Inserts a trace into the index.
	 * 
	 * @param trace the trace to be indexed
	 * @param complete true to index all trace's attributes, false to index only the event name and type attributes 
	 */
	private void insertTrace(XTrace trace, boolean complete){
		
		int eventID = 0;
		int traceID = data.size();
		
		Pair<Integer, Integer> indexEntry = new Pair<Integer, Integer>(traceID, null);
		
		if(complete){
		
			// INSTANCE-BASED ATTRIBUTES
			for(java.util.Map.Entry<String, XAttribute> entry : trace.getAttributes().entrySet()){
				
				String dimensionName = entry.getKey();
				String dimensionValue = entry.getValue().toString();
				
				if(dimensionName.startsWith("concept")) this.addEntry(dimensionName,"Instance","ID",dimensionValue,indexEntry);
				else{
					if(dimensionName.equals("description")) this.addEntry(dimensionName,"Instance","Description",dimensionValue,indexEntry);
					else this.addEntry(dimensionName,"Instance Data",dimensionName,dimensionValue,indexEntry);
				}
			}
		}
		
		// EVENT-BASED ATTRIBUTES
		for(XEvent event : trace){
			
			indexEntry = new Pair<Integer, Integer>(traceID, eventID);
			
			for(java.util.Map.Entry<String, XAttribute> entry : event.getAttributes().entrySet()){
				
				String dimensionName = entry.getKey();
				String dimensionValue = entry.getValue().toString();
				
				if(dimensionName.contains(":")){
					
					if(dimensionName.startsWith("concept")){

						String temp = dimensionName.substring(8,9).toUpperCase() + dimensionName.substring(9);
						
						this.addEntry(dimensionName,"Event",temp,dimensionValue, indexEntry);
						continue;
					}
					if(dimensionName.startsWith("lifecycle")){
						
						this.addEntry(dimensionName,"Event","Type",dimensionValue, indexEntry);
						continue;
					}
					if(!complete) continue;
					if(dimensionName.startsWith("org")){
						
						String temp = dimensionName.substring(4,5).toUpperCase() + dimensionName.substring(5);
						
						this.addEntry(dimensionName,"Originator",temp,dimensionValue, indexEntry);
						continue;
					}
					if(dimensionName.startsWith("time")){
						
						XAttributeTimestamp stamp = (XAttributeTimestamp) entry.getValue();
						
						Calendar cal = Calendar.getInstance();
						cal.setTime(stamp.getValue());
						
						int year = cal.get(Calendar.YEAR);
						int month = cal.get(Calendar.MONTH) + 1;
						int dayMonth = cal.get(Calendar.DAY_OF_MONTH);
						int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
						int hour = cal.get(Calendar.HOUR_OF_DAY);
						int minute = cal.get(Calendar.MINUTE);
						
						String hours;
						if(hour < 10) hours = 0 + String.valueOf(hour);
						else hours = String.valueOf(hour);
						
						String minutes;
						if(minute < 10) minutes = 0 + String.valueOf(minute);
						else minutes = String.valueOf(minute);
						
						String monthName;
						String quarter;
						switch (month){
							case 1: monthName = "January"; quarter = "1"; break;
							case 2: monthName = "February"; quarter = "1"; break;
							case 3: monthName = "March"; quarter = "1"; break;
							case 4: monthName = "April"; quarter = "2"; break;
							case 5: monthName = "May"; quarter = "2"; break;
							case 6: monthName = "June"; quarter = "2"; break;
							case 7: monthName = "July"; quarter = "3"; break;
							case 8: monthName = "August"; quarter = "3"; break;
							case 9: monthName = "September"; quarter = "3"; break;
							case 10: monthName = "October"; quarter = "4"; break;
							case 11: monthName = "November"; quarter = "4"; break;
							case 12: monthName = "December"; quarter = "4"; break;
							default: monthName = "Unknown"; quarter = "Unknown"; 
						}
						
						String dayWeekName;
						switch (dayWeek){
							case 1: dayWeekName = "Sunday"; break;
							case 2: dayWeekName = "Monday"; break;
							case 3: dayWeekName = "Tuesday"; break;
							case 4: dayWeekName = "Wednesday"; break;
							case 5: dayWeekName = "Thursday"; break;
							case 6: dayWeekName = "Friday"; break;
							case 7: dayWeekName = "Saturday"; break;
							default: dayWeekName = "Unknown";
						}
						
						String date = dayMonth + "-" + month + "-" + year;
						String time = hours + ":" + minutes;
						
						this.addEntry(dimensionName,"Time","Year",String.valueOf(year),indexEntry);
						this.addEntry(dimensionName,"Time","Quarter",quarter,indexEntry);
						this.addEntry(dimensionName,"Time","Month",monthName,indexEntry);
						this.addEntry(dimensionName,"Time","Day of Month",String.valueOf(dayMonth),indexEntry);
						this.addEntry(dimensionName,"Time","Day of Week",dayWeekName,indexEntry);
						this.addEntry(dimensionName,"Time","Hour",String.valueOf(hour),indexEntry);
						this.addEntry(dimensionName,"Time","Date",date,indexEntry);
						this.addEntry(dimensionName,"Time","Date & Time",date+" "+time,indexEntry);
					
						continue;
					}
					this.addEntry(dimensionName,"Unknown",dimensionName,dimensionValue, indexEntry);
				}
				else if(complete) this.addEntry(dimensionName,"Event Data",dimensionName,dimensionValue, indexEntry);			
			}			
				
			eventID ++;
		}
		this.data.put(String.valueOf(traceID), trace);
	}
	
	private void addEntry(String id, String category, String dimensionName, String dimensionValue, Pair<Integer, Integer> entry){
		
		String key = category + ":" + dimensionName + "=" + dimensionValue;
		
//		System.out.println(key+"\t"+entry.getFirst()+"\t"+entry.getSecond());
		
		if(this.index.containsKey(key)){
			
			ArrayList<Pair<Integer, Integer>> entries = this.index.get(key);
			entries.add(entry);
		}
		else{
			
			ArrayList<Pair<Integer, Integer>> entries = new ArrayList<Pair<Integer, Integer>>();
			entries.add(entry);
			this.index.put(key, entries);
		}
		
		key = category + ":" + dimensionName;
		
		if(this.dimensions.containsKey(key)){
			
			Dimension dim = this.dimensions.get(key);
			dim.addValue(dimensionValue);
		}
		else{
			
			Dimension dim = new Dimension(id, category, dimensionName);
			dim.addValue(dimensionValue);
			this.dimensions.put(key, dim);
		}
	}
	
	public void normalizeDimensionScopes(HashMap<Dimension, Boolean> scopes){
		
		for(java.util.Map.Entry<Dimension, Boolean> entry : scopes.entrySet()){
			
			Dimension dim = entry.getKey();
			
			if(entry.getValue()){
			
				ArrayList<Pair<Integer, Integer>> newTIDs = new ArrayList<Pair<Integer, Integer>>();
				
				boolean flag = false;
				for(java.util.Map.Entry<String, XTrace> trace : this.data.entrySet()){
					
					Integer traceID = Integer.parseInt(trace.getKey());
					
					int eventID = 0;
					for(XEvent event : trace.getValue()){
						
						XAttribute dimValue = event.getAttributes().get(dim.getID());
						if(dimValue == null){
						
							flag = true;
							newTIDs.add(new Pair<Integer,Integer>(traceID, new Integer(eventID)));
						}
						eventID ++;
					}
				}
				if(flag){
					
					String dimEntry = dim.getCategory() + ":" + dim.getName() + "=?";
					
					newTIDs.trimToSize();
					this.index.put(dimEntry, newTIDs);
					dim.addValue("?");
				}
			}
			else{
				
				HashMap<String, HashSet<Integer>> stack = new HashMap<String, HashSet<Integer>>();
				HashMap<String, Integer> stackInv = new HashMap<String, Integer>();

				String dimName = dim.getCategory() + ":" + dim.getName();
				for(String dimValue : dim.getValues()){

					HashSet<Integer> temp = new HashSet<Integer>();

					for(Pair<Integer, Integer> pair : this.index.remove(dimName + "=" + dimValue)){

						temp.add(pair.getFirst());

						String traceID = pair.getFirst().toString();
						if(stackInv.containsKey(traceID)){

							Integer counter = stackInv.get(traceID);
							counter++;
							stackInv.put(traceID, counter);
						}
						else stackInv.put(traceID, new Integer(1));
					}
					stack.put(dimValue, temp);
				}
				
//				System.out.println(stack);
//				System.out.println(stackInv);

				ArrayList<Integer> tracesWithConcurrentValues = new ArrayList<Integer>();
				for(String dimValue : dim.getValues()){

					HashSet<Integer> traces = stack.get(dimValue);

					ArrayList<Pair<Integer, Integer>> normalizedTIDs = new ArrayList<Pair<Integer, Integer>>(traces.size());

					for(Integer traceID : traces){

						if(stackInv.get(traceID.toString()) == 1){

							XTrace trace = this.data.get(traceID.toString());
							for(int i = 0; i < trace.size(); i++) normalizedTIDs.add(new Pair<Integer, Integer>(traceID, new Integer(i)));
						}
						else tracesWithConcurrentValues.add(traceID);

					}

					normalizedTIDs.trimToSize();
					this.index.put(dimName + "=" + dimValue, normalizedTIDs);
				}
				
//				System.out.println(tracesWithConcurrentValues);

				for(Integer traceID : tracesWithConcurrentValues){

					StringBuffer combinedValue = new StringBuffer(); 

					XTrace trace = this.data.get(traceID.toString());

					for(XEvent event : trace){

						XAttribute dimValue = event.getAttributes().get(dim.getID());
						if(dimValue == null) continue;

						if(combinedValue.length() == 0) combinedValue.append(dimValue.toString());
						else
							combinedValue.append(" > "+dimValue.toString());

						//					System.out.println(event.getAttributes().get(dim.getName()).toString());
					}

					String newValue = dimName + "=" + combinedValue.toString();
					if(this.index.containsKey(newValue)){

						ArrayList<Pair<Integer, Integer>> normalizedTIDs = this.index.get(newValue);
						for(int i = 0; i < trace.size(); i++) normalizedTIDs.add(new Pair<Integer, Integer>(traceID, new Integer(i)));
					}
					else{

						ArrayList<Pair<Integer, Integer>> normalizedTIDs = new ArrayList<Pair<Integer, Integer>>();
						for(int i = 0; i < trace.size(); i++) normalizedTIDs.add(new Pair<Integer, Integer>(traceID, new Integer(i)));
						this.index.put(newValue, normalizedTIDs);

						dim.addValue(combinedValue.toString());
					}
				}
			}
		}
	}
		
	public ArrayList<Pair<Integer, XTrace>> getTraces(LinkedList<Pair<String, String>> constraints){
		
		ArrayList<Pair<Integer, XTrace>> traces;
		
		if((constraints != null) && (!constraints.isEmpty())){
			
			ArrayList<ArrayList<Pair<Integer, Integer>>> tids = 
				new ArrayList<ArrayList<Pair<Integer, Integer>>>(constraints.size());
			
			for(Pair<String, String> constraint : constraints){
				
				String key = constraint.getFirst() + "=" + constraint.getSecond();
				
				if(this.index.containsKey(key)) tids.add(this.index.get(key));
//				else System.err.println("CONSTRAINT NOT FOUND!!!");
			}
			
			if(!tids.isEmpty()){
				
				ArrayList<Integer> intersection = this.traceIntersection(tids);
				
				traces = new ArrayList<Pair<Integer, XTrace>>(intersection.size());
				HashSet<String> stack = new HashSet<String>();
				for(Integer traceID : intersection){
					
					String traceIDkey = traceID.toString();
					
					if(!stack.contains(traceIDkey)){
						
						traces.add(new Pair<Integer, XTrace>(traceID, this.data.get(traceIDkey)));
						stack.add(traceIDkey);
					}
				}
				traces.trimToSize();
			}
			else traces = new ArrayList<Pair<Integer,XTrace>>(0);
		}
		else{
		
			traces = new ArrayList<Pair<Integer,XTrace>>(this.data.size());
			for(java.util.Map.Entry<String, XTrace> entry : this.data.entrySet()) 
				traces.add(new Pair<Integer, XTrace>(Integer.valueOf(entry.getKey()), entry.getValue()));
		}
		
		return traces;
	}
	
	public ArrayList<Pair<Pair<Integer, Integer>,XEvent>> getEvents(LinkedList<Pair<String, String>> constraints){
		
		ArrayList<Pair<Pair<Integer, Integer>,XEvent>> events;
		
		if((constraints != null) && (!constraints.isEmpty())){
			
			ArrayList<ArrayList<Pair<Integer, Integer>>> tids = 
				new ArrayList<ArrayList<Pair<Integer, Integer>>>(constraints.size());
			
			for(Pair<String, String> constraint : constraints){
				
				String key = constraint.getFirst() + "=" + constraint.getSecond(); 
				
				if(this.index.containsKey(key)) tids.add(this.index.get(key));
//				else System.err.println("CONSTRAINT NOT FOUND!!!");
			}
			
			if(!tids.isEmpty()){
				
				ArrayList<Pair<Integer, Integer>> intersection = this.eventIntersection(tids);
				
				events = new ArrayList<Pair<Pair<Integer, Integer>,XEvent>>(intersection.size());
				for(Pair<Integer, Integer> entry : intersection){

					XTrace trace = this.data.get(entry.getFirst().toString());
						
					XEvent event = trace.get(entry.getSecond());
					events.add(new Pair<Pair<Integer, Integer>, XEvent>(entry, event));
				}
				events.trimToSize();
			}
			else events = new ArrayList<Pair<Pair<Integer, Integer>,XEvent>>(0);
		}
		else{
		
			events = new ArrayList<Pair<Pair<Integer, Integer>,XEvent>>();
			for(java.util.Map.Entry<String, XTrace> entry : this.data.entrySet()){
				
				int eventIndex = 0;
				for(XEvent event : entry.getValue()){
					
					events.add(new Pair<Pair<Integer, Integer>, XEvent>(new Pair<Integer, Integer>(Integer.valueOf(entry.getKey()), new Integer(eventIndex)),event));
					eventIndex ++;
				}
			}
			events.trimToSize();
		}
		
		return events;
	}
	
	public HashMap<String, Dimension> getDimensions(){ return this.dimensions; }
	
	public Dimension getDimension(String key){ return this.dimensions.get(key); }
	
	private ArrayList<Integer> traceIntersection(ArrayList<ArrayList<Pair<Integer, Integer>>> list){
		
		HashMap<String, Integer> stack = new HashMap<String, Integer>();
						
		boolean isFirstList = true;
		for(ArrayList<Pair<Integer, Integer>> tids : list){
			
			int lastTraceID = -1;
			for(Pair<Integer, Integer> tid : tids){
				
				int traceID = tid.getFirst();
				if(traceID != lastTraceID){
					
					String traceIDkey = String.valueOf(traceID);
					
					if(isFirstList) stack.put(traceIDkey, new Integer(1));
					else{
						
						if(stack.containsKey(traceIDkey)){
							
							Integer support = stack.remove(traceIDkey);
							support ++;
							stack.put(traceIDkey, support);
						}
					}
					
					lastTraceID = traceID;
				}
			}
			isFirstList = false;
		}
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(java.util.Map.Entry<String, Integer> entry : stack.entrySet()){
			
			if(entry.getValue() == list.size()){
				
				Integer value = Integer.valueOf(entry.getKey());
				result.add(value);
			}
		}
		result.trimToSize();
		
		return result;
	}
	
	private ArrayList<Pair<Integer, Integer>> eventIntersection(ArrayList<ArrayList<Pair<Integer, Integer>>> list){
		
		HashMap<String, Integer> stack = new HashMap<String, Integer>();
						
		boolean isFirstList = true;
		for(ArrayList<Pair<Integer, Integer>> tids : list){
			
			String lastEventID = null;
			for(Pair<Integer, Integer> tid : tids){
				
				String eventID = tid.getFirst()+":"+tid.getSecond();
				if(!eventID.equals(lastEventID)){
					
					if(isFirstList) stack.put(eventID, new Integer(1));
					else{
						
						if(stack.containsKey(eventID)){
							
							Integer support = stack.remove(eventID);
							support ++;
							stack.put(eventID, support);
						}
					}
					
					lastEventID = eventID;
				}
			}
			isFirstList = false;
		}
		
		ArrayList<Pair<Integer, Integer>> result = new ArrayList<Pair<Integer, Integer>>();
		for(java.util.Map.Entry<String, Integer> entry : stack.entrySet()){
			
			if(entry.getValue() == list.size()){
				
				String value = entry.getKey();
				int index = value.indexOf(":");
				
				Integer traceID = Integer.valueOf(value.substring(0, index));
				Integer eventID = Integer.valueOf(value.substring(index + 1));
				
				result.add(new Pair<Integer, Integer>(traceID, eventID));
			}
		}
		result.trimToSize();
		
		return result;
	}
	
	public void printDimensions(){
		
		for(Dimension dim : this.dimensions.values()){
			
			System.out.println("ID: "+dim.getID());
			System.out.println("Name: "+dim.getName());
			System.out.println("Category: "+dim.getCategory());
			System.out.println("Cardinality: "+dim.getCardinality());
			System.out.println("Values: "+dim.getValues().toString());
			System.out.println();
		}
	}
}
