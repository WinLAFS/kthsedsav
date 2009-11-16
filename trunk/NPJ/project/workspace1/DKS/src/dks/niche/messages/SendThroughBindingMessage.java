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

import dks.messages.Message;
import dks.niche.NicheMessageTable;
import dks.niche.ids.BindElement;
import dks.niche.ids.BindId;
import dks.niche.ids.NicheId;

/**
 * The <code>NicheCommunicationMessage</code> class
 *
 * @author Joel
 * @version $Id: SendThroughBindingMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class SendThroughBindingMessage extends Message implements Serializable{

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 2176348506744799680L;

												
	NicheId finalReceiver;
	BindId bindId;
	
	Object message;
	int operationId;
		
	/*
	 * Remember the empty constructor...
	 */
	public SendThroughBindingMessage() {
	}
	
	
	/**
	 * @param globalBindId
	 * @param message
	 */
	public SendThroughBindingMessage(NicheId finalReceiver, BindId bindId, Object message, int operationId) {
		this.finalReceiver = finalReceiver;
		this.bindId = bindId;
		this.message = message;
		this.operationId = operationId;
	}
	
	public BindId getBindInfo() {
		return bindId;
	}

	public NicheId getDestination() {
		return finalReceiver;
	}
	public void setBindId(BindId bindId) {
		this.bindId = bindId;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}
	public int getOperationId() {
		return operationId;
	}
	
}
