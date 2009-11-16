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
import dks.dht.messages.RemoveAckMessage;

/**
 * The <code>ApplicationRemoveAckEvent</code> class
 *
 * @author Joel
 * @version $Id: ApplicationRemoveAckEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ApplicationRemoveAckEvent extends Event {

	Object result;
	int operationId;
	
	public ApplicationRemoveAckEvent(RemoveAckMessage m) {
		this.result = m.getResult();
		this.operationId = m.getOperationId();
	}
	
	public Object getResult(){
		return result;
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
