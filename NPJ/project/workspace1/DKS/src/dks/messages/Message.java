/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.messages;

import java.io.Serializable;

import dks.addr.DKSRef;

/**
 * The <code>Message</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id:Message.java 98 2006-11-14 15:13:32Z cosmin $
 */
public abstract class Message implements Serializable {

	private transient DKSRef source;

	private byte[] flattenedId;

	private int port;
	
	private Object payload;

	/**
	 * Abstract Class identifying the Messages in the system
	 */
	public Message() {
	}

	 public DKSRef getSource() {
		return source;
	}

	public Message setSource(DKSRef source) {
		this.source = source;
		return this;
	}

	public void flattenNodeRef() {
		this.flattenedId = source.getId().toByteArray();
		this.port = source.getPort();
	}

	public byte[] getFlattenedId() {
		return flattenedId;
	}

	public int getSourcePort() {
		return port;
	}

	public Object getPayload() {
		return payload;
	}
	
	public void setPayload(Object payload) {
		this.payload = payload;
	}
}
