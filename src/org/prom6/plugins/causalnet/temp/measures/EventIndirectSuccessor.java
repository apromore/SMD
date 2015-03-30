package org.prom6.plugins.causalnet.temp.measures;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;
import org.prom6.plugins.causalnet.temp.aggregation.Count;
import org.prom6.plugins.causalnet.temp.aggregation.CountMap;


public class EventIndirectSuccessor extends Measure {

	public static String CATEGORY = "Flow";
	public static String DESCRIPTION = "Indirect Successor";
	public static String DESIGNATION = CATEGORY + "." + DESCRIPTION;	

	public EventIndirectSuccessor(){
		
		super(
				new Class<?>[]{EventEntry.class, EventEntry.class},	// Collection of Event TID, Collection of Event TID 
				new Integer[]{1, 2},								// (level 1 - current cell), (level 2 - siblings)
				new Class<?>[]{Count.class, CountMap.class}
				);
	}
	
	@SuppressWarnings("unchecked")
	public Object compute(Object[] args) {
		
		Collection<Pair<Integer, Integer>> valuesA = (Collection<Pair<Integer, Integer>>) args[0];	// Internal event entries
		Collection<Pair<String, Collection<Pair<Integer, Integer>>>> valuesB = (Collection<Pair<String, Collection<Pair<Integer, Integer>>>>) args[1];	// External event entries
		
		HashMap<String, HashSet<Pair<Integer, Integer>>> successors = new HashMap<String, HashSet<Pair<Integer, Integer>>>();
		for(Pair<String, Collection<Pair<Integer, Integer>>> pair : valuesB){
			
			HashSet<Pair<Integer, Integer>> temp = new HashSet<Pair<Integer, Integer>>();
			
			
			HashMap<String, TreeSet<Integer>> stack = new HashMap<String, TreeSet<Integer>>();
			for(Pair<Integer, Integer> tid : pair.getSecond()){
				
				String traceID = tid.getFirst().toString();
				if(stack.containsKey(traceID)){
					
					TreeSet<Integer> eventIDs = stack.get(traceID);
					eventIDs.add(tid.getSecond());
				}
				else{
					
					TreeSet<Integer> eventIDs = new TreeSet<Integer>();
					eventIDs.add(tid.getSecond());
					stack.put(traceID, eventIDs);
				}
			}
			
			for(Pair<Integer, Integer> tid : valuesA){
				
				String traceID = tid.getFirst().toString();
				if(stack.containsKey(traceID)){
					
					TreeSet<Integer> eventIDs = stack.get(traceID);
					// Remark that, in the trace ABDCEDF, A>>>D is counted only once  
					if(eventIDs.ceiling(tid.getSecond() + 1) != null) 
						temp.add(new Pair<Integer,Integer>(tid.getFirst(), tid.getSecond()));
					
				}
			}
			
			if(!temp.isEmpty()) successors.put(pair.getFirst(), temp);
		}
						
		return successors;
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
