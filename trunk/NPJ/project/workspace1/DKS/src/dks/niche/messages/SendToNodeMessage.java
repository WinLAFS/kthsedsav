/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.messages;

import java.math.BigInteger;

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.messages.Message;
import dks.niche.components.NicheCommunicatingComponent;
import dks.niche.hiddenEvents.ManagementEvent;
import dks.niche.interfaces.NicheMessageInterface;

/**
 * The <code>SendToNodeMessage</code> class
 *
 * @author Joel
 * @version $Id: SendToNodeMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class SendToNodeMessage extends Message implements NicheMessageInterface{

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -770514733018029163L;

	int messageId;
	boolean request;
	
	ManagementEvent attachedWrappedMessage;
	String eventName;
	BigInteger destination;
	DKSRef destinationNode;
	
	public SendToNodeMessage() {
		
	}

		
	/**
	 * @param operationId
	 * @param message
	 */
	public SendToNodeMessage(int messageId, BigInteger destination, ManagementEvent attachedWrappedMessage, DKSRef source, boolean request) {
		this.messageId = messageId;
		this.destination = destination;
		this.attachedWrappedMessage = attachedWrappedMessage;
		this.setSource(source);
		this.request = request;
		
	}



	
	/**
	 * @return Returns the message.
	 */
	public ManagementEvent getEvent() {
		return attachedWrappedMessage;
	}


	/**
	 * @param message The message to set.
	 */
	public void setEvent(ManagementEvent attachedWrappedMessage) {
		this.attachedWrappedMessage = attachedWrappedMessage;
	}

	

	public int getMessageId() {
		return messageId;
	}
	
	public void setMessageId(int messageId) {
		this.messageId = messageId;;
	}

	public BigInteger getDestinationId() {
		return destination;
	}
	public void setDestinationId(BigInteger destination) {
		this.destination = destination;
	}
	

//	Joels lilla hemmasnickeri f att slippa en extra parameter
	public boolean acknowledgementWanted() {
		return request;
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#getDestinationNode()
	 */
	public DKSRef getDestinationNode() {
		return destinationNode;
		
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#setDestinationNode(dks.addr.DKSRef)
	 */
	public void setDestinationNode(DKSRef destinationNode) {
		this.destinationNode = destinationNode;
		
	}
	
	public Object getOriginalMessage() {
		return attachedWrappedMessage.getMessage();
	}

	
}
