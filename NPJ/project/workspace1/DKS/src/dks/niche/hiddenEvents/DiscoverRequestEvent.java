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
 * The <code>ResourceEnquiryEvent</code> class
 *
 * @author Joel
 * @version $Id: ResourceEnquiryEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class DiscoverRequestEvent extends Event {

	Object requirements;
	NicheNotifyInterface initiator;
	int opId;
	
	/**
	 * @param requirements
	 * @param initiator
	 */
	public DiscoverRequestEvent(Object requirements, NicheNotifyInterface initiator) {
		this.requirements = requirements;
		this.initiator = initiator;
		//this.opId = opId;
	}
//	public DiscoverRequestEvent(Object requirements, NicheNotifyInterface initiator, int opId) {
//		this.requirements = requirements;
//		this.initiator = initiator;
//		this.opId = opId;
//	}
	public NicheNotifyInterface getInitiator() {
		return initiator;
	}
	public void setInitiator(NicheNotifyInterface initiator) {
		this.initiator = initiator;
	}
	public int getOperationId() {
		return opId;
	}
	public Object getRequirements() {
		return requirements;
	}
	public void setRequirements(Object requirements) {
		this.requirements = requirements;
	}
	

}
