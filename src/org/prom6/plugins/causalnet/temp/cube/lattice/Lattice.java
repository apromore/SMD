package org.prom6.plugins.causalnet.temp.cube.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;
import org.prom6.plugins.causalnet.temp.elements.Dimension;
import org.prom6.plugins.causalnet.temp.elements.Perspective;
import org.prom6.plugins.causalnet.temp.index.InvertedIndex;
import org.prom6.plugins.causalnet.temp.measures.EventDirectSuccessor;
import org.prom6.plugins.causalnet.temp.measures.EventEntry;
import org.prom6.plugins.causalnet.temp.measures.EventIndirectSuccessor;
import org.prom6.plugins.causalnet.temp.measures.EventLengthTwoLoop;
import org.prom6.plugins.causalnet.temp.measures.InstanceEntry;
import org.prom6.plugins.causalnet.temp.measures.Measure;


public class Lattice {

	private InvertedIndex index;
	
	private LinkedList<Dimension> dimensions;
	private HashMap<String, Integer> dimensionsIndex;
	
	private LatticeNode map;
	
	
	public Lattice(InvertedIndex index){
		
		this.index = index;
		this.map = new LatticeNode();
		
		this.dimensions = new LinkedList<Dimension>();
		this.dimensionsIndex = new HashMap<String, Integer>();
		for(Dimension dim : index.getDimensions().values()){
			
			this.dimensionsIndex.put(dim.getCategory() + ":" + dim.getName(), new Integer(this.dimensions.size()));
			this.dimensions.add(dim);
		}
	}
	
	public void process(Perspective perspective){
		
		LinkedList<Dimension> dims = new LinkedList<Dimension>();
		for(Dimension dim : perspective.getFirstSpace()){
			
			int dimIndex = this.dimensionsIndex.get(dim.getCategory() + ":" + dim.getName());
			
			int index = 0;
			for(Dimension temp : dims){
			
				int tempIndex = this.dimensionsIndex.get(temp.getCategory() + ":" + temp.getName());
				
				if(dimIndex < tempIndex) break;
				
				index ++;
			}
			dims.add(index, dim);
		}
		for(Dimension dim : perspective.getSecondSpace()){
			
			int dimIndex = this.dimensionsIndex.get(dim.getCategory() + ":" + dim.getName());
			
			int index = 0;
			for(Dimension temp : dims){
			
				int tempIndex = this.dimensionsIndex.get(temp.getCategory() + ":" + temp.getName());
				
				if(dimIndex < tempIndex) break;
				
				index ++;
			}
			dims.add(index, dim);
		}

		
		Pair<ArrayList<Integer>, Integer> levels = this.computeMeasureLevels(perspective.getMeasures());
		
//		System.out.println(dims.toString());
		for(int level = 1; level <= levels.getSecond(); level++){
		
			int[] indices = new int[dims.size()];
			String[] dimNames = new String[dims.size()];
			String[][] dimValues = new String[dims.size()][];
			for(int i = 0; i < dims.size(); i++){
				
				Dimension dim = dims.get(i);
				
				indices[i] = dim.getCardinality();
				dimValues[i] = new String[indices[i]];
				
				int j = 0;
				for(String value : dim.getValues()){
					
					dimNames[i] = dim.getCategory() + ":" + dim.getName();
					dimValues[i][j] = value;
					j++;
				}
			}
			
			LinkedList<Pair<String, String>> constraints = new LinkedList<Pair<String, String>>();
			while(indices[0] >= 0){
				
				for(int i = 0; i < dims.size(); i++){
					
					if(indices[i] < dims.get(i).getCardinality()){
					
						constraints.add(new Pair<String,String>(dimNames[i], dimValues[i][indices[i]]));
	//					System.out.print(indices[i]+"\t");
					}
	//				else System.out.print("-\t");
				}
//				System.out.println(constraints.toString());
				
				this.processValue(level, constraints, perspective.getMeasures(), levels.getFirst());
				
				for(int i = dims.size() - 1; i >= 0; i--){
					
					indices[i] --;
					if(indices[i] >= 0) break;
					else if(i > 0) indices[i] = dims.get(i).getCardinality();
				}
				constraints.clear();
			}
		}
	}
	
