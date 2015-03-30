package org.prom6.plugins.causalnet.temp.measures;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;
import org.prom6.plugins.causalnet.temp.aggregation.Count;

public class EventEnd extends Measure {

	public static String CATEGORY = "Event";
	public static String DESCRIPTION = "End";
	public static String DESIGNATION = CATEGORY + "." + DESCRIPTION;
	
	public EventEnd(){
		
		super(
				new Class<?>[]{EventEntry.class, XTrace.class},	// Collection of Event TID, Collection of Pair<Trace TID, XTrace> 
				new Integer[]{1, 1},									// (level 1 - current cell), (level 1 - current cell)
				new Class<?>[]{Count.class}
				);
	}

	@SuppressWarnings("unchecked")
	public Object compute(Object[] args){
		
		Collection<Pair<Integer, Integer>> valuesA = (Collection<Pair<Integer, Integer>>) args[0];
		Collection<Pair<Integer, XTrace>> valuesB = (Collection<Pair<Integer, XTrace>>) args[1];
				
		HashMap<String, XTrace> stack = new HashMap<String, XTrace>();
		for(Pair<Integer, XTrace> pair : valuesB) stack.put(pair.getFirst().toString(), pair.getSecond());
		
		HashSet<Integer> entries = new HashSet<Integer>();
		for(Pair<Integer, Integer> pair : valuesA){
			
			String traceID = pair.getFirst().toString();
			int lastIndex = stack.get(traceID).size() - 1;
						
			if(pair.getSecond().intValue() == lastIndex) entries.add(pair.getFirst());
		}
		
		return entries;
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
