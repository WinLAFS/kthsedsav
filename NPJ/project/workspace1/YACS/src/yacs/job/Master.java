package yacs.job;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;

import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.ids.BindId;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;
import dks.niche.interfaces.NicheActuatorInterface;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Random;

import yacs.interfaces.*;
import yacs.job.helpers.*;
import yacs.job.interfaces.InformationInterface;
import yacs.job.interfaces.JobManagementInterface;
import yacs.job.interfaces.JobMasterGroupInterface;
import yacs.job.interfaces.StateChangeInterface;
import yacs.job.interfaces.TaskManagementInterface;
import yacs.job.interfaces.JobResultInterface;
import yacs.job.state.*;
import yacs.resources.data.ResourceInfo;
import yacs.resources.interfaces.*;
import yacs.resources.helpers.*;
import yacs.resources.data.ResourceRequest;
import yacs.utils.YacsLogger;
import yacs.utils.YacsUtils;
import yacs.utils.YacsTimer;

public class Master extends yacs.YacsComponent implements 
	JobManagementInterface, InformationInterface, JobMasterGroupInterface,
	ResourceInterface,
	BindingController, LifeCycleController 
{
	private Component myself;
	
	NicheComponentSupportInterface nicheOSSupport;
	NicheAsynchronousInterface logger;
	private NicheActuatorInterface actuator;
	
	private boolean status;
	
	ComponentId myGlobalId;
	String myLocation;
	int totalSpace;
	int freeSpace;
	
	private TaskManagementInterface taskManagement;
	private StateChangeInterface stateChangeInterface;
	private JobResultInterface jobResultInterface;
	
	private ResourceServiceRequestInterface resourceServiceRequest;
	private ResourceServiceStateInterface 	resourceServiceState;
	private ResourceReporter resourceReporter;
	
	private Job myJob;
	private long uid=0, tuid=0; // unique id for checkpoints, unique id for timing logs
	private GroupId masterGroup, workerGroup;
	private Hashtable<String,String> seenWorkers = new Hashtable<String,String>();
	private boolean jobDeleted = !YACSSettings.TEST__BUSY_UNTIL_DELETED;
	private long jobStartTime;
	private Vector<ComponentId> cachedWorkers = new Vector<ComponentId>();
	private Vector<TaskContainer> changesToReport = new Vector<TaskContainer>();
	private Random rand = new Random();
	private String jobId = null;
	
	private boolean workerWatcherInitalized = false;
	private ResourceInfo myInfo = null;
	private long cstart;
	private boolean utilizationTimed = false;
	
	private JobManagementThread mgmtThread;
	
	public Master(){
		log("Master created: "+this + " " + YACSSettings.YACS_VERSION);
	}
	
	// jobManagement-interface
	public boolean deleteJob( Job job ){
		log("Master.deleteJob: " + job.getName() +". Curr: " + (myJob==null?"NULL!":jobId));
		
		if( myJob == null ){
			return false;
		}
		
		jobDeleted = true;
		return jobDeleted;
	}
	public SubmissionReply performJob(JobCheckpoint jobCP){
		return this.performJob_( jobCP.getJob(), jobCP.getVersion()+1, jobCP.getSeenWorkers(), true );
	}
	public SubmissionReply performJob(Job job, boolean redeployment){
		return performJob_( job, 0, new Hashtable<String,String>(), redeployment ); 
	}
	protected synchronized SubmissionReply performJob_(Job job, long uidParam, Hashtable<String,String> seenWorkersParam, boolean redeployment){
		// TODO: on master redeployment, restore job checkpoint uid also?
		log("Master.performJob: "+job.getName()+"-t:"+job.getRemaining().size() + "-rd:" + redeployment +". Now: "+myJob);
		log("\tM.MID-for-log: " + myGlobalId.getId());
		
		if( myJob != null ){
			return new SubmissionReply(false);
		}
		myJob = job;
		this.jobId = myJob.getName(); 
		this.uid = uidParam;
		this.seenWorkers = seenWorkersParam;
		
		if( !utilizationTimed ){
			utilizationTimed = true;
			timefx( "Master",null,0,"TTFU",null,(System.currentTimeMillis()-cstart),null ); // Time To First Utilization
		}
		
		log("Job accepted: " + jobId);
		jobStartTime = System.currentTimeMillis();
		
		/**
		 * If busy then return error
		 * else
		 * 	if redeployment
		 * 		start mgmtThread
		 * 		return job.master.gid from job
		 * else
		 * 		create job.master.gid (and set into job)
		 * 		start mgmtThread
		 * 		return job.master.gid
		 */
		
		if( redeployment ){
			String jobStat = "R="+myJob.getRemaining().size()+":P="+myJob.getPending().size()+":D="+myJob.getDone().size()+":F="+myJob.getFailed().size();
			log( "Redeploy info=> Uid:"+(uid-1)+", JS:"+jobStat );
			
			// this is a redeployment of the job, master and worker groups already created
			// TODO: what if job fails in master before worker group is created?
			this.masterGroup = myJob.getMasterGroup();
			this.workerGroup = myJob.getWorkerGroup();
			
			log("Redeploy info=> MG:" + this.masterGroup.getId() + ", WG:" + this.workerGroup.getId() );
			time("Job",jobId,"RAS",null,(System.currentTimeMillis()-jobStartTime),null); // Redeployment Accepted and Set-up
		}
		else{
			// create group for master(s)... right now only one!
			ArrayList<ComponentId> masters = new ArrayList<ComponentId>();
			masters.add( this.myGlobalId );
			
			masterGroup = this.nicheOSSupport.createGroup( 	YACSTemplates.masterGroup(actuator),
															masters );
			
			myJob.setMasterGroup(masterGroup);
			
			log("\tCreated MASTER group: " + masterGroup.getId());
			time("Job",jobId,"MGC",null,(System.currentTimeMillis()-jobStartTime),null); // Master Group Created
		}
		
		this.mgmtThread = new JobManagementThread( redeployment );
		this.mgmtThread.start();
		
		return new SubmissionReply( true, myJob.getMasterGroup() );
	}

	
	// informationInterface
	public String componentType(){
		log("Master.componentType");
		return YACSNames.MASTER_COMPONENT;
	}
	public GroupId workerGroup(){
		log("Master.workerGroup: " + workerGroup);
		return this.workerGroup;
	}
	public GroupId masterGroup(){
		log("Master.masterGroup: " + masterGroup);
		return this.masterGroup;
	}
	
	// masterInterface
	private ComponentId worker_ = null;
	public void volunteer( ComponentId worker, boolean dummy ){
		log("---- ---- Master.volunteer: Got worker: " + worker + " (curr: " + this.worker_ + ")" );
		if( this.worker_ != null )
			return;
		
		this.worker_ = worker;
	}
	public void reportTaskStatus( TaskContainer task, ComponentId worker, boolean dummy ){
		long tStart = System.currentTimeMillis(), ttuid=tuid++;
		log("---- ---- Master.reportTaskStatus: T:"+task.getTid()+",W:"+worker.getId()+",S:"+task.getStatus());
				
		boolean reportChange = false;
		
		TaskContainer held = myJob.getTask( task.getTid() );
		if( held == null ){
			log("\tTask not known!?");
			timefx("Job",jobId,ttuid,"RTSNF",null,(System.currentTimeMillis()-tStart),null); // Report Task Status, Not Found
			return;
		}			
		
		reportChange = !( held.getWorker().getId().toString().equals(worker.getId().toString()) );
		// task state on worker should be more recent... merge with local
		held.merge( task );
		
		if( held.getStatus() == YACSNames.TASK_COMPLETED ){
			log("\t\tTask.#"+held.getTid()+" done!");
			if( !myJob.getPending().remove(held) ){ log("\tERROR: couldn't remove completed task!"); }
			myJob.getDone().add( held );
			reportChange = true;
			cachedWorkers.add( worker );
		}
		else if ( held.getStatus() == YACSNames.TASK_FAILED ){
			log("\t\tTask.#"+held.getTid()+" failed!");
			if( !myJob.getPending().remove(held) ){ log("\tERROR: couldn't remove failed task!"); }
			myJob.getFailed().add( held );
			reportChange = true;
			cachedWorkers.add( worker );
		}
		
		if( reportChange ){
			changesToReport.add( held );
		}
		
		String jobStat = "R="+myJob.getRemaining().size()  + ":P="+myJob.getPending().size()+ ":D="+myJob.getDone().size() + ":F="+myJob.getFailed().size();
		log("\t"+jobStat);
		timefx("Job",jobId,ttuid,"RTS",null,(System.currentTimeMillis()-tStart),null); // Report Task Status
	}
	public void workerWatcherInitalized( boolean dummy ){
		log("---- ---- Master.workerWatcherInitalized");
		workerWatcherInitalized = true;
	}
	public void masterWatcherInitalized( boolean dummy ){
		log("---- ---- Master.masterWatcherInitalized");
	}
	public void irrecoverableWorkerFailure( TaskContainer task ){
		log("---- ---- Master.irrecoverableWorkerFailure: " + task + ", tid: " + task.getTid() + ", redep? " + task.isRedeployable());
		
		// find task version that the Master is storing
		TaskContainer held = myJob.getTask( task.getTid() );
		if( held == null ){
			log("\tTask not known!?");
			return;
		}	
		
		// remove it
		this.myJob.getPending().remove( held );
		// add the task as it comes from the Watcher. It should contain the most up to date version
		// TODO: or does it? Should I maybe put a sequence number into it, which can be compared?
		// TODO: put a counter so that it won't go forever to the remaining list...?
		if( task.isRedeployable() )
			this.myJob.getRemaining().add(task);
		else
			this.myJob.getFailed().add(task);
	}
	public void publishState( boolean dummy ){
		log("---- ---- Master.publisState:");
		this.checkpointJobStatus();
	}
	
	// ResourceReporterInterface
	public String getId(){
		return this.myGlobalId.getId().toString();
	}
	public ResourceInfo getStatusInfo(){
		myInfo.updateLastUpdateTime();
		myInfo.setStatus( (myJob==null ?	YACSNames.AVAILABILITY_STATUS__FREE :
											YACSNames.AVAILABILITY_STATUS__BUSY) );
		return myInfo;
	}
	public YacsLogger createLogger(	String name, String id, boolean toNiche, boolean toConsole ){
		//this.createDerivedLogger(name, id, toNiche, toConsole);
		return new YacsLogger( name, id, toNiche, toConsole, this.logger );
	}
	
	// Internal job logic
	// Main thread responsible for job management
	class JobManagementThread extends Thread {
		private boolean redeployment;
				
		public JobManagementThread( boolean redeployment ){
			this.redeployment = redeployment;
		}
		public void run(){
			log("Master.JobManagemenThread.run: starting...");
			YacsTimer timer = new YacsTimer();
			/**
			 * Parse the job
			 * 
			 * Find available worker (is this needed before creating group?)
			 * 
			 * Create group for Workers, with current worker included
			 * 
			 * Subscribe to state-change events from the Worker group
			 * 
			 * While available tasks
			 * 	Find available worker
			 *  Add worker to worker group
			 * 	Assign task
			 * 
			 * 
			 * ----- more detail... for setup -----
			 * if not re-deployment
			 * 	create master group
			 * 	wait until change sensor deployed by the MasterWatcher is set up locally => signals that ME has been set up
			 * 	find the initial worker
			 * 	push Master.gid to initial worker. Needed since WorkerWatcher will ask its worker group who is the master => for reporting irrecoverable failures
			 * 	create the worker group
			 *  wait until the WorkerWatcher has reported to the Master, signaling completion of worker group setup => proceed to task processing
			 *  
			 * else if re-deployment
			 *  wait until change sensor deployed by the MasterWatcher is set up locally
			 *  
			 * process tasks until done
			 */
			
			// Create groups related to this job, i.e. master group to group Master components managing this job, and Worker group to group Workers doing tasks for this job.
			if( !redeployment ){
				createWorkerGroup();
				time("Job",jobId,"CWGD",null,(System.currentTimeMillis()-jobStartTime),null); // Create Worker Group Done
			}
			
			// binding back to client to report status
			bindJobResultInterfaceToClient();
			time("Job",jobId,"CRIB",null,(System.currentTimeMillis()-jobStartTime),null); // Client Result Interface Bound
			
			// wait until sensor has been deployed... meaning that the MasterWatcher has been deployed
			waitForSensorInterface();
			time("Job",jobId,"SIB",null,(System.currentTimeMillis()-jobStartTime),null); // Sensor Interface Bound
			
			// checkpoint the initial state to MasterWatcher
			checkpointJobStatus();
			time("Job",jobId,"IC",null,(System.currentTimeMillis()-jobStartTime),null); // Initial Checkpoint
						
			log("\tJOB SETUP DONE! Starting task processing...");
			int iteration=0;
			TaskContainer ct = null;
			while( myJob.getRemaining().size() > 0 || myJob.getPending().size() > 0 ){
				iteration++;
				String jobStat = "R="+myJob.getRemaining().size()  + ":P="+myJob.getPending().size()
									+ ":D="+myJob.getDone().size() + ":F="+myJob.getFailed().size();
				log("Performing job-mgmt iteration #" + iteration + " "+jobStat+" :::::::::::::::::::::::::");
				
				//////////////////////////////////////////////////////////////////////////////////////////////////////////
				// if any remaining tasks available try to assign them...
				log("-Iteration #"+iteration+". Remaining tasks :::::::::::::::::::::::::");
				
				ArrayList<TaskContainer> pending = new ArrayList<TaskContainer>();
				Hashtable<String,Long> found = new Hashtable<String,Long>();
				
				timer.reset( tuid++ );
				myJob.resetRemainingIterator();
				while( (ct=myJob.getNextRemaining()) != null  ){
					//log("\tFinding worker for task: " + ct.getTid());
					
					ComponentId worker = findWorker();
					if( worker == null ){
						//log( "\tNo worker found!" );
						continue;
					}
					String wkey = worker.getId().toString();
					//log("\tFound W:" + wkey);
					
					// optimize worker usage
					if( found.containsKey(wkey) && found.get(wkey)>(System.currentTimeMillis()-(YACSSettings.FUNC_RES_STATUS_REPORT_INTERVAL/2)) ){
						//log( "\tW:"+wkey+" already used in this iteration." );
						continue;
					}
					else
						found.put( wkey, new Long(System.currentTimeMillis()) );
								
					// bind to the worker to ask it perform the task
					BindId workerBinding = bindManagementInterfaceToWorker( worker );
					
					ct.setWorker( worker ); // TODO: what is this?
					if( taskManagement.performTask(ct,masterGroup,YACSNames.DUMMY_PARAM) ){
						log("\tW:"+wkey+" ACCEPTED T:"+ct.getTid()+"!");
						
						// if already in the group, i.e. has done a prior task for this job then no need to add again
						if( !seenWorkers.containsKey(wkey) ){
							//log("\tAdding W:"+wkey+" to WG:" + workerGroup.getId());
							nicheOSSupport.addToGroup( worker, workerGroup );
							seenWorkers.put( wkey, wkey );
						}
						else{
							//log( "\tW:"+wkey+" already in job-worker-group" );
						}
						pending.add( ct );
						myJob.getPending().add(ct);
					}
					else {
						ct.setWorker( null );
						log("\tW:"+wkey+" REJECTED T:"+ct.getTid()+"!");
					}
					
					nicheOSSupport.unbind(workerBinding);
					taskManagement = null;
				}
				myJob.getRemaining().removeAll( pending );
				changesToReport.addAll( pending );
				timefx("Job",jobId,timer.getTtid(),"RID",""+iteration,timer.elapsed(),null); // Remaining Iteration Done
				
				
				//////////////////////////////////////////////////////////////////////////////////////////////////////////
				// check on status of tasks already allocated...
				log("-Iteration #"+iteration+". Pending tasks :::::::::::::::::::::::::");
				
				myJob.resetPendingIterator();
				while( (ct=myJob.getNextPending()) != null  ){
					log( "\tPending T:" + ct.getTid() + " @W:" + (ct.getWorker()==null?"NULL!":ct.getWorker().getId()) + "- H:" + ct );
				}
				
				//////////////////////////////////////////////////////////////////////////////////////////////////////////
				// report all changes done in this iteration
				log("-Iteration #"+iteration+". Reporting changes :::::::::::::::::::::::::");
				
				if( changesToReport.size() > 0 ){
					// TODO: checkpoint job state after each iteration, after each change? Job is "lightweight" so shouldn't be that pushy to do often.
					checkpointJobStatus();
					
					// report all changes to the client... if there is any
					int i=0, c=changesToReport.size();
					while( changesToReport.size() > 0 ){
						TaskContainer changed = changesToReport.remove(0);
						log("\tReporting change ("+(++i)+"/"+c+") on T:" + changed.getTid() );
						reportTaskChange( changed );
					}
				}
				else {
					//log( "\tNo task changes to report!" );
				}
				
				time("Job",jobId,"TF",""+(myJob.getDone().size()+myJob.getFailed().size()),(System.currentTimeMillis()-jobStartTime),null); // Tasks Finished
				
				log( "_________________________________________________________________________________________" );
								
				
				//////////////////////////////////////////////////////////////////////////////////////////////////////////
				// sleep between management iteration checks
				try{
					// don't go to sleep unless there is some work remaining
					if( myJob.getRemaining().size() > 0 || myJob.getPending().size() > 0 )
						Thread.sleep( YACSSettings.MASTER_JOB_MGMG_ITERATION_INTERVAL );
				}
				catch( Exception e ){
					e.printStackTrace();
				}
			}
			time("Job",jobId,"TF",""+(myJob.getDone().size()+myJob.getFailed().size()),(System.currentTimeMillis()-jobStartTime),null); // Tasks Finished
			time("Job",jobId,"ATF",null,(System.currentTimeMillis()-jobStartTime),null); // All Tasks Finished
			log( "\tJob tasks done!." );
			
			// if !YACSSettings.TEST__BUSY_UNTIL_DELETED then end-state checkpoint and deleted checkpoint were so close to each other that
			// a special delete one isn't really needed. Lets just piggyback in the end-state checkpoint.
			if( !YACSSettings.TEST__BUSY_UNTIL_DELETED )
				myJob.setDeleted(true);
			
			// checkpoint the end state, mostly to inform the MW that the job is over
			checkpointJobStatus();
			time("Job",jobId,"EC",null,(System.currentTimeMillis()-jobStartTime),null); // End Checkpoint
			
			// inform client of job result
			reportJobResult( myJob );
			time("Job",jobId,"JRR",null,(System.currentTimeMillis()-jobStartTime),null); // Job Result Returned
			
			
			// Two possibilities, the job deletes "itself" after tasks done or waits for client/submitter to say: delete
			if( !jobDeleted ){
				log("Sleeping until job is deleted by submitter.");
			}
			while( !jobDeleted ){
				YacsUtils.ignorantSleep(1000);
			}
			time("Job",jobId,"JDFS",null,(System.currentTimeMillis()-jobStartTime),null); // Job Deletion Flag Set
			
			// do the actual job deletion
			handleDeleteJob();
			
			time("Job",jobId,"JD",null,(System.currentTimeMillis()-jobStartTime),null); // Job Done
			
			log("Job done!" );
		}
	}
	
	// job management functions
	private void createWorkerGroup(){
		// find the initial worker
		ComponentId initialWorker = null;
		do {
			initialWorker = findWorker();
		} while( initialWorker == null );
		time("Job",jobId,"IWF",null,(System.currentTimeMillis()-jobStartTime),null); // Initial Worker Found
		// TODO: while initial worker is null... Never give up... valiant! ...but dangerous :)
		
		// push master group info to worker
		{
			BindId initWorkerBinding = bindManagementInterfaceToWorker( initialWorker );
			taskManagement.setMasterGroup( masterGroup, YACSNames.DUMMY_PARAM );
			nicheOSSupport.unbind( initWorkerBinding );
			taskManagement = null;
		}
		time("Job",jobId,"IWI",null,(System.currentTimeMillis()-jobStartTime),null ); // Initial Worker Informed
		
		// create the worker group
		{
			ArrayList<ComponentId> workers = new ArrayList<ComponentId>();
			workers.add(initialWorker);
			
			workerGroup = nicheOSSupport.createGroup(	YACSTemplates.workerGroup(actuator),
														workers );
			myJob.setWorkerGroup(workerGroup);					
			log("\tCreated WORKER group: " + workerGroup.getId());
			
			// note that worker has been added to the group, to prevent future re-adds
			seenWorkers.put( initialWorker.getId().toString(), initialWorker.getId().toString() );
			cachedWorkers.add( initialWorker );
		}
		time("Job",jobId,"WGC",null,(System.currentTimeMillis()-jobStartTime),null); // Worker Group Created
				
		// wait until the worker group has been properly set up
		while( !workerWatcherInitalized ){
			YacsUtils.ignorantSleep(10);
		}
		time("Job",jobId,"WWI",null,(System.currentTimeMillis()-jobStartTime),null); // Worker Watcher Initialized
		
		log("\tInitial worker and group setup done");
	}
	
	private void handleDeleteJob(){
		log("Start delete procedure for job: " + (myJob==null?"NULL":jobId) );
		if( myJob == null ){
			jobDeleted = !YACSSettings.TEST__BUSY_UNTIL_DELETED;
			return;
		}
		
		try {
			ArrayList<Vector<TaskContainer>> allTasks = new ArrayList<Vector<TaskContainer>>();
			allTasks.add( myJob.getRemaining() );
			allTasks.add( myJob.getPending() );
			allTasks.add( myJob.getDone() );
			allTasks.add( myJob.getFailed() );
			
			// "manually" tell worker they should become free. If !YACSSettings.TEST__BUSY_UNTIL_DELETED they will become free on their own
			if( YACSSettings.TEST__BUSY_UNTIL_DELETED ){
				for( Vector<TaskContainer> tasks : allTasks ){
					for( TaskContainer task : tasks ){
						time("Job",jobId,"DT",""+task.getTid(),(System.currentTimeMillis()-jobStartTime),"@W:"+(task.getWorker()!=null?task.getWorker().getId():"NULL") ); // Delete Task
						if( task.getWorker() == null ){
							log("\tT:" + task.getTid()+" has no Worker!");
							continue;
						}
						//log("\tDeleting task: " + task.getTid() + " @" + task.getWorker().getId() );
						
						
						BindId b = bindManagementInterfaceToWorker( task.getWorker() );
						//log("\tBinding done.");
						if( !taskManagement.deleteTask(task) ){
							log("\tT:"+task.getTid()+" deleted: false!");
						}
						//log("\tTask: " + task.getTid() + " deleted: " + deleted);
						nicheOSSupport.unbind(b);
						taskManagement = null;
					}
				}
				time("Job",jobId,"ATD",null,(System.currentTimeMillis()-jobStartTime),null); // All Tasks Deleted
			}
			
			// if free as soon as all tasks done, i.e. not when client/submitter says: delete, then the end-state checkpoint will convey this
			// to the MasterWatcher, no need to checkpoint again.
			if( YACSSettings.TEST__BUSY_UNTIL_DELETED ){
				myJob.setDeleted(true);
				// TODO: is this the best way to inform the ME of job end?
				this.checkpointJobStatus();
				time("Job",jobId,"DC",null,(System.currentTimeMillis()-jobStartTime),null); // Delete Checkpoint
			}
						
			nicheOSSupport.removeGroup( this.workerGroup );
			time("Job",jobId,"WGR",null,(System.currentTimeMillis()-jobStartTime),null); // Worker Group Removed
			
			nicheOSSupport.removeGroup( this.masterGroup );
			time("Job",jobId,"JDD",null,(System.currentTimeMillis()-jobStartTime),null); // Job Deletion Done
			
			this.myJob = null;
			jobDeleted = !YACSSettings.TEST__BUSY_UNTIL_DELETED;
		}
		catch( Exception e ){
			log("Exception in deleting job: " + e.getMessage());
			time("Job",jobId,"JDEX",null,(System.currentTimeMillis()-jobStartTime),null); // Job Delete EXception
			e.printStackTrace();
		}
	}
	
	// Utility functions used by main job management logic
	// find resources
	private ComponentId findWorker(){
		long tStart = System.currentTimeMillis(), ttuid=tuid++;
		try{
			// Vector is internally synchronized, to avoid potential race conditions between 
			// checking size and removing, i.e. I simply remove right away, and deal with the expected exception
			ComponentId worker = cachedWorkers.remove(0);
			timefx("Job",jobId,ttuid,"CWF",null,(System.currentTimeMillis()-tStart),null ); // Cached Worker Found
			return worker;
		}
		catch( java.lang.ArrayIndexOutOfBoundsException e ){
			//log("No cached worker");
		}
		catch( Exception e ){
			nlog("Exception in findWorkers - Checking cached workers: " + e.getMessage() );
			e.printStackTrace();
		}
		
		try {
			//return findWorker_Global();
			ComponentId worker = findWorker_Service( tStart, ttuid );
			timefx("Job",jobId,ttuid,"SWF",null,(System.currentTimeMillis()-tStart),"W:" + (worker!=null?worker.getId():"NULL") ); // Service Worker Found
			return worker;
		}
		catch( Exception e ){
			log("Exception in findWorker: " + e + ", msg:"+ e.getMessage() );
			timefx("Job",jobId,ttuid,"FWEX",null,(System.currentTimeMillis()-tStart),null ); // Find Worker EXception
			e.printStackTrace();
			return null;
		}
	}
	private ComponentId findWorker_Global( long ttuid, long tstart ) throws Exception {
		throw new Exception("Deprecated!");
	}
	private ComponentId findWorker_Service( long ttuid, long tstart ){
		//log("Master.findWorker_Service");
				
		ResourceRequest specs = new ResourceRequest();
		specs.setComponentType( YACSNames.WORKER_COMPONENT );
		
		ResourceRequest reply = this.resourceServiceRequest.request(specs);
		ArrayList<ComponentId> available = reply.getAvailableComponents();
		if( available.size() > 0 ){
			ComponentId found = available.get( (int)(rand.nextDouble()*available.size()) );
			//log("\tGot worker: " + found.getId().toString());
			return found;
		}
		else
			return null;
	}
	
	// checkpointing
	private void checkpointJobStatus(){
		stateChangeInterface.checkpoint( myGlobalId, new JobCheckpoint(uid++,myJob,seenWorkers) );
	}
	
	
	private void reportTaskChange( TaskContainer change ){
		if( myJob.getCreator() != null ){
			jobResultInterface.receiveTaskChange( change );
			//log("\tTask change reported to client!");
		}
		// already logged in bindJobResultInterfaceToClient
		/*else {
			log( "\tJob creator is null. Can't report change!" );
		}*/
		
	}
	private void reportJobResult( Job result ){
		if( myJob.getCreator() != null ){
			jobResultInterface.receiveJobResult( myJob );
			//log("\tJob result reported to client!");
		}
		// already logged in bindJobResultInterfaceToClient
		/*else {
			log( "\tJob creator is null. Can't report result!" );
		}*/
	}
	
	// OTHER HELPERS
	// binding functions
	private void waitForTaskManagement(){
		while( this.taskManagement == null )
			YacsUtils.ignorantSleep(10);
	}
	private void waitForSensorInterface(){
		while( this.stateChangeInterface == null )
			YacsUtils.ignorantSleep(10);
	}
	
	private void bindJobResultInterfaceToClient(){
		long tstart = System.currentTimeMillis(), ttuid = tuid++;
		if( myJob.getCreator() != null ){
			BindId b = nicheOSSupport.bind( myGlobalId, 		YACSNames.JOB_RESULT_CLIENT_INTERFACE, 
											myJob.getCreator(), YACSNames.JOB_RESULT_SERVER_INTERFACE,
											JadeBindInterface.ONE_TO_ONE );
			timefx("Job",jobId,tuid,"CRBC",null,(System.currentTimeMillis()-tstart),null); // Client Result Binding Called
			while( jobResultInterface == null )
				YacsUtils.ignorantSleep(10);
			timefx("Job",jobId,tuid,"CRBD",null,(System.currentTimeMillis()-tstart),null); // Client Result Binding Done
		}
		else
			log( "\tJob creator is null. Will not be able to report changes or result!" );
	}
	private BindId bindManagementInterfaceToWorker( ComponentId worker ){
		long tStart = System.currentTimeMillis(), ttuid=tuid++;
		
		BindId b = nicheOSSupport.bind( myGlobalId, YACSNames.TASK_MANAGEMENT_CLIENT_INTERFACE, 
										worker, YACSNames.TASK_MANAGEMENT_SERVER_INTERFACE,
										JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE );
		timefx("Job",jobId,ttuid,"TMBC",null,(System.currentTimeMillis()-tStart),null ); // Task Management Binding Called

		waitForTaskManagement();
		timefx("Job",jobId,ttuid,"TMBD",null,(System.currentTimeMillis()-tStart),null ); // Task Management Binding Done
		
		return b;
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public String[] listFc() {
		log("---- ---- Master.listFc");
		return new String[] {	"component", 
								FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
								YACSNames.TASK_MANAGEMENT_CLIENT_INTERFACE,
								YACSNames.STATE_CHANGE_CLIENT_INTERFACE,
								YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE,
								YACSNames.RESOURCE_SERVICE_STATE_MASTER_CLIENT_INTERFACE,
								YACSNames.JOB_RESULT_CLIENT_INTERFACE };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {
		log("---- ---- Master.lookupFc: "+itfName);
		if (itfName.equals(YACSNames.TASK_MANAGEMENT_CLIENT_INTERFACE))
			return taskManagement;
		else if (itfName.equals(YACSNames.STATE_CHANGE_CLIENT_INTERFACE))
			return this.stateChangeInterface;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE))
			return this.resourceServiceRequest;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_STATE_MASTER_CLIENT_INTERFACE))
			return this.resourceServiceState;
		else if (itfName.equals(YACSNames.JOB_RESULT_CLIENT_INTERFACE))
			return this.jobResultInterface;
		else if (itfName.equals("component"))
			return myself;		
		else
			throw new NoSuchInterfaceException(itfName);

	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		log("---- ---- Master.bindFc: " + itfName );
		if (itfName.equals(YACSNames.TASK_MANAGEMENT_CLIENT_INTERFACE))
			taskManagement = (TaskManagementInterface)itfValue;
		else if (itfName.equals(YACSNames.STATE_CHANGE_CLIENT_INTERFACE))
			stateChangeInterface = (StateChangeInterface)itfValue;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE))
			resourceServiceRequest = (ResourceServiceRequestInterface)itfValue;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_STATE_MASTER_CLIENT_INTERFACE)){
			resourceServiceState = (ResourceServiceStateInterface)itfValue;
			// start the ResourceReporter thread, if not already started
			if( status && resourceReporter == null ){
				resourceReporter = new ResourceReporter(this,this.resourceServiceState);
				resourceReporter.start();
			}
		}
		else if (itfName.equals(YACSNames.JOB_RESULT_CLIENT_INTERFACE))
			jobResultInterface = (JobResultInterface)itfValue;
		else if (itfName.equals("component"))
			myself = (Component) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		log("---- ---- Master.unbindFc: " + itfName);
		if (itfName.equals(YACSNames.TASK_MANAGEMENT_CLIENT_INTERFACE))
			taskManagement = null;
		else if (itfName.equals(YACSNames.STATE_CHANGE_CLIENT_INTERFACE))
			stateChangeInterface = null;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE))
			resourceServiceRequest = null;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_STATE_MASTER_CLIENT_INTERFACE))
			resourceServiceState = null;
		else if (itfName.equals(YACSNames.JOB_RESULT_CLIENT_INTERFACE))
			jobResultInterface = null;
		else if (itfName.equals("component"))
			myself = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {
		log("---- ---- Master.getFcState");
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		log("---- ---- Master.startFc");
		
		Component jadeNode = null;
		Component niche = null;
		OverlayAccess overlayAccess = null;

		Component comps[] = null;
		try {
			comps = Fractal.getSuperController(myself).getFcSuperComponents();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < comps.length; i++) {
			try {
				if (Fractal.getNameController(comps[i]).getFcName().equals("managed_resources")) {
					jadeNode = comps[i];
					break;
				}
			}
			catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}
		}

		try {
			niche = FractalUtil.getFirstFoundSubComponentByName(jadeNode,"nicheOS");
		}
		catch (NoSuchComponentException e1) {
			e1.printStackTrace();
		}

		try {
			overlayAccess = (OverlayAccess)niche.getFcInterface("overlayAccess");
		}
		catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		nicheOSSupport = overlayAccess.getOverlay().getComponentSupport(myself);
		logger = overlayAccess.getOverlay().getNicheAsynchronousSupport();
		this.createYacsLogger( "Master", null, true, true, logger );
		actuator = overlayAccess.getOverlay().getJadeSupport();

		myGlobalId = nicheOSSupport.getResourceManager().getComponentId(myself);
		myLocation = "" + myGlobalId.getResourceRef().getDKSRef().getId();

		totalSpace = freeSpace = nicheOSSupport.getResourceManager().getTotalStorage(myself);
		nicheOSSupport.setOwner(myGlobalId);
		
		status = true;
	
		myInfo = new ResourceInfo( this.myGlobalId, YACSNames.MASTER_COMPONENT, System.currentTimeMillis(), YACSNames.AVAILABILITY_STATUS__FREE );
				
		double cpuSpeed = 1000;
		try{
			String n = myGlobalId.getComponentName(); // nota nafnið sem cpu power: master1, master2... losna við að fá active master á Jadeboot
			//log("\tMaster name is: " + n);
			cpuSpeed = Double.parseDouble( ""+n.charAt(n.length()-1) );
		}
		catch( Exception e ){
			cpuSpeed = new java.util.Random().nextDouble()*3000;
		}
		myInfo.setCpuSpeed( cpuSpeed );
		
		if( resourceServiceState != null ){
			resourceReporter = new ResourceReporter(this,this.resourceServiceState);
			resourceReporter.start();
		}
		
		this.cstart = System.currentTimeMillis();
		
		log("Started YACS MASTER component = " + myGlobalId.getId() + " at " + myLocation + " with totalspace: " + totalSpace);
	}

	public void stopFc() throws IllegalLifeCycleException {
		log("---- ---- Master.stopFc");
		status = false;
	}
}
