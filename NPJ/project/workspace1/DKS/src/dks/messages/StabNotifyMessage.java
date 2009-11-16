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
 * The <code>JoinRequestMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: StabNotifyMessage.java 452 2007-11-27 22:13:23Z roberto $
 */
public class StabNotifyMessage extends Message {

	private static final long serialVersionUID = -5978515616849806947L;


	private DKSRef p;

	/**
	 * Default constructor for marshaller instantiation
	 */
	public StabNotifyMessage() {

	}

	/**
	 * @param n
	 */
	public StabNotifyMessage(DKSRef p) {
		this.p = p;
	}

	public DKSRef getP() {
		return p;
	}

}
