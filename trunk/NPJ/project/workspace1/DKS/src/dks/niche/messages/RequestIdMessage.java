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
import dks.niche.hiddenEvents.ManagementEvent;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.NicheMessageInterface;
import dks.niche.interfaces.NicheNotifyInterface;

/**
 * The <code>RequestIdMessage</code> class
 *
 * @author Joel
 * @version $Id: RequestIdMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class RequestIdMessage extends Message implements NicheMessageInterface {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -713830037644260070L;
	private NicheId originalId;
	private NicheNotifyInterface initiator;
	private int operationId;
	private BigInteger destinationId;
	
	public RequestIdMessage() {
		
	}
	
	public RequestIdMessage(Object originalId, IdentifierInterface initiator) {
		this.originalId = (NicheId)originalId;
		this.initiator = (NicheNotifyInterface)initiator;
	}
	
	public int getOperationId() {
		return operationId;
	}
	public int getMessageId() {
		return operationId;
	}
	public NicheId getResponse(BigInteger ringId) {
		return originalId.setLocation(ringId.toString());
	}
	public NicheNotifyInterface getInitiator() {
		return initiator;
	}

	public boolean acknowledgementWanted() {
		return true;
	}

	public BigInteger getDestinationId() {
		return new BigInteger(originalId.getLocation());
	}

	public ManagementEvent getEvent() {
		return null; //should be null!
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheMessageInterface#setMessageId(int)
	 */
	public void setMessageId(int messageId) {
		operationId = messageId;
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
	 * @see dks.niche.interfaces.NicheMessageInterface#getDestinationNode()
	 */
	public DKSRef getDestinationNode() {
		// TODO Auto-generated method stub
		return null;
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
