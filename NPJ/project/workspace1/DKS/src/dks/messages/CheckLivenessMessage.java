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


/**
 * The <code>CheckLivenessMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: CheckLivenessMessage.java 452 2007-11-27 22:13:23Z roberto $
 */
public class CheckLivenessMessage extends Message {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = -6542675002646647572L;

	/**
	 * Message sent to check if a node is alive or not
	 */
	public CheckLivenessMessage() {
	}

}
