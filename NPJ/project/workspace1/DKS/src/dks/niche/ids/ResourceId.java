/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */

package dks.niche.ids;

import dks.addr.DKSRef;
import dks.niche.wrappers.NodeRef;

/**
 * The <code>ResourceId</code> class
 *
 * @author Joel
 * @author Ahmad
 * @version $Id: ResourceId.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public interface ResourceId {
	
	
	
	public Object getJadeNode();
	
	public NodeRef getNodeRef();
	
	public DKSRef getDKSRef();

	public int getOperationId();
	
	public int getTotalStorage();

	public int getAllocatedStorage();
	
	
}
