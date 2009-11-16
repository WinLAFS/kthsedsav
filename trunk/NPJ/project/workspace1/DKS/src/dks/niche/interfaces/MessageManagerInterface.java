/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.interfaces;

import dks.messages.Message;

/**
 * The <code>MessageManagerInterface</code> class
 *
 * @author Joel
 * @version $Id: MessageManagerInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface MessageManagerInterface extends Runnable {

	public final int SUCCESSFULLY_SENT = -2;
	public final int RETURN_VALUE = -4;
	//public final int NODE_ERROR = 8;
	public final int CHANNEL_ERROR = -8;
	public final int ID_ERROR = -16;
	
	public boolean invokeOnIdError();
	//public boolean invokeOnNodeError();
	public boolean invokeOnChannelError();
	public boolean invokeOnSendSuccess();
	public boolean dropMessage();
	
	public String getMessageManagerId();
	public void notify(int statusCode, Object object);
	//will be true for at least bind-send-managers
	
}
