package org.prom6.plugins.causalnet.temp.measures;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;
import org.prom6.plugins.causalnet.temp.aggregation.CountMap;

public class EventLengthTwoLoop extends Measure {

	public static String CATEGORY = "Flow";
	public static String DESCRIPTION = "Length-two Loop";
	public static String DESIGNATION = CATEGORY + "." + DESCRIPTION;
	
	public EventLengthTwoLoop(){
		
		super(
				new Class<?>[]{EventEntry.class, EventEntry.class},	// Collection of Event TID, Collection of Event TID 
				new Integer[]{1, 2},								// (level 1 - current cell), (level 2 - siblings)
				new Class<?>[]{CountMap.class}
				);
	}

	@SuppressWarnings("unchecked")
	public Object compute(Object[] args){
		
		Collection<Pair<Integer, Integer>> valuesA = (Collection<Pair<Integer, Integer>>) args[0];	// Internal event entries
		Collection<Pair<String, Collection<Pair<Integer, Integer>>>> valuesB = (Collection<Pair<String, Collection<Pair<Integer, Integer>>>>) args[1];	// External event entries
		
		HashMap<String, HashSet<String>> tids = new HashMap<String, HashSet<String>>();
		for(Pair<Integer, Integer> tid : valuesA){
			
			String traceID = tid.getFirst().toString();
			
			if(tids.containsKey(traceID)){
				
				HashSet<String> temp = tids.get(traceID);
				temp.add(tid.getSecond().toString());
			}
			else{
				
				HashSet<String> temp = new HashSet<String>();
				temp.add(tid.getSecond().toString());
				tids.put(traceID, temp);
			}
		}
		
		HashMap<String, HashSet<Pair<Integer, Integer>>> loops = new HashMap<String, HashSet<Pair<Integer, Integer>>>();
		for(Pair<String, Collection<Pair<Integer, Integer>>> pair : valuesB){
			
			for(Pair<Integer, Integer> tid : pair.getSecond()){
				
				String traceID = tid.getFirst().toString();
				int eventID = tid.getSecond().intValue();
				
				if(!tids.containsKey(traceID)) continue;
				
				HashSet<String> stack = tids.get(traceID);
				if(stack.contains(String.valueOf(eventID - 1)) && !stack.contains(String.valueOf(eventID)) && stack.contains(String.valueOf(eventID + 1))){
				
					if(loops.containsKey(pair.getFirst())){
						
						HashSet<Pair<Integer, Integer>> temp = loops.get(pair.getFirst());
						temp.add(new Pair<Integer, Integer>(tid.getFirst(), tid.getSecond() - 1));
					}
					else{
						
						HashSet<Pair<Integer, Integer>> temp = new HashSet<Pair<Integer, Integer>>();
						temp.add(new Pair<Integer, Integer>(tid.getFirst(), tid.getSecond() - 1));
						loops.put(pair.getFirst(), temp);
					}
				}
			}
		}
		

		return loops;
	}
	
	//-----------------------------------------------------
	
	public String getCategory() { return CATEGORY; }
	public String getDescription() { return DESCRIPTION; }
	public String getDesignation() { return DESIGNATION; }
	public String getDesignation(AggregationFunction<?> func){ 
		
		if(func != null) return func.getDescription() + "( " + DESIGNATION + " )";
		else return DESIGNATION;
	}
}
