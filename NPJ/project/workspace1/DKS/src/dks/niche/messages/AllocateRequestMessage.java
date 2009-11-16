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

import dks.messages.Message;
import dks.niche.wrappers.NodeRef;

/**
 * The <code>AllocateRequestMessage</code> class
 *
 * @author Joel
 * @version $Id: AllocateRequestMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class AllocateRequestMessage extends Message {
	
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 8872410402589304301L;
	
	int operationId;
	NodeRef resourceToBeUsed;
	Object description;
	String owner;
	
	/**
	 * @param operationId
	 * @param resourceToBeUsed
	 * @param description
	 */
	public AllocateRequestMessage(int operationId, NodeRef resourceToBeUsed, Object description, String owner) {
		this.operationId = operationId;
		this.resourceToBeUsed = resourceToBeUsed;
		this.description = description;
		this.owner = owner;
		
	}

	public Object getDescription() {
		return description;
	}

	public void setDescription(Object description) {
		this.description = description;
	}

	public int getOperationId() {
		return operationId;
	}

	public void setOperationId(int operationId) {
		this.operationId = operationId;
	}

	public NodeRef getNodeRef() {
		return resourceToBeUsed;
	}

	public void setNodeRef(NodeRef resourceToBeUsed) {
		this.resourceToBeUsed = resourceToBeUsed;
	}
	public String getOwner() {
		return owner;
	}
	
	
}
