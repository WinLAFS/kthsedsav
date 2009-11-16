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

import dks.messages.Message;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.ReplicableMessageInterface;

/**
 * The <code>GetReferenceMessage</code> class
 *
 * @author Joel
 * @version $Id: GetReferenceMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class GetReferenceMessage extends Message implements ReplicableMessageInterface {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 1384444797061054153L;

	BigInteger destinationRingId;
	int replicaNumber;
	
	NicheId id;
	Serializable[] references;
	
	public static final int GET_ALL = 0;
	public static final int GET_ANY = 1;
	
	int type;
	
	public GetReferenceMessage(NicheId id, int type) {
		this.id = id;
		this.type = type;
	}
	
	public NicheId getId () {
		return id;
	}

	public NicheId getDestination () {
		return id;
	}
	public int getType() {
		return type;
	}
	public void setReferences(Serializable[] refs) {
		this.references = refs;
	}
	
	public Serializable[] getReferences() {
		return references;
	}

	public BigInteger getDestinationRingId() {
		return destinationRingId;
	}

	public Message getReplicaCopy(int replicaNumber, BigInteger destinationRingId) {
		return new GetReferenceMessage(id, type).setReplicaInformation(replicaNumber, destinationRingId);
	}

	public Message setReplicaInformation(int replicaNumber, BigInteger destinationRingId) {
		this.replicaNumber = replicaNumber;
		this.destinationRingId = destinationRingId;
		return this;
	}

	public int getReplicaNumber() {
		return replicaNumber;
	}

	public void setReplicaNumber(int replicaNumber) {
		this.replicaNumber = replicaNumber;
	}

	public void setDestinationRingId(BigInteger destinationRingId) {
		this.destinationRingId = destinationRingId;
	}
}
