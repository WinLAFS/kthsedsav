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

import java.math.BigInteger;

/**
 * The <code>NicheOneToOneCommunicationInterface</code> class
 *
 * @author Joel
 * @version $Id: NicheOneToOneCommunicationInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface NicheOneToOneCommunicationInterface {
	public void setDefaultSendAckReceiver(Object senderObject, String handlerMethod);
	public void setDefaultSendReceiver(Object receiverObject, String handlerMethod);
	public void registerReceiver(Object receiverObject, String receiverMethod);
	public void unregisterReceiver(Object receiverObject, String receiverMethod);
	public Object send(BigInteger id, Object message, String receiverHandlerClass, String receiverHandlerMethod);
	public void asynchronousSend(BigInteger id, Object message, String receiverHandlerClass, String receiverHandlerMethod);
	public void asynchronousSend(BigInteger id, Object message, String receiverHandlerClass, String receiverHandlerMethod, Object responseHandlerObject, String responseHandlerMethod);
	
}
