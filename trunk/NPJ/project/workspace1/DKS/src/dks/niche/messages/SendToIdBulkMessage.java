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
import java.util.ArrayList;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.niche.hiddenEvents.ManagementEvent;
import dks.niche.interfaces.NicheMessageInterface;

/**
 * The <code>SendToIdBulkMessage</code> class
 *
 * @author Joel
 * @version $Id: SendToIdBulkMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class SendToIdBulkMessage extends Message implements NicheMessageInterface {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 4010186135729061114L;
	ArrayList<SendToIdMessage> sendToIdMessages;
	int messageId;
	BigInteger destinationId;
	
	public SendToIdBulkMessage() {
		
	}

	public SendToIdBulkMessage(int messageId, BigInteger destinationId, ArrayList<SendToIdMessage> sendToIdMessages) {
		this.messageId = messageId;
		this.destinationId = destinationId;
		this.sendToIdMessages = sendToIdMessages;
	}
	public ArrayList<SendToIdMessage> getMessages() {
		return sendToIdMessages;
	}

	public boolean acknowledgementWanted() {
		return false;
	}

	public BigInteger getDestinationId() {
		return destinationId;
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#getEvent()
	 */
	public ManagementEvent getEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#getMessageId()
	 */
	public int getMessageId() {
		return messageId;
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#setMessageId(int)
	 */
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
	 * @see dks.niche.interfaces.NicheMessageInterface#setDestinationId(java.math.BigInteger)
	 */
	public void setDestinationId(BigInteger destination) {
		this.destinationId = destination;
		
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
	 * @see dks.niche.interfaces.NicheMessageInterface#getOriginalMessage()
	 */
	@Override
	public Object getOriginalMessage() {
		// TODO Auto-generated method stub
		return null;
	}
}
