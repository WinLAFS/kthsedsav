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
 * The <code>LocalDeploymentEvent</code> class
 *
 * @author Joel
 * @version $Id: LocalDeploymentEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class DeployRequestEvent extends Event {

	NicheNotifyInterface initiator;
	Object destinationObject;
	Object descriptionObject;
	String owner;
	boolean management;

	public DeployRequestEvent(Object destinations, Object descriptions, NicheNotifyInterface initiator, String owner) {
		
		this.destinationObject = destinations;
		this.descriptionObject = descriptions;
		this.initiator = initiator;
		this.owner = owner;
		this.management = false;
	}
	public DeployRequestEvent(Object destinations, Object descriptions, NicheNotifyInterface initiator, String owner, boolean management) {
		
		this.destinationObject = destinations;
		this.descriptionObject = descriptions;
		this.initiator = initiator;
		this.management = management;
		this.owner = owner;
	}


	public Object getDestinationObject() {
		return destinationObject;
	}


	public void setDestinationObject(Object destinations) {
		this.destinationObject = destinations;
	}


	public Object getDescriptionObject() {
		return descriptionObject;
	}

	public void setDescriptionObject(Object requirements) {
		this.descriptionObject = requirements;
	}

	public String getOwner() {
		return owner;
	}
	public NicheNotifyInterface getInitiator() {
		return initiator;
	}

	public void setInitiator(NicheNotifyInterface initiator) {
		this.initiator = initiator;
	}
	
public boolean isManagementDeployment() {
	return management;
}
	

}
