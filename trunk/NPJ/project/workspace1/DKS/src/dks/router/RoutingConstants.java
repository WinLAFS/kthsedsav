/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.router;

import dks.router.Router.LookupStrategy;

/**
 * The <code>RoutingConstants</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RoutingConstants.java 489 2007-12-10 15:49:00Z roberto $
 */
public interface RoutingConstants {

	public static final LookupStrategy TOPOLOGY_MAINTENANCE_LOOKUP_STRATEGY = LookupStrategy.TRANSITIVE;

	public static final long TRANSITIVE_RELIABLE_LOOKUP_TIMER =
		System.getProperty("dks.reliableLookupTimeout") instanceof String ?
					Integer.parseInt(System.getProperty("dks.reliableLookupTimeout"))
				:
					5000
	;
	
	public static final long PERIODIC_MAINTENANCE_TIMER = 20000;
	
	//public static final int CLEAN_DUPLICATE_LOOKUPS_TIMER = 30000;
	
	public static final boolean TOPOLOGY_MAINTENANCE_ACTIVATED = true;
}
