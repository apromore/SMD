package org.prom6.plugins.causalnet.temp.elements;

import java.util.HashSet;

public class Dimension {

	private String category;
	
	private String name;
	private HashSet<String> values;
	
	private String id;
	
	public Dimension(String id, String category, String name){
		
		this.id = id;
		this.category = category;
		this.name = name;
		this.values = new HashSet<String>();
	}
	
	public Dimension(String id, String category, String name, HashSet<String> values){
		
		this.id = id;
		this.category = category;
		this.name = name;
		this.values = values;
	}
	
	public String getName(){ return this.name; }
	public void setName(String name){ this.name = name; }
	
	public String getCategory(){ return this.category; }
	public void setCategory(String category){ this.category = category; }
	
	public String getID(){ return this.id; }
	
	public boolean addValue(String value){ return this.values.add(value); }
	public void addValues(HashSet<String> values){
		
		for(String value : values) this.values.add(value);
	}
	public boolean contains(String value){ return this.values.contains(value); }
	public void removeValue(int position){ this.values.remove(position); }

	public HashSet<String> getValues(){ return this.values; }
	
	public int getCardinality(){ return this.values.size();}
	
	public Dimension instance(){ return new Dimension(this.id, this.category, this.name); }
	
	public String toString(){ return this.category + ":" + this.name + "[" + this.values.size() + "]"; }
}