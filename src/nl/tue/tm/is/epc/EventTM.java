package nl.tue.tm.is.epc;

public class EventTM extends NodeTM {

	public EventTM() {
	}
	public EventTM(String id){
		this.id = id;
	}
	public EventTM(String id, String label){
		this.id = id;
		this.name = label;
	}

}
