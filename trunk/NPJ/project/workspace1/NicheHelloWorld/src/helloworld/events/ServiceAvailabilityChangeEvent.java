/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package helloworld.events;

import java.io.Serializable;

import dks.arch.Event;

/**
 * The <code>StorageAvailabilityChangeEvent</code> class
 * 
 * @author Joel
 * @version $Id: StorageAvailabilityChangeEvent.java 294 2006-05-05 17:14:14Z
 *          joel $
 */
public class ServiceAvailabilityChangeEvent extends Event implements Serializable {
    private static final long serialVersionUID = -932360186229368709L;
}
