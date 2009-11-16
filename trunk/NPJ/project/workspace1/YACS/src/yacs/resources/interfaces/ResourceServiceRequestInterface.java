package yacs.resources.interfaces;

import yacs.resources.data.*;

/**
 * For clients to request resources from the resource service.
 * @author LTDATH
 */
public interface ResourceServiceRequestInterface {
	/**
	 * Used by clients of the resource service to request resources.
	 * @param specs Specification of resources needed
	 * @return Information about found resources, if any
	 */
	public ResourceRequest request( ResourceRequest specs );
}
