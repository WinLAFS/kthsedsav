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

import dks.utils.IntervalsList;

/**
 * The <code>NicheBroadcastInterface</code> class
 *
 * @author Joel
 * @version $Id: NicheBroadcastInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface NicheBroadcastInterface {
	
	public void setDefaultBroadcastResultReceiver(Object resultHandlerObject, String resultHandlerMethod);
	public void setDefaultBroadcastReceiver(Object receiverObject, String handlerMethod);
	public void registerBroadcastReceiver(Object receiver, String handlerMethod);
	public void unregisterBroadcastReceiver(Object receiverObject, String handlerMethod);
	public Object broadcast(Object message, String intervalStart, String intervalEnd, boolean reliable, boolean aggregate, Integer timeout, String remoteSideHandlerClass, String remoteSideHandlerMethod);
	public Object broadcast(Object message, IntervalsList receivers, boolean reliable, boolean aggregate, Integer timeout, String remoteSideHandlerClass, String remoteSideHandlerMethod);
	public void asynchronousBroadcast(Object message, String intervalStart, String intervalEnd, boolean reliable, boolean aggregate, Integer timeout, String remoteSideHandlerClass, String remoteSideHandlerMethod, Object localSideHandlerObject, String localSideHandlerMethod);
	public void asynchronousBroadcast(Object message, IntervalsList receivers, boolean reliable, boolean aggregate, Integer timeout, String remoteSideHandlerClass, String remoteSideHandlerMethod, Object localSideHandlerObject, String localSideHandlerMethod);	
}
