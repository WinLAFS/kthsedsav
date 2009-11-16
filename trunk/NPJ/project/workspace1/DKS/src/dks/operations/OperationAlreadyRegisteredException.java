/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.operations;

/**
 * The <code>OperationAlreadyRegisteredException</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: OperationAlreadyRegisteredException.java 294 2006-05-05
 *          17:14:14Z Roberto $
 */
public class OperationAlreadyRegisteredException extends Exception {

	/**
	 * @serialVersionUID -
	 */
	static final long serialVersionUID = -2654015068568712687L;

	/**
	 * 
	 */
	public OperationAlreadyRegisteredException() {
	}

	/**
	 * @param arg0
	 */
	public OperationAlreadyRegisteredException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public OperationAlreadyRegisteredException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public OperationAlreadyRegisteredException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
