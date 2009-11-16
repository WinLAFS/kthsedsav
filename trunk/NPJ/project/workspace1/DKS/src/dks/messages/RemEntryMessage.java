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

import static dks.messages.MessageTypeTable.MSG_TYPE_REM_RT_ENTRY;

/**
 * The <code>RemRoutingTableEntryMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RemRoutingTableEntryMessage.java 294 2006-05-05 17:14:14Z
 *          Roberto $
 */
public class RemEntryMessage extends Message {

	private static final long serialVersionUID = 4715929237375575430L;

	public static final int messageType = MSG_TYPE_REM_RT_ENTRY;

	private boolean leaving = false;

	public RemEntryMessage() {
	}
	/**
	 * 
	 */
	public RemEntryMessage(boolean leaving) {
		this.leaving = leaving;
	}

	/**
	 * @return Returns the leaving.
	 */
	public boolean isLeaving() {
		return leaving;
	}

}
