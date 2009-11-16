/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.ring.events;

import dks.addr.DKSRef;
import dks.arch.Event;

/**
 * The <code>RingNewPredecessorEvent</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingNewPredecessorEvent.java 294 2006-05-05 17:14:14Z roberto $
 */
public class RingNewPredecessorEvent extends Event {
	
	
	private DKSRef newPredecessor;
	private DKSRef oldPredecessor;
	/**
	 * 
	 */
	public RingNewPredecessorEvent(DKSRef oldPred, DKSRef pred) {
		this.newPredecessor=pred;
		this.oldPredecessor=oldPred;
	}
	/**
	 * @return Returns the newPredecessor.
	 */
	public DKSRef getNewPredecessor() {
		return newPredecessor;
	}
	
	/**
	 * @return Returns the oldPredecessor.
	 */
	public DKSRef getOldPredecessor() {
		return oldPredecessor;
	}

}
