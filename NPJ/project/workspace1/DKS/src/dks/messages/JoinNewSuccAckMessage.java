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
import dks.ring.OperationNumber;

/**
 * The <code>JoinRequestMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: JoinNewSuccAckMessage.java 452 2007-11-27 22:13:23Z roberto $
 */
public class JoinNewSuccAckMessage extends Message {

	private static final long serialVersionUID = 3589014040427751799L;

	private DKSRef nodeDKSRef;

	private OperationNumber opNum;

	/**
	 * Default constructor for marshaller instantiation
	 * 
	 */
	public JoinNewSuccAckMessage() {
	}

	/**
	 * @param source
	 */
	public JoinNewSuccAckMessage(OperationNumber opNum, DKSRef nodeDKSRef) {
		this.opNum = opNum;
		this.nodeDKSRef = nodeDKSRef;
	}

	/**
	 * @return Returns the nodeDKSRef.
	 */
	public DKSRef getNodeDKSRef() {
		return nodeDKSRef;
	}

	/**
	 * @return Returns the opNum.
	 */
	public OperationNumber getOpNum() {
		return opNum;
	}

}
