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

import dks.niche.ids.NicheId;
import dks.niche.interfaces.IdentifierInterface;

/**
 * The <code>Subscription</code> class
 *
 * @author Joel
 * @version $Id: Subscription.java 294 2006-05-05 17:14:14Z joel $
 */
public class Subscription implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 1062611982174837626L;
	NicheId sourceId;
	NicheId sinkId;
	String eventName;
	Object tag;
	
	public Subscription() {
		
	}
	/**
	 * @param source
	 * @param sink
	 * @param eventName
	 */
	public Subscription(IdentifierInterface source, IdentifierInterface sink, String eventName) {
		this.sourceId = source.getId();
		this.sinkId = sink.getId();
		this.eventName = eventName;
	}
	
	public Subscription(IdentifierInterface source, IdentifierInterface sink, String eventName, Object tag) {
		this.sourceId = source.getId();
		this.sinkId = sink.getId();
		this.eventName = eventName;
		this.tag = tag;
	}
	
	public boolean equals(Object compareObject) {
		if(compareObject instanceof Subscription) {
			Subscription compareSubscription = (Subscription)compareObject;
			if( compareSubscription.getSourceId().equals(sourceId) && compareSubscription.getSinkId().equals(sinkId) && compareSubscription.getEventName().equals(eventName)) {
				return true;
			}
		}
		return false;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public NicheId getSinkId() {
		return sinkId;
	}
	public void setSink(IdentifierInterface sink) {
		this.sinkId = sink.getId();
	}
	public NicheId getSourceId() {
		return sourceId;
	}
	public void setSource(IdentifierInterface source) {
		this.sourceId = source.getId();
	}
	
	public Object getTag() {
		return tag;
	}
	
	public void setTag(Object tag) {
		this.tag = tag;
	}
	
}
