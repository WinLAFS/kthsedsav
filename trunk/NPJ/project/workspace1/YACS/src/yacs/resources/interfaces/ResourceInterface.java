package yacs.resources.interfaces;

import yacs.resources.data.ResourceInfo;
import yacs.utils.YacsLogger;

/**
 * Interface for the ResourceReporter to get status information about the owning resource, 
 * i.e. the one it is reporting on. 
 * @author LTDATH
 */
public interface ResourceInterface {
	
	/**
	 * For logging. Get the id of the resource.
	 * @return String representation of resource id
	 */
	public String getId();
	
	/**
	 * The ResourceReporter uses this function to get the status information it will report to the resource service.
	 * @return Status info and specification of the resource
	 */
	public ResourceInfo getStatusInfo();
	
	public YacsLogger createLogger(	String name, String id, boolean toNiche, boolean toConsole );
}
