package yacs.job.interfaces;

import yacs.job.Job;
import yacs.job.TaskContainer;

/**
 * Interface which the client needs to implement to get task change notification and job result back from the Master(s)
 * @author LTDATH
 */
public interface JobResultInterface {
	
	/**
	 * Used to notify the client of Task(Container) changes which happen within YACS.
	 * Currently these changes are aggregated by the Master and then reported at a short interval  
	 * @param task Information about the TaskContainer that was change, including service status and current Worker.
	 */
	public void receiveTaskChange( 	TaskContainer task );
	/**
	 * Used to return a Job result to the client.
	 * @param result A Job object containing the previously submitted Tasks, grouped in lists by status.
	 */
	public void receiveJobResult(	Job result);

}
