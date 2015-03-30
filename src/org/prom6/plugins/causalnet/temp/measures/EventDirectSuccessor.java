package org.prom6.plugins.causalnet.temp.measures;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;
import org.prom6.plugins.causalnet.temp.aggregation.Count;
import org.prom6.plugins.causalnet.temp.aggregation.CountMap;

public class EventDirectSuccessor extends Measure {
	
	public static String CATEGORY = "Flow";
	public static String DESCRIPTION = "Direct Successor";
	public static String DESIGNATION = CATEGORY + "." + DESCRIPTION;	

	public EventDirectSuccessor(){
		
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
		
		HashSet<String> tids = new HashSet<String>();
		for(Pair<Integer, Integer> tid : valuesA) tids.add(tid.getFirst().toString()+":"+tid.getSecond().toString());
			
		HashMap<String, HashSet<Pair<Integer, Integer>>> successors = new HashMap<String, HashSet<Pair<Integer, Integer>>>();
		for(Pair<String, Collection<Pair<Integer, Integer>>> pair : valuesB){
			
			for(Pair<Integer, Integer> tid : pair.getSecond()){
				
				if(tids.contains(tid.getFirst().toString()+":"+(tid.getSecond().intValue() - 1))){
					
					if(successors.containsKey(pair.getFirst())){
						
						HashSet<Pair<Integer, Integer>> temp = successors.get(pair.getFirst());
						temp.add(new Pair<Integer, Integer>(tid.getFirst(), tid.getSecond() - 1));
					}
					else{
						
						HashSet<Pair<Integer, Integer>> temp = new HashSet<Pair<Integer, Integer>>();
						temp.add(new Pair<Integer, Integer>(tid.getFirst(), tid.getSecond() - 1));
						successors.put(pair.getFirst(), temp);
					}
				}
			}
		}
		
//		for(java.util.Map.Entry<String, Integer> entry : counters.entrySet()){
//			
//			f.aggregate(new Pair<String, Integer>(entry.getKey(), entry.getValue()));
//		}
		
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
