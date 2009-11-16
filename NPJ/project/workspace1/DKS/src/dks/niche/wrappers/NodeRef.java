/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.wrappers;

import java.io.Serializable;

import dks.addr.DKSRef;
import dks.niche.ids.ResourceId;

/**
 * The <code>NodeRef</code> class
 *
 * @author Joel
 * @version $Id: NodeRef.java 294 2006-05-05 17:14:14Z joel $
 */
public class NodeRef implements Serializable, ResourceId {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 2218447923664407458L;
	private DKSRef dksRef;
	Object jn;
	int operationId;
	int totalSize;
	
	public NodeRef() {
		
	}
	public NodeRef(DKSRef dksRef) {
		this.dksRef = dksRef;
	}
	
	public Object getJadeNode() {
		return jn;
	}
	
	public void setJadeNode(Object jn) {
		this.jn = jn;
	}
	
	public void setDKSinfo(int operationId, DKSRef dksRef) {
		this.operationId = operationId;
		this.dksRef = dksRef;
		
	}
	public DKSRef getDKSRef() {
		return dksRef;
	}
	
	public void setDKSRef(DKSRef dksRef) {
		this.dksRef = dksRef;
	}
	/**
	 * @return Returns the operationId.
	 */
	public int getOperationId() {
		return operationId;
	}
	/**
	 * @param operationId The operationId to set.
	 */
	public void setOperationId(int operationId) {
		this.operationId = operationId;
	}

//	public BundleDescription getDescription() {
//		return nd;
//	}

//	public void setDescription(BundleDescription nd) {
//		this.nd = nd;
//	}

//	public ResourceDescription getResourceDescription() {
//		return rd;
//	}
	
//	public void setDescription(ResourceDescription rd) {
//		this.rd = rd;
//	}

	
	public NodeRef getNodeRef() {
		return this;
	}
	public void setSize(int i) {
		totalSize = i;
	}
	
	public int getTotalStorage() {
		return totalSize;
	}
	/* (non-Javadoc)
	 * @see dks.niche.ids.ResourceId#getAllocatedStorage()
	 */
	public int getAllocatedStorage() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
}
