package yacs.job.interfaces;

import yacs.utils.YacsLogger;


/**
 * An interface through which the Task programmer can access YACS service functions.<br><br>
 * Right now it contains storage management functions store, read, delete but behind those
 * is <b>NOT</b> a real implementation. Just a single storage component that does everything.<br><br>
 * It also contains a checkpoint function which has a more reasonable implementation. 
 * The functional programmer can use this function to push important information to the management elements.
 * @author LTDATH
 */
// TODO: rename interface to something more appropriate, like YACSServiceInterface
public interface TaskExecutionContext {

	/**
	 * Force storing of values important to the task in the management element monitoring the group, i.e the WorkerWatcher.
	 * Will store the TaskContainer and contained task description and variables, 
	 * most importantly the initParams/value array, i.e. a meta-checkpoint.
	 * Upon re-deployment the Task instance will be feed the initParams array from which it should
	 * restore itself to a known state.
	 * The initParams array should not be used to store large values, rather the location
	 * of containers for those large values, e.g. checkpoint files tens of MB large stored in the cloud.
	 */
	public void metacheckpoint();
	
	/**
	 * Copy file from one location to another. 
	 * Part of a primitive storage management function meant to hide away the details of the
	 * underlying implementation, e.g. if the implementation of a shared NFS system this will simply
	 * be a copy function on mounted drives. 
	 * @param from Location of source file
	 * @param to Destination of source file copy
	 */
	public void copyFile( Object from, Object to ) throws Exception;
	public boolean deleteFile( Object location ) throws Exception;
	
	// TODO: reconsider this with respect to how tasks should log
	public YacsLogger createLogger(	String name, String id, 
									boolean toNiche, boolean toConsole );
}
