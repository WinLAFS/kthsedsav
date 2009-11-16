package yacs.job;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Vector;

import dks.niche.ids.*;

/**
 * A container for user-defined tasks meant for processing by Workers.
 * @author LTDATH
 */
public class Job implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * User defined name of job.
	 */
	private String name;
	/**
	 * The id of the component that submitted the job.
	 */
	private ComponentId creator;
	/**
	 * The id of a group containing Master components responsible for the job. 
	 * Now the group only contains one Master.
	 */
	private GroupId masterGroup;
	/**
	 * The id of a group containing all Worker component processing tasks related to this job.
	 */
	private GroupId workerGroup;
	
	private boolean deleted = false;
	
	
	// TODO: got to be a better way of doing this other than maintaining 4 lists, e.g. hashtable+status
	/**
	 * List of tasks which have not been assigned to a worker.
	 */
	private Vector<TaskContainer> remaining = new Vector<TaskContainer>();
	/**
	 * List of tasks which have been assigned to a worker but which processing is not finished.
	 */
	private Vector<TaskContainer> pending = new Vector<TaskContainer>();
	/**
	 * List of tasks that the service completed successfully. Could contain tasks with functional errors.
	 */
	private Vector<TaskContainer> done = new Vector<TaskContainer>();
	/**
	 * List of tasks that completed with a service error.
	 */
	private Vector<TaskContainer> failed = new Vector<TaskContainer>();
	
	
	/**
	 * Construct a named job 
	 * @param jobName A user defined name for the job. Not used by service except for logging.
	 */
	public Job(String jobName){
		this.name=jobName;
	}
	
	/**
	 * 
	 */
	public synchronized TaskContainer getTask( int tid ){
		
		for( TaskContainer task : remaining ){
			if( task.getTid() == tid )	return task;
		}
		for( TaskContainer task : pending ){
			if( task.getTid() == tid )	return task;
		}
		for( TaskContainer task : done ){
			if( task.getTid() == tid )	return task;
		}
		for( TaskContainer task : failed ){
			if( task.getTid() == tid )	return task;
		}
		
		return null;
	}
	
	// getters and setters
	/**
	 * User defined name of task. Not used by service except for logging.
	 * @return The name of the job.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * The id of the group containing the Master(s) responsible for the job.
	 * @return The master Group-Id 
	 */
	public GroupId getMasterGroup() {
		return masterGroup;
	}
	/**
	 * The id of the group containing the Master(s) responsible for the job.
	 * @param masterGroup The master Group-Id
	 */
	public void setMasterGroup(GroupId masterGroup) {
		this.masterGroup = masterGroup;
	}

	/**
	 * The id of the group containing the Worker(s) responsible for the tasks of the job.
	 * @return The worker Group-Id 
	 */
	public GroupId getWorkerGroup() {
		return workerGroup;
	}
	/**
	 * The id of the group containing the Worker(s) responsible for the tasks of the job.
	 * @param workerGroup The worker Group-Id
	 */
	public void setWorkerGroup(GroupId workerGroup) {
		this.workerGroup = workerGroup;
	}
	
	/**
	 * List of tasks which have not been assigned to a worker.
	 * @return List of tasks which have not been assigned to a worker.
	 */
	public Vector<TaskContainer> getRemaining() {
		return remaining;
	}
	/**
	 * List of tasks which have been assigned to a worker but which processing is not finished.
	 * @return List of tasks which have been assigned to a worker but which processing is not finished.
	 */
	public Vector<TaskContainer> getPending() {
		return pending;
	}
	/**
	 * List of tasks that the service completed successfully. Could contain tasks with functional errors.
	 * @return List of tasks that the service completed successfully. Could contain tasks with functional errors.
	 */
	public Vector<TaskContainer> getDone() {
		return done;
	}
	/**
	 * List of tasks that completed with a service error.
	 * @return List of tasks that completed with a service error.
	 */
	public Vector<TaskContainer> getFailed() {
		return failed;
	}
	/**
	 * Id of client component which submitted job. Not used right now.
	 * @return Id of client component which submitted job.
	 */
	public ComponentId getCreator() {
		return creator;
	}
	/**
	 * Set the id of the client which submitted the job.
	 * @param creator Id of submitting client. 
	 */
	public void setCreator(ComponentId creator){
		this.creator = creator;
	}

	/**
	 * Tells if the job has been, or is in the process of being deleted form the system.
	 * @return True if being/been deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}
	/**
	 * Indicate the job has been, or is in the process of being deleted from the system.
	 * @param deleted Is the job being deleted?
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}	

	// multiple threads will be using the job within the Master component
	private transient int remainingIterator=0;
	public synchronized void resetRemainingIterator(){remainingIterator=0;}
	public synchronized TaskContainer getNextRemaining(){
		/*if( remainingIterator >= this.remaining.size() )
			return null;
		
		return remaining.get( remainingIterator++ );*/
		try {
			return remaining.get( remainingIterator++ );
		}
		catch( ArrayIndexOutOfBoundsException e ){
			return null;
		}
	}
	
	private transient int pendingIterator=0;
	public synchronized void resetPendingIterator(){pendingIterator=0;}
	public synchronized TaskContainer getNextPending(){
		/*if( pendingIterator >= this.pending.size() )
			return null;
		
		return pending.get( pendingIterator++ );*/
		
		try {
			return pending.get( pendingIterator++ );
		}
		catch( ArrayIndexOutOfBoundsException e ){
			return null;
		}
	}
}
