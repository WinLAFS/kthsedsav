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

import dks.niche.ids.ComponentId;

/**
 * The <code>ComponentStateChangeEvent</code> class
 *
 * @author Joel
 * @version $Id: ComponentStateChangeEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ComponentStateChangeEvent implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -8126605566897914879L;
	//For testing only
	ComponentId affectedComponent;
	int oldState, newState;
	
	
	public ComponentStateChangeEvent(ComponentId affectedComponent, int oldState, int newState) {
		this.affectedComponent = affectedComponent;
		this.oldState = oldState;
		this.newState = newState;
	}

	public int getNewState() {
		return newState;
	}

	public void setNewState(int newState) {
		this.newState = newState;
	}

	public int getOldState() {
		return oldState;
	}

	public void setOldState(int oldState) {
		this.oldState = oldState;
	}

	public ComponentId getAffectedComponent() {
		return affectedComponent;
	}

	public void setAffectedComponent(ComponentId affectedComponent) {
		this.affectedComponent = affectedComponent;
	}

	@Override
	public String toString() {
		return "newState " + newState + " oldState " + oldState + " affectedComponent " + affectedComponent.getId();
	}
	
	
	

}
