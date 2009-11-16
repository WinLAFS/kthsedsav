/*
 * Created on 12 juil. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.jasmine.jade.control.repair.util.event;

/**
 * @author ssicard
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public final class EventType {
	// Event type
	public static final EventType	DEBUG		= new EventType(0);
	public static final EventType	PING		= new EventType(1);
	public static final EventType	BINDING		= new EventType(2);
	public static final EventType	LIFECYCLE	= new EventType(3);
	public static final EventType	CONTENT		= new EventType(4);
  public static final EventType NODEALLOCATION   = new EventType(5);
  public static final EventType NODEFAILURE   = new EventType(6);
  public static final EventType NODEFREE   = new EventType(7);
  public static final EventType NEWNODE   = new EventType(8);
  public static final EventType NODEREMOVE   = new EventType(8);

	///////
	public String toString() {
		return image[pos];
	}

	public static EventType toEventType(String image) {
		if( image.equalsIgnoreCase(DEBUG.toString())) 		return DEBUG;
		if( image.equalsIgnoreCase(PING.toString())) 		return PING;
		if( image.equalsIgnoreCase(BINDING.toString())) 	return BINDING;
		if( image.equalsIgnoreCase(LIFECYCLE.toString())) 	return LIFECYCLE;
		if( image.equalsIgnoreCase(CONTENT.toString())) 	return CONTENT;
    if( image.equalsIgnoreCase(NODEALLOCATION.toString()))   return NODEALLOCATION;
    if( image.equalsIgnoreCase(NODEFAILURE.toString()))   return NODEFAILURE;
    if( image.equalsIgnoreCase(NODEFREE.toString()))   return NODEFREE;
    if( image.equalsIgnoreCase(NEWNODE.toString()))   return NEWNODE;
    if( image.equalsIgnoreCase(NODEREMOVE.toString()))   return NODEREMOVE;
		return null;
	}
	
	///////
	private static final String[]		image	= { "DEBUG", "PING", "BINDING", "LIFECYCLE", "CONTENT", "NODEALLOCATION", "NODEFAILURE", "NODEFREE", "NEWNODE", "NODEREMOVE" };

	private int							pos;

	private EventType(int pos) {
		this.pos = pos;
	}
}