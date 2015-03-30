package nl.tue.tm.is.epc;

public class ArcFromMerge{
	
	private String id;
	private Node source;
	private Node target;
	
	public ArcFromMerge(){
	}
	
	public ArcFromMerge(String id, Node source, Node target){
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
		return "Arc(("+id+") , " + source.toString() + "," + target.toString() + ")";
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof ArcFromMerge){
			return id.equals(((ArcFromMerge)arg0).getId());
		}else{
			return false;
		}
	}
	
	public int hashCode() {
		return Integer.parseInt(id);
	}
	
}
