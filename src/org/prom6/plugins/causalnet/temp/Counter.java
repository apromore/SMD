package org.prom6.plugins.causalnet.temp;

public class Counter {

	private int count;
	
	public Counter(){ this.count = 0; }
	public Counter(int initialValue){ this.count = initialValue; }
	
	public void increment(){ this.count++; }
	public void increment(int value){ this.count += value; }
	
	public int getValue(){ return this.count; }
	
	public String toString(){ return String.valueOf(this.count); }
}
