/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.fd.messsages;

import dks.messages.Message;

/**
 * The <code>Ping</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: Ping.java 294 2006-05-05 17:14:14Z roberto $
 */
public class Ping extends Message {

	private static final long serialVersionUID = 2547205617632336021L;

	/**
	 * @param pingNumber
	 */
	public Ping() {
	}

}
