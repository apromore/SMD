package org.prom6.plugins.causalnet.temp.measures;

import java.util.Collection;
import java.util.HashSet;

import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;
import org.prom6.plugins.causalnet.temp.aggregation.Count;

public class EventStart extends Measure {

	public static String CATEGORY = "Event";
	public static String DESCRIPTION = "Start";
	public static String DESIGNATION = CATEGORY + "." + DESCRIPTION;
	
	public EventStart(){
		
		super(
				new Class<?>[]{EventEntry.class},	// Collection of Event TID 
				new Integer[]{1},				// (level 1 - current cell)
				new Class<?>[]{Count.class}
				);
	}

	@SuppressWarnings("unchecked")
	public Object compute(Object[] args){
		
		Collection<Pair<Integer, Integer>> values = (Collection<Pair<Integer, Integer>>) args[0];
		
		HashSet<Integer> entries = new HashSet<Integer>();
		for(Pair<Integer, Integer> pair : values){
			
			if(pair.getSecond().intValue() == 0) entries.add(pair.getFirst());
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
