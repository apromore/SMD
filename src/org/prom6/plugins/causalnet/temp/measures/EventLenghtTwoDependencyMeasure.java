package org.prom6.plugins.causalnet.temp.measures;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;

import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;
import org.prom6.plugins.causalnet.temp.elements.Perspective;

public class EventLenghtTwoDependencyMeasure extends Measure {

	public static String CATEGORY = "Flow";
	public static String DESCRIPTION = "Lenght-two Dependency Measure";
	public static String DESIGNATION = CATEGORY + "." + DESCRIPTION;
	
	public EventLenghtTwoDependencyMeasure(){
		
		super(
				new Class<?>[]{EventLengthTwoLoop.class, Perspective.class, EventLengthTwoLoop.class},	// Map of Event TID, Cell's constraints, Collection of Event TID 
				new Integer[]{1, 0, 2},		// (level 1 - current cell), (level 0 - external argument), (level 2 - siblings)
				new Class<?>[]{}
				);
	}

	@SuppressWarnings("unchecked")
	public Object compute(Object[] args) {	
		
		AbstractMap<String, Collection<Pair<Integer, Integer>>> valuesA = (AbstractMap<String, Collection<Pair<Integer, Integer>>>) args[0];
		Collection<Pair<String, String>> valuesB = (Collection<Pair<String, String>>) args[1];
		Collection<Pair<String, AbstractMap<String, Collection<Pair<Integer, Integer>>>>> valuesC = (Collection<Pair<String, AbstractMap<String, Collection<Pair<Integer, Integer>>>>>) args[2];
		
		HashMap<String, AbstractMap<String, Collection<Pair<Integer, Integer>>>> stack = new HashMap<String, AbstractMap<String, Collection<Pair<Integer, Integer>>>>();
		for(Pair<String, AbstractMap<String, Collection<Pair<Integer, Integer>>>> pair : valuesC) stack.put(pair.getFirst(), pair.getSecond());
		
		StringBuffer buffer = new StringBuffer();
		for(Pair<String, String> pair : valuesB){
			
			String value = pair.getSecond();
			
			if(buffer.length() == 0) buffer.append(value);
			else buffer.append(":"+value);
		}
		String id = buffer.toString();
		
		HashMap<String, Float> measures = new HashMap<String, Float>();
		for(java.util.Map.Entry<String, Collection<Pair<Integer, Integer>>> entry : valuesA.entrySet()){
			
			String key = entry.getKey();
			float valueAB = entry.getValue().size();
			float valueBA = 0;
			float measure = 0;
			
			if(id.equals(key)) measure = valueAB / (valueAB + 1);
			else {
				
				if(stack.containsKey(key)){
					
					AbstractMap<String, Collection<Pair<Integer, Integer>>> temp = stack.get(key);
					
					if(temp.containsKey(id)) valueBA = temp.get(id).size();
				}
				
				measure = (valueAB + valueBA) / (valueAB + valueBA + 1);
			}

			if(measure >= 0f) 
				measures.put(key, new Float(measure));
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
