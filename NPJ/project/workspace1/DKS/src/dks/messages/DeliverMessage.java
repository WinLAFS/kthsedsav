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


/**
 * The <code>DeliverMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DeliverMessage.java 452 2007-11-27 22:13:23Z roberto $
 */
public class DeliverMessage extends Message {

	private static final long serialVersionUID = 1523195860485564141L;

	private Message messageToDeliver;

	/**
	 * Default Constructor for the Marshaler instanciation
	 */
	public DeliverMessage() {
	}

	/**
	 * @param messageToDeliver
	 */
	public DeliverMessage(Message messageToDeliver) {
		super();
		this.messageToDeliver = messageToDeliver;
	}
	/**
	 * @return Returns the messageToDeliver.
	 */
	public Message getMessageToDeliver() {
		return messageToDeliver;
	}

}
