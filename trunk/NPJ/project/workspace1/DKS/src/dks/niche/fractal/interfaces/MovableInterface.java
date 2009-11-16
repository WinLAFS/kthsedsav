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

/**
 * The <code>MovableInterface</code> class
 *
 * Any management interface which wants support from the system
 * to be automatically moved and redeployed upon churn needs to implement
 * this interface.
 *  
 * @author Joel
 * @version $Id: MovableInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface MovableInterface {

	/**
	 * The system will call this method on a management element which is about to be
	 * moved or copied.
	 * 
	 * @return	An array of any parameters the management element is dependent on
	 * 			to be properly re-initialized. It is the responsibility of the ME
	 * 			designer to record the state in the way expected by the re-init
	 * 			method of the same ME class.
	 * 
	 * @see InitInterface
	 */
	public Serializable[] getAttributes();
}
