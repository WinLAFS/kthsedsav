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

import java.io.Serializable;
import java.util.ArrayList;

import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.jasmine.jade.util.Invocation;

import dks.niche.exceptions.DestinationUnreachableException;
import dks.niche.exceptions.OperationTimedOutException;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.ids.ResourceId;
import dks.niche.wrappers.ManagementDeployParameters;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.Subscription;

/**
 * The <code>NicheActuatorInterface</code> class
 * 
 * This class fills three purposes: 
 * - It acts as the interface class for Niche operations which are available 
 * only for management elements
 * - It acts as the interface class for interface proxies created by Jade
 * - It gives access to primitive resource management services for systems
 * and applications that do not provide those services themselves
 *  
 * @author Joel
 * @version $Id: NicheActuatorInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface NicheActuatorInterface extends NicheComponentSupportInterface {

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
	public ArrayList discover(Serializable requirements);

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
	public NodeRef oneShotDiscoverResource(Serializable requirements) throws OperationTimedOutException;

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
	public ArrayList<ResourceId> allocate(Serializable destinations,
			Object descriptions) throws OperationTimedOutException;

	/**
	 * Frees a previously allocated resource
	 * 
	 * @param resourceId
	 * 			The reference to the resource which should be deallocated
	 */
	public void deallocate(ResourceId resourceId);

	/**
	 * Deploys one or more fractal components as specified by one or more
	 * component descriptions.
	 * 
	 * As of now the code of the component to be deployed has to exist on the
	 * receiving computer.
	 * 
	 * @param destinations
	 *            Either a single (allocated) ResourceId of an ArrayList<ResourceId>
	 *            for bulk operation
	 * @param descriptions
	 *            Either a single description or an ArrayList of descriptions
	 *            for bulk operation. *Insert text from Nikos*
	 * @return A list containing one or more global component ids
	 */
	public ArrayList deploy(Serializable destinations, Serializable descriptions) throws OperationTimedOutException;

	/**
	 * Deploys a management element as specified by a management element
	 * component description.
	 * 
	 * As of now the code of the component to be deployed has to exist on the
	 * receiving computer.
	 * 
	 * @param description
	 *            A management element description *Insert text from Nikos*
	 * @param destination
	 *            The reference to another management element, with which the
	 *            new element should be collocated
	 * 
	 * @return A management element id
	 */
	public NicheId deployManagementElement(ManagementDeployParameters description,
			IdentifierInterface destination);

	/**
	 * Redeploys a management element which has failed due to no, or insufficient replication
	 * 
	 * @param description
	 *            A management element description 
	 * @param destination
	 *            The id of the failed ME to be recreated
	 * 
	 * @return A management element id
	 */

	public void redeployManagementElement(ManagementDeployParameters description,
			IdentifierInterface oldId);

	// SUBSCRIPTION MANAGEMENT

	/**
	 * Adds a new sink to the event generating management element source
	 * 
	 * @param source
	 *            The id of the management element generating the events of
	 *            intrest, or the id of the group defining the scope of interest
	 *            in case of a subscription to an infrastructure event
	 * @param sink
	 *            The id of the management element interested in the event
	 * @param eventName
	 *            The full classname of the event
	 * @return A subscription which can be used to later change or stop the
	 *         subscription
	 */
	public Subscription subscribe(IdentifierInterface source,
			IdentifierInterface sink, String eventName);
	
	/**
	 * Adds a new sink to the event generating management element source
	 * 
	 * @param source
	 *            The id of the management element generating the events of
	 *            intrest, or the id of the group defining the scope of interest
	 *            in case of a subscription to an infrastructure event
	 * @param sink
	 *            The id of the management element interested in the event
	 * @param eventName
	 *            The full classname of the event
	 * @param tag
	 * 			  Can be used to filter events based on the tag
	 * @return A subscription which can be used to later change or stop the
	 *         subscription
	 */
	public Subscription subscribe(IdentifierInterface source,
			IdentifierInterface sink, String eventName, Serializable tag);

	/**
	 * Adds a new sink to the event generating management element source, given
	 * that the sink is present
	 * 
	 * @param source
	 *            The id of the management element generating the events of
	 *            intrest
	 * @param sink
	 *            The ADL name of the management element interested in the event
	 * @param eventName
	 *            The full classname of the event
	 * @param sinkLocation
	 *            The id of any element known to be collocated with the sink
	 * @return A subscription which can be used to later change or stop the
	 *         subscription, or null if the sink did not exist
	 */
	public Subscription subscribe(IdentifierInterface source, String sink,
			String eventName, IdentifierInterface sinkLocation);

	/**
	 * Cancels a subscription
	 * 
	 * @param subscription
	 *            The subscription specifying source, sink and event to stop
	 *            listening to
	 */
	public boolean unsubscribe(Subscription subscription);

	/**
	 * Generic method to update groups or management elements
	 * 
	 * @param objectToBeUpdated
	 *            The id of the management element which should be updated
	 * @param argument
	 *            The update message, or the item to add/remove, depending on
	 *            the type
	 * @param type
	 *            The type specifying the update operation, as given by the
	 *            constants in *NicheComponentSupportInterface*
	 */
	public void update(Object objectToBeUpdated, Object argument, int type);

	// SENDING
	// To be used by Jade/the Jade-created interface proxies
	
	/**
	 * Used by Jade-created interface proxies for two-way bindings.
	 * Synchronous - propagates a method invocation and waits for a reply.
	 * 
	 * @param localBindId
	 *            The id of the proxy
	 * 
	 * @param invocation
	 *            The wrapped method invocation
	 *            
	 * @return Any object as specified by the interface description
	 */
	public Object sendWithReply(Object localBindId, Serializable invocation) throws DestinationUnreachableException, OperationTimedOutException;

	/**
	 * Used by Jade-created interface proxies for one-way bindings.
	 * Semi-synchronous - propagates a method invocation and waits until the message is on the network.
	 * 
	 * @param localBindId
	 *            The id of the proxy
	 * 
	 * @param invocation
	 *            The wrapped method invocation
	 *            
	 * @param shortcut
	 *            Gives the possibility to specify a specific receiver out of a group
	 *            
	 * @return Any object as specified by the interface description
	 */
	public Object sendOnBinding(Object localBindId, Invocation invocation, ComponentId shortcut) throws DestinationUnreachableException, OperationTimedOutException;

	/**
	 * Allows a management element to register a one-off timer. When the time
	 * delay has expired the eventHandler method of the management element will
	 * be called with a event of class eventClassName
	 * 
	 * @param managementElement
	 *            The management element which will be called when the timer
	 *            goes off
	 * @param eventClassName
	 *            The event which will be generated
	 * @param timerDelay
	 *            The timer delay in milliseconds
	 * @return A timer id which is needed for cancellation
	 */
	public long registerTimer(EventHandlerInterface managementElement,
			Class eventClassName, int timerDelay);

	/**
	 * Cancels a timer previously registered with registerTimer
	 * 
	 * @param timerId
	 */
	public void cancelTimer(long timerId);

	public NicheAsynchronousInterface testingOnly();
	
	/**
	 * Allows the applications/system developer to reuse
	 * the logging functionality already present in Niche
	 * 
	 * @return A reference to the Niche log4j-logger
	 */
	public LoggerInterface getLogger();

	public NicheId getId();
	
	/**
	 * Returns the Jade component type corresponding to a given adl name.
	 * If the component type was not previously generated on the node where
	 * the method call is done, it will be generated on the first invocation.
	 * This therefore requires that the given adl name corresponds to an
	 * existing adl file. 
	 * 
	 * @param adlName
	 * 	 
	 * @return A Jade component type object
	 */
	public ComponentType getComponentType(String adlName);
	
	/**
	 * Returns an 'empty' GroupId to be used as template when specifying which
	 * interfaces that should be automatically bound by the system when a component
	 * becomes member in a specific group.
	 * 	 
	 * @return An empty GroupId
	 */
	public GroupId getGroupTemplate();
	
	//public ArrayList<SNRElement> getCurrentMembers(SNRElement snrName);

}
