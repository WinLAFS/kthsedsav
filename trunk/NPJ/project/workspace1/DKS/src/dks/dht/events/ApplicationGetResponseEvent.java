/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.dht.events;

import dks.arch.Event;
import dks.dht.messages.GetResponseMessage;

/**
 * The <code>ApplicationGetResponseEvent</code> class
 *
 * @author Joel
 * @version $Id: ApplicationGetResponseEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ApplicationGetResponseEvent extends Event {

	Object result;
	int operationId;

	/**
	 * @param result
	 */
	public ApplicationGetResponseEvent(int operationId, Object result) {
		this.operationId = operationId;
		this.result = result;
	}
	
	public ApplicationGetResponseEvent(GetResponseMessage grm, Object result) {
		this.operationId = grm.getOperationId();
		this.result = result;
	}

	/**
	 * @return Returns the result.
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * @param result The result to set.
	 */
	public void setResult(Object result) {
		this.result = result;
	}

	/**
	 * @return Returns the operationId.
	 */
	public int getOperationId() {
		return operationId;
	}

	/**
	 * @param operationId The operationId to set.
	 */
	public void setOperationId(int operationId) {
		this.operationId = operationId;
	}
	
}
