/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.events;

import dks.arch.Event;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.ResourceRef;

/**
 * The <code>ResourceJoinEvent</code> class
 *
 * @author Joel
 * @version $Id: ResourceJoinEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ResourceJoinEvent extends Event {

	NodeRef nodeRef;
	ResourceRef ref;
	String resource;
	/**
	 * 
	 */
	public ResourceJoinEvent() {
		
	}
	public ResourceJoinEvent(NodeRef nr) {
		this.nodeRef = nr;
	}

	public ResourceJoinEvent(String resource) {
		this.resource = resource;
	}
	/**
	 * @return
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
		
	}
	public ResourceRef getResourceRef() {
		return ref;
	}
	public String getResource() {
		return resource;
	}
	
}
