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

//import dks.niche.ids.BindElement;
import dks.niche.exceptions.OperationTimedOutException;
import dks.niche.ids.BindId;
import dks.niche.ids.GroupId;
import dks.niche.ids.SNR;
import dks.niche.ids.SNRElement;
import dks.niche.wrappers.SimpleResourceManager;

/**
 * The <code>NicheComponentSupportInterface</code> class. Gives access to
 * group management and (Niche wide) bind functionality.
 * 
 * @author Joel
 * @version $Id: NicheComponentSupportInterface.java 294 2006-05-05 17:14:14Z
 *          joel $
 */
public interface NicheComponentSupportInterface {

	// GROUPING

	public static final int ADD_TO_GROUP = 0;

	public static final int ADD_TO_GROUP_AND_START = 1;

	public static final int REMOVE_FROM_GROUP = 2;

	public static final int REMOVE_GROUP = 3;
	
	public static final int GET_CURRENT_MEMBERS = 1001;

	/**
	 * Creates a new group based on the given template and the components in the array list.
	 * 
	 * @param template
	 * 			A template which defines the interfaces which the group should manage upon
	 * 			membership changes
	 * 
	 * @param items
	 *            An array list of all components which should be part of the
	 *            group. The components must have at least one interface in
	 *            common.
	 *            
	 * @return A group id representing the new group
	 */
	public GroupId createGroup(SNR template, ArrayList items);
	

	/**
	 * Creates a new group based on the template name and the components in the array list.
	 * This requires that the template has been previously created and registered with
	 * the template name
	 * 
	 * @param templateName
	 * 			A template name which refers to a template which defines the interfaces
	 * 			which the group should manage upon membership changes
	 *
	 * @param items
	 *            An array list of all components which should be part of the
	 *            group. The components must have at least one interface in
	 *            common.
	 *            
	 * @return A group id representing the new group
	 */
	public GroupId createGroup(String templateName, ArrayList items); //<IdentifierInterface>
	
	/**
	 * Registers a group template to be used for subsequent group creation,
	 * where the user has specified which interfaces that the group should
	 * make available to any component bound to that group.
	 * Please note the created template name is only valid locally, for one node in the 
	 * system 
	 * 
	 * @param templateName
	 *            A String representing the name of the template.
	 * @param template
	 *            The group template with the interfaces of interest specified
	 * @return	True if the template was successfully registered, false if there already
	 * 		 	existed a template with that name
	 */
	public boolean registerGroupTemplate(String templateName, SNR template);
	
	/**
	 * Add a new component to an existing group
	 * 
	 * @param newItem
	 *            A ComponentId representing the component to be added to the
	 *            group. The component has to share the same interfaces as the
	 *            previous group members, as it will be automatically become
	 *            part of the existing bindings related to the group.
	 * @param groupId
	 *            The id of the existing group
	 */
	public void addToGroup(Object newItem, Object groupId);

	/**
	 * Removes a component from an existing group
	 * 
	 * @param item
	 *            A ComponentId representing the component to be removed from
	 *            the group.
	 * @param groupId
	 *            The id of the existing group
	 */
	public void removeFromGroup(Object item, Object groupId);

	/**
	 * Removes an existing group. All watchers subscribed through that group
	 * will no longer be notified of changes to the previous group members,
	 * although the components themselves will remain unaltered
	 * 
	 * @param gid
	 *            The id of the group to remove
	 * 
	 */
	public void removeGroup(GroupId gid);

	/**
	 * Binds the fractal client interface of 'client' to server interface of
	 * 'server'
	 * 
	 * @param client
	 *            Normally a ComponentId. Can also be a GroupId, in which case
	 *            bindings are created between all members of 'client' to the
	 *            server
	 * @param clientInterface
	 *            The ADL name of the client interface
	 * @param server
	 *            Either a single ComponentId or a GroupId where all group
	 *            members expose 'serverInterface'
	 * @param serverInterface
	 *            The ADL name of the server interface
	 * @return A bindId id
	 */
//	public BindId bind(Object client, String clientInterface, Object server,
//			String serverInterface) throws OperationTimedOutException;

	/**
	 * Binds the fractal client interface of the component calling the method
	 * to the server interface of 'server'
	 * 
	 * @param clientInterface
	 *            The ADL name of the client interface
	 * @param server
	 *            Either a single ComponentId or a GroupId where all group
	 *            members expose 'serverInterface'
	 * @param serverInterface
	 *            The ADL name of the server interface
	 * @param type
	 *            The type of the bindId: one-to-one, one-to-any, one-to-many,
	 *            defined by constants in *currently* JadeBindInterface
	 * @return A bindId id
	 */
	public void bind(String senderInterface, Object receiver,
			String receiverInterface, int type) throws OperationTimedOutException;
	/**
	 * Binds the fractal client interface of 'client' to server interface of
	 * 'server'
	 * 
	 * @param client
	 *            Normally a ComponentId. Can also be a GroupId, in which case
	 *            bindings are created between all members of 'client' to the
	 *            server
	 * @param clientInterface
	 *            The ADL name of the client interface
	 * @param server
	 *            Either a single ComponentId or a GroupId where all group
	 *            members expose 'serverInterface'
	 * @param serverInterface
	 *            The ADL name of the server interface
	 * @param type
	 *            The type of the bindId: one-to-one, one-to-any, one-to-many,
	 *            defined by constants in *currently* JadeBindInterface
	 * @return A bindId id
	 */
	public BindId bind(Object sender, String senderInterface, Object receiver,
			String receiverInterface, int type) throws OperationTimedOutException;

	/**
	 * Removes a previously established binding
	 * 
	 * @param binding
	 *            The id of the binding to remove.
	 */
	public void unbind(IdentifierInterface binding);

	/**
	 * Generic method to update groups or management elements. The available
	 * update-types are as of now given as constants by this class, but they
	 * might later be moved to the <code>DCMSInterface</code>
	 * 
	 * @param objectToBeUpdated
	 *            The id of the management element which should be updated
	 * @param argument
	 *            The update message, or the item to add/remove, depending on
	 *            the type
	 * @param type
	 *            The type specifying the update operation, as given by the
	 *            constants in <code>NicheComponentSupportInterface</code>
	 */
	public void update(Object objectToBeUpdated, Object argument, int type);
	
	/**
	 * Generic query method to ask queries about elements in the system.
	 * The available query-types are as of now given as constants by this class
	 * 
	 * @param queryObject
	 *            The id of the element which the query is concerning
	 * @param queryType
	 *            The type specifying the query operation, as given by the
	 *            constants in <code>NicheComponentSupportInterface</code>
	 * @return	The return type is dependent on the query - it can be a 
	 * 			single object/identifierinterface or an arraylist of
	 * 			objects/identifierinterfaces 
	 */
	public Object query(IdentifierInterface queryObject, int queryType);

	/**
	 * Gives components access to the local resource manager, which can be used
	 * to get the component id based on the ADL name
	 * 
	 * @return The local resource manager
	 */
	public SimpleResourceManager getResourceManager();

}
