package org.prom6.plugins.causalnet.temp.measures;

import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;

public abstract class Measure {
	
	private final Class<?>[] argumentTypes;
	private final Integer[] argumentLevels;
	
	private final Class<?>[] aggFunctions;
	
	public Measure(Class<?>[] argumentTypes, Integer[] argumentLevels, Class<?>[] aggFunctions){
		
		this.argumentTypes = argumentTypes;
		this.argumentLevels = argumentLevels;
		this.aggFunctions = aggFunctions;
	}
	
	public abstract String getCategory();
	public abstract String getDescription();
	public abstract String getDesignation();
	public abstract String getDesignation(AggregationFunction<?> func);
	
	public abstract Object compute(Object[] args);
	
	public Class<?>[] getArgumentTypes(){ return this.argumentTypes; }
	public Integer[] getArgumentLevels(){ return this.argumentLevels; }
	public Class<?>[] getAggFunctions(){ return this.aggFunctions; }
	
	public Object getAggregatedValue(Object[] args, AggregationFunction<?> func) throws Exception{
		
		if(func != null){
		
			this.checkAggregationFunction(func);
			return func.aggregate(this.compute(args));
		}
		else return this.compute(args);
	}
	
	private void checkAggregationFunction(AggregationFunction<?> func) throws Exception{
		
		boolean isSupported = false;
		for(int i = 0; i < this.aggFunctions.length; i++){
			
			if(this.aggFunctions[i].isInstance(func)){
				
				isSupported = true;
				break;
			}
		}
		
		if(!isSupported)
			throw new Exception(func.getClass().getCanonicalName()+" cannot be used as aggregation function on "+this.getDesignation());
	}
}
