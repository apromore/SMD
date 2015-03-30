package org.prom6.plugins.causalnet.temp.cube.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.processmining.framework.util.Pair;

public class LatticeIterator implements Iterator<Pair<ArrayList<Pair<String, String>>, HashMap<String, Object>>> {

	private LatticeValues values;

	private ArrayList<Pair<String, String>> id;
	
	private LinkedList<LatticeIterator> subTreesToExplore;
	private boolean isInstanceExplored;
	
	public LatticeIterator(LatticeValues values){
		
		this(values, new ArrayList<Pair<String, String>>(0));
	}
	
	public LatticeIterator(LatticeValues values, ArrayList<Pair<String, String>> id){
		
		this.values = values;
		
		this.subTreesToExplore = new LinkedList<LatticeIterator>();
		
		this.id = id;
		
		if(this.values.subTrees != null) 
			for(java.util.Map.Entry<String, LatticeValues> entry : this.values.subTrees.entrySet()){
				
				ArrayList<Pair<String, String>> newID = new ArrayList<Pair<String, String>>(this.id.size() + 1);
				for(Pair<String, String> pair : this.id) newID.add(pair);
				
				newID.add(new Pair<String, String>(this.values.nextLevel, entry.getKey()));
				
				this.subTreesToExplore.add(new LatticeIterator(entry.getValue(), newID));
			}
		
		if(!this.values.values.isEmpty()) this.isInstanceExplored = false;
		else this.isInstanceExplored = true;		
	}

	public boolean hasNext() { return !this.subTreesToExplore.isEmpty() || !this.isInstanceExplored; }

	public Pair<ArrayList<Pair<String, String>>, HashMap<String, Object>> next() {
		
		if(!this.isInstanceExplored){
			
			this.isInstanceExplored = true;
			return new Pair<ArrayList<Pair<String, String>>, HashMap<String, Object>>(this.id, this.values.values);
		}
		else{
						
			if(this.subTreesToExplore.isEmpty()) return null;
			else{
				
				LatticeIterator it = this.subTreesToExplore.getLast();
				Pair<ArrayList<Pair<String, String>>, HashMap<String, Object>> nextValue = it.next();
				if(!it.hasNext()) this.subTreesToExplore.removeLast();
			
				return nextValue;
			}
		}
	}

	public void remove() { throw new java.lang.UnsupportedOperationException("remove"); }
}
