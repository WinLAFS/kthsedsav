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

import static dks.messages.MessageTypeTable.MSG_TYPE_LEAVE_DONE;
import dks.ring.OperationNumber;

/**
 * The <code>JoinRequestMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: LeaveDoneMessage.java 452 2007-11-27 22:13:23Z roberto $
 */
public class LeaveDoneMessage extends Message {

	private static final long serialVersionUID = -8501548973457400212L;

	private static final int messageType = MSG_TYPE_LEAVE_DONE;

	private OperationNumber opNum;

	/**
	 * Default constructor for marshaller instantiation
	 */
	public LeaveDoneMessage() {

	}

	/**
	 * @param opNum
	 */
	public LeaveDoneMessage(OperationNumber opNum) {
		this.opNum = opNum;
	}

	public static int getStaticMessageType() {
		return messageType;
	}

	/**
	 * @return Returns the opNum.
	 */
	public OperationNumber getOpNum() {
		return opNum;
	}

}
