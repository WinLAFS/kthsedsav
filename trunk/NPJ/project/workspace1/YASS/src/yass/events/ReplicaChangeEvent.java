/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package yass.events;

import java.io.Serializable;

import dks.arch.Event;
import dks.niche.ids.GroupId;

/**
 * The <code>ReplicaChangeEvent</code> class
 *
 * @author Joel
 * @version $Id: ReplicaChangeEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ReplicaChangeEvent extends Event implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 7511178714134287798L;
	GroupId gid;
	private String failedNodeId;
	
	public ReplicaChangeEvent() {
		
	}

	public ReplicaChangeEvent(String failedNodeId, GroupId gid) {
		this.failedNodeId = failedNodeId;
		this.gid = gid;
	}
	
	public GroupId getGroupId() {
		return gid;
	}
	public String getFailedNodeId() {
		return failedNodeId;
	}
	
}
