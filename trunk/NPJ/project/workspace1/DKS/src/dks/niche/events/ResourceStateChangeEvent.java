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

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.niche.ids.NicheId;
import dks.niche.wrappers.ResourceRef;

/**
 * The <code>ResourceStateChangeEvent</code> class
 *
 * @author Joel
 * @version $Id: ResourceStateChangeEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ResourceStateChangeEvent extends Event implements Serializable {
		
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -5685908150278799738L;
	
	//ResourceId resourceId;
	NicheId id;
	DKSRef dksRef;
	
	public ResourceStateChangeEvent() {
		
	}
	/**
	 * @param resourceId
	 */

	public ResourceStateChangeEvent(NicheId id, DKSRef dksRef) {
	
		this.id = id;
		this.dksRef = dksRef;
	}

	
	/**
	 * @return
	 */
	public NicheId getNicheId() {
		return id;
	}
	public void setNicheId(NicheId id) {
		this.id = id;
	}
	/**
	 * @return Returns the dksRef.
	 */
	public DKSRef getDKSRef() {
		return dksRef;
	}
	/**
	 * @param dksRef The dksRef to set.
	 */
	public void setDKSRef(DKSRef dksRef) {
		this.dksRef = dksRef;
	}

}
