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

import java.util.ArrayList;

import dks.niche.ids.ResourceId;
import dks.niche.wrappers.NodeRef;

/**
 * The <code>ResourceManagementInterface</code> class
 *
 * @author Joel
 * @version $Id: ResourceManagementInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface ResourceManagementInterface {

	/**
	 * Method to ask the resource manager for currently free nodes matching
	 * the requirements
	 * 
	 * @param requirements
	 *            The format of requirement description will depend on the
	 *            resource management being implemented
	 * 
	 * @return A list of all nodes which can provide resources matching the
	 *         requirements, null if none could be found
	 */
	public ArrayList discover(Object requirements);

	/**
	 * A shorthand to grab just one node matching the requirements
	 * 
	 * @param requirements
	 *            The format of requirement description will depend on the
	 *            resource management being implemented
	 * 
	 * @return The first found resource that matched the requirements, null if
	 *         none could be found
	 */
	public NodeRef oneShotDiscoverResource(Object requirements);

	/**
	 * Allocates a (part of a) discovered node, which is needed before deploying components
	 * 
	 * @param destinations
	 *            Either a single ResourceId or an ArrayList<ResourceId> for
	 *            bulk operation
	 * @param descriptions
	 *            Either a single description or an ArrayList of descriptions
	 *            for bulk operation. The format of allocate description will
	 *            depend on the resource management being implemented
	 * 
	 * @return A list of the allocated resource identifiers, null if the
	 *         operation could not be completed for the resource
	 */
	public ArrayList<ResourceId> allocate(Object destinations,
			Object descriptions);

	/**
	 * Frees a previously allocated resource
	 * 
	 * @param resourceId
	 * 			The reference to the resource which should be deallocated
	 */
	public void deallocate(ResourceId resourceId);

}
