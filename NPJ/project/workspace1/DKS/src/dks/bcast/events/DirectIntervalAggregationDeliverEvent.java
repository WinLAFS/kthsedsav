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

import java.math.BigInteger;
import java.util.ArrayList;

import dks.arch.Event;

/**
 * The <code>DirectIntervalAggregationDeliverEvent</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: DirectIntervalAggregationDeliverEvent.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class DirectIntervalAggregationDeliverEvent extends Event {
	ArrayList<Object> values;
	BigInteger instanceId;
	
	public ArrayList<Object> getValues() {
		return values;
	}

	public void setValues(ArrayList<Object> values) {
		this.values = values;
	}

	public BigInteger getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(BigInteger instanceId) {
		this.instanceId = instanceId;
	}
	

}
