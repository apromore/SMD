package org.prom6.plugins.causalnet.temp.cube.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.processmining.framework.util.Pair;


public class LatticeValues {

	protected HashMap<String, Object> values;
	protected String nextLevel;
	protected HashMap<String, LatticeValues> subTrees;
	protected int level;
	
	public LatticeValues(){
		
		this(0);
	}
	
	public LatticeValues(int level){
		
		this.level = level;
		
		this.nextLevel = null;  // Which dimension the subtrees refer
		this.subTrees = null;
		this.values = new HashMap<String, Object>();
	}
	
	public boolean isEmpty(){ return ((this.subTrees == null) && (this.values == null)); }
	
	public void setLatticeValue(String key, LatticeValues value){
		
		this.subTrees.put(key, value);
	}
	
	public void setLatticeValue(LinkedList<Pair<String, String>> dimValues, LatticeValues value){ 
		
		if(!dimValues.isEmpty()){
			
			if(this.level == dimValues.size() - 1){
				
				Pair<String, String> pair = dimValues.get(this.level);
				
				if(this.subTrees == null){
					
					this.subTrees = new HashMap<String, LatticeValues>();
					this.nextLevel = pair.getFirst();
				}
								
				this.subTrees.put(pair.getSecond(), value);
			}
			else{
				
				Pair<String, String> pair = dimValues.get(this.level);
				String temp = pair.getSecond();
				
				if(this.subTrees == null){
					
					this.subTrees = new HashMap<String, LatticeValues>();
					this.nextLevel = pair.getFirst();
				}
				
				LatticeValues subTree;
				if(!this.subTrees.containsKey(temp)){
					
					subTree = new LatticeValues(this.level + 1);
					this.subTrees.put(temp, subTree);
				}
				else subTree = this.subTrees.get(temp);
				
				subTree.setLatticeValue(dimValues, value);
			}
		}
	}
		
	public void addAggregatedValue(String key, Object value){ 
		
		this.values.put(key, value); 
	}
	
	public void addAggregatedValue(LinkedList<Pair<String, String>> dimValues, String key, Object value){ 
		
		if(dimValues.isEmpty()) this.addAggregatedValue(key, value);
		else{
			
			if(this.level == dimValues.size()) this.addAggregatedValue(key, value);
			else{
				
				Pair<String, String> pair = dimValues.get(this.level);
				String temp = pair.getSecond();
				
				if(this.subTrees == null) this.subTrees = new HashMap<String, LatticeValues>();
				
				LatticeValues subTree;
				if(!this.subTrees.containsKey(temp)){
					
					subTree = new LatticeValues(this.level + 1);
					this.subTrees.put(temp, subTree);
				}
				else subTree = this.subTrees.get(temp);
				
				subTree.addAggregatedValue(dimValues, key, value);
			}
		}
	}
	
	public boolean containsValue(String key){ return this.values.containsKey(key); }
	public boolean containsValue(LinkedList<Pair<String, String>> dimValues, String key){ 
	
		if(dimValues.isEmpty()) return this.containsValue(key);
		else{
			
			if(this.level == dimValues.size()) return this.containsValue(key);
			else{
				
				if(this.subTrees != null){
					
					Pair<String, String> pair = dimValues.get(this.level); 
					String temp = pair.getSecond();
				
					if(this.subTrees.containsKey(temp)) return this.subTrees.get(temp).containsValue(dimValues, key);
					else return false;
				}
				else return false;
			}
		}
	}
	
	public Object getValue(String key){ return this.values.get(key);}
	public Object getValue(List<Pair<String, String>> dimValues, String key){
		
		if(dimValues.isEmpty()) return this.getValue(key);
		else{
			
			if(this.level == dimValues.size()) return this.getValue(key);
			else{
				
				if(this.subTrees != null){
					
					Pair<String, String> pair = dimValues.get(this.level); 
					String temp = pair.getSecond();
				
					if(this.subTrees.containsKey(temp)) return this.subTrees.get(temp).getValue(dimValues, key);
					else return null;
				}
				else return null;
			}
		}
	}
	
	public ArrayList<Pair<String, Object>> getValues(String key){
		
		ArrayList<Pair<String, Object>> stack = new ArrayList<Pair<String, Object>> ();
	
		this.getValuesAux(key, "", stack);
		
		stack.trimToSize();
		
		return stack;
	}
	private void getValuesAux(String key, String id, ArrayList<Pair<String, Object>> stack){
		
		if(this.values != null){
			
			if(this.values.containsKey(key)) stack.add(new Pair<String, Object>(id, this.values.get(key)));
		}
		if(this.subTrees != null){
			
			if(id.length() > 0) id += ":";
			
			for(java.util.Map.Entry<String, LatticeValues> entry : this.subTrees.entrySet()){
				
				entry.getValue().getValuesAux(key, id+entry.getKey(), stack);
			}
		}
	}
	
	
	public int size(){
		
		int counter = 0;
		
		if(!this.values.isEmpty()) counter++;
		
		if(this.subTrees != null){
			
			for(LatticeValues child : this.subTrees.values()){
				
				counter += child.size();
			}
		}
		
		return counter;
	}
}
