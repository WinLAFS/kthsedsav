/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.hiddenEvents;

import dks.arch.Event;

/**
 * The <code>DelegationEvent</code> class
 *
 * @author Joel
 * @version $Id: DelegationEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class DelegationEvent extends Event {

	public static final String DYNAMIC_EVENT_TYPE = "dynamic event";
	
	String eventName;
	String methodName;
	Object eventHandlerClassInstance;
	Object eventClassInstance;
	
	/**
	 * @param eventHandlerClass
	 */
	public DelegationEvent(String eventName, String methodName, Object eventHandlerClassInstance) {
		this.eventName = eventName;
		this.methodName = methodName;
		this.eventHandlerClassInstance = eventHandlerClassInstance;
		this.eventClassInstance = "";
	}
	
	public DelegationEvent(String eventName, String methodName, Object eventHandlerClassInstance, Object eventClassInstance) {
		this.eventName = eventName;
		this.methodName = methodName;
		this.eventHandlerClassInstance = eventHandlerClassInstance;
		this.eventClassInstance = eventClassInstance;
	}

	public Object getEventHandlerClassInstance() {
		return eventHandlerClassInstance;
	}

	public void setEventHandlerClassInstance(Object eventHandlerClassInstance) {
		this.eventHandlerClassInstance = eventHandlerClassInstance;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @return
	 */
	public Object getEventClassInstance() {
		return eventClassInstance;
	}
	public void setEventClassInstance(Object eventClassInstance) {
		this.eventClassInstance = eventClassInstance;
	}
	
	
}
