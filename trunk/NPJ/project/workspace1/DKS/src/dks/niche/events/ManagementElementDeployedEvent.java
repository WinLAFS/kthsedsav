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
import dks.niche.ids.NicheId;
import dks.niche.interfaces.IdentifierInterface;

/**
 * The <code>CreateGroupEvent</code> class
 * 
 * @author Joel
 * @version $Id: CreateGroupEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ManagementElementDeployedEvent extends Event implements
		Serializable {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = 666115616326882789L;

	NicheId id;

	transient IdentifierInterface initiator;

	public ManagementElementDeployedEvent(NicheId id,
			IdentifierInterface initiator) {
		this.id = id;
		this.initiator = initiator;
	}

	public NicheId getManagementElementId() {
		return id;
	}

	public IdentifierInterface getInitiator() {
		return initiator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.events.ConfigurationEvent#getSource()
	 */
	public String getSource() {
		return initiator.getId().toString();
	}
}
