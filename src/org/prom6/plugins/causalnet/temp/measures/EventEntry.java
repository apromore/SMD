package org.prom6.plugins.causalnet.temp.measures;

import java.util.Collection;
import java.util.HashSet;

import org.deckfour.xes.model.XEvent;
import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;
import org.prom6.plugins.causalnet.temp.aggregation.Count;

public class EventEntry extends Measure {

	public static String CATEGORY = "Event";
	public static String DESCRIPTION = "Entry";
	public static String DESIGNATION = CATEGORY + "." + DESCRIPTION;
	
	public EventEntry(){
		
		super(
				new Class<?>[]{XEvent.class},	// Collection of Pair<Event TID, XEvent> 
				new Integer[]{1},				// (level 1 - current cell)
				new Class<?>[]{Count.class}
				);
	}

	@SuppressWarnings("unchecked")
	public Object compute(Object[] args){
		
		Collection<Pair<Pair<Integer, Integer>, XEvent>> values = (Collection<Pair<Pair<Integer, Integer>, XEvent>>) args[0];
		
		HashSet<Pair<Integer, Integer>> entries = new HashSet<Pair<Integer, Integer>>();
		for(Pair<Pair<Integer, Integer>, XEvent> pair : values){
			
			entries.add(pair.getFirst());
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
