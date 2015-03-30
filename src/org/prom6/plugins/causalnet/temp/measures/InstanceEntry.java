package org.prom6.plugins.causalnet.temp.measures;

import java.util.Collection;
import java.util.HashSet;

import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;
import org.prom6.plugins.causalnet.temp.aggregation.Count;

public class InstanceEntry extends Measure {

	public static String CATEGORY = "Instance";
	public static String DESCRIPTION = "Entry";
	public static String DESIGNATION = CATEGORY + "." + DESCRIPTION;	
	
	public InstanceEntry(){
		
		super(
				new Class<?>[]{XTrace.class},	// Collection of Pair<Trace TID, XTrace> 
				new Integer[]{1},				// (level 1 - current cell)
				new Class<?>[]{Count.class}
				);
	}

	@SuppressWarnings("unchecked")
	public Object compute(Object[] args){
		
		Collection<Pair<Integer, XTrace>> values = (Collection<Pair<Integer, XTrace>>) args[0];
		
		HashSet<Integer> entries = new HashSet<Integer>();
		for(Pair<Integer, XTrace> pair : values){
			
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
