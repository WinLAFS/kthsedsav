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
 * The <code>ExampleResultEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id$
 */
public class ExampleResponseEvent extends Event {

	private int attribute;

	/**
	 * Example Event Constructor , in the description of the constructor always
	 * put when the event is issued, for clarity's sake
	 */
	public ExampleResponseEvent(int value) {
		this.attribute = value;
	}

	/**
	 * @return Returns the attribute.
	 */
	public int getAttribute() {
		return attribute;
	}

}
