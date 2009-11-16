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

import dks.arch.Event;
import dks.messages.Message;
import dks.niche.NicheMessageTable;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.ReplicableMessageInterface;

/**
 * The <code>SendToIdMessage</code> class
 *
 * @author Joel H
 * @version $Id: SendToIdMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class DeliverEventMessage extends Message implements Serializable, ReplicableMessageInterface {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -5865885073598770829L;

	int replicaNumber;
	BigInteger destinationRingId;
	NicheId destination;
	Serializable event;
	
	public DeliverEventMessage() {
		
	}
	public DeliverEventMessage(NicheId destination, Serializable event) {
		this.destination = destination ;
		this.event = event;
	}
	public DeliverEventMessage(DeliverEventMessage parent) {
		this.destination = parent.destination;
		this.event = parent.event; //assume not mutable...
	}

		
	

	public NicheId getDestination() {
		return destination;
	}


	public void setDestination(NicheId destination) {
		this.destination = destination;
	}

	public Serializable getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}




	public BigInteger getDestinationRingId() {
		return destinationRingId;
	}




	public Message getReplicaCopy(int replicaNumber, BigInteger destinationRingId) {
		return new DeliverEventMessage(this).setReplicaInformation(replicaNumber, destinationRingId);
	}


	public Message setReplicaInformation(int replicaNumber, BigInteger destinationRingId) {
		this.replicaNumber = replicaNumber;
		this.destinationRingId = destinationRingId;
		return this;
	}
	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ReplicableMessageInterface#getReplicaNumber()
	 */
	public int getReplicaNumber() {
		return replicaNumber;
	}

	
	
}
