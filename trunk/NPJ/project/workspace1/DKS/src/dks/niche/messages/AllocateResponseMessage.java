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
import dks.niche.wrappers.ResourceRef;

/**
 * The <code>AllocateResponseMessage</code> class
 *
 * @author Joel
 * @version $Id: AllocateResponseMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class AllocateResponseMessage extends Message {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 6209645131357660847L;
	
	int operationId;
	ResourceRef resourceRef;
	
	/**
	 * @param operationId
	 * @param resourceId
	 */
	public AllocateResponseMessage(int operationId, ResourceRef resourceRef) {
		this.operationId = operationId;
		this.resourceRef = resourceRef;
	}

	public int getOperationId() {
		return operationId;
	}

	public void setOperationId(int operationId) {
		this.operationId = operationId;
	}

	public ResourceRef getResourceRef() {
		return resourceRef;
	}

	public void setResourceRef(ResourceRef resourceRef) {
		this.resourceRef = resourceRef;
	}
	
	
	
	
}
