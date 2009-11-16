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
import dks.messages.Message;
import dks.niche.components.NicheCommunicatingComponent;
import dks.niche.hiddenEvents.ManagementEvent;
import dks.niche.interfaces.NicheMessageInterface;

/**
 * The <code>SendToIdMessage</code> class
 *
 * @author Joel H
 * @version $Id: SendToIdMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class SendToIdMessage extends Message implements NicheMessageInterface {


	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -3231175432790059551L;

	int messageId;
	
	ManagementEvent event;
	String eventName;
	BigInteger destination;
	boolean request;
	
	public SendToIdMessage() {
		
	}

		
	/**
	 * @param message
	 */
	public SendToIdMessage(int messageId, BigInteger destination, ManagementEvent event, DKSRef source, boolean request) {
		this.messageId = messageId; //this is default, unless later overwritten by setMessageId()
		this.destination = destination;
		this.event = event;
		this.setSource(source);
		this.request = request;
		
	}



	
	/**
	 * @return Returns the message.
	 */
	public ManagementEvent getEvent() {
		return event;
	}


	/**
	 * @param message The message to set.
	 */
	public void setEvent(ManagementEvent event) {
		this.event = event;
	}

	


	public BigInteger getDestinationId() {
		return destination;
	}
	public void setDestinationId(BigInteger destination) {
		this.destination = destination;
	}


	public boolean acknowledgementWanted() {
		return request;
	}


	public int getMessageId() {
		return messageId;
	}


	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#getDestinationNode()
	 */
	public DKSRef getDestinationNode() {
		return null;
		
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#setDestinationNode(dks.addr.DKSRef)
	 */
	public void setDestinationNode(DKSRef destinationNode) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#getOriginalMessage()
	 */
	@Override
	public Object getOriginalMessage() {
		return event.getMessage();
	}

	
}
