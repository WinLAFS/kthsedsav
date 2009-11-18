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

/**
 * The <code>StartScriptEvent</code> class
 *
 * @author J & A
 * @version $Id: StartScriptEvent.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class StartScriptEvent extends Event {

	Object o;
	
	public StartScriptEvent() {
	}
	public StartScriptEvent(Object o) {
		this.o = o; 
	}
	public Object getObject() {
		return o;
	}
}