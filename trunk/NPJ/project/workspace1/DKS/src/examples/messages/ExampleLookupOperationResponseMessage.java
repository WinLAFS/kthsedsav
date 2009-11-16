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
 * The <code>ExampleLookupOperationResponseMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: ExampleLookupOperationResponseMessage.java 294 2006-05-05
 *          17:14:14Z Roberto $
 */
public class ExampleLookupOperationResponseMessage extends Message {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = 1L;

	private int operationValue;

	/**
	 * Default constructor, necessary for the marshaler
	 */

	public ExampleLookupOperationResponseMessage() {
	}

	/**
	 * The constructor defined by the user
	 */

	public ExampleLookupOperationResponseMessage(int operationValue) {
		this.operationValue = operationValue;
	}

	/**
	 * @return Returns the operationValue.
	 */
	public int getOperationValue() {
		return operationValue;
	}
}