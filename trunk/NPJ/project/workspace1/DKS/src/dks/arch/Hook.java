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
 * The <code>Hook</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: Hook.java 165 2007-01-28 12:22:42Z Roberto $
 */
public class Hook {

	private Component component;

	private Method handler;

	/**
	 * @param component
	 * @param handler
	 */
	public Hook(Component component, Method handler) {
		super();
		this.component = component;
		this.handler = handler;
	}

	/**
	 * @return Returns the component.
	 */
	public Component getComponent() {
		return component;
	}

	/**
	 * @return Returns the handler.
	 */
	public Method getHandler() {
		return handler;
	}

}
