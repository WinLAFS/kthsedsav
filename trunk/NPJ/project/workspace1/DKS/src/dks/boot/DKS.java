/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.boot;

import dks.addr.DKSRef;

/**
 * The <code>NodeInterface</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DKS.java 211 2007-02-20 14:41:15Z Roberto $
 */
public interface DKS {

	/**
	 * Called by the first d of the ring
	 */
	public void create();
	
	/**
	 * Called by the d that wants to join the Ring
	 * @param ref The reference of the d through which the join must be done
	 */
	public void join(DKSRef ref);
	
	/**
	 * Called when the d wants to leave
	 */
	public void leave();
	
}
