package org.prom6.plugins.causalnet.temp.measures;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;

import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;

public class EventLongDistanceDependencyMeasure extends Measure {

	public static String CATEGORY = "Flow";
	public static String DESCRIPTION = "Long Distance Dependency Measure";
	public static String DESIGNATION = CATEGORY + "." + DESCRIPTION;
	
	public EventLongDistanceDependencyMeasure(){
		
		super(
				new Class<?>[]{EventIndirectSuccessor.class, EventEntry.class, EventEntry.class},	// Map of Event TID, Collection of Event TID, Collection of Event TID 
				new Integer[]{1, 1, 2},		// (level 1 - current cell), (level 1 - current cell), (level 2 - siblings)
				new Class<?>[]{}
				);
	}
	
	@SuppressWarnings("unchecked")
	public Object compute(Object[] args) {	
		
		AbstractMap<String, Collection<Pair<Integer, Integer>>> valuesA = (AbstractMap<String, Collection<Pair<Integer, Integer>>>) args[0];
		Collection<Pair<Integer, Integer>> valuesB = (Collection<Pair<Integer, Integer>>) args[1]; // Internal event entries
		Collection<Pair<String, Collection<Pair<Integer, Integer>>>> valuesC = (Collection<Pair<String, Collection<Pair<Integer, Integer>>>>) args[2];	// External event entries
				
		HashMap<String, Integer> stack = new HashMap<String, Integer>();
		for(Pair<String, Collection<Pair<Integer, Integer>>> pair : valuesC){
			
			if(valuesA.containsKey(pair.getFirst())) stack.put(pair.getFirst(), new Integer(pair.getSecond().size()));
		}
		
		int a = valuesB.size();
		
		HashMap<String, Float> measures = new HashMap<String, Float>();
		for(java.util.Map.Entry<String, Collection<Pair<Integer,Integer>>> entry : valuesA.entrySet()){
			
			int b = stack.get(entry.getKey());
			
			Float measure = (float) (2 * (entry.getValue().size() - Math.abs(a - b))) / (float) (a + b + 1);
			
			measures.put(entry.getKey(), measure);
		}	
		
		return measures;
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
