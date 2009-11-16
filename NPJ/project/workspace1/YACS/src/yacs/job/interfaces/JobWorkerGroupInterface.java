package yacs.job.interfaces;

import dks.niche.ids.GroupId;

/**
 * For communication with an entire Worker group formed around a particular Job
 * @author LTDATH
 */
public interface JobWorkerGroupInterface {
	
	/**
	 * Self-management WorkerWatcher can use this to "pull" the latest task states, e.g. when recovering after departure.
	 * @param jobOwner The group of Masters which "own" the workers. Now workers become free immediately after finishing a task so they can be in many job-groups. This can be used to distinguish between. 
	 * @param dummy Dummy
	 */
	public void publishState( GroupId jobOwner, boolean dummy );

}
