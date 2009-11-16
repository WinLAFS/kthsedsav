/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.ring.events;

import dks.addr.DKSRef;
import dks.arch.Event;

/**
 * The <code>RingLeaveDoneInterceptorEvent</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: RingLeaveDoneInterceptorEvent.java 294 2006-05-05 17:14:14Z alshishtawy $
 */

/**
 * This event is used to inform components that the node left the ring.
 * So all components should finish any pending tasks before telling the App to leave.
 * The number of the components registered to receive this event should be equal to
 * RingMaintenanceComponent.RING_LEAVE_DONE_INTERCEPTORS
 * when finishing all pending tasks a component should trigger RingLeaveDoneInterceptorAckEvent
 * 
 * Only the RingMaintenanceComponent should trigger this event (RingLeaveDoneInterceptorEvent)
 * and when doing so it should also reset the ringLeaveDoneInterceptorAckCounter to zero
 * 
 * when ringLeaveDoneInterceptorAckCounter == RING_LEAVE_DONE_INTERCEPTORS the 
 * RingMaintenanceComponent will trigger RingLeaveDoneEvent.
 * 
 * Please note that this is a temporary solution and should be replace with a general
 * framework for interceptors that work with any event.
 */
public class RingLeaveDoneInterceptorEvent extends Event {
    
    DKSRef succesor;
    /**
     * 
     */
    public RingLeaveDoneInterceptorEvent(DKSRef successor) {
	this.succesor = successor;
	
    }
    public DKSRef getSuccesor() {
        return succesor;
    }
    public void setSuccesor(DKSRef succesor) {
        this.succesor = succesor;
    }
    
    

}
