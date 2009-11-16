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
 * The <code>ResourceRef</code> class
 *
 * @author Joel
 * @version $Id: ResourceRef.java 294 2006-05-05 17:14:14Z joel $
 */
public class ResourceRef implements Serializable, ResourceId {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -8862088580744062243L;

	
	Object jn;
	
	//DKSRef dksRef;
	NodeRef nodeRef;
	//BundleDescription nd;
	//ResourceDescription rd;
	Integer totalSize = 0; //????
	int allocatedSize = 0;
	
	int operationId;
	String owner;
	
	//HashMap<String, Integer> 

	
	//silly constructor for testing!
	public ResourceRef(NodeRef ri, String owner) {
		this.nodeRef = ri;
		this.jn = ri.getJadeNode();
		this.operationId = ri.getOperationId();
		this.totalSize = ri.getTotalStorage();
		this.owner = owner;
	}

////	silly constructor for jade-testing!
//	public ResourceRef(Object jadeNodeName, Integer i, String owner) {
//		this.jn = jadeNodeName;
//		this.totalSize = i;
//		this.owner = owner;
//	}

	public ResourceRef(NodeRef ref, Object jadeNodeName, String owner) {
		this.nodeRef = ref;
		this.jn = jadeNodeName;
		this.owner = owner;
		
	}
	
	public ResourceRef(NodeRef dksRef, int size) {
		this.nodeRef = dksRef;
		this.allocatedSize = size;
	}
	
	//Temporarily disabled
	
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	
	public Object getJadeNode() {
		return jn;
	}
	
	public void setJadeNode(Object jn) {
		this.jn = jn;
	}
	
	public void setDKSinfo(int operationId, NodeRef dksRef) {
		this.operationId = operationId;
		this.nodeRef = dksRef;
		
	}
	public DKSRef getDKSRef() {
		return nodeRef.getDKSRef();
	}
//	
//	public void setDKSRef(DKSRef dksRef) {
//		this.dksRef = dksRef;
//	}
	
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

	public String getOwner() {
		return owner;
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

	public boolean allocate(int size) {
		allocatedSize = size;
		return true;
	}
	
	public ResourceRef setSize(int i) {
		totalSize = i;
		return this;
	}
	
	public int getTotalStorage() {
		return totalSize;
	}
	
	
	//Allocated for the component this ref belongs to, that is!	
	public int getAllocatedStorage() {
		return allocatedSize;
	}
	

}
