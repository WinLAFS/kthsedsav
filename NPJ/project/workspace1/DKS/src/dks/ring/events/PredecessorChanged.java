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
 * The <code>PredecessorChanged</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: PredecessorChanged.java 294 2006-05-05 17:14:14Z Roberto $
 */
public class PredecessorChanged extends Event {

	private DKSRef oldPred;

	private DKSRef newPred;

	public PredecessorChanged(DKSRef oldPred, DKSRef newPred) {
		super();
		this.oldPred = oldPred;
		this.newPred = newPred;
	}

	/**
	 * @return Returns the oldPred.
	 */
	public DKSRef getOldPred() {
		return oldPred;
	}

	/**
	 * @return Returns the newPred.
	 */
	public DKSRef getNewPred() {
		return newPred;
	}

}
