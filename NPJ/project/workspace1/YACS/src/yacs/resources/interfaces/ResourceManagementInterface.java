package yacs.resources.interfaces;

import yacs.resources.data.*;

/**
 * Used by ResourceService components for group internal cooperation
 * @author LTDATH
 */
public interface ResourceManagementInterface {

	/**
	 * Used by a newly added ResourceService component to get the "latest" system resource state from
	 * an existing component.
	 * @return Latest known state of resources within the system
	 */
	public ResourceState getState();
	
	/**
	 * Resources periodically advertise their state to the resource service group. Any one
	 * component of the group receives this state. At an appropriate time this component will
	 * disseminate this information to other ResourceService components within the group. This
	 * function is used to notify others of a new resource within the system.
	 * @param resource Information about a new resource within the system.
	 * @param sender Piggyback information about the sending ResourceService component. For primitive group management.
	 */
	public void newResource( ResourceInfo resource, ResourceInfo sender );
	/**
	 * Resources periodically advertise their state to the resource service group. Any one
	 * component of the group receives this state. At an appropriate time this component will
	 * disseminate this information to other ResourceService components within the group. This
	 * function is used to notify others of an updated resource within the system.
	 * @param resource Information about an updated resource within the system.
	 * @param sender Piggyback information about the sending ResourceService component. For primitive group management.
	 */
	public void updatedResource( ResourceInfo resource, ResourceInfo sender );
	/**
	 * Resources periodically advertise their state to the resource service group. Any one
	 * component of the group receives this state. At an appropriate time this component will
	 * disseminate this information to other ResourceService components within the group. This
	 * function is used to notify others of resource departure from the system.
	 * @param resource Information about a departed resource from the system.
	 * @param sender Piggyback information about the sending ResourceService component. For primitive group management.
	 */
	public void departedResource( ResourceInfo resource, ResourceInfo sender );
	
	/**
	 * Resources periodically advertise their state to the resource service group. Any one
	 * component of the group receives this state. At an appropriate time this component will
	 * disseminate this information to other ResourceService components within the group. This
	 * function is used to notify others of that particular component's system view.
	 * @param state Entire system state as known by the sender
	 * @param sender Piggyback information about the sending ResourceService component. For primitive group management.
	 */
	public void systemResourceState( ResourceState state, ResourceInfo sender );	
}
