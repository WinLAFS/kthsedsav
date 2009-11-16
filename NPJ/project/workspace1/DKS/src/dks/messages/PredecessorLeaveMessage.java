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

import java.io.Serializable;

import dks.addr.DKSRef;
import dks.ring.OperationNumber;

/**
 * The <code>JoinRequestMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: JoinPointMessage.java 259 2007-03-23 14:59:06Z Roberto $
 */
public class PredecessorLeaveMessage extends Message implements Serializable{

	private static final long serialVersionUID = 1822874484150488817L;


	private DKSRef nodeDKSRef;

	private OperationNumber opNum;

	/**
	 * Default constructor for marshaller instantiation
	 */
	public PredecessorLeaveMessage() {

	}

	public PredecessorLeaveMessage(OperationNumber opNum, DKSRef nodeDKSRef) {
		this.opNum = opNum;
		this.nodeDKSRef = nodeDKSRef;
	}

	/**
	 * @return Returns the nodeDKSRef.
	 */
	public DKSRef getPredecessorDKSRef() {
		return nodeDKSRef;
	}

	/**
	 * @return Returns the opNum.
	 */
	public OperationNumber getOpNum() {
		return opNum;
	}

}
