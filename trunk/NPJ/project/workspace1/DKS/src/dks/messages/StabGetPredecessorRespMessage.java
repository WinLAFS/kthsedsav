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
 * @version $Id: StabGetPredecessorRespMessage.java 452 2007-11-27 22:13:23Z roberto $
 */
public class StabGetPredecessorRespMessage extends Message {

	private static final long serialVersionUID = 9131157323519854598L;

	private DKSRef predecesor;
	
	/**
	 * @param predecesor
	 */
	public StabGetPredecessorRespMessage(DKSRef predecesor) {
		super();
		this.predecesor = predecesor;
	}

	/**
	 * Default constructor for marshaller instantiation
	 */
	public StabGetPredecessorRespMessage() {

	}

	/**
	 * @return Returns the predecesor.
	 */
	public DKSRef getPredecesor() {
		return predecesor;
	}

}
