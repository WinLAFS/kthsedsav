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

/**
 * The <code>ResourceDescription</code> class
 *
 * @author Joel
 * @version $Id: ResourceDescription.java 294 2006-05-05 17:14:14Z joel $
 */
public class ResourceDescription implements Serializable {
	
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 851691159894363942L;

	public final static int TYPE_STORAGE = 0;
	
	int type;
	int maxValue;
	int currentUtilization;
	/**
	 * @param type
	 * @param value
	 */
	public ResourceDescription(int type, int maxValue) {
		this.type = type;
		this.maxValue = maxValue;
		this.currentUtilization = 0;
	}
	public ResourceDescription(int type, int maxValue, int currentlyUsed) {
		this.type = type;
		this.maxValue = maxValue;
		this.currentUtilization = currentlyUsed;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	public int getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}
	
	
	public int getCurrentUtilization() {
		return currentUtilization;
	}
	public void setCurrentUtilization(int currentUtilization) {
		this.currentUtilization = currentUtilization;
	}
	public Serializable[] getDescription() {
		
		return new Serializable[]{type, maxValue, currentUtilization}; //some wrapping
		
	}
	
}
