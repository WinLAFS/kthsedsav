package yacs.resources.interfaces;

import yacs.resources.data.*;

/**
 * For resources to report their state and specs to the resource service 
 * @author LTDATH
 */
public interface ResourceServiceStateInterface {
	
	/**
	 * Used by resources within the system to (periodically) notify the resource service
	 * of their existence and state within the system
	 * @param resourceInfo Detailed information about the resource
	 */
	public void advertise( ResourceInfo resourceInfo );
}
