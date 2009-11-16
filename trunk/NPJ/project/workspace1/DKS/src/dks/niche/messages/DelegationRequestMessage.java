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
import dks.niche.ids.ComponentElement;
import dks.niche.ids.GroupElement;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.ManagementElementInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.interfaces.ReplicableMessageInterface;
import dks.niche.wrappers.ManagementDeployParameters;

/**
 * The <code>DelegationRequestMessage</code> class
 * 
 * @author Joel
 * @version $Id: DelegationRequestMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class DelegationRequestMessage extends Message implements Serializable, ReplicableMessageInterface, IdentifierInterface {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = -2059262569046926715L;

	// public final static int messageType =
	// NicheMessageTable.MSG_TYPE_DELEGATION_REQUEST_MESSAGE +
	// NicheMessageTable.INTERVAL_STARTING;
	public final static int TYPE_SENSOR = -1;

	public final static int TYPE_WATCHER = 1;

	public final static int TYPE_AGGREGATOR = 2;

	public final static int TYPE_MANAGER = 3;

	public final static int TYPE_BINDING = 4;

	public final static int TYPE_COMPONENT_ID = 5;
	
	public final static int TYPE_GROUP_ID = 6;
	//public final static int TYPE_SNR = 5;

	// public final static int TYPE_COMPONENT_SNR = 6;

	public final static int TYPE_FRACTAL_MANAGER = 33;

	public final static int TYPE_BULK = 42;

	//public final static int LIVE_FLAG = 42;

	NicheId destination;

	int type;

	String className;

	int mode;

	//Object theLiveObject;

	ManagementDeployParameters params;

	// ArrayList<SNRElement> references;

	Serializable[] parameters;
	
	//	Serializable[] infrastructureParameters;
//
//	Serializable[] applicationParameters;

	ArrayList bulk;

	int replicaNumber;
	BigInteger destinationRingId;

	private Object initiator;
	/*
	 * remember empty constructor
	 */
	public DelegationRequestMessage() {
	}

	/**
	 * @param eventName
	 * @param methodName
	 * @param eventHandlerClassInstance
	 */
	public DelegationRequestMessage(NicheId destination, int type,
			String meClassName, Serializable[] parameters) {

		this.destination = destination;
		this.type = type;
		this.className = meClassName;

		this.parameters = parameters;
		//this.applicationParameters = applicationParameters;

	}

	
	public DelegationRequestMessage(NicheId destination, int type, Serializable parameterBlob) {

			this.destination = destination;
			this.type = type;
			this.replicaNumber = -1; //to indicate it must be set later
			switch (type) {
				case TYPE_SENSOR:
					if(!(parameterBlob instanceof Serializable[])) {
						this.parameters = new Serializable[]{parameterBlob};
					} else {
						this.parameters = (Serializable[])parameterBlob;
					}
					break;
				case TYPE_BULK:
					this.bulk = (ArrayList<Serializable>)parameterBlob;
					break;
				case TYPE_BINDING:
				
				default:
					this.params = (ManagementDeployParameters) parameterBlob;
			}
			
			
		}

	
	
	public DelegationRequestMessage(NicheId destination, String creatorClassName, Serializable[] parameterBlob) {
	//public DelegationRequestMessage(NicheId destination, int type, Object creatorClass, Object parameterBlob) {

		this.destination = destination;
		//this.type = type;
		this.replicaNumber = -1; //to indicate it must be set later
		
		this.parameters = (Serializable[]) parameterBlob;
		// TODO: check this for consistency
		this.className = creatorClassName;
		if(className.equals(GroupElement.class.getName())) {
			type = TYPE_GROUP_ID;
		} else if(className.equals(ComponentElement.class.getName())) {
			type = TYPE_COMPONENT_ID;
		}

	}

	public DelegationRequestMessage(DelegationRequestMessage parent) {

		this.parameters = parent.parameters;
		this.bulk = parent.bulk;
		this.className = parent.className;
		this.destination = parent.destination;
		this.parameters = parent.parameters;
		this.params = parent.params;
		this.type = parent.type;
		
	}
	
	public DelegationRequestMessage(NicheId destination, ArrayList bulk) {

		this.destination = destination;
		this.type = TYPE_BULK;
		this.bulk = bulk;

	}



	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 *            The className to set.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return Returns the destination.
	 */
	public NicheId getDestination() {
		return destination;
	}

	/**
	 * @param destination
	 *            The destination to set.
	 */
	public void setDestination(NicheId destination) {
		this.destination = destination;
	}

	/**
	 * @return Returns the infrastructureParameters.
	 */
	public Serializable[] getParameters() {
		return parameters;
	}

	/**
	 * @param infrastructureParameters
	 *            The infrastructureParameters to set.
	 */
	public void setParameters(Serializable[] infrastructureParameters) {
		this.parameters = infrastructureParameters;
	}

	public void setInitiator(Object initiator) {
		this.initiator = initiator;
	}
	public NicheNotifyInterface getInitiator() {
		return (NicheNotifyInterface)initiator;
	}
	
	public ManagementDeployParameters getManagementDeployParameters() {
		return params;
	}

	// public ArrayList<SNRElement> getReferences() {
	// return references;
	// }
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 *            The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return Returns the bulk.
	 */
	public ArrayList getBulk() {
		return bulk;
	}

	public boolean isLive() {
		return mode != ManagementElementInterface.NEW;
	}
	public int getMode() {
		return mode;
	}
	/**
	 * @param bulk
	 *            The bulk to set.
	 */
	public void setBulk(ArrayList<DelegationRequestMessage> bulk) {
		this.bulk = bulk;
	}

	public int getReplicaNumber() {
		return replicaNumber;
	}
	public BigInteger getDestinationRingId() {
		return destinationRingId;
	}

	public Message getReplicaCopy(int replicaNumber, BigInteger destinationRingId) {
		return new DelegationRequestMessage(this).setReplicaInformation(replicaNumber, destinationRingId);
	}

	public Message setReplicaInformation(int replicaNumber, BigInteger destinationRingId) {
		this.replicaNumber = replicaNumber;
		this.destinationRingId = destinationRingId;
		return this;
	}
	public Message getLiveCopy(int replicaNumber, int mode, BigInteger destinationRingId) {
		return new DelegationRequestMessage(this).setLiveInformation(replicaNumber, mode, destinationRingId);
	}
	
	public Message setLiveInformation(int replicaNumber, int mode, BigInteger destinationRingId) {
		this.replicaNumber = replicaNumber;
		this.destinationRingId = destinationRingId;
		this.mode = mode;
		return this;
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.IdentifierInterface#getId()
	 */
	@Override
	public NicheId getId() {
		return destination;
	}

	
}

