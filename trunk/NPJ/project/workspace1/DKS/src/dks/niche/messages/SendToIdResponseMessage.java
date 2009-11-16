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

import java.io.Serializable;
import java.math.BigInteger;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.niche.hiddenEvents.ManagementEvent;
import dks.niche.interfaces.NicheMessageInterface;
import dks.niche.interfaces.NicheResponseMessageInterface;

/**
 * The <code>SendToIdResponseMessage</code> class
 *
 * @author Joel
 * @version $Id: SendToIdResponseMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class SendToIdResponseMessage extends Message implements NicheResponseMessageInterface {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 4590810946041301482L;
	
	//int operationId;
	int messageId;
	
	Serializable message;
	boolean failedLookup = false;
	int originalMessageId;
	
	public static String FAILED_LOOKUP = "failed lookup";
	
	/**
	 * @param SendToIdMessage m
	 * @param message
	 */
	public SendToIdResponseMessage(int messageId, NicheMessageInterface m, Serializable message) {
		
		this.messageId = messageId;
		
		this.originalMessageId = m.getMessageId();
		
		if(message.equals(FAILED_LOOKUP)) {
			this.failedLookup = true;
		} else {
			this.message = message;
		}
	}

	/**
	 * @return Returns the message.
	 */
	public Serializable getMessage() {
		return message;
	}


	/**
	 * @param message The message to set.
	 */
	public void setMessage(Serializable message) {
		this.message = message;
	}


//	/**
//	 * @return Returns the operationId.
//	 */
//	public int getOperationId() {
//		return operationId;
//	}
	
	public int getMessageId() {
		return messageId;
	}


//	/**
//	 * @param operationId The operationId to set.
//	 */
//	public void setOperationId(int operationId) {
//		this.operationId = operationId;
//	}
	
	public boolean failedLookup() {
		return failedLookup;
	}
	public int getOriginalMessageId() {
		return originalMessageId;
	}
	
	public boolean acknowledgementWanted() {
		return false;
	}

	
	public BigInteger getDestinationId() {
		// TODO Auto-generated method stub
		return null;
	}

	public ManagementEvent getEvent() {
		// should be null!
		return null;
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#setMessageId(int)
	 */
	public void setMessageId(int messageId) {
		this.messageId = messageId;
		
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#setDestinationId(java.math.BigInteger)
	 */
	public void setDestinationId(BigInteger destination) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#setDestinationNode(dks.addr.DKSRef)
	 */
	public void setDestinationNode(DKSRef destinationNode) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#setEvent(dks.niche.hiddenEvents.ManagementEvent)
	 */
	public void setEvent(ManagementEvent event) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#getDestinationNode()
	 */
	public DKSRef getDestinationNode() {
		// TODO Auto-generated method stub
		return null;
	}
	public Object getOriginalMessage() {
		return null;
	}

}
