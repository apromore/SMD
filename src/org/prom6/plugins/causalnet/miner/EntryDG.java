package org.prom6.plugins.causalnet.miner;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.processmining.framework.util.Pair;

public class EntryDG {

	private List<Pair<String, String>> id;
	private String key;
	private int frequency;
	private HashMap<String, Object> inputs;
	private HashMap<String, Object> outputs;
	
	private ArrayList<Pair<String,Float>> l2lDependencies;
	private ArrayList<Pair<String,Float>> directDependencies;
	
	protected Pair<String,Float> strongestFollower;
	protected Pair<String,Float> strongestCause;
	
	
	public EntryDG(List<Pair<String, String>> id, int frequency){
		
		this.id = id;

		StringBuffer buffer = new StringBuffer();
		boolean flag = true;
		for(Pair<String,String> pair : this.id){
			
			if(flag){
				
				buffer.append(pair.getSecond());
				flag = false;
			}
			else buffer.append(":"+pair.getSecond());
		}
		this.key = buffer.toString();
		
		this.frequency = frequency;
		this.inputs = new HashMap<String, Object>();
		this.outputs = new HashMap<String, Object>();
	}
	
	public EntryDG(List<Pair<String, String>> id){ this(id, 0); }
	
	public void addL1Ldependency(Float dependencyValue, double l1lThreshold){
			
		if(dependencyValue >= l1lThreshold){	// (a,a) >= l1lThreshold
			
			this.inputs.put(this.key, dependencyValue);
			this.outputs.put(this.key, dependencyValue);
		}
	}
	
	public void addL2Ldependencies(AbstractMap<String, Float> dependencyValues, double l2lThreshold, AbstractMap<String, EntryDG> relations){
		
		if(!this.inputs.containsKey(this.key)){	// (a,a) cannot belong to C1
		
			this.l2lDependencies = new ArrayList<Pair<String, Float>>(dependencyValues.size());
			
			for(java.util.Map.Entry<String, Float> entry : dependencyValues.entrySet()){
				
				if(relations.get(entry.getKey()).containsInput(entry.getKey())) continue;	// (b,b) cannot belong to C1
				
				Float dependencyValue = entry.getValue();
				
				Pair<String, Float> dependency = new Pair<String, Float>(entry.getKey(), dependencyValue);
				this.add(this.l2lDependencies, dependency);
				
				if(dependencyValue >= l2lThreshold){	// (a,b) >= l2lThreshold
					
					this.inputs.put(entry.getKey(), dependencyValue);
					this.outputs.put(entry.getKey(), dependencyValue);
				}
			}
			
			if(l2lDependencies.isEmpty()) this.l2lDependencies = null;
		}
	}
	
	public void addDirectDependencies(AbstractMap<String, Float> dependencyValues){
		
		if(!dependencyValues.isEmpty()){
		
			this.directDependencies = new ArrayList<Pair<String, Float>>(dependencyValues.size());
			
			for(java.util.Map.Entry<String, Float> entry : dependencyValues.entrySet()){
				
				Float dependencyValue = entry.getValue();
				
				this.add(this.directDependencies, new Pair<String, Float>(entry.getKey(), dependencyValue));
			}
		}
	}
	
	public void computeStrongestDependencies(AbstractMap<String, EntryDG> relations){
		
		if(this.l2lDependencies != null){
			
			for(Pair<String,Float> dependency : this.l2lDependencies){
				
				this.strongestFollower = this.max(this.strongestFollower, dependency);
				this.strongestCause = this.max(this.strongestCause, dependency);
				
				EntryDG dest = relations.get(dependency.getFirst());
				
				Pair<String, Float> dependencyDest = new Pair<String, Float>(this.key, dependency.getSecond());
				dest.strongestFollower = this.max(dest.strongestFollower, dependencyDest);
				dest.strongestCause = this.max(dest.strongestCause, dependencyDest);
			}
		}
		
		// the strongest follower
		
		Pair<String, Float> strongestFollowerDirect = null;
		if(this.directDependencies != null) strongestFollowerDirect = this.directDependencies.get(0);
		
		this.strongestFollower = this.max(this.strongestFollower, strongestFollowerDirect);

		// the strongest cause
		
		Pair<String, Float> strongestCauseDirect = null;
		
		for(EntryDG relation : relations.values()){
			
			if(relation == this) continue;
			
			ArrayList<Pair<String,Float>> relationDirectDependencies = relation.getDirectDependencies();
			
			if(relationDirectDependencies != null){
				
				for(Pair<String,Float> dependency : relationDirectDependencies){
					
					if(dependency.getFirst().equals(this.key)){
						
						strongestCauseDirect = this.max(strongestCauseDirect, new Pair<String,Float>(relation.getKey(),dependency.getSecond()));
						break;
					}
				}
			}
		}
		
		this.strongestCause = this.max(this.strongestCause, strongestCauseDirect);
		
	}

