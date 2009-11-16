package yacs.job;

import java.io.*;
import java.nio.channels.*;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;

import dks.niche.ids.*;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;

import yacs.interfaces.*;
import yacs.job.interfaces.InformationInterface;
import yacs.job.interfaces.JobMasterGroupInterface;
import yacs.job.interfaces.StateChangeInterface;
import yacs.job.interfaces.TaskExecutionContext;
import yacs.job.interfaces.TaskManagementInterface;
import yacs.job.interfaces.JobWorkerGroupInterface;
import yacs.job.state.*;
import yacs.resources.interfaces.ResourceServiceStateInterface;
import yacs.resources.interfaces.ResourceInterface;
import yacs.resources.data.ResourceInfo;
import yacs.resources.helpers.ResourceReporter;
import yacs.utils.YacsUtils;
import yacs.utils.YacsLogger;
import yacs.utils.YacsTimer;

public class Worker extends yacs.YacsComponent implements 
	TaskManagementInterface, InformationInterface, JobWorkerGroupInterface, 
	TaskExecutionContext, ResourceInterface,
	BindingController, LifeCycleController 
{
	private Component myself;
	ComponentId myGlobalId;
	String myLocation;
	int totalSpace;
	int freeSpace;
	private boolean status;
	
	NicheComponentSupportInterface nicheOSSupport;
	NicheAsynchronousInterface logger;
	
	private StateChangeInterface stateChangeInterface;
	private JobMasterGroupInterface masterInterface;
	
	private ResourceServiceStateInterface resourceServiceState;
	private ResourceReporter resourceReporter;
	
	private ResourceInfo myInfo = null;
	private long cstart, tuid;
	private boolean utilizationTimed = false;
	
	// task specific
	private TaskContainer myTask;
	private GroupId workerGroup;
	private GroupId masterGroup;
	private WorkerThread worker = null;
	private long uid=0;
	private boolean taskDeleted = !YACSSettings.TEST__BUSY_UNTIL_DELETED;
	private YacsTimer taskTimer;
	private boolean redeployment = false;
	
	private byte[] lastCheckpointPayload; 
		
	public Worker(){
		log("Worker created: "+this + " " + YACSSettings.YACS_VERSION);
	}
	
	// taskManagementInterface
	public boolean performTask( TaskContainer task, GroupId masterGroup, boolean dummy ){
		log("PerformTask: T:" + (task!=null?task.getTid():"NULL") );
		return performTask_( task, masterGroup, 0, false );
	}
	public boolean performTask( TaskCheckpoint task, GroupId masterGroup, boolean dummy, boolean dummy2 ){
		log("PerformTask: redeployment of T:" + (task.getTask()!=null?task.getTask().getTid():"NULL") + ", from CP.id:" + task.getVersion() );
		return performTask_( task.getTask(), masterGroup, task.getVersion()+1, true );
	}
	protected synchronized boolean performTask_(TaskContainer task, GroupId masterGroup, long uid, boolean redeployment ){
		log("Worker("+this.myGlobalId.getComponentName()+").performTask_: " + task.getTid() + " for master: " + masterGroup.getId() +", redep: " + redeployment );
		log("\tW.MID-for-log: " + myGlobalId.getId());
		log("\tholder:" + (task.getWorker()==null?"NULL!":task.getWorker().getId()));
				
		if( myTask != null ){
			log("\tAlready busy with T:" + myTask.getJTid() );
			return false;
		}
		this.uid = uid;
		task.setWorker( this.myGlobalId );
		this.masterGroup = masterGroup;
		this.redeployment = redeployment;
		
		if( !utilizationTimed ){
			utilizationTimed = true;
			timefx("Worker",null,0,"TTFU",null,(System.currentTimeMillis()-cstart),null ); // Time To First Utilization
		}
		
		log("\tTask accepted: " + task.getJTid());
		taskTimer = new YacsTimer( task.getTid() );
				
		myTask = task;
		myTask.setStatus( YACSNames.TASK_NOT_INITIALIZED ); // TODO: examine well the semantics of processing status being transported between processing Workers.
		myTask.setExecutionContext(this);
		
		worker = new WorkerThread();
		worker.start();
		
		return true;
	}
	public TaskContainer taskStatus(){
		log(	"Worker("+this.myGlobalId.getComponentName()+").taskStatus: "
								+ "State: " + (myTask==null?"NULL":myTask.getStatus()) );
		
		return myTask;
	}
	public boolean setWorkerGroup( GroupId workerGroup, boolean dummy ){
		log("Worker.setWorkerGroup: " + workerGroup.getId());
		this.workerGroup = workerGroup;
		return true;
	}
	public boolean setMasterGroup( GroupId masterGroup, boolean dummy ){
		log("Worker.setMasterGroup: " + masterGroup.getId());
		this.masterGroup = masterGroup;
		return true;
	}
	public boolean deleteTask( TaskContainer task ){
		log("Worker.deleteTask: " + task.getTid()+ ". Curr: " + (myTask==null?"NULL!":myTask.getTid()) );
		if( myTask==null )
			return false;
		
		// notify the WorkerWatcher of job deletion
		//myTask.setDeleted(true);
		//stateChangeInterface.checkpoint( myGlobalId, new TaskCheckpoint(uid++,createCheckpoint()) ); //checkpoint();
		taskDeleted = true;
		
		return taskDeleted;
	}
	
	// informationInterface
	public String componentType(){
		log("Worker.componentType("+this.myGlobalId.getComponentName()+")");
		return YACSNames.WORKER_COMPONENT;
	}
	public GroupId workerGroup(){
		log("Worker.workerGroup: "+(workerGroup==null?"NULL":workerGroup.getId()));
		return this.workerGroup;
	}
	public GroupId masterGroup(){
		log("Worker.masterGroup: "+(masterGroup==null?"NULL":masterGroup.getId()));
		return this.masterGroup;
	}
	
	// TaskExecutionInterface
	public void metacheckpoint(){
		if( myTask != null ){
			myTask.prepareCheckpoint();
			myTask.debug_printMetacheckpoint();
		}
		stateChangeInterface.checkpoint( myGlobalId, new TaskCheckpoint(uid++,myTask) );
	}
	public void copyFile( Object from, Object to ) throws Exception {
		try {
			File fi = new File((String)from);
			File fo = new File((String)to);
			
			FileChannel ic = new FileInputStream(fi).getChannel();
			FileChannel oc = new FileOutputStream(fo).getChannel();
			
			ic.transferTo(0, ic.size(), oc);
			
			ic.close();
			oc.close();
		}
		catch( Exception e ){
			throw e;
		}
	}
	public boolean deleteFile( Object location ) throws Exception {
		File del = new File((String)location);
		if( !del.isFile() ){
			throw new Exception(location+" is not a file. Cannot delete!");
		}
		return del.delete();
	}
	public YacsLogger createLogger(	String name, String id, boolean toNiche, boolean toConsole ){
		//this.createDerivedLogger(name, id, toNiche, toConsole);
		return new YacsLogger( name, id, toNiche, toConsole, this.logger );
	}
	
	// ResourceReporterInterface
	public String getId(){
		return this.myGlobalId.getId().toString();
	}
	public ResourceInfo getStatusInfo(){
		myInfo.updateLastUpdateTime();
		myInfo.setStatus( (myTask==null ?	YACSNames.AVAILABILITY_STATUS__FREE :
											YACSNames.AVAILABILITY_STATUS__BUSY) );
		return myInfo;
	}
	
	// internal logic
	class WorkerThread extends Thread {
		public void run(){
			try {
				log("Worker.WorkerThread.run: starting..." );
				
				// TODO: report to masters before or after checkpointing worker state?
				// is it more likely to fail on checkpointing or reporting... event vs binding...?
				
				// report initial state to master
				reportTaskStatus();
				time("Task",myTask.getJTid(),"ITS",null,taskTimer.elapsed(),null); // Initial Task Status
				
				// wait until sensor deployed and bound
				while( stateChangeInterface == null )
					YacsUtils.ignorantSleep(10);
				time("Task",myTask.getJTid(),"SCIB",null,taskTimer.elapsed(),null); // State Change Interface Bound
				
				// TODO: hmmm, or should I not use the way that tells the task to perpare checkpoint... it shouldn't even have started to do anything, i.e. its state is already known by the system 
				stateChangeInterface.checkpoint( myGlobalId, new TaskCheckpoint(uid++,createCheckpoint()) ); //checkpoint();
				time("Task",myTask.getJTid(),"IC",null,taskTimer.elapsed(),null); // Initial Checkpoint
				
				myTask.execute( redeployment );
				time("Task",myTask.getJTid(),"TED",null,taskTimer.elapsed(),null); // Task Execution Done
				
				// report end state to master
				reportTaskStatus();
				time("Task",myTask.getJTid(),"ETS",null,taskTimer.elapsed(),null); // End Task Status
				
				// TODO: might be wise to skip the prepareCheckpointing of checkpoint(). The task is already finished what it was doing and should have checkpointed what it wanted
				stateChangeInterface.checkpoint( myGlobalId, new TaskCheckpoint(uid++,createCheckpoint()) ); //checkpoint();
				time("Task",myTask.getJTid(),"EC",null,taskTimer.elapsed(),null); // End Checkpoint
				
				String stid = String.valueOf(myTask.getJTid());
				handleTaskDone();
				time("Task",stid,"TTT",null,taskTimer.elapsed(),null); // Total Task Time
				
				log("Worker.WorkerThread.run: Task done!" );
			}
			catch( Exception e ){
				time("Task",(myTask!=null?myTask.getJTid():null),"EXT",null,taskTimer.elapsed(),null ); // EXception Time
				log("Worker.WorkerThread.run: error: " + e.getMessage() );
				e.printStackTrace();
			}
		}
	}
	
	// WorkerGroupInterface
	public void publishState( GroupId jobOwner, boolean dummy ){
		if( myTask == null ){
			log("publishState: worker does not have a task to publish state about. JobMaster: " + jobOwner.getId() );
			return;
		}
		else if( masterGroup!=null && !masterGroup.getId().toString().equals(jobOwner.getId().toString()) ){
			log( "publishState: worker now part of a different job. C:" + masterGroup.getId() + " vs. P:"+jobOwner.getId()+". T: " + myTask.getTid() );
			return;			
		}
		
		log("Worker.publishState: current cp store: " + this.lastCheckpointPayload);
		// This function is used by the WorkerWatcher when it needs to pull the "latest" state
		// We don't want the WW to control the state checkpointed so we used the last "safe" checkpoint, as defined by the worker and task itself.
		// Hence storing the last "safe" checkpoint and resending when WW asks
		this.stateChangeInterface.checkpoint( myGlobalId, new TaskCheckpoint(uid++,restoreFromCheckpoint()) );
	}
	
	// helpers
	private void reportTaskStatus(){
		long tstart = System.currentTimeMillis(), ttuid = tuid++;
		log("Worker.reportTaskStatus: T:" + myTask.getTid() + ", S: " + myTask.getStatus() );
		
		if( masterInterface == null ){
			BindId b = nicheOSSupport.bind( myGlobalId, YACSNames.MASTER_CLIENT_INTERFACE,
											masterGroup, YACSNames.MASTER_SERVER_INTERFACE,
											JadeBindInterface.ONE_TO_MANY );
	
			timefx("Task",""+myTask.getJTid(),ttuid,"MIBC",null,(System.currentTimeMillis()-tstart),null ); // Master Interface Binding Called
			log("\tBound masterInterface to masters: " + masterGroup.getId() + ", bid: " + b);
			
			while( masterInterface == null )
				YacsUtils.ignorantSleep(10);
			
			timefx("Task",""+myTask.getJTid(),ttuid,"MIBD",null,(System.currentTimeMillis()-tstart),null ); // Master Interface Binding Done
		}
		
		log("\tReporting to Master(s)...");
		masterInterface.reportTaskStatus(myTask, myGlobalId, YACSNames.DUMMY_PARAM);
		
	}
	private synchronized TaskContainer createCheckpoint(){
		try {
			if( myTask != null ){
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				
				ObjectOutputStream oos = new ObjectOutputStream( bos );
				oos.writeObject( myTask );
				oos.close();
				
				this.lastCheckpointPayload = bos.toByteArray();
			}
		}
		catch( Exception e ){
			log( "Unable to record last checkpoint: " + e.getMessage() );
		}
		return myTask;
	}
	private synchronized TaskContainer restoreFromCheckpoint(){
		if( lastCheckpointPayload == null )
			return null;
		
		try{
			ByteArrayInputStream bis = new ByteArrayInputStream( this.lastCheckpointPayload );
			ObjectInputStream ois = new ObjectInputStream( bis );
			
			return (TaskContainer)ois.readObject();
		}
		catch( Exception e ){
			log( "Unable to restore from last checkpoint: " + e.getMessage() );
			// better to checkpoint something rather than nothing... even if that something might be in an unsafe state
			return myTask;
		}
	}
	private void handleTaskDone(){
		
		if( !taskDeleted ){
			log("Sleeping until task is deleted by master.");
		}
		while( !taskDeleted ){
			YacsUtils.ignorantSleep(1000);
		}
		
		myTask = null;
		taskDeleted = !YACSSettings.TEST__BUSY_UNTIL_DELETED;
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public String[] listFc() {
		log("Worker.listFc");
		return new String[] { 	"component", 
								YACSNames.STATE_CHANGE_CLIENT_INTERFACE, 
								YACSNames.MASTER_CLIENT_INTERFACE,
								YACSNames.RESOURCE_SERVICE_STATE_CLIENT_INTERFACE };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {
		log("Worker.lookupFc: "+itfName);
		if(itfName.equals(YACSNames.STATE_CHANGE_CLIENT_INTERFACE))
			return this.stateChangeInterface;
		else if(itfName.equals(YACSNames.MASTER_CLIENT_INTERFACE))
			return this.masterInterface;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_STATE_CLIENT_INTERFACE))
			return this.resourceServiceState;
		else if (itfName.equals("component"))
			return myself;
		else{
			log("\tInterface not bindable");
			throw new NoSuchInterfaceException(itfName);
		}
	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		log("Worker.bindFc: " + itfName);
		if(itfName.equals(YACSNames.STATE_CHANGE_CLIENT_INTERFACE))
			stateChangeInterface = (StateChangeInterface) itfValue;
		else if(itfName.equals(YACSNames.MASTER_CLIENT_INTERFACE))
			masterInterface = (JobMasterGroupInterface) itfValue;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_STATE_CLIENT_INTERFACE)){
			resourceServiceState = (ResourceServiceStateInterface)itfValue;
			if( status && resourceReporter == null ){
				resourceReporter = new ResourceReporter(this,this.resourceServiceState);
				resourceReporter.start();
			}
		}
		else if (itfName.equals("component"))
			myself = (Component) itfValue;
		else {
			log("\tInterface not bindable");
			throw new NoSuchInterfaceException(itfName);
		}
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		log("Worker.unbindFc: " + itfName);
		if(itfName.equals(YACSNames.STATE_CHANGE_CLIENT_INTERFACE))
			stateChangeInterface = null;
		else if(itfName.equals(YACSNames.MASTER_CLIENT_INTERFACE))
			masterInterface = null;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_STATE_CLIENT_INTERFACE))
			resourceServiceState = null;
		else if (itfName.equals("component"))
			myself = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {
		log("Worker.getFcState");
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		log("Worker.startFc");
		
		Component jadeNode = null;
		Component niche = null;
		OverlayAccess overlayAccess = null;

		Component comps[] = null;
		try {
			comps = Fractal.getSuperController(myself).getFcSuperComponents();
		}
		catch (NoSuchInterfaceException e) {
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
			overlayAccess = (OverlayAccess) niche.getFcInterface("overlayAccess");
		}
		catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		nicheOSSupport = overlayAccess.getOverlay().getComponentSupport(myself);
		logger = overlayAccess.getOverlay().getNicheAsynchronousSupport();
		this.createYacsLogger( "Worker", null, true, true, logger );

		myGlobalId = nicheOSSupport.getResourceManager().getComponentId(myself);
		myLocation = "" + myGlobalId.getResourceRef().getDKSRef().getId();

		totalSpace = freeSpace = nicheOSSupport.getResourceManager().getTotalStorage(myself);
		nicheOSSupport.setOwner(myGlobalId);
		
		status = true;
		
		myInfo = new ResourceInfo( this.myGlobalId, YACSNames.WORKER_COMPONENT, System.currentTimeMillis(), YACSNames.AVAILABILITY_STATUS__FREE );
				
		double cpuSpeed = 1000;
		try{
			String n = myGlobalId.getComponentName(); // nota nafnið sem cpu power: worker1, worker2... losna við að fá active master á Jadeboot
			//log("\tWorker name is: " + n);
			cpuSpeed = 40-Double.parseDouble( ""+n.charAt(n.length()-1) );
		}
		catch( Exception e ){
			cpuSpeed = new java.util.Random().nextDouble()*3000;
		}
		myInfo.setCpuSpeed( cpuSpeed );
		//myInfo.setCpuSpeed( new java.util.Random().nextDouble()*3000 );
		
		if( resourceServiceState != null ){
			resourceReporter = new ResourceReporter(this,this.resourceServiceState);
			resourceReporter.start();
		}
		
		this.cstart = System.currentTimeMillis();
		
		log("Started YACS WORKER component = " + myGlobalId.getId() + " at " + myLocation + " with totalspace: " + totalSpace);
	}

	public void stopFc() throws IllegalLifeCycleException {
		log("Worker.stopFc");
		status = false;
	}
}
