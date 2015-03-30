package nl.tue.tm.is.epc;

public class ArcTM{
	
	String id;
	Node source;
	Node target;
	
	public ArcTM(){
	}
	public ArcTM(String id, Node source, Node target){
		this.id = id;
		this.source = source;
		this.target = target;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Node getSource() {
		return source;
	}
	public void setSource(Node source) {
		this.source = source;
	}
	public Node getTarget() {
		return target;
	}
	public void setTarget(Node target) {
		this.target = target;
	}
	
	public String toString(){
		return "(" + source.toString() + "," + target.toString() + ")";
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof ArcTM){
			return id.equals(((ArcTM)arg0).getId());
		}else{
			return false;
		}
	}
	
	public int hashCode() {
		return id.hashCode();
	}
	
}