	private void processValue(int level, LinkedList<Pair<String, String>> constraints, LinkedList<Pair<Measure, AggregationFunction<?>>> measures, ArrayList<Integer> levels){
		
		ArrayList<Pair<Integer, XTrace>> traces = null;
		ArrayList<Pair<Pair<Integer, Integer>,XEvent>> events = null;
		
		LatticeValues values;
		if(level > 1){
			
			values = this.map.getValues(constraints);
			if(values == null){
				
				values = new LatticeValues(constraints.size());
				this.map.setValue(constraints, values);
			}
		}
		else{
		
			values = new LatticeValues(constraints.size());
			this.map.setValue(constraints, values);
		}
		

		
		int measureIndex = 0;
		for(Pair<Measure, AggregationFunction<?>> measure : measures){
			
			if(levels.get(measureIndex) == level){

				Class<?>[] argTypes = measure.getFirst().getArgumentTypes();
				Integer[] argLevels = measure.getFirst().getArgumentLevels();
				Object[] args = new Object[argTypes.length];

				for(int i = 0; i < argTypes.length; i++){

					Class<?> type = argTypes[i];
					int typeLevel = argLevels[i].intValue();

					if(type == XEvent.class){

						switch(typeLevel){
						case 1: {
							if(events == null) events = index.getEvents(constraints);
							args[i] = events;
							break;}
						default: {break;}	//DO NOTHING
						}

						continue;
					}
					if(type == XTrace.class){

						switch(typeLevel){
						case 1: {
							if(traces == null) traces = index.getTraces(constraints); 
							args[i] = traces; 
							break;}
						default: {break;}	//DO NOTHING
						}

						continue;
					}
					if(type == EventEntry.class){
						
						switch(typeLevel){
						case 1: {args[i] = this.map.getValue(constraints, EventEntry.DESIGNATION); break;}
						case 2: {args[i] = values.getValues(EventEntry.DESIGNATION); break;}
						default: {break;}	//DO NOTHING
						}

						continue;
					}
					if(type == InstanceEntry.class){
						
						switch(typeLevel){
						case 1: {args[i] = this.map.getValue(constraints, InstanceEntry.DESIGNATION); break;}
						case 2: {args[i] = values.getValues(InstanceEntry.DESIGNATION); break;}
						default: {break;}	//DO NOTHING
						}

						continue;
					}
					if(type == EventDirectSuccessor.class){
						
						switch(typeLevel){
						case 1: {args[i] = this.map.getValue(constraints, EventDirectSuccessor.DESIGNATION); break;}
						case 2: {args[i] = values.getValues(EventDirectSuccessor.DESIGNATION); break;}
						default: {break;}	//DO NOTHING
						}

						continue;
					}
					if(type == EventIndirectSuccessor.class){
						
						switch(typeLevel){
						case 1: {args[i] = this.map.getValue(constraints, EventIndirectSuccessor.DESIGNATION); break;}
						case 2: {args[i] = values.getValues(EventIndirectSuccessor.DESIGNATION); break;}
						default: {break;}	//DO NOTHING
						}

						continue;
					}
					if(type == EventLengthTwoLoop.class){

						switch(typeLevel){
						case 1: {args[i] = this.map.getValue(constraints, EventLengthTwoLoop.DESIGNATION); break;}
						case 2: {args[i] = values.getValues(EventLengthTwoLoop.DESIGNATION); break;}
						default: {break;}	//DO NOTHING
						}

						continue;
					}
					if(type == Perspective.class){

						switch(typeLevel){
						case 0: {args[i] = constraints; break;}
						default: {break;}	//DO NOTHING
						}

						continue;
					}
				}

				try{

					Object aggValue = measure.getFirst().getAggregatedValue(args, measure.getSecond());
					if(level == 1) values.addAggregatedValue(measure.getFirst().getDesignation(measure.getSecond()), aggValue);
					else values.addAggregatedValue(constraints, measure.getFirst().getDesignation(measure.getSecond()), aggValue);
				}
				catch(Exception e){ e.printStackTrace(); }
			}
			
			measureIndex++;
		}
//		if(insertValue && (!values.isEmpty())) this.map.setValue(constraints, values);
	}
	
	
	
