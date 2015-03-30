package org.prom6.plugins.causalnet.temp.elements;

import java.util.LinkedList;

import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.aggregation.AggregationFunction;
import org.prom6.plugins.causalnet.temp.measures.Measure;

public class Perspective {

	private LinkedList<Dimension> firstSpace;
	private LinkedList<Dimension> secondSpace;
	private LinkedList<Pair<Measure, AggregationFunction<?>>> measures;
//	private LinkedList<String> filter; 

	public Perspective(){
		
		this.firstSpace = new LinkedList<Dimension>();
		this.secondSpace = new LinkedList<Dimension>();
		this.measures = new LinkedList<Pair<Measure, AggregationFunction<?>>>();
//		this.filter = new LinkedList<String>();
	}
	
	public Perspective(LinkedList<Dimension> firstSpace, LinkedList<Dimension> secondSpace, LinkedList<Pair<Measure, AggregationFunction<?>>> measures){
		
		this.firstSpace = firstSpace;
		this.secondSpace = secondSpace;
		this.measures = measures;
	}
	
	public LinkedList<Dimension> getFirstSpace(){ return this.firstSpace; }
	public LinkedList<Dimension> getSecondSpace(){ return this.secondSpace; }
	public LinkedList<Dimension> getDimensions(){
		
		LinkedList<Dimension> dims = new LinkedList<Dimension>();
		
		for(Dimension dim : this.firstSpace) dims.add(dim);
		for(Dimension dim : this.secondSpace) dims.add(dim);
		
		return dims;
	}
	public LinkedList<Pair<Measure, AggregationFunction<?>>> getMeasures(){ return this.measures; }
	
//	public void swap(){
//		
//		LinkedList<Dimension> temp = this.firstSpace;
//		this.firstSpace = this.secondSpace;
//		this.secondSpace = temp;
//	}
}
