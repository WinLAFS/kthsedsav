/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.ring;

/**
 * The <code>IdentifierAlreadyTakenException</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: IdentifierAlreadyTakenException.java 294 2006-05-05 17:14:14Z
 *          Roberto $
 */
public class IdentifierAlreadyTakenException extends Exception {

	/**
	 * @serialVersionUID -
	 */
	static final long serialVersionUID = -1563486949509896216L;
	/**
	 * 
	 */
	public IdentifierAlreadyTakenException() {
	}

	/**
	 * @param arg0
	 */
	public IdentifierAlreadyTakenException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public IdentifierAlreadyTakenException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public IdentifierAlreadyTakenException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
