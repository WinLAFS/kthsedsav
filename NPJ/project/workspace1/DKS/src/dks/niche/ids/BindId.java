/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.ids;

import java.io.Serializable;

import dks.niche.interfaces.IdentifierInterface;

/**
 * The <code>BindId</code> class
 * 
 * @author Joel
 * @version $Id: BindElement.java 294 2006-05-05 17:14:14Z joel $
 */
public class BindId implements Serializable, IdentifierInterface {

	/**
	 * @serialVersionUID -
	 */
	
	//This is general bookkeeping stuff:
	
	private static final long serialVersionUID = 2227322099838937626L;

	NicheId id;
	protected IdentifierInterface sender, receiver;
	
	protected Object senderSideInterfaceDescription, receiverSideInterfaceDescription;
	
	int type;
	
	
	
	public BindId() {
		
	}
	/**
	 * @param id
	 * @param sender
	 * @param receiver
	 * @param senderSideInterfaceDescription
	 * @param receiverSideInterfaceDescription
	 */
	public BindId(NicheId id, IdentifierInterface sender, IdentifierInterface receiver,
			Object senderSideInterfaceDescription,
			Object receiverSideInterfaceDescription, int type) {
		super();
		this.id = id;
		this.sender = sender;
		this.receiver = receiver;
		this.senderSideInterfaceDescription = senderSideInterfaceDescription;
		this.receiverSideInterfaceDescription = receiverSideInterfaceDescription;
		this.type = type;
		
	}

	/**
	 * @return Returns the id.
	 */
	public NicheId getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(NicheId id) {
		this.id = id;
	}

	/**
	 * @return Returns the sender.
	 */
	public IdentifierInterface getSender() {
		return sender;
	}

	/**
	 * @param sender The sender to set.
	 */
	public void setSender(SNRElement sender) {
		this.sender = sender;
	}

	/**
	 * @return Returns the receiver.
	 */
	public IdentifierInterface getReceiver() {
		return receiver;
	}

	/**
	 * @param receiver The receiver to set.
	 */
	public void setReceiver(IdentifierInterface receiver) {
		this.receiver = receiver;
	}

	/**
	 * @return Returns the senderSideInterfaceDescription.
	 */
	public Object getSenderSideInterfaceDescription() {
		return senderSideInterfaceDescription;
	}

	/**
	 * @param senderSideInterfaceDescription The senderSideInterfaceDescription to set.
	 */
	public void setSenderSideInterfaceDescription(
			Object senderSideInterfaceDescription) {
		this.senderSideInterfaceDescription = senderSideInterfaceDescription;
	}

	/**
	 * @return Returns the receiverSideInterfaceDescription.
	 */
	public Object getReceiverSideInterfaceDescription() {
		return receiverSideInterfaceDescription;
	}

	/**
	 * @param receiverSideInterfaceDescription The receiverSideInterfaceDescription to set.
	 */
	public void setReceiverSideInterfaceDescription(
			Object receiverSideInterfaceDescription) {
		this.receiverSideInterfaceDescription = receiverSideInterfaceDescription;
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


}