/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.ids;

import java.io.Serializable;

import dks.addr.DKSRef;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.ReliableInterface;

/**
 * The <code>ManagementElementId</code> class
 *
 * @author Joel
 * @version $Id: ManagementElementId.java 294 2006-05-05 17:14:14Z joel $
 */
public class ManagementElementId implements Serializable, IdentifierInterface, ReliableInterface {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 4933089564699600281L;
	NicheId myId;
	int type;
	boolean reliable;
	DKSRef dksRef;
	
	public ManagementElementId() {
		
	}
	public ManagementElementId(NicheId id, int type, boolean reliable) {
		this.myId = id;
		this.type = type;
		this.reliable = reliable;
	}
	
	public boolean isReliable() {
		return reliable;
	}


	public NicheId getId() {
		return myId;
	}
	
	public ManagementElementId setDKSRef(DKSRef dksRef) {
		this.dksRef = dksRef;
		return this;
	}

	public DKSRef getDKSRef() {
		return dksRef;
	}
	
	
}
