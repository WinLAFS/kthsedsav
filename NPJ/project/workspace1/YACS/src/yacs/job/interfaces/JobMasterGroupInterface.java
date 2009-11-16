package yacs.job.interfaces;

import dks.niche.ids.*;
import yacs.job.*;

/**
 * For communication with an entire Master group formed around a particular Job
 * @author LTDATH
 */
public interface JobMasterGroupInterface {
	
	/**
	 * The MasterWatcher advertises for Worker to perform tasks. This interface can be used to volunteer for processing those tasks.
	 * Not currently used.
	 * @param worker Id of the worker component.
	 * @param dummy Dummy
	 */
	public void volunteer( ComponentId worker, boolean dummy );
	
	/**
	 * Push interface for a Worker component to report Task and status to Master(s).
	 * Notable use: during re-deployment of a Task to another Worker that Worker informs the Master of the new processing "location"
	 * @param task The task being processed.
	 * @param worker Id of worker processing the task.
	 * @param dummy Dummy
	 */
	public void reportTaskStatus( TaskContainer task, ComponentId worker, boolean dummy );
		
	/**
	 * Self-management WorkerWatcher informing Master(s) that it is ready for work.
	 * Currently the Master doesn't start assigning tasks until the self-management is correctly initalized.
	 */
	public void workerWatcherInitalized( boolean dummy );
	/**
	 * Self-management MasterWatcher informing Master(s) that it is ready for work.
	 * Not currently used.
	 */
	public void masterWatcherInitalized( boolean dummy );
	
	/**
	 * WorkerWatcher informing the Master(s) if the self-management can't recover from worker resource departure  
	 * @param task Information about the task that failed
	 */
	public void irrecoverableWorkerFailure( TaskContainer task );
	/**
	 * Self-management MasterWatcher can use this to "pull" the latest job state, e.g. when recovering after departure.
	 * @param dummy Dummy
	 */
	public void publishState( boolean dummy );
}
