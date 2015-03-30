package org.prom6.plugins.causalnet.temp.cube;

import java.util.HashMap;
import java.util.LinkedList;

import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.miner.HeuristicsMiner;
import org.prom6.plugins.causalnet.miner.settings.HeuristicsMinerSettings;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;
import org.prom6.plugins.causalnet.temp.cube.lattice.Lattice;
import org.prom6.plugins.causalnet.temp.elements.Dimension;
import org.prom6.plugins.causalnet.temp.elements.Perspective;
import org.prom6.plugins.causalnet.temp.index.InvertedIndex;
import org.prom6.plugins.causalnet.temp.measures.Measure;


public class EventCube {

	private InvertedIndex index;
	
	private HashMap<String, Dimension> dimensions;
	private HashMap<String, Pair<Measure, AggregationFunction<?>>> measures;
	
	private Lattice data;
	
	public EventCube(InvertedIndex index, LinkedList<Pair<Measure, AggregationFunction<?>>> measures){
		
		this.index = index;
		this.measures = new HashMap<String, Pair<Measure, AggregationFunction<?>>>();
		for(Pair<Measure, AggregationFunction<?>> measure : measures){
			
			if(measure.getSecond() == null) this.measures.put(measure.getFirst().getDesignation(), measure);
			else this.measures.put(measure.getFirst().getDesignation(measure.getSecond()), measure);
		}
	}
	
	public EventCube(InvertedIndex index, LinkedList<Dimension> dimensions, LinkedList<Pair<Measure, AggregationFunction<?>>> measures){
		
		this(index, measures);
		this.dimensions = new HashMap<String, Dimension>();
		for(Dimension dim : dimensions) this.dimensions.put(dim.getCategory()+":"+dim.getName(), dim);
	}
	
	public HashMap<String, Dimension> getDimensions(){ return this.dimensions; }
	public HashMap<String, Pair<Measure, AggregationFunction<?>>> getMeasures(){ return this.measures; }
	
	public void processValues(Perspective perspective){
			
		if(this.data == null) this.data = new Lattice(index);
		
		this.data.process(perspective);
	}
		
	public Object[] computeCausalNet(String logID, Perspective perspective, HeuristicsMinerSettings settings){
		
		return HeuristicsMiner.buildACNet(logID, this.data.getValues(perspective.getFirstSpace()), settings);
	}
	
	public HashMap<String, Integer> getMapColorLevels(String measure){
		
		HashMap<String, Integer> levels = new HashMap<String, Integer>();

		for(Dimension dim : this.dimensions.values()){
			
			String dimKey = dim.getCategory() + ":" + dim.getName();
			
			LinkedList<Pair<String,String>> query = new LinkedList<Pair<String,String>>();
			
			HashMap<String, Float> stack = new HashMap<String, Float>();
			
			float min = Float.POSITIVE_INFINITY;
			float max = Float.NEGATIVE_INFINITY;
			for(String val : dim.getValues()){
				
				query.add(new Pair<String,String>(dimKey, val));
				
				Object measureObj = this.data.getValue(query, measure);
				
				if(measureObj != null){
					
					try{
						
						float measureValue = ((Number) measureObj).floatValue(); 
					
						if(min > measureValue) min = measureValue;
						if(max < measureValue) max = measureValue;
						
						stack.put(dim.getCategory() + "/" + dim.getName() + " = " + val, measureValue);
					}
					catch(java.lang.ClassCastException e){ }
				}
				
				query.removeLast();
			}
			
			if(min < max){
				
				for(java.util.Map.Entry<String, Float> entry : stack.entrySet()){
					
					float a = (entry.getValue() - min) / (max - min);
					
					int level = (int) (((1f - a) * 100f) + 33f);
					levels.put(entry.getKey(), new Integer(level));
				}
			}
		}
		
//		System.out.println(levels);
		
		return levels;
	}
	
	public void stats(){
		
		this.data.stats();
	}
}
