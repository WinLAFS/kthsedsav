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

import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.wrappers.Subscription;

/**
 * The <code>WatcherInterface</code> class
 *
 * @version $Id: WatcherInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface WatcherInterface  {
	
//	public void addSink(Subscription ehs);
//	public void removeSink(Subscription ehs);

	public DelegationRequestMessage getSensorMessage(NicheId id, ComponentId cid); 
	public DelegationRequestMessage getSensorMessage(NicheId id, ComponentId cid, String sensedComponentName); 
	
	public NicheId getId();
	//OBS, important, at the time of the creation of the subscription,
	//all sensor-info needs to be in place in the watcher... 
	//public boolean sensorCollocation();


}
