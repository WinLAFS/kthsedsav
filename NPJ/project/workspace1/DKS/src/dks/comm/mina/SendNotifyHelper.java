/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.comm.mina;

import org.apache.mina.core.future.WriteFuture;

//import org.apache.mina.common.WriteFuture;

/**
 * The <code>SendNotifyHelper</code> class
 *
 * @author Joel
 * @version $Id: SendNotifyHelper.java 294 2006-05-05 17:14:14Z joel $
 */
public class SendNotifyHelper {

	int operationId;
	WriteFuture writeFuture;
	
	public SendNotifyHelper() {
		
	}
	public SendNotifyHelper(int id, WriteFuture writeFuture) {
		this.operationId = id;
		this.writeFuture = writeFuture;
	}
	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return operationId;
	}
	
	/**
	 * @return Returns the attachment.
	 */
	public Object getWriteFuture() {
		return writeFuture;
	}
	
	
}
