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

import static dks.arch.EventPriorityTable.NORMAL_PRIORITY;

import java.io.Serializable;
import java.util.Set;

/**
 * The <code>Event</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: Event.java 306 2007-06-21 13:57:10Z roberto $
 */
public abstract class Event implements Serializable{
	
	public static enum Priority { HIGH, NORMAL, LOW };

	/**
	 * the Name of the event
	 */
	public static String name;

	/**
	 * the Attachment of the event
	 */
	protected Object attachment;
	
	public Priority fairPriority=Priority.NORMAL;

	protected int priority = NORMAL_PRIORITY;

	protected Set<EventConsumer> consumers;

	/**
	 * Set the general attachment for the event
	 */
	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

	/**
	 * Returns the general attachment of the Object
	 * 
	 * @return The attachment
	 */
	public Object getAttachment() {
		return attachment;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean hasConsumers() {
		return consumers != null;
	}

	public Set<EventConsumer> getConsumers() {
		return consumers;
	}

	public void setConsumers(Set<EventConsumer> consumers) {
		this.consumers = consumers;
	}

}
