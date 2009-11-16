package org.objectweb.jasmine.jade.control.repair.util.event;


public class SimpleEvent implements Event {
	
	private EventType type;
	private EventBody body;
	
	
	
	public SimpleEvent(EventType type, EventBody body){
		this.type = type;
		this.body = body;
	}
	
	
	
	public EventType getEventType() {
		return this.type;
	}

	public EventBody getEventBody() {
		return this.body;
	}
	
	public String toString(){
		return "Type : \n\t" + type.toString() + "\nBody : \n\t"+body.toString();
	}
	
}
