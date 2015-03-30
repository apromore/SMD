package nl.tue.tm.is.epc;

public class FunctionFromMerge extends Node {

	public FunctionFromMerge() {
	}
	public FunctionFromMerge(String id){
		super(id);
	}
	public FunctionFromMerge(String id, String label){
		super(id, label);
	}

	@Override
	public String toString(){
		return "Function("+getId() +", " + getName() + ")";
	}
}
