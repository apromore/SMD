package org.prom6.plugins.causalnet.temp.aggregation;

import java.util.AbstractMap;
import java.util.Collection;

public class Count<A extends Number> implements AggregationFunction<A> {

	public static String CATEGORY = "Numerical";
	public static String DESCRIPTION = "Count";
	
	public Count(){
		
		super();
	}

	@SuppressWarnings("unchecked")
	public A aggregate(Object value) throws Exception {
		
		if(value instanceof Collection<?>) return (A) new Integer(((Collection<?>) value).size());
		if(value instanceof AbstractMap<?, ?>) return (A) new Integer(((AbstractMap<?, ?>) value).size());
				
		throw new Exception(value.getClass().getName()+" not supported for Count");
	}
	
	//-----------------------------------------------------
	
	public String getCategory() { return CATEGORY; }
	public String getDescription() { return DESCRIPTION; }
}
