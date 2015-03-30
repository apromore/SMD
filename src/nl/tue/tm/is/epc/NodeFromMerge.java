package nl.tue.tm.is.epc;

public class NodeFromMerge {

	private String id = "";
	private String name = "";
	
	public NodeFromMerge() {}
	
	public NodeFromMerge(String id){
		this.id = id;
	}
	public NodeFromMerge(String id, String label){
		this.id = id;
		this.name = label;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object arg0) {
		if (arg0.getClass().getSimpleName().equals(this.getClass().getSimpleName())){
			return id.equals(((NodeFromMerge)arg0).getId());
		}else{
			return false;
		}
	}
	public int hashCode() {
		return Integer.parseInt(id);
	}
}