	private Pair<ArrayList<Integer>, Integer> computeMeasureLevels(LinkedList<Pair<Measure, AggregationFunction<?>>> measures){
		
		ArrayList<Integer> levels = new ArrayList<Integer>(measures.size());
		int maxLevel = 0;
		
		for(Pair<Measure, AggregationFunction<?>> measure : measures){
			
			int level = this.getMeasureLevel(measure.getFirst());
			levels.add(new Integer(level));
			if(level > maxLevel) maxLevel = level;
		}
		
		return new Pair<ArrayList<Integer>, Integer>(levels, new Integer(maxLevel));
	}
	
	private int getMeasureLevel(Measure measure){
		
		int level = 0;
		
		Class<?>[] argTypes = measure.getArgumentTypes();
		Integer[] argLevels = measure.getArgumentLevels();
		
		for(int i = 0; i < argTypes.length; i++){
			
			Class<?> type = argTypes[i];
			int typeLevel = argLevels[i];
			
			if(typeLevel == 0) continue;
			if(type == XEvent.class){ level = Math.max(level, typeLevel); continue; }
			if(type == XTrace.class){ level = Math.max(level, typeLevel); continue; }
			if(type == EventEntry.class){ level = Math.max(level, typeLevel); continue; }
			if(type == InstanceEntry.class){ level = Math.max(level, typeLevel); continue; }
			if(type == EventDirectSuccessor.class){ level = Math.max(level, 1 + typeLevel); continue; }
			if(type == EventIndirectSuccessor.class){ level = Math.max(level, 1 + typeLevel); continue; }
			if(type == EventLengthTwoLoop.class){ level = Math.max(level, 1 + typeLevel); continue; }
			
			System.err.println(type.getCanonicalName()+" has no level assigned");
		}
		
		return level;
	}
		
	public Object getValue(List<Pair<String, String>> cell, String measureKey){ return this.map.getValue(cell, measureKey); }
	public LatticeValues getValues(LinkedList<Dimension> dimensions){ 
		
		LinkedList<Dimension> dims = new LinkedList<Dimension>();
		
		for(Dimension dim : dimensions){
			
			Integer dimPosition = this.dimensionsIndex.get(dim.getCategory()+":"+dim.getName());
			
			int index = 0;
			for(Dimension temp : dims){
				
				String tempKey = temp.getCategory() + ":" + temp.getName();
				
				if(this.dimensionsIndex.get(tempKey).compareTo(dimPosition) > 0) break;
				
				index++;
			}
			dims.add(index, dim);
		}
		
		return this.map.getValues(dims); 	
	}
	
	public void stats(){
		
		this.map.stats();		
	}
	
	public LinkedList<Dimension> normalizeConstraints(LinkedList<Dimension> constraints, LinkedList<Dimension> dimensions){
		
		LinkedList<Dimension> cons = new LinkedList<Dimension>();
		for(Dimension dim : dimensions){
			
			int dimIndex = this.dimensionsIndex.get(dim.getCategory() + ":" + dim.getName());
			
			int index = 0;
			for(Dimension temp : cons){
			
				int tempIndex = this.dimensionsIndex.get(temp.getCategory() + ":" + temp.getName());
				
				if(dimIndex < tempIndex) break;
				
				index ++;
			}
			cons.add(index, dim);
		}
		
		for(int i = 0; i < cons.size(); i++){
			
			Dimension dim = cons.get(i);
			if(!constraints.contains(dim)) cons.set(i, null);
		}
		
		return cons;
	}
}
