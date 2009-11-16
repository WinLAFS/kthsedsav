/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package examples.events;

import dks.arch.Event;

/**
 * The <code>ExampleRequestEvent</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: ExampleRequestEvent.java 231 2007-03-08 14:34:49Z Roberto $
 */
public class ExampleRequestEvent extends Event {
	
	private int attribute;
	
	/**
	 * ExampleRequestEvent constructor 
	 */
	public ExampleRequestEvent(int value) {
		this.attribute=value;
	}

	/**
	 * @return Returns the attribute.
	 */
	public int getAttribute() {
		return attribute;
	}

}
