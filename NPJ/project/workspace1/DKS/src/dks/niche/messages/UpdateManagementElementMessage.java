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
import java.util.ArrayList;

import dks.messages.Message;
import dks.niche.NicheMessageTable;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.ReplicableMessageInterface;
import dks.niche.wrappers.NodeRef;

/**
 * The <code>UpdateManagementElementMessage</code> class
 *
 * @author Joel
 * @version $Id: UpdateManagementElementMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class UpdateManagementElementMessage extends Message implements Serializable, ReplicableMessageInterface {

/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -5464861783066673069L;

public final static int messageType = NicheMessageTable.MSG_TYPE_UPDATE_MANAGEMENT_ELEMENT_MESSAGE + NicheMessageTable.INTERVAL_STARTING;
	
	public final static int TYPE_ADD_SOURCE = 0;
	public final static int TYPE_REMOVE_SOURCE = 1;
	public final static int TYPE_ADD_SINK = 2;
	public final static int TYPE_REMOVE_SINK = 3;
	public final static int TYPE_REMOVE_BINDING = 4;
	
	public final static int TYPE_UPDATE_PARAMETERS = 5;
	
	int replicaNumber;
	BigInteger destinationRingId;
	NicheId destination;
	boolean bulkOperation;
	
	int type;
	
	Object thing;
	ArrayList things; //no typechecking for now. rebuild, if desired later...
	private Serializable[]parameters;
	
	//Object reference; ArrayList references;	Object watcher; ArrayList watchers;
	
	public UpdateManagementElementMessage() {
		
	}
	
	public UpdateManagementElementMessage(UpdateManagementElementMessage parent) {
		this.bulkOperation = parent.bulkOperation;
		this.destination = parent.destination;
		this.parameters = parent.parameters;
		this.thing = parent.thing;
		this.things = parent.things;
		this.type = parent.type;
	}

	public UpdateManagementElementMessage(NicheId id, int type, ArrayList things) {
		this.destination = id;
		this.bulkOperation = true;
		this.type = type;
		this.things = things;
		
	}
	
	public UpdateManagementElementMessage(NicheId id, int type, Object thing) {
		this.destination = id;
		this.bulkOperation = false;
		this.type = type; 
		if(thing instanceof Serializable[]) {
			parameters = (Serializable[]) thing;
		} else  {
			this.thing = thing;
		}
		
	}

	/**
	 * @return Returns the bulkOperation.
	 */
	public boolean isBulkOperation() {
		return bulkOperation;
	}

	/**
	 * @param bulkOperation The bulkOperation to set.
	 */
	public void setBulkOperation(boolean bulkOperation) {
		this.bulkOperation = bulkOperation;
	}

	/**
	 * @return Returns the thing.
	 */
	public Object getReference() {
		return thing;
	}

	/**
	 * @param thing The thing to set.
	 */
	public void setReference(Object thing) {
		this.thing = thing;
	}

	/**
	 * @return Returns the things.
	 */
	public ArrayList getReferences() {
		return things;
	}

	/**
	 * @param things The things to set.
	 */
	public void setReferences(ArrayList things) {
		this.things = things;
	}

	public Serializable[] getParameters() {
		return parameters;		
	}
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}

	
	/**
	 * @return Returns the destination.
	 */
	public NicheId getDestination() {
		return destination;
	}

	/**
	 * @param destination The destination to set.
	 */
	public void setDestination(NicheId destination) {
		this.destination = destination;
	}

	
	public BigInteger getDestinationRingId() {
		return destinationRingId;
	}

	public Message getReplicaCopy(int replicaNumber, BigInteger destinationRingId) {
		return new UpdateManagementElementMessage(this).setReplicaInformation(replicaNumber, destinationRingId);
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


}
