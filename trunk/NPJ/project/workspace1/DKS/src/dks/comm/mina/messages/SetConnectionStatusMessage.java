/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.comm.mina.messages;

import dks.messages.Message;

/**
 * The <code>SetConnectionStatusMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: SetConnectionStatusMessage.java 294 2006-05-05 17:14:14Z
 *          roberto $
 */
public class SetConnectionStatusMessage extends Message {

	private static final long serialVersionUID = -4436684473802527907L;

	private boolean permanent = false;

	public SetConnectionStatusMessage(boolean permanent) {
		super();
		this.permanent = permanent;
	}

	public boolean isPermanent() {
		return permanent;
	}

}
