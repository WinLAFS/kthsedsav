/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.comm.mina.events;

import dks.arch.Event;
import dks.comm.MessageInfo;
import dks.comm.mina.TransportProtocol;
import dks.messages.Message;

/**
 * The <code>MessageUnMarshalledEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DeliverMessageEvent.java 496 2007-12-20 15:39:02Z roberto $
 */
public class DeliverMessageEvent extends Event {

	private Message message;

	private MessageInfo messageInfo;

	private TransportProtocol usedProtocol;

	public DeliverMessageEvent(Message message, MessageInfo messageInfo,
			TransportProtocol protocol) {
		this.message = message;
		this.messageInfo = messageInfo;
		this.usedProtocol = protocol;
	}

	/**
	 * @return Returns the message umarshaled.
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * Get the {@link MessageInfo} attached to the Message
	 * 
	 * @return The {@link MessageInfo}
	 */
	public MessageInfo getMessageInfo() {
		return messageInfo;
	}

	public TransportProtocol getUsedTransportProtocol() {
		return usedProtocol;
	}

}
