/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.wrappers;

import java.io.Serializable;

/**
 * The <code>BroadcastContent</code> class
 *
 * @author Joel
 * @version $Id: BroadcastContent.java 294 2006-05-05 17:14:14Z joel $
 */
public class BroadcastContent implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 1193049583263733755L;
	int operationId;
	Object content;
	String handlerId;
	/**
	 * @param operationId
	 * @param content
	 */
	public BroadcastContent(int operationId, Object content, String handlerId) {
		this.operationId = operationId;
		this.content = content;
		this.handlerId = handlerId;
	}
	
	public BroadcastContent(BroadcastContent source, Object content) {
		this.operationId = source.getOperationId();
		this.handlerId = source.getHandlerId();
		this.content = content;
	}
	
	/**
	 * @return Returns the content.
	 */
	public Object getContent() {
		return content;
	}
	/**
	 * @param content The content to set.
	 */
	public void setContent(Object content) {
		this.content = content;
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

	/**
	 * @return Returns the eventName.
	 */
	public String getHandlerId() {
		return handlerId;
	}

	/**
	 * @param eventName The eventName to set.
	 */
	public void setHandlerId(String handlerId) {
		this.handlerId = handlerId;
	}
	
	
}
