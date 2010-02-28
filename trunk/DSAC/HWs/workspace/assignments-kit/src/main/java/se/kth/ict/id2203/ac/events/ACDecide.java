package se.kth.ict.id2203.ac.events;

import se.sics.kompics.Event;

public class ACDecide extends Event{
	private final String value;
	
	public ACDecide (String value){
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
