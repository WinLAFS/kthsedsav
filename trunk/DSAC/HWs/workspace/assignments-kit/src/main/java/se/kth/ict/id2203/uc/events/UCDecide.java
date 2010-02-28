package se.kth.ict.id2203.uc.events;

import se.sics.kompics.Event;

public class UCDecide extends Event {
	private final int id;
	private final String value;
	
	public UCDecide (int id, String value){
		this.value = value;
		this.id = id;
	}
	
	public String getValue() {
		return value;
	}
	
	public int getId() {
		return id;
	}
}
