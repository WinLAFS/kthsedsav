/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.hiddenEvents;

import dks.arch.Event;
import dks.niche.interfaces.NicheNotifyInterface;

/**
 * The <code>AllocateRequestEvent</code> class
 *
 * @author Joel
 * @version $Id: AllocateRequestEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class AllocateRequestEvent extends Event {

	Object destinationObject;
	Object descriptionObject;
	NicheNotifyInterface initiator;
	String owner;
	
	/**
	 * @param destinationObject
	 * @param descriptionObject
	 * @param initiator
	 */
	public AllocateRequestEvent(Object destinationObject, Object descriptionObject, NicheNotifyInterface initiator, String owner) {
		this.destinationObject = destinationObject;
		this.descriptionObject = descriptionObject;
		this.initiator = initiator;
		this.owner = owner;
	}
	
	public AllocateRequestEvent(Object destinationObject, NicheNotifyInterface initiator, String owner) {
		this.destinationObject = destinationObject;
		this.initiator = initiator;
		this.owner = owner;
		//this.mode = deallocate!
	}
	
	public Object getDescriptionObject() {
		return descriptionObject;
	}
	public void setDescriptionObject(Object descriptionObject) {
		this.descriptionObject = descriptionObject;
	}
	public Object getDestinationObject() {
		return destinationObject;
	}
	public void setDestinationObject(Object destinationObject) {
		this.destinationObject = destinationObject;
	}
	public NicheNotifyInterface getInitiator() {
		return initiator;
	}
	public void setInitiator(NicheNotifyInterface initiator) {
		this.initiator = initiator;
	}
	public String getOwner() {
		return owner;
	}
	
	
}
