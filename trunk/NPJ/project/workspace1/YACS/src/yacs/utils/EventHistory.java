package yacs.utils;

import java.util.Hashtable;
import java.io.Serializable;

/**
 * Class for containing a record of received events. Useful, for example, in
 * subscriptions for FailEvents. It can happen with replication that many such
 * events will be delivered multiple times. It can be important the the failure
 * be handled only once.
 * @author LTDATH
 */
public class EventHistory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Hashtable<String,Serializable> history = new Hashtable<String,Serializable>();
	
	/**
	 * Has the event identified by Id been seen before?
	 * @param id The identity of this event
	 * @return True if on record, else false.
	 */
	public synchronized boolean eventSeenBefore( String id ){
		return (history.containsKey(id) ? true : false);
	}
	
	/**
	 * Record a unique event instance in history.
	 * @param id The unique id of the event
	 * @param event A reference to an associated data container. If null then the event will store a reference to its own id.
	 * @return False if this event has been seen before, else true.
	 */
	public synchronized boolean record( String id, Serializable event ){
		if( history.containsKey(id) )
			return false;
		
		if( event == null )
			history.put( id, id );
		else
			history.put( id, event );
		
		return true;
	}
	
	/**
	 * Add an event, identified by id, to the record of received events 
	 * @param id Unique id of the id
	 * @param event Free reference. If null the history will simply store the id itself behind the id.
	 * @deprecated Use record and check for return value. If false then event seen before!
	 */
	public synchronized void addEvent( String id, Serializable event ){
		if( event == null )
			history.put( id, id );
		else
			history.put( id, event );
	}
	
	/**
	 * Get the stored value of the event.
	 * @param id The unique id of the event.
	 * @return The value behind the id. Null if id not stored.
	 */
	public synchronized Serializable getEvent( String id ){
		return history.get( id );
	}
	
}
