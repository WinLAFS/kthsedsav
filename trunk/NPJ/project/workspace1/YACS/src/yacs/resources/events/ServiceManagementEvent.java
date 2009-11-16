package yacs.resources.events;

import java.io.Serializable;

import dks.arch.Event;
import dks.niche.interfaces.IdentifierInterface;

import yacs.resources.data.AvailabilityInformation;


public class ServiceManagementEvent extends Event implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private TYPE eventType;
	private IdentifierInterface id;
	private AvailabilityInformation availability;
	
	/*public ServiceManagementEvent(){
		eventType = SM_EVENT_TYPE.UNDEFINED;
	}*/
	public ServiceManagementEvent( IdentifierInterface id, TYPE eventType ){
		this.id = id;
		this.eventType = eventType;
	}	
	
	public TYPE getEventType() {
		return eventType;
	}
	public void setEventType(TYPE eventType) {
		this.eventType = eventType;
	}
	public IdentifierInterface getId() {
		return id;
	}
	public void setId(IdentifierInterface id) {
		this.id = id;
	}
	public AvailabilityInformation getAvailability() {
		return availability;
	}
	public void setAvailability(AvailabilityInformation availability) {
		this.availability = availability;
	}

	public String getKey(){
		if( this.id == null )
			return null;
		else
			return this.id.getId().toString();
	}
	
	public String toString(){
		return "SME:{"+getKey()+","+eventType+",aid:"+(availability==null?"NULL":availability.getId())+"}";
	}

	//
	public enum TYPE {
		UNDEFINED,
		SERVICE_ADDED,
		SERVICE_DEPARTED,
		SERVICE_HIGH_LOAD,
		SERVICE_LOW_LOAD,
		AVAILABILITY_INFORMATION,
		AVAILABILITY_MASTER_NEEDED,
		AVAILABILITY_MASTER_SURPLUS,
		AVAILABILITY_WORKER_NEEDED,
		AVAILABILITY_WORKER_SURPLUS
	};
}
