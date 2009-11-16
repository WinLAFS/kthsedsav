package yacs.job.watchers;

import java.util.*;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import dks.niche.events.ComponentFailEvent;
import dks.niche.events.ResourceLeaveEvent;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.fractal.interfaces.DeploySensorsInterface;
import dks.niche.ids.BindId;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.wrappers.ManagementDeployParameters;
import dks.niche.ids.*;

import yacs.interfaces.YACSNames;
import yacs.interfaces.YACSSettings;
import yacs.job.*;
import yacs.job.events.*;
import yacs.job.helpers.DiscoveryReply;
import yacs.job.interfaces.InformationInterface;
import yacs.job.interfaces.JobMasterGroupInterface;
import yacs.job.interfaces.TaskManagementInterface;
import yacs.job.interfaces.JobWorkerGroupInterface;
import yacs.job.sensors.StateChangeSensor;
import yacs.job.state.*;
import yacs.resources.data.ResourceRequest;
import yacs.resources.interfaces.ResourceServiceRequestInterface;
import yacs.utils.YacsUtils;
import yacs.utils.EventHistory;
import yacs.utils.YacsTimer;

public class WorkerWatcher  extends yacs.YacsManagementElement
							implements 	EventHandlerInterface, MovableInterface, 
										BindingController, 
										LifeCycleController
{
	private Component myself;	
	private NicheActuatorInterface actuator;
	private NicheAsynchronousInterface logger;
	
	private TriggerInterface triggerInterface;
	private DeploySensorsInterface deploySensor;
	
	private InformationInterface informationInterface;
	private JobMasterGroupInterface masterInterface;
	private JobWorkerGroupInterface workerGroupInterface;
	private TaskManagementInterface taskManagement;
	private ResourceServiceRequestInterface resourceServiceRequest;
		
	private boolean status;
	private NicheId myGlobalId;
	
	// members for replication - begin
	private GroupId workerGroup, masterGroup;
	private GroupId globalWorkerGroup, globalMasterGroup, resourceServiceGroup;
		
	private NicheId watchdog, workerAggregator;
	
	//private Hashtable<String,TaskCheckpoint> workers = new Hashtable<String,TaskCheckpoint>();
	// Worker can have many tasks within a single job
	private Hashtable<String,Hashtable<String,TaskCheckpoint>> workers = new Hashtable<String,Hashtable<String,TaskCheckpoint>>();
	private Hashtable<String,String> seenTasks = new Hashtable<String,String>(); // just for ME statistics and management
	private Hashtable<String,String> seenWorkers = new Hashtable<String,String>();
	
	private EventHistory eventHistory = new EventHistory();
	// members for replication - end
		
	// for cleanup
	private ArrayList<BindId> bindings = new ArrayList<BindId>();
	private ArrayList<dks.niche.wrappers.Subscription> subscriptions = new ArrayList<dks.niche.wrappers.Subscription>();
	
	// for logging
	private String currentHealTarget = null;
	private long tuid;
	
	static final boolean USE_WATCHDOG = System.getProperty("yacs.job.worker.watcher.watchdog") instanceof String ?
											(0 < Integer.parseInt(System.getProperty("yacs.job.worker.watcher.watchdog")) ? true : false) 
											: false;
	
	public WorkerWatcher(){
		log("WorkerWatcher created!");
	}

	// EventHandlerInterface
	public void eventHandler(Object e, int flag) {
		log("WorkerWatcher.eventHandler: "+e);
		YacsTimer timer = new YacsTimer( tuid++ );
		
		if(e instanceof ResourceLeaveEvent) {
			String id = ((ResourceLeaveEvent)e).getNicheId().toString();
			
			if( !eventHistory.record("ResourceLeaveEvent:"+id,null) ){
				log("\tResourceLeaveEvent event seen before! ID: " + id);
				return;
			}
			
			// which component left, watchdog or a worker?
			if( watchdog != null && watchdog.toString() == id )
				this.handleWatchdogDeparture();
			else
				handleWorkerDeparture( id, timer );
			time("WW",""+timer.getTtid(),"HTRLE",null,timer.elapsed(),null); // Handle Task Resource Leave Event
		}
		else if(e instanceof ComponentFailEvent) {
			String id = ((ComponentFailEvent)e).getNicheId().toString();
			
			if( !eventHistory.record("ComponentFailEvent:"+id,null) ){
				log("\tComponentFailEvent event seen before! ID: " + id);
				return;
			}
			
			// which component failed, watchdog or a worker?
			if( watchdog != null && watchdog.toString() == id )
				this.handleWatchdogDeparture();
			else {
				if( currentHealTarget != null )
					log("Currently healing! Will block for: " + currentHealTarget );
				handleWorkerDeparture( id, timer );
			}
			time("WW",""+timer.getTtid(),"HTCFE",null,timer.elapsed(),null); // Handle Task Component Failure Event
		}
		else if(e instanceof StateChangeEvent){
			StateChangeEvent change = (StateChangeEvent)e;
			if( !(change.getStateInformation() instanceof TaskCheckpoint) ){
				log("\tState isn't a TaskCheckpoint: " + change.getStateInformation());
				return;
			}
			this.handleTaskChange( change.getSource(), (TaskCheckpoint)change.getStateInformation(), timer.getTtid() );
			time("WW",""+timer.getTtid(),"HTSCE",null,timer.elapsed(),null); // Handle Task State Change Event
		}
		else if(e instanceof ManagementEvent){
			ManagementEvent me = (ManagementEvent)e;
			watchdog = (NicheId)me.globalId;
		}
		// TODO: add to worker group event?
		logStatus();
	}
	
	// CUSTOM EVENT HANDLERS begin
	private void handleTaskChange( ComponentId worker, TaskCheckpoint cp, long ttuid ){
		log( "handleTC: W:"+worker+", TC:"+cp );
		
		// TODO: store more than just the Task "runnable"
		String wkey = worker.getId().toString();
		String tkey = (cp.getTask() == null ? null : String.valueOf(cp.getTask().getTid()));
		
		
		if( seenTasks!=null && !seenTasks.containsKey(tkey) ){
			seenTasks.put( tkey, tkey );
			trigger( new WorkerManagementEvent(WorkerManagementEvent.TYPE.TASK_STARTED) );
		}
		
		if( haveNewerTaskVersion(cp.getTask().getTid(),cp.getVersion()) ){
			return;
		}
		
		removeOldVersions( cp.getTask().getTid(), cp.getVersion() );
		
		if( !workers.containsKey(wkey) ){
			workers.put( wkey, new Hashtable<String,TaskCheckpoint>() );
			seenWorkers.put( wkey, wkey );
		}
		Hashtable<String,TaskCheckpoint> workerTasks = workers.get( wkey );		
		
		TaskContainer task = cp.getTask();
		log("\tStoring T:" + task.getTid() + "@v:" + cp.getVersion()+ "@W:"+wkey );
		
		// if the task is already completed, no need to update the map
		if( task.getStatus()==YACSNames.TASK_COMPLETED || task.getStatus()==YACSNames.TASK_FAILED ){
			trigger( new WorkerManagementEvent( task.getStatus()==YACSNames.TASK_COMPLETED ?
													WorkerManagementEvent.TYPE.TASK_COMPLETED :
													WorkerManagementEvent.TYPE.TASK_FAILED ) );
			//stop monitoring if task is over
			log("\tT:"+tkey+" is done. Removing!");
			workerTasks.remove( tkey );
			if( workerTasks.size() == 0 ){
				log("W:"+wkey+" has no more active tasks. Removing!");
				workers.remove( wkey );
			}
		}
		else // store in map
			workerTasks.put(tkey, cp);
		
	}
	private void handleWatchdogDeparture(){
		this.deployWatchdog();
		log("\tDone with Watchdog healing!");
		
	}
	private synchronized void handleWorkerDeparture( String key, YacsTimer timer ){
		log("handleDeparture: W:" + key);
		timefx("WW",""+timer.getTtid(),timer.getTtid(),"HWDB",null,timer.elapsed(),null); // Handle Worker Departure, Block
		try {
			currentHealTarget = key;
			handleWorkerDeparture_( key, timer.getTtid() );
		}
		catch( Exception e ){
			log(" \tFailed in healing W:" + key + ". Exception: " + e.getMessage());
			e.printStackTrace();			
		}
		finally {
			currentHealTarget = null;
		}
	}
	private void handleWorkerDeparture_( String key, long ttid ){
		//log("WorkerWatcher.handleDeparture: " + key);
		YacsTimer timer = new YacsTimer( ttid );
		
		if( !workers.containsKey(key) ){
			log("\tWorker has no tasks here! Probably already done with its tasks.");
			return;
		}
		
		if( !isActiveReplica() ){
			log("\tNOT active replica. Quitting!");
			return;
		}
		
		// Remove it from list of maintained workers+tasks
		// The replacement worker will checkpoint upon accepting => thereby adding notifying the watcher again
		// Could do below, i.e. after finding replacement worker but this way is make is more likely
		// that watcher replicas will share the same view.
		// The only problem is if the replacement worker crashes before checkpointing, then the state will
		// be lost. This could perhaps be tackled with some task timeout in the Master itself?
		Hashtable<String,TaskCheckpoint> workerTasks = workers.remove(key);
		log("\tW:"+key+" has " + workerTasks.size() +" here.");
		
		
		for( TaskCheckpoint cp : workerTasks.values() ){
			TaskContainer task = cp.getTask();		
			currentHealTarget = task.getTid()+" @" + key;
			
		
			log("\tRecovering T:" + task.getTid() );
			
			if( isAlreadyHealed(task.getTid(),cp.getVersion()) ){
				log("\tTask is at another worker with a more recent version.");
				continue;
			}
			else if( !task.isRedeployable() ){
				log("\tTask is NOT redeployable!");
				this.masterInterface.irrecoverableWorkerFailure(task);
				log("\tMaster(s) notified of irrecoverable failure!");
				continue; // to for
			}
			
			boolean taskHealed = false;
			String bkey = null; long btime = 0; long itid=0;
			do {
				itid++;
				ComponentId worker = null;
				do{
					worker = findReplacementWorker( timer.getTtid() );
					if( worker == null ){
						log("\tDidn't find replacement worker. Try again in "+(YACSSettings.WW_REPLACEMENT_WORKER_REQUEST_INTERVAL/1000)+" sec");
						YacsUtils.ignorantSleep( YACSSettings.WW_REPLACEMENT_WORKER_REQUEST_INTERVAL );
					}
					 // in case the RS doesn't know about the failure and gives the same worker again
					else if( key.equals(worker.getId().toString()) ){
						worker = null;
						// TODO: specify in ResourceRequirements that absolutely don't want Cid=X. Can even use to tell it that func is suspected, to update its view faster
						log("\tRS gave me failed worker as replacement! Trying again later!");
						YacsUtils.ignorantSleep( YACSSettings.WW_REPLACEMENT_WORKER_REQUEST_INTERVAL );
					}
					// In case the RS repeatedly returns the same worker thinking it is free, yet when asked it has taken on other task => become busy
					// There is a delay between this and RS learning of it
					// Let wait 5000 sec between trying the same Worker
					else if( bkey != null && bkey.equals(worker.getId().toString()) && ((btime+5000) > System.currentTimeMillis()) ){
						// TODO: specify in RS that the worker is busy and should not be returned immediately... somehow tell RS so it can "suspect" it of being busy and give lesser preference to
						worker = null;
						log("\tRS gave me worker supected busy. Eligible again in msek: "+((btime+5000)-System.currentTimeMillis()));
						YacsUtils.ignorantSleep( YACSSettings.WW_REPLACEMENT_WORKER_REQUEST_INTERVAL );
					}
				}
				while( worker == null );
				
				String wkey = worker.getId().toString();
				log("\tFound replacement worker: " + wkey );
				
				try {
					timer.reset();
					BindId b = actuator.bind(	myGlobalId, YACSNames.TASK_MANAGEMENT_CLIENT_INTERFACE, 
												worker, YACSNames.TASK_MANAGEMENT_SERVER_INTERFACE,
												JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE );
					timefx("WW",""+timer.getTtid(),timer.getTtid(),"HWDWBC",""+itid,timer.elapsed(),""+cp.getTask().getTid()); // Handle Worker Departure, Worker Binding Called
					while( this.taskManagement == null )
						YacsUtils.ignorantSleep(10);
					timefx("WW",""+timer.getTtid(),timer.getTtid(),"HWDWBD",""+itid,timer.elapsed(),""+cp.getTask().getTid()); // Handle Worker Departure, Worker Binding Done
					
					log("\tAsk replacement to perform T:" + task.getTid()+ ", CP:"+cp.getVersion() );
					
					timer.reset();
					boolean accepted = taskManagement.performTask(cp,this.masterGroup,YACSNames.DUMMY_PARAM,YACSNames.DUMMY_PARAM);
					timefx("WW",""+timer.getTtid(),timer.getTtid(),"HWDPT",""+itid,timer.elapsed(),""+cp.getTask().getTid()); // Handle Worker Departure, Peform Task
					
					if( !accepted ){
						log("\tTask was NOT accepted by worker!" );
						// mark as busy so not to repeatedly ask can you take on task
						bkey = wkey; btime = System.currentTimeMillis();
					}
					else {
						log("\tTask was accepted by worker!" );
						taskHealed = true;
						
						// add the replacement worker to the worker group
						// if worker has already completed a task in this job it will already be in the group
						if( !seenWorkers.containsKey(wkey) ){
							timer.reset();
							actuator.addToGroup( worker, workerGroup );
							timefx("WW",""+timer.getTtid(),timer.getTtid(),"HWDRAG",""+itid,timer.elapsed(),""+cp.getTask().getTid()); // Handle Worker Departure, Replacement Added to Group
							
							seenWorkers.put( wkey, wkey );
							log("\tAdded replacement worker to job-worker-group.");				
						}
						else
							log("\tReplacement worker already in job-worker-group.");
					}
					
					actuator.unbind(b);
					taskManagement = null;
				}
				finally {
					taskManagement = null;
				}
			}
			while( !taskHealed );
		}
		log("\tDone Worker healing!" );
	}
	// TODO: how to delete a WorkerWatcher? It only knows when the tasks it knows about are done, not when ALL are done
	private void handleWatcherDeletion(){
		log("WorkerWatcher.handleWatcherDeletion: no Workers+Tasks left to watch!");
		
		// TODO: WorkerWatcher: if not active replica is there any cleanup to be done? At least un-deploy, or does DCMS replication handling take care of that?
		if( !isActiveReplica() ){
			log("\tNOT active replica. Quitting!");
			return;
		}
		
		/**
		 * Trigger WorkerManagementEvent
		 * Unbind all bindings
		 * Unsubscribe all subscriptions
		 * Undeploy
		 */
		
		// TODO: Trigger proper WorkerManagementEvent indicating deletion
		//trigger( new WorkerManagementEvent(WorkerManagementEvent.TYPE.TASK_DELETED) );
			
		// unbind known bindings
		log("\tUnbinding...");
		for( BindId bid : bindings ){
			if( bid != null ){
				try{
					this.actuator.unbind( bid );
				} catch( Exception e ){
					e.printStackTrace();
				}
			}
		}
		log("\tUnsubscribing...");
		for( dks.niche.wrappers.Subscription sub : subscriptions ){
			if( sub != null ){
				try{
					this.actuator.unsubscribe( sub );
				} catch( Exception e ){
					e.printStackTrace();
				}
			}
		}
		log("\tUndeploying...");
		// TODO: WorkerWatcher undeploy function...
		log("\tDismantling done!");
	}
	// CUSTOM EVENT HANDLERS end
	
	// HELPERS begin
	// find resources
	private ComponentId findReplacementWorker( long ttuid ){
		YacsTimer timer = new YacsTimer( ttuid );
		ComponentId worker = null;
		try {
			worker = findReplacementWorker_Service();
			//return findReplacementWorker_Global();
		}
		catch( Exception e ){
			log("Exception in finding replacement master: " + e.getMessage());
			e.printStackTrace();
			worker = null;
		}
		finally {
			timefx("WW",""+timer.getTtid(),timer.getTtid(),"FRW",null,timer.elapsed(),(worker!=null?worker.getId().toString():"NULL")); // Find Replacement Worker
		}
		return worker;
	}	
	private ComponentId findReplacementWorker_Global() throws Exception {
		throw new Exception("Deprecated!");
	}
	private ComponentId findReplacementWorker_Service(){
		
		ResourceRequest specs = new ResourceRequest();
		specs.setComponentType( YACSNames.WORKER_COMPONENT );
		
		int tries = 0;
		while( tries++<1 ){
			log( "\tAttempt "+(tries)+" to ask RS for replacement Worker..." );
			ResourceRequest reply = this.resourceServiceRequest.request(specs);
			ArrayList<ComponentId> available = reply.getAvailableComponents();
			if( available.size() > 0 ){
				ComponentId found = available.get(0);
				log("\tGot Worker: " + found.getId().toString());
				return found;
			}
			//YacsUtils.ignorantSleep(1000);
		}
		
		return null;
	}
	
	private void trigger( Object event ){
		if( !isActiveReplica() )
			return;
		if( workerAggregator == null ){
			log("WorkerAggregator is null. No need to report: " + event);
			return;
		}
		log( "Triggering: " + event );
		triggerInterface.trigger( event );			
	}
	
	private boolean canDelete(){
		if( workers.size() == 0 )
			return true;
		
		/*for( Hashtable<String,TaskCheckpoint> workerTasks : workers.values() ){
			
		}*/
		
		return false;
	}
	
	private boolean haveNewerTaskVersion( long tid, long cid ){
		log( "haveNewerTaskVersion: T:"+tid+", C.id:"+cid );
		
		String stid = String.valueOf(tid);
		String scid = String.valueOf(cid);
		
		ArrayList<String> rems = new ArrayList<String>();
		Enumeration<String> wkeys = workers.keys();
		while( wkeys.hasMoreElements() ){
			
			String wkey = wkeys.nextElement();
			Hashtable<String,TaskCheckpoint> worker = workers.get( wkey );
			
			if( worker.containsKey(stid) ){
				TaskCheckpoint tp = worker.get( stid );
				if( tp.getVersion() > cid ){
					log("\thave NEWER task-checkpoint! T:"+stid+"@v:"+tp.getVersion()+"@W:"+wkey + " vs v:" + cid );
					return true;
				}
			}
		}
		
		return false;
	}
	private void removeOldVersions( long tid, long cid ){
		log( "removeOldVersions: T:"+tid+", C.id:"+cid );
		
		String stid = String.valueOf(tid);
		String scid = String.valueOf(cid);
		
		ArrayList<String> rems = new ArrayList<String>();
		Enumeration<String> wkeys = workers.keys();
		while( wkeys.hasMoreElements() ){
			
			String wkey = wkeys.nextElement();
			Hashtable<String,TaskCheckpoint> worker = workers.get( wkey );
			
			if( worker.containsKey(stid) ){
				TaskCheckpoint tp = worker.get( stid );
				if( tp.getVersion() <= cid ){
					log("\tRemoving T:"+stid+"@v:"+tp.getVersion()+"@W:"+wkey);
					worker.remove( stid );
				}
			}
			// if worker has no more tasks
			if( worker.size() == 0 ){
				rems.add( wkey );
			}
		}
		for( String wkey : rems ){
			workers.remove( wkey );
			log("\tWorker:" + wkey + " removed due to empty tasklist");
		}
	}
	private boolean isAlreadyHealed( long tid, long cid ){
		log( "isAlreadyHealed: T:"+tid+", C.id:"+cid );
		
		String stid = String.valueOf(tid);
		String scid = String.valueOf(cid);
		
		/**
		 * Go through workers list and see if this task is at another worker with at least the same version of the task.
		 * If true then no need to heal.
		 * If task at another worker but with even older version then remove that entry... lazy cleanup
		 */
		
		ArrayList<String> rems = new ArrayList<String>();
		Enumeration<String> wkeys = workers.keys();
		while( wkeys.hasMoreElements() ){
			
			String wkey = wkeys.nextElement();
			Hashtable<String,TaskCheckpoint> worker = workers.get( wkey );
			
			// if another worker has the task
			if( worker.containsKey(stid) ){
				TaskCheckpoint cp = worker.get( stid );
				
				// if that worker has the latest version then there is no need to heal
				if( cp.getVersion() >= cid ){ 
					log("\tNewer T:"+stid+"@v:"+cp.getVersion()+" at another worker W:" + cp.getTask().getWorker().getId());
					return true;
				}
				// else we have found a stale association, lets remove it
				else { 
					worker.remove( stid );
					log("\tRemoved old T:"+stid+"@v:"+cp.getVersion()+" from W:" + cp.getTask().getWorker().getId());
					if( worker.size() == 0 ){
						rems.add( wkey );
					}
				}
			}
		}
		for( String wkey : rems ){
			workers.remove( wkey );
			log("\tWorker:" + wkey + " removed due to empty tasklist");
		}
		
		return false;
	}
	
	protected void logStatus(){
		StringBuilder buffer = new StringBuilder();
		buffer.append( "WW monitoring view status:{" );
				
		Enumeration<String> wkeys = workers.keys();
		while( wkeys.hasMoreElements() ){
			String wkey = wkeys.nextElement();
			Hashtable<String,TaskCheckpoint> worker = workers.get( wkey );
			
			buffer.append( "W:" + wkey +"=");
			
			for( TaskCheckpoint tc : worker.values() ){
				buffer.append( "[T:" + tc.getTask().getTid() +"@v:" + tc.getVersion()+"]" );
			}
		}
		buffer.append( "}" );
		
		log( buffer.toString() );
	}
	
	// HELPERS end
	
	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// Attributes
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	protected void doInit(Object[] parameters) {

		log("WorkerWatcher.init(Object[]): "+parameters);
		workerGroup = (GroupId)parameters[0];
		globalMasterGroup = (GroupId)parameters[1];
		globalWorkerGroup = (GroupId)parameters[2];
		if( parameters[3] != null )
			this.watchdog = (NicheId)parameters[3];
		workerAggregator = (NicheId)parameters[4];
		resourceServiceGroup = (GroupId)parameters[5];

		if( this.deploySensor == null ){
			log("\tDeploySensorInterface is null!");
			return;
		}
		
		// TODO: is a sensor deployed on every single component in the group, including future-added? Or is there only one sensor?
		YacsTimer timer = new YacsTimer( tuid++ );
		Object[] sensorParameters = new Object[2];
		deploySensor.deploySensor(	StateChangeSensor.class.getName(),
									StateChangeEvent.class.getName(), sensorParameters, 
									
									// client interface
									null,
									//new String[] { YACSNames.STATE_CHANGE_CLIENT_INTERFACE },
									
									// server interface
									new String[] { YACSNames.STATE_CHANGE_SERVER_INTERFACE }
									//null
								);
		timefx("WW",""+timer.getTtid(),timer.getTtid(),"SCSD",null,timer.elapsed(),null); // State Change Sensor Deployed
		
		log("\tSensor deployment done!");			
	}
	protected void doInit(NicheActuatorInterface actuator) {
		log("WorkerWatcher.init(NicheActuatorInterface): "+actuator);
		this.actuator = actuator;
		this.logger = actuator.testingOnly();
		
		if( myGlobalId != null ){
			this.createYacsLogger( "WorkerWatcher", String.valueOf(myGlobalId.getReplicaNumber()), true, true, logger );
			log("Logger initialized");
		}
		else {
			this.createYacsLogger( "WorkerWatcher", null, true, true, logger );
		}
		this.logReinit(); // will log only if re-inited
	}
	protected void doInitId(Object id) {
		log("WorkerWatcher.initId: "+id + ", rep#: " + (id!=null&&(id instanceof NicheId)?((NicheId)id).getReplicaNumber():"NULL"));
		myGlobalId = (NicheId)id;
		setActiveReplica( myGlobalId.getReplicaNumber() == 0 );
		log("\tWW-Rep.#: " + myGlobalId.getReplicaNumber());
		
		if( yacsLog != null ){
			yacsLog.setId( String.valueOf(myGlobalId.getReplicaNumber()) );
			log("Logger initialized");
		}
	}
	protected void doReinit(Object[] applicationParameters) {
		log("WorkerWatcher.REinit(Object[]): "  + applicationParameters);
		
		this.setReinited(true);
		
		/**
		 * return new Object[]{
				workerGroup, masterGroup,
				globalWorkerGroup, globalMasterGroup, resourceServiceGroup,					
				watchdog, workerAggregator,				
				workers,				
				eventHistory
			};
		 */
		
		if( applicationParameters == null || applicationParameters.length != 11 ){
			log("\tArray ABNORMAL!");

			this.setAbnormalReinit( "Params null or length!=11" );
			if( yacsLog != null ) this.logReinit();
					
			return;
		}
		if( yacsLog != null ) this.logReinit();
		
		workerGroup = 			(applicationParameters[0]==null?null:(GroupId)applicationParameters[0]);
		masterGroup = 			(applicationParameters[1]==null?null:(GroupId)applicationParameters[1]);
		globalWorkerGroup = 	(applicationParameters[2]==null?null:(GroupId)applicationParameters[2]);
		globalMasterGroup = 	(applicationParameters[3]==null?null:(GroupId)applicationParameters[3]);
		resourceServiceGroup = 	(applicationParameters[4]==null?null:(GroupId)applicationParameters[4]);
		watchdog = 				(applicationParameters[5]==null?null:(NicheId)applicationParameters[5]);
		workerAggregator = 		(applicationParameters[6]==null?null:(NicheId)applicationParameters[6]);
		workers = 				(applicationParameters[7]==null?null:(Hashtable<String,Hashtable<String,TaskCheckpoint>>)applicationParameters[7]);			
		//eventHistory = 			(applicationParameters[8]==null?null:(EventHistory)applicationParameters[8]);
		seenTasks = 			(applicationParameters[9]==null?null:(Hashtable<String,String>)applicationParameters[9]);
		seenWorkers = 			(applicationParameters[9]==null?null:(Hashtable<String,String>)applicationParameters[10]);
	}
	
	// @Override
	protected void doInitCallsPostprocessing(){
		// TODO: WorkerWatcher, what about subscriptions if not active replica?
		if( !isActiveReplica() ){
			log("\tNOT active replica. Quitting!");
			return;
		}
		YacsTimer timer = new YacsTimer( tuid++ );
		
		// subscribe to (worker) resource departure events		
		if( !this.isReinited() ) // subscriptions don't need to be re-inited
		{
			log("\tSubscribing events...");
			dks.niche.wrappers.Subscription subLeave=null, subFail=null, subAggregator=null;
			
			// TODO: subLeave = actuator.subscribe(this.workerGroup, this.myGlobalId, ResourceLeaveEvent.class.getName() );
			timer.reset();
			subFail =  actuator.subscribe(this.workerGroup, this.myGlobalId, ComponentFailEvent.class.getName() );
			timefx("WW",""+timer.getTtid(),timer.getTtid(),"FS",null,timer.elapsed(),null); // Fail Subscription
			
			if( workerAggregator != null ){ 
				timer.reset();
				subAggregator = actuator.subscribe(this.myGlobalId, this.workerAggregator, WorkerManagementEvent.class.getName() );
				timefx("WW",""+timer.getTtid(),timer.getTtid(),"WAS",null,timer.elapsed(),null); // Worker Aggregator Subscription
			}
			
			if( subLeave != null ) subscriptions.add( subLeave );
			if( subFail != null ) subscriptions.add( subFail );
			if( subAggregator != null ) subscriptions.add( subAggregator );
			
			log("\tSubscriptions completed: {"+subLeave+", "+subFail+", "+subAggregator+"}");
		}
		else
			log("Re-inited WorkerWatcher. Subscriptions already in place.");
		
		log("\tBinding to ResourceService: " + this.resourceServiceGroup.getId());
		{
			timer.reset();
			BindId bid = actuator.bind(	this.myGlobalId,
										YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE,
										resourceServiceGroup,
										YACSNames.RESOURCE_SERVICE_REQUEST_SERVER_INTERFACE, 
										JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE );
			timefx("WW",""+timer.getTtid(),timer.getTtid(),"RSBC",null,timer.elapsed(),null); // Resource Service Binding Called
			bindings.add( bid );
		}
		
		// for management of entire worker job-group
		log("\tBinding workerGroupInterface to group: " + workerGroup.getId());
		{
			timer.reset();
			BindId bid = actuator.bind(	this.myGlobalId,
										YACSNames.WORKER_GROUP_CLIENT_INTERFACE,
										workerGroup,
										YACSNames.WORKER_GROUP_SERVER_INTERFACE, 
										JadeBindInterface.ONE_TO_MANY );
			timefx("WW",""+timer.getTtid(),timer.getTtid(),"WJGBC",null,timer.elapsed(),null); // Worker Job Group Binding Called
			
			// since re-deployment need to get latest state from workers 
			if( isReinited() ){
				log("\tRedeployment! Instruct workers to send latest state...");
				while( this.workerGroupInterface == null )
					YacsUtils.ignorantSleep(10);
				timefx("WW",""+timer.getTtid(),timer.getTtid(),"WJGBD",null,timer.elapsed(),null); // Worker Job Group Binding Called
				
				timer.reset();
				workerGroupInterface.publishState(masterGroup,YACSNames.DUMMY_PARAM);
				timefx("WW",""+timer.getTtid(),timer.getTtid(),"PS",null,timer.elapsed(),null); // Publish State
			}
			bindings.add( bid );
		}
		
		// to get information about the master group to which the worker group belong
		log("\tBinding Information-Interface to group: " + workerGroup.getId());
		{
			timer.reset();
			BindId bid = actuator.bind(	this.myGlobalId,	YACSNames.INFORMATION_CLIENT_INTERFACE,
										workerGroup,		YACSNames.INFORMATION_SERVER_INTERFACE, 
										JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE );
			timefx("WW",""+timer.getTtid(),timer.getTtid(),"IBC",null,timer.elapsed(),null); // Information Binding Called
			
			log("\tBinding done. Waiting for interface...");
			while( this.informationInterface == null )
				YacsUtils.ignorantSleep(10);
			timefx("WW",""+timer.getTtid(),timer.getTtid(),"IBD",null,timer.elapsed(),null); // Information Binding Done
			
			timer.reset();
			masterGroup = informationInterface.masterGroup();
			timefx("WW",""+timer.getTtid(),timer.getTtid(),"MGL",null,timer.elapsed(),null); // Master Group Learnt
			log("\tMaster group is: " + masterGroup.getId());
			
			// TODO: should I maybe keep this binding... are bindings expensive?
			actuator.unbind(bid);
		}
		
		// got the master group id, not bind to it to inform it of initialization completion
		log("\tBinding masterInterface to group: " + masterGroup);
		{	
			timer.reset();
			BindId bid = actuator.bind(	this.myGlobalId,	YACSNames.MASTER_CLIENT_INTERFACE,
										masterGroup,		YACSNames.MASTER_SERVER_INTERFACE, 
										JadeBindInterface.ONE_TO_MANY );
			timefx("WW",""+timer.getTtid(),timer.getTtid(),"MGBC",null,timer.elapsed(),null); // Master Group Binding Called

			log("\tBinding done. Waiting for interface...");
			while( this.masterInterface == null )
				YacsUtils.ignorantSleep(10);
			timefx("WW",""+timer.getTtid(),timer.getTtid(),"MGBD",null,timer.elapsed(),null); // Master Group Binding Done
			
			log("\tInforming Master(s)...");
			timer.reset();
			masterInterface.workerWatcherInitalized( YACSNames.DUMMY_PARAM );
			timefx("WW",""+timer.getTtid(),timer.getTtid(),"MII",null,timer.elapsed(),null); // Master Informed of Initialization
			
			//actuator.unbind(bid);
			bindings.add( bid );
		}
		
		log("\tWorkerWatcher init post-processing done!");
	}
	
	private void deployWatchdog(){
		
		if( !USE_WATCHDOG ){
			log("WorkerWatcher.deployWatchdog: NOT using watchdogs!");
			return;
		}
		
		if( this.watchdog != null ){
			log("WorkerWatcher.deployWatchdog: Watchdog already in place: " + watchdog);
			return;
		}
		
		ManagementDeployParameters params = new ManagementDeployParameters();
		
		log("\tDescribing Watchdog...");
		params.describeWatcher(
				YACSNames.WATCHDOG_CLASS_NAME,
				YACSNames.WATCHDOG_ADL_NAME,
				actuator.getComponentType(YACSNames.WATCHDOG_CLASS_NAME),
				new Object[]{ 	this.myGlobalId, YACSNames.WORKER_COMPONENT, workerGroup, 
								globalMasterGroup, globalWorkerGroup, null, workerAggregator,
								resourceServiceGroup },
				workerGroup.getId()
			);
		
		watchdog = actuator.deploy(params, null);
		/*dks.niche.wrappers.Subscription rle, rfe, me;
		
		log("\tDeploying Watchdog WITH NULL CO-LOCATION...");
		//ManagementElementId watchdog = actuator.deploy(params, workerGroup);
		ManagementElementId watchdog = actuator.deploy(params, null);
		log("\tSubscribing Watchdog to WorkerWatcher...");
		rle = actuator.subscribe(myGlobalId, watchdog, ResourceLeaveEvent.class.getName() );
		rfe = actuator.subscribe(myGlobalId, watchdog, ResourceFailEvent.class.getName() );
		me  = actuator.subscribe(myGlobalId, watchdog, ManagementEvent.class.getName() );
		log("\tWD-2-WM.subs: " + rle +", " + rfe + ", " + me);
		
		log("\tSubstribing WorkerWatcher to Watchdog...");
		rle = actuator.subscribe(watchdog, myGlobalId, ResourceLeaveEvent.class.getName() );
		rfe = actuator.subscribe(watchdog, myGlobalId, ResourceFailEvent.class.getName() );
		me  = actuator.subscribe(watchdog, myGlobalId, ManagementEvent.class.getName() );
		log("\tWM-2-WD.subs: " + rle +", " + rfe + ", " + me);*/
		
		log("\tWorker-Watchdog setup done! Id: " + watchdog.getId());
	}

	public Object[] getAttributes() {
		log("WorkerWatcher.getAttributes");
		return new Object[]{
				workerGroup, masterGroup,
				globalWorkerGroup, globalMasterGroup, resourceServiceGroup,					
				watchdog, workerAggregator,				
				workers,				
				eventHistory,
				seenTasks,
				seenWorkers
			};
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	public String[] listFc() {
		log("WorkerWatcher.listFc");
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE,
				YACSNames.DEPLOY_SENSOR,
				YACSNames.INFORMATION_CLIENT_INTERFACE,
				YACSNames.MASTER_CLIENT_INTERFACE,
				YACSNames.TASK_MANAGEMENT_CLIENT_INTERFACE,
				YACSNames.WORKER_GROUP_CLIENT_INTERFACE,
				YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {
		log("WorkerWatcher.lookupFc: "+itfName);
		if (itfName.equals(FractalInterfaceNames.COMPONENT))
			return myself;
		else if (itfName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return actuator;		
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			return triggerInterface;		
		else if(itfName.equals(YACSNames.DEPLOY_SENSOR))
			return deploySensor;		
		else if(itfName.equals(YACSNames.INFORMATION_CLIENT_INTERFACE))
			return informationInterface;
		else if(itfName.equals(YACSNames.MASTER_CLIENT_INTERFACE))
			return this.masterInterface;
		else if(itfName.equals(YACSNames.TASK_MANAGEMENT_CLIENT_INTERFACE))
			return this.taskManagement;
		else if(itfName.equals(YACSNames.WORKER_GROUP_CLIENT_INTERFACE))
			return this.workerGroupInterface;
		else if(itfName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE))
			return this.resourceServiceRequest;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		log("WorkerWatcher.bindFc: " + itfName );
		if (itfName.equals("component"))
			myself = (Component) itfValue;
		else if (itfName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			actuator = (NicheActuatorInterface) itfValue;
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			triggerInterface = (TriggerInterface)itfValue;
		else if (itfName.equals(YACSNames.DEPLOY_SENSOR))
			deploySensor = (DeploySensorsInterface)itfValue;
		else if(itfName.equals(YACSNames.INFORMATION_CLIENT_INTERFACE))
			informationInterface = (InformationInterface)itfValue;
		else if(itfName.equals(YACSNames.MASTER_CLIENT_INTERFACE))
			masterInterface = (JobMasterGroupInterface)itfValue;
		else if(itfName.equals(YACSNames.TASK_MANAGEMENT_CLIENT_INTERFACE))
			taskManagement = (TaskManagementInterface)itfValue;
		else if(itfName.equals(YACSNames.WORKER_GROUP_CLIENT_INTERFACE))
			workerGroupInterface = (JobWorkerGroupInterface)itfValue;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE))
			resourceServiceRequest = (ResourceServiceRequestInterface)itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		log("WorkerWatcher.unbindFc: " + itfName);
		if (itfName.equals(FractalInterfaceNames.COMPONENT))
			myself = null;
		else if (itfName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			actuator = null;
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			triggerInterface = null;
		else if (itfName.equals(YACSNames.DEPLOY_SENSOR))
			deploySensor = null;
		else if(itfName.equals(YACSNames.INFORMATION_CLIENT_INTERFACE))
			informationInterface = null;
		else if(itfName.equals(YACSNames.MASTER_CLIENT_INTERFACE))
			masterInterface = null;
		else if(itfName.equals(YACSNames.TASK_MANAGEMENT_CLIENT_INTERFACE))
			taskManagement = null;
		else if(itfName.equals(YACSNames.WORKER_GROUP_CLIENT_INTERFACE))
			workerGroupInterface = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {
		log("WorkerWatcher.getFcState");
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		log("WorkerWatcher.startFc");
		status = true;
		log("WW started. Version: " + YACSSettings.YACS_VERSION);
	}

	public void stopFc() throws IllegalLifeCycleException {
		log("WorkerWatcher.stopFc");
		status = false;

	}
}
