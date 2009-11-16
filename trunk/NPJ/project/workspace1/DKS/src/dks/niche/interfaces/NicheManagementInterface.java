/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.interfaces;

import dks.niche.components.NicheOverlayServiceComponent;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.NicheId;
import dks.niche.wrappers.SimpleResourceManager;

/**
 * The <code>NicheManagerInterface</code> class
 *
 * @author Joel
 * @version $Id: NicheManagementInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface NicheManagementInterface {
	
	public void boot();
	public void join();
	public void leave();
	public void destroy();
	public NicheActuatorInterface getNicheActuator(NicheId id);
	//public NicheActuatorInterface getNicheActuator(NicheId id, TriggerInterface proxy);
	public NicheAsynchronousInterface getNicheAsynchronousSupport();
	//public NicheActuatorInterface getJadeSupport(String owner);
	//public NicheOverlayServiceComponent getDirectOverlayAccess();
	
	public void registerManagementElement(int replicaNumber, int flag, ManagementElementInterface mei);
	/**
	 * @return
	 */
	public SimpleResourceManager getResourceManager();

}
