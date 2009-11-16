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

import dks.ring.OperationNumber;

/**
 * The <code>JoinRequestMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: LeaveUpdateSuccessorAckMessage.java 164 2007-01-26 18:03:38Z
 *          Roberto $
 */
public class LeaveUpdateSuccessorAckMessage extends Message {

	private static final long serialVersionUID = 5602495068017824739L;

	private OperationNumber opNum;

	/**
	 * Default constructor for marshaller instantiation
	 */
	public LeaveUpdateSuccessorAckMessage() {

	}

	/**
	 * @param opNum
	 */
	public LeaveUpdateSuccessorAckMessage(OperationNumber opNum) {
		this.opNum = opNum;
	}

	/**
	 * @return Returns the opNum.
	 */
	public OperationNumber getOpNum() {
		return opNum;
	}

}
