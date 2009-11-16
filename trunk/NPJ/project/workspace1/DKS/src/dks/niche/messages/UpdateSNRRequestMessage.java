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

/**
 * The <code>CreateGroupRequest</code> class
 *
 * @author Joel
 * @version $Id: UpdateSNRRequestMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class UpdateSNRRequestMessage extends Message implements Serializable, ReplicableMessageInterface {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -3409155999980890912L;

	public final static int messageType = NicheMessageTable.MSG_TYPE_UPDATE_SNR_REQUEST_MESSAGE + NicheMessageTable.INTERVAL_STARTING;
	
	public final static int TYPE_ADD_REFERENCE = 0;
	public final static int TYPE_ADD_REFERENCE_AND_START = 666;
	
	public final static int TYPE_REMOVE_REFERENCE = 1;
	public final static int TYPE_ADD_WATCHER = 2;
	public final static int TYPE_REMOVE_WATCHER = 3;
	public final static int TYPE_ADD_SERVER_BINDING = 4;
	public final static int TYPE_ADD_CLIENT_BINDING = 5;
	public final static int TYPE_REMOVE_BINDING= 6;

	public final static int TYPE_REMOVE_GROUP = 7;
	public final static int TYPE_UNDEPLOY_COMPONENT = 8;
	
	public final static int TYPE_ADD_EXECUTOR = 9;
	public final static int TYPE_REMOVE_EXECUTOR = 10;
	
	NicheId destination;
	BigInteger destinationRingId;
	boolean bulkOperation;
	
	int type;
	int replicaNumber;
	
	IdentifierInterface reference;
	ArrayList references; //no typechecking for now. rebuild, if desired later...
	String touchpointClassName;
	String touchpointEventClassName;
	Serializable[] touchpointParameters;
	
	//Object reference; ArrayList references;	Object watcher; ArrayList watchers;
	
	public UpdateSNRRequestMessage() {
		
	}

	public UpdateSNRRequestMessage(UpdateSNRRequestMessage parent) {
		this.bulkOperation = parent.bulkOperation;
		this.destination = parent.destination;
		this.touchpointParameters = parent.touchpointParameters;
		this.reference =  parent.reference;
		this.references =  parent.references;
		this.touchpointClassName = parent.touchpointClassName;
		this.touchpointEventClassName = parent.touchpointEventClassName;
		this.type = parent.type;
		//set or not?
		//this.destinationRingId = parent.destinationRingId;
		//this.replicaNumber = parent.replicaNumber 
	}

	public UpdateSNRRequestMessage(NicheId id, int type, ArrayList things) {
		this.destination = id;
		this.bulkOperation = true;
		this.type = type;
		this.references = things;
		
	}
	
	public UpdateSNRRequestMessage(NicheId id, int type, IdentifierInterface reference) {
		this.destination = id;
		this.bulkOperation = false;
		this.type = type;
		this.reference = reference;
		
	}
	
	public UpdateSNRRequestMessage(NicheId id, IdentifierInterface thing, String sensorClassName, Serializable[]sensorParameters) {
		this(id, TYPE_ADD_WATCHER, thing, sensorClassName, sensorParameters );
	}
	
	public UpdateSNRRequestMessage(NicheId id, int type, IdentifierInterface thing, String touchpointClassName, Serializable[]touchpointParameters) {
		if(type != TYPE_ADD_WATCHER && type != TYPE_ADD_EXECUTOR) {
			throw new RuntimeException("Unsupported Type");
		}
		
		this.destination = id;
		this.bulkOperation = false;
		this.type = type;
		this.reference = thing;
		this.touchpointClassName = touchpointClassName;
		this.touchpointParameters = touchpointParameters;
	}
	
	
//	public UpdateSNRRequestMessage(NicheId id, IdentifierInterface binding, Object[]parameters) {
//		this.destination = id;
//		this.reference = binding;
//		this.bulkOperation = false;
//		this.type = TYPE_ADD_CLIENT_BINDING;
//		this.parameters = parameters;
//		
//	}
	
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
	public IdentifierInterface getReference() {
		return reference;
	}

	/**
	 * @param thing The thing to set.
	 */
	public void setReference(IdentifierInterface thing) {
		this.reference = thing;
	}

	/**
	 * @return Returns the things.
	 */
	public ArrayList getReferences() {
		return references;
	}

	/**
	 * @param things The things to set.
	 */
	public void setReferences(ArrayList things) {
		this.references = things;
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

	
	public String getTouchpointClassName() {
		return touchpointClassName;
	}

	public void setTouchpointClassName(String touchpointClassName) {
		this.touchpointClassName = touchpointClassName;
	}
	
	public String getTouchpointEventClassName() {
		return touchpointEventClassName;
	}

	public void setTouchpointEventClassName(String touchpointEventClassName) {
		this.touchpointEventClassName = touchpointEventClassName;
	}

	public Serializable[] getTouchpointParameters() {
		if(type == TYPE_ADD_WATCHER || type == TYPE_ADD_EXECUTOR) {
			return touchpointParameters;
		}
		return null;
	}

	public void setTouchpointParameters(Serializable[] touchpointParameters) {
		this.touchpointParameters = touchpointParameters;
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ReplicableMessageInterface#getDestinationRingId()
	 */
	public BigInteger getDestinationRingId() {
		return destinationRingId;
	}

	public Message setReplicaInformation(int replicaNumber, BigInteger destinationRingId) {
		this.replicaNumber = replicaNumber;
		this.destinationRingId = destinationRingId;
		return this;
	}

public Message getReplicaCopy(int replicaNumber, BigInteger destinationRingId) {
		return new UpdateSNRRequestMessage(this).setReplicaInformation(replicaNumber, destinationRingId);
	}

public int getReplicaNumber() {
	return replicaNumber;
}

public void setReplicaNumber(int replicaNumber) {
	this.replicaNumber = replicaNumber;
}

	


}