	public void removeWeakDependencies(AbstractMap<String, EntryDG> relations, double l2lThreshold, double dependencyThreshold, double relativeToBestThreshold){
		
		if(this.l2lDependencies != null){
		
			for(Pair<String,Float> dependency : this.l2lDependencies){
				
				if(dependency.getSecond() < l2lThreshold) continue;	// L2L dependency needs to belong to C2
					
				EntryDG b = relations.get(dependency.getFirst());
	
				if(this.strongestFollower.getSecond() < dependencyThreshold){
					if((this.strongestFollower.getSecond() - b.strongestFollower.getSecond()) > relativeToBestThreshold) 
						this.strongestFollower = null;
				}
				
				if(this.strongestCause.getSecond() < dependencyThreshold){
					if((this.strongestCause.getSecond() - b.strongestCause.getSecond()) > relativeToBestThreshold) 
						this.strongestCause = null;
				}
				
			}
		}
	}
	
	public void computeDependencies(AbstractMap<String, EntryDG> relations, double dependencyThreshold, double relativeToBestThreshold){
		
		if(this.directDependencies != null){
			
			for(Pair<String,Float> dependency : this.directDependencies){
				
				boolean in = false, out = false;
				
				EntryDG dest = relations.get(dependency.getFirst());
				
				if(dependency.getSecond() >= dependencyThreshold){
					
					in = true;
					out = true;
				}
				else{
					
					if(this.strongestFollower != null)
						if((this.strongestFollower.getSecond() - dependency.getSecond()) < relativeToBestThreshold) out = true;
					
					if(dest.strongestCause != null)
						if((dest.strongestCause.getSecond() - dependency.getSecond()) < relativeToBestThreshold) in = true;
				}
				
				if(out){
					
					this.outputs.put(dependency.getFirst(), dependency.getSecond());
					dest.addInput(this.key, dependency.getSecond());
				}
				else
					if(in){
					
					this.outputs.put(dependency.getFirst(), dependency.getSecond());
					dest.addInput(this.key, dependency.getSecond());
				}
			}
		}
	}
	
	public void checkUnconnectedTasks(AbstractMap<String, EntryDG> relations){
		
		if(this.strongestCause != null){
			
			this.inputs.put(this.strongestCause.getFirst(), this.strongestCause.getSecond());
			relations.get(this.strongestCause.getFirst()).addOutput(this.key, this.strongestCause.getSecond());
		}
		if(this.strongestFollower != null){
			
			this.outputs.put(this.strongestFollower.getFirst(), this.strongestFollower.getSecond());
			relations.get(this.strongestFollower.getFirst()).addInput(this.key, this.strongestFollower.getSecond());
		}
	}
	
	private void add(ArrayList<Pair<String, Float>> list, Pair<String, Float> element){
		
		int index = 0;
		for(; index < list.size(); index++){
			
			if(element.getSecond() > list.get(index).getSecond()) break;
		}
		list.add(index, element);
	}
	
	private Pair<String, Float> max(Pair<String, Float> value1, Pair<String, Float> value2){
		
		if(value1 != null){
		
			if(value2 != null){

				if(value1.getSecond() > value2.getSecond()) return value1;
				else return value2;
			}
			else return value1;
		}
		else if(value2 != null) return value2;
		
		return null;
	}
	
	//-------------------------------------------------
	
	public ArrayList<Pair<String,Float>> getL2Ldependencies(){ return this.l2lDependencies; }
	public ArrayList<Pair<String,Float>> getDirectDependencies(){ return this.directDependencies; }
 
	public int getFrequency(){ return this.frequency; }
	
	protected Object addInput(String input, Object info){ return this.inputs.put(input, info); }
	protected Object addOutput(String output, Object info){ return this.outputs.put(output, info); }
	public boolean containsInput(String input){ return this.inputs.containsKey(input); }
	public boolean containsOutput(String output){ return this.outputs.containsKey(output); }
	
	public Object getInputInfo(String input){ return this.inputs.get(input); }
	public Object getOutputInfo(String output){ return this.outputs.get(output); }
	public Set<String> getInputs(){ return this.inputs.keySet(); }
	public Set<String> getOutputs(){ return this.outputs.keySet(); }
	public int inputsSize(){ return this.inputs.size(); }
	public int outputsSize(){ return this.outputs.size(); }
	
	public List<Pair<String, String>> getID(){ return this.id; }
	public String getKey(){ return this.key; }
	
	public String toString(){
		
		return this.id.toString() + "=" + this.inputs.toString() + "," + this.outputs.toString();
	}
}
