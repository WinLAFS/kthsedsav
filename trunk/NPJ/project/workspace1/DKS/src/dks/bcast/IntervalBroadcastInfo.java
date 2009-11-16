/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.bcast;

import java.io.Serializable;
import java.math.BigInteger;

import dks.addr.DKSRef;
import dks.utils.IntervalsList;

/**
 * The <code>IntervalBroadcastInfo</code> class
 * 
 * @author Ahmad Al-Shishtawy
 * @version $Id: IntervalBroadcastInfo.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class IntervalBroadcastInfo implements Serializable{
	
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 5246889053335209334L;

	/*
	 * Default timeout in milliseconds 
	 */
	public static final int DEFAULT_AGGREGATION_TIMEOUT = 10000;
	
	DKSRef initiator;
	DKSRef source;
	DKSRef destination;
	IntervalsList interval;
	BigInteger instanceId;
	Serializable message;
	Boolean aggregate;
	boolean idRangeCast = false;
	//The followin two variables are used if you want to recieve your own events & override defaults
	String deliverEventClassName;
	private String ackAggrEventClassName;
	String processValuesClassName;
	
	//Set to true if you want intermediate nodes to process aggregation values before forwarding
	//used only with PseudoReliable interval broadcast
	Boolean processValues;
	
	
	// this variable is for local use and is not marshalled
	int aggregationTimeout;
	
	/**
	 * 
	 */
	public IntervalBroadcastInfo() {
		super();
		aggregate = new Boolean(false);
		aggregationTimeout = DEFAULT_AGGREGATION_TIMEOUT;
		deliverEventClassName = null;
		ackAggrEventClassName = null;
		processValuesClassName = null;
		processValues = new Boolean(false);
	}
	
	public IntervalBroadcastInfo(IntervalBroadcastInfo info) {
		initiator = info.initiator;
		source = info.source;
		destination = info.destination;
		interval = new IntervalsList(info.interval);
		instanceId = info.instanceId;
		message = info.message;
		aggregate = info.aggregate;
		idRangeCast = info.idRangeCast;
		aggregationTimeout = info.aggregationTimeout;		
		if(info.deliverEventClassName!=null)
			deliverEventClassName = new String(info.deliverEventClassName);
		else
			deliverEventClassName = null;
		if(info.ackAggrEventClassName != null)
			ackAggrEventClassName = new String(info.ackAggrEventClassName);
		else
			ackAggrEventClassName = null;
		if(info.processValuesClassName != null)
			processValuesClassName = new String(info.processValuesClassName);
		else
			processValuesClassName = null;
		processValues = info.processValues;
		
	}

	
	@Override
	public String toString() {
		String str = "Init: ";
		str += String.format("%5s", initiator.getId());
		str += ", ";
		
		str += "Src: ";
		str += String.format("%5s",source.getId());
		str += ", ";
		
		str += "Dest: ";
		str += String.format("%5s",destination.getId());
		str += ", ";
		
		str += "Id: ";
		str += String.format("%5s", instanceId);
		str += ", ";
		
		str += "Msg: ";
		str += message;
		str += ", ";
		
		str += "Aggr: ";
		str += aggregate;
		str += ", ";

		str += "IdRangeCast: ";
		str += idRangeCast;
		str += ", ";
		
		str += "I: ";
		str += interval;
		
		if(deliverEventClassName != null) {
			str += "eventType: ";
			str += deliverEventClassName;			
		}
			
		return str;
	}

	
	public DKSRef getDestination() {
		return destination;
	}
	public void setDestination(DKSRef destination) {
		this.destination = destination;
	}
	public DKSRef getInitiator() {
		return initiator;
	}
	public void setInitiator(DKSRef initiator) {
		this.initiator = initiator;
	}
	public BigInteger getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(BigInteger InstanceId) {
		this.instanceId = InstanceId;
	}
	public IntervalsList getInterval() {
		return interval;
	}
	public void setInterval(IntervalsList interval) {
		this.interval = interval;
	}
	public Serializable getMessage() {
		return message;
	}
	public void setMessage(Serializable message) {
		this.message = message;
	}
	public DKSRef getSource() {
		return source;
	}
	public void setSource(DKSRef source) {
		this.source = source;
	}

	public boolean getAggregate() {
		return aggregate;
	}

	public void setAggregate(boolean aggregate) {
		this.aggregate = aggregate;
	}
	
	public String getUniqueId(){
		return initiator.getId()+":"+instanceId;
	}

	public int getAggregationTimeout() {
		return aggregationTimeout;
	}

	public void setAggregationTimeout(int aggregationTimeout) {
		this.aggregationTimeout = aggregationTimeout;
	}

	public String getAckAggrEventClassName() {
		return ackAggrEventClassName;
	}

	public void setAckAggrEventClassName(String ackAggrEventClassName) {
		this.ackAggrEventClassName = ackAggrEventClassName;
	}

	public String getDeliverEventClassName() {
		return deliverEventClassName;
	}

	public void setDeliverEventClassName(String deliverEventClassName) {
		this.deliverEventClassName = deliverEventClassName;
	}

	/**
	 * @return Returns the processValuesClassName.
	 */
	public String getProcessValuesClassName() {
		return processValuesClassName;
	}

	/**
	 * @param processValuesClassName The processValuesClassName to set.
	 */
	public void setProcessValuesClassName(String processValuesClassName) {
		this.processValuesClassName = processValuesClassName;
	}

	/**
	 * @return Returns the processValues.
	 */
	public boolean getProcessValues() {
		return processValues;
	}

	/**
	 * @param processValues The processValues to set.
	 */
	public void setProcessValues(boolean processValues) {
		this.processValues = processValues;
	}

	public boolean isIdRangeCast() {
		return idRangeCast;
	}

	public void setIdRangeCast(Boolean idRangeCast) {
		this.idRangeCast = idRangeCast;
	}
	
	

}
