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

/**
 * The <code>StorageAvailabilityChangeEvent</code> class
 *
 * @author Joel
 * @version $Id: StorageAvailabilityChangeEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class StorageAvailabilityChangeEvent extends Event implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -932360186229368709L;
	int totalCapacity;
	double totalLoad;
	
	public StorageAvailabilityChangeEvent(int totalCapacity, double totalLoad) {
		
		this.totalCapacity = totalCapacity;
		this.totalLoad = totalLoad;
	}

	/**
	 * @return Returns the totalCapacity.
	 */
	public int getTotalCapacity() {
		return totalCapacity;
	}

	/**
	 * @param totalCapacity The totalCapacity to set.
	 */
	public void setTotalCapacity(int totalCapacity) {
		this.totalCapacity = totalCapacity;
	}

	/**
	 * @return Returns the totalLoad.
	 */
	public double getTotalLoad() {
		return totalLoad;
	}

	/**
	 * @param totalLoad The totalLoad to set.
	 */
	public void setTotalLoad(double totalLoad) {
		this.totalLoad = totalLoad;
	}
	
	
}
