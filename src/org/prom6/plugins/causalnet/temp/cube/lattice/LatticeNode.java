package org.prom6.plugins.causalnet.temp.cube.lattice;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.processmining.framework.util.Pair;
import org.prom6.plugins.causalnet.temp.elements.Dimension;

public class LatticeNode {

	private HashMap<String, LatticeNode> subTrees;
	private LatticeValues values;
	private int level;
	
	public LatticeNode(){
		
		this(0);
	}
	
	public LatticeNode(int level){
		
		this.level = level;
		this.values = new LatticeValues();
	}
	
	public void setValue(LinkedList<Pair<String, String>> dimensions, LatticeValues value){
		
		if(!dimensions.isEmpty()){
			
			if(this.level == dimensions.size()) this.values.setLatticeValue(dimensions, value);
			else{
				
				String temp = dimensions.get(this.level).getFirst(); 
				
				if(this.subTrees == null) this.subTrees = new HashMap<String, LatticeNode>();
				
				LatticeNode subTree;
				if(!this.subTrees.containsKey(temp)){
					
					subTree = new LatticeNode(this.level + 1);
					this.subTrees.put(temp, subTree);
				}
				else subTree = this.subTrees.get(temp);
				
				subTree.setValue(dimensions, value);
			}
		}
		else this.values = value;
	}
	
	public Object getValue(List<Pair<String, String>> dimValues, String key){
		
		if(this.level == dimValues.size()) return this.values.getValue(dimValues, key);
		else{
			
			String temp = dimValues.get(this.level).getFirst();
			
			if(this.subTrees.containsKey(temp)){
				
				return this.subTrees.get(temp).getValue(dimValues, key);
			}
			else return null;
		}
	}
	
	public LatticeValues getValues(LinkedList<?> dimValues){
		
		if(this.level == dimValues.size()) return this.values;
		else{
			
			Object dimValue = dimValues.get(this.level);
			
			String temp;
			if(dimValue instanceof Pair<?,?>) temp = ((Pair<String, String>) dimValue).getFirst();
			else{
				
				Dimension dim = (Dimension) dimValue; 
				temp = dim.getCategory() + ":" + dim.getName();
			}
				
			if(this.subTrees != null){
				
				if(this.subTrees.containsKey(temp)){

					return this.subTrees.get(temp).getValues(dimValues);
				}
				else return null;
			}
			else return null;
		}
	}
		
	public void stats(){
		
		int cuboidsCounter = 1;
		int cellsCounter = 1;
		
		for(LatticeNode child : this.subTrees.values()){
			
			int[] childCounter = child.statsAux();
			
			cuboidsCounter += childCounter[0];
			cellsCounter += childCounter[1];
		}
		
		System.out.println("Cuboids: "+cuboidsCounter);
		System.out.println("Cells: "+cellsCounter);
	}
	
	private int[] statsAux(){
		
		int[] counters = new int[]{1,0};
				
		if(this.values != null) counters[1] = this.values.size();
		
		if(this.subTrees != null){
			
			for(LatticeNode child : this.subTrees.values()){
				
				int[] childCounter = child.statsAux();
				
				counters[0] += childCounter[0];
				counters[1] += childCounter[1];
			}
		}
		
		return counters;
	}
}
