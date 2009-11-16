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

import java.io.Serializable;

import org.objectweb.fractal.api.control.AttributeController;

import dks.niche.ids.ComponentId;
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
public interface ManagementElementAttributeController extends AttributeController {
	public void setId(NicheId id);
	public NicheId getId();
	public void setNicheManagementInterface(NicheManagementInterface host);
	public void setManagementDeployParameters(ManagementDeployParameters managementDeployParameters);
	public NicheManagementInterface getNicheManagementInterface();
	public void setInitialParameters(Serializable[] parameters);
	public void setReplicaNumber(int replicaNumber);
	public void setStartupMode(int flag);
	public Serializable[]  getInitialParameters();
	
	//watcher stuff, optional!
	
	public void setSensorClassName(String sensorClassName);
	public String getSensorClassName();

	public void setSensorEventClassName(String sensorEventClassName);
	public String getSensorEventClassName();

	public void setSensorParameters(Serializable[] sensorParameters);
	public Serializable[] getSensorParameters();
	
	public void setWatchedComponentId(NicheId watchedComponentsId);
	public NicheId getWatchedComponentId();

	//sensor stuff, also optional!
	
	public void setComponentId(ComponentId id);
	public ComponentId getComponentId();
	
	
	//Executor stuff, optional!
	
	public void setActuatorClassName(String actuatorClassName);
	public String getActuatorClassName();

	public void setActuatorEventClassName(String actuatorEventClassName);
	public String getActuatorEventClassName();

	public void setActuatorParameters(Serializable[] actuatorParameters);
	public Serializable[] getActuatorParameters();
	
	public void setActuatedComponentId(NicheId watchedComponentsId);
	public NicheId getActuatedComponentId();

//	//actuator stuff, also optional!
//	
//	public void setComponentId(ComponentId id);
//	public ComponentId getComponentId();

	
	
}
