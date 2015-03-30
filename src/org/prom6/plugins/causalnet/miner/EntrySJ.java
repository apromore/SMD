package org.prom6.plugins.causalnet.miner;

import java.util.HashMap;
import java.util.List;

import org.processmining.framework.util.Pair;

public class EntrySJ<A> {

	private List<Pair<String, String>> id;
	private HashMap<String, A> joins;
	private HashMap<String, A> splits;
	
	public EntrySJ(List<Pair<String, String>> id){
		
		this.id = id;
		this.joins = new HashMap<String, A>();
		this.splits = new HashMap<String, A>();
	}
	public EntrySJ(List<Pair<String, String>> id, HashMap<String, A> joins, HashMap<String, A> splits){
		
		this.id = id;
		this.joins = joins;
		this.splits = splits;
	}
	
	public A addJoin(String joinKey, A object){ return this.joins.put(joinKey, object); }
	public A addSplit(String splitKey, A object){ return this.splits.put(splitKey, object); }
	public HashMap<String, A> getJoins(){ return this.joins; }
	public HashMap<String, A> getSplits(){ return this.splits; }
	public A removeJoin(String joinKey){ return this.joins.remove(joinKey); }
	public A removeSplit(String splitKey){ return this.splits.remove(splitKey); }
	
	public List<Pair<String, String>> getID(){ return this.id; }
	public String getKey(){
		
		StringBuffer buffer = new StringBuffer();
		boolean flag = true;
		for(Pair<String,String> pair : this.id){
			
			if(flag){
				
				buffer.append(pair.getSecond());
				flag = false;
			}
			else buffer.append(":"+pair.getSecond());
		}
		
		return buffer.toString();
	}
	
	public String toString(){
		
		return this.id.toString() + "=" + this.joins.toString() + "," + this.splits.toString();
	}
}
