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

import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;

/**
 * The <code>InitInterface</code> class
 * 
 * This interface must be implemented by all management elements to be
 * properly initialized by the Niche framework. Please observe that the 
 * system gives no guarantees about the order which these methods are 
 * called upon ME creation
 *
 * @author Joel
 * @version $Id: InitInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface InitInterface {

	/**
	 * The system will invoke this method on a management element during its creation, if it
	 * is being created for the first time as a result of managment deployment
	 * 
	 * @param parameters	The initialArguments parameter of the ManagementDeployParameters 
	 * 						instance used to deploy the ME
	 * 
	 * @see ManagementDeployParameters
	 * 
	 */
	public void init(Serializable[] parameters);
	
	/**
	 * The system will invoke this method on a management element during its creation, if it
	 * is being recreated after a churn event
	 * 
	 * @param parameters	The ME-parameters as given by the getAttributes method of the
	 * 						MovableInterface 
	 * 
	 * @see MovableInterface
	 */
	public void reinit(Serializable[] parameters);
	
	/**
	 * The system will invoke this method on a management element during its creation.
	 * 
	 * @param parameters	An instance of the NicheActuatorInterface to be used by the ME 
	 */
	public void init(NicheActuatorInterface actuator);

	/**
	 * The system will invoke this method on a management element during its creation
	 * 
	 * @param parameters	The NicheId of the ME being initialized
	 */
	public void initId(NicheId id);
}
