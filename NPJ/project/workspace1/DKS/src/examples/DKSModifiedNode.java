/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package examples;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.bcast.PseudoReliableIntervalBroadcastComponent;
import dks.bcast.SimpleIntervalBroadcastComponent;
import dks.boot.DKSNode;
import dks.dht.DHTComponent;

/**
 * The <code>DKSModifiedNode</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DKSModifiedNode.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class DKSModifiedNode extends DKSNode {

	/**
	 * @param myRef
	 * @param dksParameters
	 * @param webcacheAddress
	 */
	public DKSModifiedNode(DKSRef myRef, DKSParameters dksParameters, String webcacheAddress) {
		super(myRef, dksParameters, webcacheAddress);
		
		new SimpleIntervalBroadcastComponent(scheduler, registry);
		
		new PseudoReliableIntervalBroadcastComponent(scheduler, registry);

		new DHTComponent(scheduler, registry);
		
	}

	
}
