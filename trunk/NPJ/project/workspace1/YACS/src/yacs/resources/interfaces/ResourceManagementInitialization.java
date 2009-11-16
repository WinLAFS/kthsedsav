package yacs.resources.interfaces;

import yacs.resources.data.*;

/**
 * Used by a ResourceService upon initialization, when its just 
 * been added to the resource service group, to get the latest system state.
 * It should be bound to ANY in the group, excluding self. The other resource management
 * interface, i.e. ResourceManagement, will be permanently bound to ALL of the group, excluding self. 
 * @author LTDATH
 */
public interface ResourceManagementInitialization {

	/**
	 * Used by a newly added ResourceService component to get the "latest" system resource state from
	 * an existing component.
	 * @return Latest known state of resources within the system
	 */
	public ResourceState getState();
}
