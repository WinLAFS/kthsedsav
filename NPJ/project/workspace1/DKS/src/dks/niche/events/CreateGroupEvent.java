/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.events;

import java.io.Serializable;

import dks.arch.Event;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;

/**
 * The <code>CreateGroupEvent</code> class
 *
 * @author Joel
 * @version $Id: CreateGroupEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class CreateGroupEvent extends Event implements Serializable, ConfigurationEvent {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 666115616326882789L;
	
	GroupId gid;
	NicheId initiator;
	NicheId broker;
	
	
	public CreateGroupEvent(GroupId gid, NicheId initiator) {
		this.gid = gid;
		this.initiator = initiator;
	}
	
	public GroupId getGroupId() {
		return gid;
	}
	public NicheId getInitiator() {
		return initiator;
	}
	
	public NicheId getBroker() {
		return broker;
	}
	public CreateGroupEvent setBroker(NicheId broker) {
		this.broker = broker;
		return this;
	}

	/* (non-Javadoc)
	 * @see dks.niche.events.ConfigurationEvent#getSource()
	 */
	@Override
	public String getSource() {
		return initiator.toString();
	}
}
