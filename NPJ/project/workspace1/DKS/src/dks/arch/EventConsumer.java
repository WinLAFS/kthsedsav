/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.arch;

import java.lang.reflect.Method;

/**
 * The <code>EventConsumer</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id:EventConsumer.java 112 2006-11-16 16:39:07Z cosmin $
 */
public class EventConsumer {
	
	private Component component;
	
	private Method handler;

	/**
	 * @param component
	 * @param handler
	 */
	public EventConsumer(Component component, Method handler) {
		super();
		this.component = component;
		this.handler = handler;
	}

	public Component getComponent() {
		return component;
	}

	public Method getHandler() {
		return handler;
	}
}
