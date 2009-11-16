/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.fractal.interfaces;

import org.objectweb.fractal.api.control.AttributeController;

import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.wrappers.ManagementDeployParameters;

/**
 * The <code>ManagementElementAttributeController</code> class
 *
 * @author Joel
 * @version $Id: ManagementElementAttributeController.java 294 2006-05-05 17:14:14Z joel $
 */
public interface OldMEAttributeController extends AttributeController {
	
	public void setId(NicheId id);
	public NicheId getId();
	public void setNicheManagementInterface(NicheManagementInterface host);
	public void setManagementDeployParameters(ManagementDeployParameters managementDeployParameters);
	public NicheManagementInterface getNicheManagementInterface();
	public void setInitialParameters(Object[] parameters);
	public void setReplicaNumber(int replicaNumber);
	public Object[]  getInitialParameters();
	
	
	
}
