package yacs.job.interfaces;

import yacs.job.Job;
import yacs.job.state.JobCheckpoint;
import yacs.job.helpers.SubmissionReply;

/**
 * The interface through which the client interacts with a Master component,
 * i.e. the Master component which will be responsible for managing the submitted job.
 * @author LTDATH
 */
public interface JobManagementInterface {
	
	/**
	 * Submit a job that is to be performed, i.e. managed, by a particular Master component. 
	 * @param job An instance of a Job class containing a remaining list of Tasks
	 * @param redeployment Flag not normally used by a regular client but a MasterWatcher management element when it re-deploys a job to another Master upon previous Master's failure.
	 * @return A reply which tells if the job was accepted or not.
	 */
	public SubmissionReply performJob(Job job, boolean redeployment);
	public SubmissionReply performJob(JobCheckpoint jobCP);
	/**
	 * Delete a previously submitted job.
	 * @param job The previously submitted job. Not currently used.
	 * @return True if successful, false otherwise
	 */
	public boolean deleteJob( Job job );
}
