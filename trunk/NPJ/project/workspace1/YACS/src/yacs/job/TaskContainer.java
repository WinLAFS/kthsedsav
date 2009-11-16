package yacs.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;

import dks.niche.ids.ComponentId;
import yacs.interfaces.YACSNames;
import yacs.job.interfaces.TaskExecutionContext;
import yacs.job.tasks.*;
import yacs.job.tasks.helpers.*;
import yacs.utils.YacsLogger;

// TODO: static interface on a task which knows how to read from the array?
/**
 * Class for containing a Task implementation that a Worker will eventually process.
 * @author LTDATH
  */
public class TaskContainer implements Serializable, Comparable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	///////////////// serializable variables that are maintained e.g. in meta-checkpoints ///////////////////
	/**
	 * Id of the contained task. Has to be unique among all tasks in a job.
	 */
	private int tid;
	/**
	 * A user defined result code representing the logical conclusion of the job. Not used by the service itself?
	 */
	private int resultCode = YACSNames.RESULT_NOT_SET;
	/**
	 * Status of task processing. Initialized with YACSNames.TASK_NOT_FINISHED and believed
	 * by service to be processing until set with YACSNames.TASK_FINISHED.  
	 */
	private int status = YACSNames.TASK_IS_PROCESSING;
	/**
	 * Is it OK to re-deploy the task in case of Worker failure?
	 * For example, if the task contains logic that can only be run once this should be set to false.
	 * Default is true. 
	 */
	private boolean redeployable = YACSNames.DEFAULT_REDEPLOYABLE;
	/**
	 * The fully qualified name of the class. Will be used by the ClassLoader to load the class.
	 */
	private String className;
	/**
	 * A byte array storing the class file defining the task contained.
	 */
	private byte[] classDef;
	/**
	 * Initialization parameters and other values relevant to the task, as freely interpreted by the task programmer.
	 * Used initially by the task creator and submitter, and subsequently for meta-checkpointing of tasks.
	 * The container will load this into the Task before calling the process function of the task. 
	 */
	private Serializable[] initParams = null;
	/**
	 * The current Worker component responsible for processing the Task.
	 * Used by the Master to keep track of where the Tasks are.
	 */
	private ComponentId worker;
	/**
	 * Name/id of job that this TaskContainer is part of. Used for logging.
	 */
	private String jobId;
	
	///////////////// transient variable that only have value once deployed on a Worker ///////////////////
	/**
	 * An instance of a Task derived class containing the functional logic of the task.
	 * It will be instantiated from the class contained in classDef byte[] array, through Class.newInstance().
	 * It is a transient variable and will not be transported between clients, masters or workers along
	 * with the TaskContainer.
	 */
	private transient Task instantiatedTask = null;
	/**
	 * For access to the Worker component processing the Task. Should eventually be used to access
	 * YACS system services available to the task, e.g. storage management.
	 */
	protected transient TaskExecutionContext actualTaskProcessor;
	
	protected transient YacsLogger logger;
	
	
	/**
	 * Create a container
	 */
	private TaskContainer( int tid, boolean redeployable, String className, Serializable[] initParams ){
		this.tid = tid;
		this.redeployable = redeployable;
		this.className = className;
		this.initParams = initParams;
	}
	
	
	/**
	 * Contain a class description which will be instantiated at the Worker.
	 * Suitable for cases where it might be very expensive to transport at client instantiated tasks. 
	 * @param tid A user defined id for the task. Has to be unique among all tasks in a job.
	 * @param redeployable Can the task be re-deployed to another worker? 
	 * @param className A fully qualified name of the class. Used by a ClassLoader to load the class before instantiating it.
	 * @param classfileLocation Path to where the class file of the task can be read.
	 * @param initParams Initialization values and other values useful for the functional logic in the task. 
	 * @return a TaskContainer containing a description of a task for instantiation at a Worker.
	 * @throws Exception
	 */
	public static TaskContainer contain( int tid, boolean redeployable, String className, String classfileLocation, Serializable[] initParams ) throws Exception {
		TaskContainer t = new TaskContainer( tid, redeployable, className, initParams );
		t.wrap( classfileLocation );
		return t;
	}
	
	/**
	 * This function is invoked by the worker to begin execution of the particular task's functionality.
	 * This will cause the container to unwrap the contained Task, set it with supplied initialization
	 * variable and finally invoke it execute function.
	 * @param redeployment Is the task being re-deployed at a replacement worker?
	 */
	public void execute( boolean redeployment ){
		log( "TaskContainer.execute. Redep: " + redeployment );
		
		try {
			unwrap();
			
			// errors occurring in initializing from meta-checkpoint are programmer errors, not service
			// since it is their task to interpret the meta-checkpoint
			instantiatedTask.setContext( this );
			if( !redeployment )
				instantiatedTask.initFromState(); // interpret the initParams/state array
			else
				instantiatedTask.reinitFromState();  // interpret the initParams/state array
			
			this.setStatus( YACSNames.TASK_IS_PROCESSING ); // change from not_instantiated to "being processed"
			instantiatedTask.execute();
		}
		// TODO: catch deployment and other service errors => task.status=TASK_FAILED, else TASK_COMPLETED!
		// e.g. have all service exception derive from YACSException
		// for some reason catch( Exception ) wasn't catching the following
		catch( java.lang.NoClassDefFoundError de ){
			de.printStackTrace();
			// error in classloading... probably service fault?
			// but might be bad class data from user... 
			// hmmm, should maybe assume everything except not-finding a Worker or explicit YACSExceptions are task errors, not service errors 
			this.setStatus( YACSNames.TASK_FAILED );
		}
		catch( Exception e ){
			e.printStackTrace();
			if( this.getStatus() != YACSNames.TASK_COMPLETED && this.getStatus() !=  YACSNames.TASK_FAILED ){
				// assume other exceptions to be programmers "fault"
				this.setStatus( YACSNames.TASK_COMPLETED );
			}
		}
		finally {
			// to be absolutely sure the task never becomes a zombie
			if( this.getStatus() != YACSNames.TASK_COMPLETED && this.getStatus() !=  YACSNames.TASK_FAILED )
				this.setStatus( YACSNames.TASK_COMPLETED );
		}
	}
	
	/**
	 * The TaskProcessor, i.e. the Worker, will call this function on the TaskContainer when
	 * it is meta-checkpointing a task, whether from it is own accord or through
	 * external checkpoint invocation. The TaskContainer will in turn call the
	 * prepareCheckpoint function on the contained task.
	 * In this way the contained task will be explicitly informed when it should convert its state
	 * to the state-array that will be meta-checkpointed.
	 */
	public void prepareCheckpoint(){
		this.instantiatedTask.prepareMetacheckpoint();
	}
	
	// BEGIN: helpers
	/**
	 * Convert transported byte arrays into meaningful objects, 
	 * i.e. the class definition for loading by the ClassLoader and a task instance for processing. 
	 */
	private void unwrap() throws Exception {
		log("TaskContainer.unwrap:");
		
		// debug
		/*{
			long cs = 0;
			for( byte b : classDef )
				cs += b;
			log("\tBOS-CD cs: " + cs + ", byte[] size: " + classDef.length );
		}*/
		
		// TODO: can avoid always loading task, i.e. if Worker already know Task class...?
		//ClassLoader cl = ClassLoader.getSystemClassLoader();
		ClassLoader cl = this.getClass().getClassLoader() != null ? this.getClass().getClassLoader() :
																	ClassLoader.getSystemClassLoader();
		TaskClassLoader loader = new TaskClassLoader( cl );
		Class taskClass = loader.loadClass(this.className, classDef);
		log("\tLoaded: " + taskClass.getName() + ", w/CL: " + cl);
		
		instantiatedTask = (Task)taskClass.newInstance();
		//contained.setTid( this.tid );
		log("\tInstantiation done!");
	}
	/**
	 * Wrap a task class definition contained in the classfile in a byte array for
	 * transportation to a Worker. Will be loaded by a ClassLoader.
	 * @param classfileLocation Location classfile on disk
	 * @throws Exception
	 */
	private void wrap( String classfileLocation ) throws Exception {
		log("TaskContainer.wrap:");
		classDef = readClass( classfileLocation );
		
		// debug
		/*long cs = 0;
		for( byte b : classDef )
			cs += b;
		log("\tBOS-CD cs: " + cs + ", byte[] size: " + classDef.length );*/
	}
	
	/**
	 * Read the classfile and convert to bytes.
	 * @param classfileLocation Location of classfile on disk
	 * @return byte array of classfile contents
	 * @throws Exception
	 */
	private byte[] readClass( String classfileLocation ) throws Exception {
		File f = new File( classfileLocation );
		int flen = (int)f.length();
		
		FileInputStream reader = new FileInputStream( f );
		
		byte[] classbuffer = new byte[flen];
		
		int read=0;
		while( read<flen ){
			read += reader.read( classbuffer, read, flen-read );
		}
		
		return classbuffer;		
	}
	
	public void merge( TaskContainer rhs ){
		this.setResultCode( rhs.getResultCode() );
		this.setStatus( rhs.getStatus() );
		this.setRedeployable( rhs.isRedeployable() );
		this.setInitParams( rhs.getInitParams() );
		this.setWorker( rhs.getWorker() );
	}
	
	protected void log( String message ){
		if( logger != null )
			logger.log( message );
		else
			System.err.println( message );
	}
	
	public int compareTo( Object rhsObj ){
		
		// an existing object must be greater than null
		if( rhsObj == null )
			return 1;
		
		// hmmm
		if( !(rhsObj instanceof TaskContainer) )
			return 1;
		
		TaskContainer rhs = (TaskContainer)rhsObj;
		
		if( this.getTid() == rhs.getTid() )
			return 0;
		else if( this.getTid() < rhs.getTid() )
			return -1;
		else // this.tid > rhs.tid
			return 1;
	}
	
	/**
	 * Combined id of job+task. Only for improving uniqueness of logging.
	 * @return String representation of combined id of job+task. If job.id has not been set returns only the tid.
	 */
	public String getJTid(){
		if( this.jobId == null )
			return String.valueOf( this.tid );
		else
			return jobId + ":"  + this.tid;
	}
	
	// END: helpers
	
	
	// getters and setters	
	/**
	 * User defined id of contained task. Uniquely identifies a task in a particular job.
	 * @return Id of the contained task.
	 */
	public int getTid() {
		return this.tid;
	}
	/**
	 * User defined id of contained task. Has to be unique among all tasks in a job.
	 * @param tid The id of the task
	 */
	public void setTid(int tid) {
		this.tid = tid;
	}

	/**
	 * Functional result of task processing.
	 * If initialization or conversion from serialized form failed this function will return YACSNames.RESULT_TASK_INITALIZATION_FAILED.
	 * If initialization or conversion from serialized form is not finished this function will return YACSNames.RESULT_NOT_SET.
	 * In all other cases this function will return a value set by the programmer of the respective task, i.e. in the process function.
	 * @return The result of the task.
	 */
	public int getResultCode() {
		return this.resultCode;
	}
	/**
	 * Functional result of task.
	 * @param resultCode Functional result of task.
	 */
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	
	/**
	 * Processing state of the contained task.
	 * If the task has not been initialized or converted yet from its wrapped serializable state this function will return YACSNames.TASK_NOT_INITIALIZED. 
	 * If initialization or conversion of contained task into a runnable entity failed this function will return YACSNames.TASK_FINISHED.
	 * In all other cases this function will invoke the same function on the contained task, i.e. the status will be set by processing of contained task. 
	 * @return The processing state of the contained task.
	 */
	public int getStatus() {
		return this.status;
	}
	/**
	 * Processing state of the contained task.
	 * @param status Processing state of the contained task.
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	/**
	 * Can the task be re-deployed to another Worker if the "current" worker has failed or left?
	 * @return True if can be re-deployed, else false. 
	 */
	public boolean isRedeployable() {
		return this.redeployable;
	}
	/**
	 * Can the task be re-deployed to another Worker if the "current" worker has failed or left?
	 * @param redeployable True if can be re-deployed, else false. 
	 */
	public void setRedeployable(boolean redeployable) {
		this.redeployable = redeployable;
	}
	
	public Serializable[] getInitParams() {
		return initParams;
	}
	public void setInitParams(Serializable[] initParams) {
		this.initParams = initParams;
	}

	/**
	 * The current Worker component responsible for processing the Task.
	 * User by the Master to keep track of where the Tasks are.
	 * @return ComponentId of Worker responsible for processing. 
	 */
	public ComponentId getWorker() {
		return worker;
	}
	/**
	 * The current Worker component responsible for processing the Task.
	 * User by the Master to keep track of where the Tasks are.
	 * @param worker The ComponentId of the Worker which has taken responsibility for the task
	 */
	public void setWorker(ComponentId worker) {
		this.worker = worker;
	}

	// TODO: javadoc
	public String getJobId() {
		return jobId;
	}


	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
	
	


	/**
	 * A handle to access service functions like checkpointing and storage management, from functional code.
	 * @param actualTaskProcessor Reference to the Worker that should process the task
	 */
	public void setExecutionContext(TaskExecutionContext actualTaskProcessor){
		this.actualTaskProcessor = actualTaskProcessor;
		this.logger = actualTaskProcessor.createLogger( "TaskContainer", String.valueOf(tid), 
														true, true );
	}
	/**
	 * Get a reference to the Worker that is actually processing the task, and through it
	 * access to services that YACS offers, such as meta-checkpointing, storage management and so on.
	 * @return Reference to the Worker/Service
	 */
	public TaskExecutionContext getExecutionContext(){
		return this.actualTaskProcessor;
	}
	
	
	// temp debug
	public void debug_printMetacheckpoint(){
		/*log("\tTaskContainer.META_CHECKPOINT:");
		log("\tParams: " + this.initParams);
		if( this.initParams != null ){
			log("\t\tLength: " + this.initParams.length );
			int i = 1;
			for( Serializable obj : initParams ){
				log("\t\t\t"+(i++)+" is: " + (obj==null?"NULL":obj.getClass()) );
			}
		}*/
	}
}
