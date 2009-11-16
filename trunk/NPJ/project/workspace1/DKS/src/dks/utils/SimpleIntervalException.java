/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.utils;

/**
 * The <code>SimpleIntervalException</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: SimpleIntervalException.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class SimpleIntervalException extends Exception {

	/**
	 * 
	 */
	public SimpleIntervalException() {
		super();
	}

	/**
	 * @param message
	 */
	public SimpleIntervalException(String message) {
		super(message);
	}

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -8446566233547719178L;

}
