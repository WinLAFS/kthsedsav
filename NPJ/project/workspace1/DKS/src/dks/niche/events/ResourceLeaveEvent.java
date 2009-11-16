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
import dks.niche.ids.NicheId;


/**
 * The <code>ResourceJoinEvent</code> class
 *
 * @author Joel
 * @version $Id: ResourceLeaveEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ResourceLeaveEvent extends ResourceStateChangeEvent implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -6105867177881839084L;

	public ResourceLeaveEvent(NicheId id, DKSRef dksRef) {
		super(id, dksRef);

	}
}
