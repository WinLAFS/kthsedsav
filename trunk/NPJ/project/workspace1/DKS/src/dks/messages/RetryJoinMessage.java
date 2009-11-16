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

import static dks.messages.MessageTypeTable.MSG_TYPE_JOIN_RETRY;

/**
 * The <code>JoinRequestMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RetryJoinMessage.java 452 2007-11-27 22:13:23Z roberto $
 */
public class RetryJoinMessage extends Message {

	private static final long serialVersionUID = -8882069666941019281L;

	private static final int messageType = MSG_TYPE_JOIN_RETRY;

	/**
	 * Default constructor for marshaller instantiation
	 */
	public RetryJoinMessage() {

	}

	public static int getStaticMessageType() {
		return messageType;
	}

}
