package nl.tue.tm.is.epc;

public class EventFromMerge extends Node {

	public EventFromMerge() {
	}
	public EventFromMerge(String id){
		super(id);
	}
	public EventFromMerge(String id, String label){
		super(id, label);
	}

	@Override
	public String toString(){
		return "Event("+getId() +", " + getName() + ")";
	}
}
