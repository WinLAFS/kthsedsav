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
 * @version $Id: LeaveUpdateSuccMessage.java 452 2007-11-27 22:13:23Z roberto $
 */
public class LeaveUpdateSuccMessage extends Message {

	private static final long serialVersionUID = -4311042968129330610L;

	private OperationNumber opNum;

	/**
	 * Default constructor for marshaller instantiation
	 */
	public LeaveUpdateSuccMessage() {

	}

	/**
	 * @param opNum
	 */
	public LeaveUpdateSuccMessage(OperationNumber opNum) {
		this.opNum = opNum;
	}

	/**
	 * @return Returns the opNum.
	 */
	public OperationNumber getOpNum() {
		return opNum;
	}

}
