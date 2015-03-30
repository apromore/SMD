package org.prom6.plugins.causalnet.temp.aggregation;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;

public class CountMap<A extends AbstractMap<?, ?>> implements AggregationFunction<A> {

	public static String CATEGORY = "Map";
	public static String DESCRIPTION = "Count";
	
	public CountMap(){
		
		super();
	}

	@SuppressWarnings("unchecked")
	public A aggregate(Object value) throws Exception {
		
		if(value instanceof AbstractMap<?, ?>){
			
			AbstractMap<?, ?> valueA = (AbstractMap<?, ?>) value;
			
			HashMap<Object, Integer> counter = new HashMap<Object, Integer>();
			for(java.util.Map.Entry<?, ?> entry : valueA.entrySet()){
				
				if(entry.getValue() instanceof Collection<?>) counter.put(entry.getKey(), ((Collection<?>) entry.getValue()).size());
				else throw new Exception(entry.getValue().getClass().getName()+" not supported for Count");
			}
						
			return (A) counter;
		}
				
		throw new Exception(value.getClass().getName()+" is not an instance of Map");
	}
	
	//-----------------------------------------------------
	
	public String getCategory() { return CATEGORY; }
	public String getDescription() { return DESCRIPTION; }
}
