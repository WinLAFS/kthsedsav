/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.messages;

import dks.addr.DKSRef;

/**
 * The <code>HelloMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: HelloMessage.java 452 2007-11-27 22:13:23Z roberto $
 */
public class HelloMessage extends Message {

	private static final long serialVersionUID = 8383834825535727397L;

	private DKSRef ref;

	/**
	 * Default constructor to be called by the newInstance() Method
	 */
	public HelloMessage() {

	}

	/**
	 * Real Constructor
	 * 
	 * @param The
	 *            DKSRef of the peer
	 */
	public HelloMessage(DKSRef ref) {
		this.ref = ref;
	}

	public DKSRef getDKSRef() {
		return ref;
	}

}
