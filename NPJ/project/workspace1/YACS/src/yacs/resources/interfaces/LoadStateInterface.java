package yacs.resources.interfaces;

import yacs.resources.data.*;

/**
 * For ResourceService components to push events about high/low resource service load and/or
 * resource availability to the self-management. 
 * @author LTDATH
 *
 */
public interface LoadStateInterface {
	
	/**
	 * Send load information to the self-management part.
	 * @param load Information about the load state
	 */
	public void loadState( LoadInformation load );
	
	/**
	 * Send availability information to the self-management part.
	 * @param availability Information about availability state
	 */
	public void availabilityState( AvailabilityInformation availability );

}
