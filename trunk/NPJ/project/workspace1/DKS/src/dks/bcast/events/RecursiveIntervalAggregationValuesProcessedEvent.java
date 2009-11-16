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
 * The <code>RecursiveIntervalAggregationValuesProcessedEvent</code> class
 *
 * @author Roberto Ahmad Al-Shishtawy
 * @version $Id: RecursiveIntervalAggregationValuesProcessedEvent.java 294 2006-05-05 17:14:14Z ahmad $
 */
public class RecursiveIntervalAggregationValuesProcessedEvent extends Event {
	
	ArrayList<Object> values;
	String uniqueID;
	
		
	/**
	 * @param values
	 * @param uniqueID
	 */
	public RecursiveIntervalAggregationValuesProcessedEvent(
			ArrayList<Object> values, String uniqueID) {
		super();
		this.values = values;
		this.uniqueID = uniqueID;
	}

	public ArrayList<Object> getValues() {
		return values;
	}

	public void setValues(ArrayList<Object> values) {
		this.values = values;
	}

	/**
	 * @return Returns the uniqueID.
	 */
	public String getUniqueId() {
		return uniqueID;
	}

	/**
	 * @param uniqueID The uniqueID to set.
	 */
	public void setUniqueId(String uniqueID) {
		this.uniqueID = uniqueID;
	}


}
