/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.bcast.events;

import java.util.ArrayList;

import dks.arch.Event;

/**
 * The <code>RecursiveIntervalAggregationProcessValuesEvent</code> class
 * 
 * The applicaion should handel this event if it wants to modifi intermediat aggregation results
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: RecursiveIntervalAggregationProcessValuesEvent.java 294 2006-05-05 17:14:14Z ahmad $
 */
public class RecursiveIntervalAggregationProcessValuesEvent extends Event {
	
	ArrayList<Object> values;
	String uniqueId;
	
	
	
	/**
	 * 
	 */
	public RecursiveIntervalAggregationProcessValuesEvent() {
		super();
	}

	/**
	 * @param values
	 * @param uniqueId
	 */
	public RecursiveIntervalAggregationProcessValuesEvent(
			ArrayList<Object> values, String uniqueId) {
		super();
		this.values = values;
		this.uniqueId = uniqueId;
	}
	
	/**
	 * @return Returns the uniqueId.
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @param uniqueId The uniqueId to set.
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public ArrayList<Object> getValues() {
		return values;
	}

	public void setValues(ArrayList<Object> values) {
		this.values = values;
	}

	
}
