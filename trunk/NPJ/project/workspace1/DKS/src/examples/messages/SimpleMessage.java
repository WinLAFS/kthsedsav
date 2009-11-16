/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package examples.messages;

import dks.messages.Message;

/**
 * The <code>SimpleMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: SimpleMessage.java 496 2007-12-20 15:39:02Z roberto $
 */
public class SimpleMessage extends Message {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = 1L;

	private int carriedValue;

	/**
	 * Default constructor, necessary for the marshaler
	 */
	public SimpleMessage() {
	}

	/**
	 * The constructor defined by the user
	 */
	public SimpleMessage(int value) {
		this.carriedValue = value;
	}

	/**
	 * @return Returns the carried Value.
	 */
	public int getCarriedValue() {
		return carriedValue;
	}

}