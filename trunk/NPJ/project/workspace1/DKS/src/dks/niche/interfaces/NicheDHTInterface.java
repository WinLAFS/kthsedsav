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

/**
 * The <code>NicheDHTInterface</code> class
 *
 * @author Joel
 * @version $Id: NicheDHTInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface NicheDHTInterface {
	/*
	 * Put, get, remove and associated setters go here 
	 */
	public void setDefaultDHTPutAckReceiver(Object ackHandlerObject, String ackHandlerMethod);
	public void setDefaultDHTGetReceiver(Object getHandlerObject, String getHandlerMethod);
	
}
