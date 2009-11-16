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
 * The <code>RingIdentifierAlreadyTakenMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingIdentifierAlreadyTakenMessage.java 294 2006-05-05 17:14:14Z
 *          Roberto $
 */
public class RingIdentifierAlreadyTakenMessage extends Message {

	private static final long serialVersionUID = -2133648766268186861L;
	
	/**
	 * Message sent when the identifier selected is already taken by another
	 * peer
	 */
	public RingIdentifierAlreadyTakenMessage() {
	}

}
