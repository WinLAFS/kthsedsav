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
 * The <code>ExecutorInterface</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: ExecutorInterface.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public interface ExecutorInterface  {

	public DelegationRequestMessage getActuatorMessage(NicheId id, ComponentId cid); 
	public DelegationRequestMessage getActuatorMessage(NicheId id, ComponentId cid, String actuatorComponentName); 
	
	public NicheId getId();

}
