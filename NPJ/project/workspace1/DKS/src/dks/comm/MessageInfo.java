/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.comm;

import dks.addr.DKSRef;

/**
 * The <code>MessageInfo</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: MessageInfo.java 254 2007-03-16 13:38:58Z Roberto $
 */
public class MessageInfo {

	private DKSRef source;

	private DKSRef dest;

	private Class sendNotifyEvent;

	private Class ackNotifyEvent;

	private int messageUniqueId;

	private int transfId;

	/**
	 * Generates an attachment for the MessageBuffers that contains all the
	 * informations for the handling of the message in the stack
	 * 
	 * @param source
	 *            Source of the packet
	 * @param dest
	 *            Destination of the packet
	 * @param sendNotifyEvent
	 *            Contains the class of the event that must be issued if the
	 *            send of the packet was successful, null if no notification is
	 *            necessary
	 * @param ackNotifyEvent
	 *            Contains the class of the event that must be issued if the
	 *            packet was successfully received, null if no notification is
	 *            necessary
	 */
	public MessageInfo(DKSRef source, DKSRef dest, Class sendNotifyEvent,
			Class ackNotifyEvent, int messageId) {
		super();
		this.source = source;
		this.dest = dest;
		this.sendNotifyEvent = sendNotifyEvent;
		this.ackNotifyEvent = ackNotifyEvent;
		this.messageUniqueId = messageId;
	}

	public Class getAckNotifyEvent() {
		return ackNotifyEvent;
	}

	public DKSRef getDest() {
		return dest;
	}

	public Class getSendNotifyEvent() {
		return sendNotifyEvent;
	}

	public DKSRef getSource() {
		return source;
	}

	public int getMessageUniqueId() {
		return messageUniqueId;
	}

	/**
	 * @param transfId
	 * @return
	 */
	public void  setTransfrerMessageId(int transfId) {
		this.transfId=transfId;
	}

	/**
	 * @return Returns the transfId.
	 */
	public int getTransfId() {
		return transfId;
	}

}
