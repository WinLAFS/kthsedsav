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

import java.util.List;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;

/**
 * The <code>RingMaintenanceComponentInt</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingMaintenanceComponentInt.java 294 2006-05-05 17:14:14Z
 *          Roberto $
 */
public interface RingMaintenanceComponentInt {

	public DKSParameters getDksParameters();

	public void remNeighbor(DKSRef ref);

	public DKSRef getMyDKSRef();

	public RingState getRingState();

//	private void addNeighbor(DKSRef node);

	public boolean isStabilizationRunning();

	//public List<DKSRef> getNeighbors();
	
	public ComponentRegistry getRegistry();
}
