package se.kth.ict.id2203.ac.events;

import se.sics.kompics.Event;

public class ACPropose extends Event{
	private final String value;
	private final int id;
	
	public ACPropose (int id, String value){
		this.id = id;
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	public int getId() {
		return id;
	}
}
