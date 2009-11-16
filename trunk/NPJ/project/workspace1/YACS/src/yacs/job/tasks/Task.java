package yacs.job.tasks;

import java.io.Serializable;

import yacs.job.interfaces.TaskExecutionContext;
import yacs.job.TaskContainer;

import yacs.utils.YacsLogger;

/**
 * This is the base class for all tasks that the service should be able to process.
 * It contains an abstract function called process which the client programmer should implement with
 * the necessary logic related to that task.
 * @author LTDATH
 */
public abstract class Task {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Name used in log message format. Can therefore be useful for log scanning.
	 */
	protected String logName;
	
	
	/**
	 * Handle to the TaskContainer that contains the task. Since the Task itself is not serializable
	 * the TaskContainer will be used to carry important status, result and data variables.
	 * Will also be used to access YACS system services available to the task, e.g. storage management,
	 * through the Worker that is actually processing the task.
	 */
	private TaskContainer container;
	
	/**
	 * A logger which is possibly connected to the execution context logger.
	 * If not it will only log to the console.
	 */
	private YacsLogger logger;
	
	/**
	 * Handle to the Worker that is actually processing the task, 
	 * e.g. to get access to YACS services like checkpointing and storage management
	 */
	private TaskExecutionContext executionContext;
	
	/**
	 * This function is invoked by the worker to begin execution of the particular task's functionality
	 */
	public abstract void execute();
	/**
	 * Each task must implement this as a way of interpreting the initial/setup parameters that are supplied
	 * with the task. These might be the absolute initial parameters or the latest meta-checkpointed state.
	 * For example, from a "checkpoint" state the task programmer will interpret the values to set up
	 * the state as it was at the time of checkpointing. This can include a location to a more substantial
	 * checkpoint file, e.g. with tens of MBs of data, that the task programmer should be able to retrieve
	 * using some means and then further interpret or use.
	 */
	public void initFromState(){}
	/**
	 * This function will be called if a task is being re-initialized at a replacement worker after the
	 * initial worker has failed. It's role is very similar to that of initFromState, i.e. initialize task
	 * state from supplied state array. However, this function will signify to the task programmer that
	 * the task is being re-initialized and enables the programmer, for whatever reasons, to initialize
	 * differently in those circumstances.<br>
	 * The Task superclass implementation of this function will simply call: initFromState(). 
	 */
	public void reinitFromState(){
		initFromState();
	}
	
	/**
	 * Tasks should override this function if they need to do task-specific state to state-array
	 * conversion before the TaskProcessor performs actual meta-checkpointing of the container, including array.
	 * Depending on the task, it should be able to restore sufficient state from this state-array.
	 * This state-array is NOT meant to store large value, only the bare mininum of information needed.
	 * For example, it should not contain large checkpoint data chunks but rather the location of where
	 * that data chunk could be found, e.g. an overlay address of a file.
	 */
	public void prepareMetacheckpoint(){}
	
	/**
	 * Convenience function for logging. Will log to Worker/Execution context log
	 * if has been connected, else to console.
	 * @param message The information to be logged
	 */
	public void log( String message ){
		// TODO: configure YacsLogger, in a convenient, but customizable way for the task implementation
		// or should I just provide a simple configure interface, i.e. to hook up to the "real"
		// Niche logger and let the task do the rest?
		if( logger != null )
			logger.log( message );
		else
			System.err.println( message );
	}
	
	/**
	 * Information about task which might be useful for logging, e.g. task name/type.
	 * Right now only sets name
	 */
	protected void initTaskInfo(){
		logName = "UTask"; // U = unnamed
	}
	
	// BEGIN: getters and setters
	/**
	 * Get unique id of the task. Uniquely identifies a task within a particular job.
	 * @return Id of the task
	 */
	public int getTid(){
		return container.getTid();
	}

	/**
	 * Get the processing status of task. Service believes task to be processing until value equals YACSNames.TASK_FINISHED.
	 * @return Processing status of task. 
	 */
	public int getStatus() {
		return container.getStatus();
	}
	/**
	 * Set the processing status of the task.
	 * @param status The processing status of the task. See YACSNames.TASK_* for ideas.
	 */
	public void setStatus(int status) {
		container.setStatus( status );
	}

	/**
	 * Logical result of the task. Set by functional programmer in implementation of process function.
	 * @return The logical result of the task.
	 */
	public int getResultCode() {
		return container.getResultCode();
	}
	/**
	 * Set the logical result of the task.
	 * @param resultCode The logical result of the task.
	 */
	public void setResultCode(int resultCode) {
		container.setResultCode( resultCode );
	}

	/**
	 * Can the task be re-deployed to another Worker in case of "current" Worker failure?
	 * Default value is true. 
	 * @return True if can be re-deployed, else false.
	 */
	public boolean isRedeployable() {
		return container.isRedeployable();
	}
	/**
	 * Set if the task is re-deployable or not.
	 * @param redeployable Boolean value indicating it can be done or not.
	 */
	public void setRedeployable(boolean redeployable) {
		container.setRedeployable( redeployable );
	}

	/**
	 * Initialization parameters or other values of interest can be transported along with the task
	 * to the eventual worker. Before invoking the process function this function will be called.
	 * The functional programmer can then assume to have access to those values in the process
	 * function. This is especially relevant for the case when the task is simply a Java class which
	 * will be instantiated at the Worker, i.e. not an already instantiated at the client and loaded
	 * with values before submission to the service. 
	 * @return An array of user defined values relevant to the task.
	 */
	public Serializable[] getState() {
		return container.getInitParams();
	}
	/**
	 * Initialization parameters or other values of interest can be transported along with the task
	 * to the eventual worker. Before invoking the process function this function will be called.
	 * The functional programmer can then assume to have access to those values in the process
	 * function. This is especially relevant for the case when the task is simply a Java class which
	 * will be instantiated at the Worker, i.e. not an already instantiated at the client and loaded
	 * with values before submission to the service. 
	 * @param state An array of user defined values which are relevant to the logic in the process function
	 */
	public void setState(Serializable[] state) {
		container.setInitParams( state );
	}

	/**
	 * To get access to the task container, which contains serializable variable important for
	 * meta-checkpointing and re-deployment. Should never be null.  
	 * @return Reference to the container.
	 */
	protected TaskContainer getContainer(){
		return container;
	}
	/**
	 * Set access to the task container, which contains serializable variable important for
	 * meta-checkpointing and re-deployment  
	 * @param container Reference to the container.
	 */
	public void setContext( TaskContainer container ){
		this.container = container;
		this.executionContext = container.getExecutionContext();
		
		this.initTaskInfo();
		this.logger = executionContext.createLogger(	logName, String.valueOf(getTid()), 
														true, true );
	}

	/**
	 * Handle to the Worker component that is processing the task.
	 * This interface should provide access to YACS services available to the task, e.g. storage management.
	 * @return A handle to the Worker
	 */
	protected TaskExecutionContext getExecutionContext() {
		return executionContext;
	}

	protected YacsLogger getLogger(){
		// TODO: make the logger a protected member instead?
		return this.logger;
	}
}
