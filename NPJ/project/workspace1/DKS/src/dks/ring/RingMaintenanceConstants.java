/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.ring;

/**
 * The <code>RingMaintenanceCostants</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingMaintenanceCostants.java 627 2008-07-11 23:17:56Z joel $
 */
public interface RingMaintenanceConstants {

	public static final long JOIN_RETRY_TIMEOUT = 2000;

	public static final long LOCK_TIMER = 3000;

	public static final long LEAVE_RETRY_TIMEOUT = 2000;

	public static final long STAB_RPC_TIMEOUT = 
		System.getProperty("dks.ring.rpcTimeout") instanceof String ?
			Integer.parseInt(System.getProperty("dks.ring.rpcTimeout"))
		:
			20000;

	public static final long STABILIZATION_TIMER =
		System.getProperty("dks.ring.stabilizationTimer") instanceof String ?
					Integer.parseInt(System.getProperty("dks.ring.stabilizationTimer"))
				:
					10000;

	//public static final long STAB_CHECK_PRED_TIMER = 5000;

	//public static final int INCREMENTAL_LOCK_TIMER_FACTOR = 2;

}
