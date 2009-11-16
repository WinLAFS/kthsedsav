package yacs.job.watchers;

import java.util.ArrayList;

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

import yacs.interfaces.YACSNames;
import yacs.interfaces.YACSSettings;
import yacs.job.*;
import yacs.job.events.*;
import yacs.job.helpers.DiscoveryReply;
import yacs.job.interfaces.JobManagementInterface;
import yacs.job.interfaces.JobMasterGroupInterface;
import yacs.job.sensors.StateChangeSensor;
import yacs.job.state.JobCheckpoint;
import yacs.resources.data.ResourceRequest;
import yacs.resources.interfaces.ResourceServiceRequestInterface;
import yacs.utils.YacsUtils;
import yacs.utils.EventHistory;
import yacs.utils.YacsTimer;


public class MasterWatcher extends yacs.YacsManagementElement 
							implements 	EventHandlerInterface, MovableInterface, 
										InitInterface, BindingController, 
										LifeCycleController
{
	private Component myself;	
	private NicheActuatorInterface actuator;
	private NicheAsynchronousInterface logger;
	
	private TriggerInterface triggerInterface;
	private DeploySensorsInterface deploySensor;
	
	private JobMasterGroupInterface masterGroupInterface;
	private JobManagementInterface jobManagement;
	private ResourceServiceRequestInterface resourceServiceRequest;
	
	private boolean status;
	private NicheId myGlobalId;
	
	// members for replication - begin
	private NicheId watchdog, masterAggregator;
	
	private GroupId masterGroup;
	private GroupId globalMasterGroup, globalWorkerGroup, resourceServiceGroup;
	private JobCheckpoint jobCP;
	
	private EventHistory eventHistory = new EventHistory();
	// members for replication - end
	
	// for timing
	private long tuid;
		
	// for cleanup
	private ArrayList<BindId> bindings = new ArrayList<BindId>();
	private ArrayList<dks.niche.wrappers.Subscription> subscriptions = new ArrayList<dks.niche.wrappers.Subscription>();
	
	static final boolean USE_WATCHDOG = System.getProperty("yacs.job.master.watcher.watchdog") instanceof String ?
			(0 < Integer.parseInt(System.getProperty("yacs.job.master.watcher.watchdog")) ? true : false) 
			: false;
	
	public MasterWatcher(){
		log("MasterWatcher created!");
	}

	// EventHandlerInterface
	public void eventHandler(Object e, int flag) {
		long tstart = System.currentTimeMillis(), ttuid = tuid++;
		log("MasterWatcher.eventHandler: "+e);
		
		if(e instanceof ResourceLeaveEvent) {
			String cid = ((ResourceLeaveEvent)e).getNicheId().toString();
			
			if( !eventHistory.record("ResourceLeaveEvent:"+cid,null) ){
				log("\tResourceLeaveEvent event seen before! ID: " + cid);
				return;
			}

			// watchdog or master left...?
			if( watchdog!=null && cid==watchdog.toString() )
				this.handleWatchdogDeparture();
			else
				this.handleMasterDeparture( ttuid );
			time("MW",""+tuid,"HJRLE",null,(System.currentTimeMillis()-tstart),null); // Handle Job Resource Leave Event
		}
		else if(e instanceof ComponentFailEvent ) {
			String cid = ((ComponentFailEvent)e).getNicheId().toString();
			
			if( !eventHistory.record("ComponentFailEvent:"+cid,null) ){
				log("\tComponentFailEvent event seen before! ID: " + cid);
				return;
			}
			
			// watchdog or master failed...?
			if( watchdog!=null && cid==watchdog.toString() )
				this.handleWatchdogDeparture();
			else
				this.handleMasterDeparture( ttuid );
			time("MW",""+tuid,"HJCFE",null,(System.currentTimeMillis()-tstart),null); // Handle Job Component Failure Event
		}
		else if(e instanceof StateChangeEvent){
			StateChangeEvent change = (StateChangeEvent)e;
			
			if( !(change.getStateInformation() instanceof JobCheckpoint) ){
				log("\tState isn't a JobCheckpoint: " + change.getStateInformation());
				return;
			}
			this.handleJobStateChange( change, ttuid );
			time("MW",""+tuid,"HJSCE",null,(System.currentTimeMillis()-tstart),null); // Handle Job State Change Event
		}
	}
	
	// CUSTOM EVENT HANDLERS begin
	private void handleWatchdogDeparture(){
		this.deployWatchdog();
		log("\tDone with Watchdog healing!");
	}
	private void handleMasterDeparture( long ttuid ){
		log("MasterWatcher.handleMasterDeparture:");
		YacsTimer timer = new YacsTimer( ttuid );
		
		if( !isActiveReplica() ){
			log("\tNOT active replica. Quitting!");
			return;
		}
		
		// find a replacement Master component to take on the job
		ComponentId master = this.findReplacementMaster( ttuid );
		
		// if none is found we are in trouble.
		if( master == null ){
			// TODO: try again... and maybe eventually report complete failure to Frontend
			log("\tCRITICAL ERROR: cannot find replacement master!");
			return;
		}
		log("\tFound replacement Master: " + master.getId() );
		
		timer.reset();
		actuator.addToGroup(master, this.masterGroup);
		timefx("MW",""+timer.getTtid(),timer.getTtid(),"HMDRAG",null,timer.elapsed(),null); // Handle Master Departure, Replacement Added to Group
	
		// bind to the new Master and tell it to continue from the last job checkpoint
		try{
			log("\tBinding to replacement Master..." );
			timer.reset();
			
			BindId bid = actuator.bind(	this.myGlobalId,	YACSNames.JOB_MANAGEMENT_CLIENT_INTERFACE, 
										master, 			YACSNames.JOB_MANAGEMENT_SERVER_INTERFACE, 
										JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE );
			timefx("MW",""+timer.getTtid(),timer.getTtid(),"HMDMBC",null,timer.elapsed(),null); // Handle Master Departure, Master Binding Called
			
			while( jobManagement == null )
				YacsUtils.ignorantSleep(10);
			timefx("MW",""+timer.getTtid(),timer.getTtid(),"HMDMBD",null,timer.elapsed(),null); // Handle Master Departure, Master Binding Done
			
			log("\tInstructing Master to perform job: " + jobCP );
			timer.reset();
			jobManagement.performJob( jobCP );
			timefx("MW",""+timer.getTtid(),timer.getTtid(),"HMDPJ",null,timer.elapsed(),null); // Handle Master Departure, Peform Job
			
			actuator.unbind(bid);
			log("\tDone with Master healing!" );
		}
		finally {
			this.jobManagement = null;
		}
	}
	private void handleJobStateChange( StateChangeEvent change, long ttuid ){
		JobCheckpoint cp = (JobCheckpoint)change.getStateInformation();
		log("MasterWatcher.handleJobStateChange: CP.id:" + cp.getVersion()  );
		
		if( jobCP == null ){
			trigger( new MasterManagementEvent(MasterManagementEvent.TYPE.JOB_STARTED) );
		}
		
		// in case checkpoint events get reordered in transit... check if have more recent
		if( jobCP != null && jobCP.getVersion() >= cp.getVersion() ){
			log("\thave NEWER checkpoint! " + jobCP.getVersion() +" vs " + cp.getVersion() );
			return;
		}
		
		jobCP = cp;
		Job job = jobCP.getJob();
		
		// TODO: More thought-out event reporting to MasterAggregator. Now just empty event on job completion
		if( job.getRemaining().size()==0 && job.getPending().size()==0 )
			trigger( new MasterManagementEvent(MasterManagementEvent.TYPE.JOB_COMPLETED) );
		
		if( job.isDeleted() ){
			this.handleJobDeletion( ttuid );
		}
	}
	private void handleJobDeletion( long ttuid ){
		log("MasterWatcher.handleJobDeletion:");
		YacsTimer timer = new YacsTimer( ttuid );
		
		// TODO: MasterWatcher: if not active replica is there any cleanup to be done? At least un-deploy, or does DCMS replication handling take care of that?
		if( !isActiveReplica() ){
			log("\tNOT active replica. Quitting!");
			return;
		}
		
		// TODO: MasterWatcher: job deleted. E.g. how to undeploy watcher?
		/**
		 * Trigger MasterManagementEvent
		 * Unbind all bindings
		 * Unsubscribe all subscriptions
		 * Undeploy
		 */
		
		// TODO: appropriate proper content in the MME
		trigger( new MasterManagementEvent(MasterManagementEvent.TYPE.JOB_DELETED) );
		
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
		// TODO: MasterWatcher undeploy function...
		log("\tDismantling done!");
		timefx("MW",""+timer.getTtid(),timer.getTtid(),"HJDDC",null,timer.elapsed(),null); // Handle Job Deletion, Deletion Completed - TODO: more fine grained timing, e.g. how long it takes to unsubscribe
	}
	// CUSTOM EVENT HANDLERS end
	
	// HELPERS begin
	private ComponentId findReplacementMaster( long ttuid ){
		YacsTimer timer = new YacsTimer( ttuid );
		ComponentId master = null;
		try {
			master = findReplacementMaster_Service();
		}
		catch( Exception e ){
			log("Exception in finding replacement master: " + e.getMessage());
			e.printStackTrace();
			master = null;
		}
		finally {
			timefx("MW",""+timer.getTtid(),timer.getTtid(),"FRM",null,timer.elapsed(),(master!=null?master.getId().toString():"NULL")); // Find Replacement Master
		}
		return master;
	}
	private ComponentId findReplacementMaster_Global() throws Exception {
		throw new Exception("Deprecated!");
	}	
	private ComponentId findReplacementMaster_Service(){
		ResourceRequest specs = new ResourceRequest();
		specs.setComponentType( YACSNames.MASTER_COMPONENT );
		
		int tries = 0;
		while( tries++<5 ){
			log( "\tAttempt "+(tries)+" to ask RS for replacement Master..." );
			ResourceRequest reply = this.resourceServiceRequest.request(specs);
			ArrayList<ComponentId> available = reply.getAvailableComponents();
			if( available.size() > 0 ){
				ComponentId found = available.get(0);
				log("\tGot Master: " + found.getId().toString());
				return found;
			}
			else {
				try{
					Thread.sleep(1000);
				}
				catch( Exception e ){
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	private void trigger( Object event ){
		if( !isActiveReplica() )
			return;
		if( masterAggregator == null ){
			log("MasterAggregator is null. No need to report: " + event);
			return;
		}
		log( "Triggering: " + event );
		triggerInterface.trigger( event );			
	}
	// HELPERS end
	
	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// Attributes
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	protected void doInit(Object[] parameters) {
		// No params to set
		log("MasterWatcher.init(Object[]): "+parameters);
		
		masterGroup = (GroupId)parameters[0];
		globalMasterGroup = (GroupId)parameters[1];
		globalWorkerGroup = (GroupId)parameters[2];
		this.watchdog = (parameters[3]==null?null:(NicheId)parameters[3]);
		masterAggregator = (NicheId)parameters[4];
		resourceServiceGroup = (GroupId)parameters[5];
		
		if( this.deploySensor == null ){
			log("\tDeploySensorInterface is null!");
			return;
		}
		
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
		timefx("MW",""+timer.getTtid(),timer.getTtid(),"SCSD",null,timer.elapsed(),null); // State Change Sensor Deployed
		
		log("\tSensor deployment done!");
	}
	protected void doInit(NicheActuatorInterface actuator) {
		log("MasterWatcher.init(NicheActuatorInterface): "+actuator);
		this.actuator = actuator;
		this.logger = actuator.testingOnly();

		if( myGlobalId != null ){
			this.createYacsLogger( "MasterWatcher", String.valueOf(myGlobalId.getReplicaNumber()), true, true, logger );
			log("Logger initialized");
		}
		else {
			this.createYacsLogger( "MasterWatcher", null, true, true, logger );
		}
		this.logReinit(); // will log only if re-inited
	}
	protected void doInitId(Object id) {
		log("MasterWatcher.initId: "+id + ", rep#: " + (id!=null&&(id instanceof NicheId)?((NicheId)id).getReplicaNumber():"NULL"));
		myGlobalId = (NicheId)id;
		setActiveReplica( myGlobalId.getReplicaNumber() == 0 );
		log("\tMW-Rep.#: " + myGlobalId.getReplicaNumber());
		
		if( yacsLog != null ){
			yacsLog.setId( String.valueOf(myGlobalId.getReplicaNumber()) );
			log("Logger initialized");
		}
	}
	protected void doReinit(Object[] applicationParameters) {
		log("MasterWatcher.REinit(Object[]): "  + applicationParameters);
		
		this.setReinited(true);
				
		/**
		 * return new Object[]{
				watchdog, masterAggregator,				
				masterGroup,
				globalMasterGroup, globalWorkerGroup, resourceServiceGroup,
				job,				
				eventHistory
			};
		 */
		if( applicationParameters == null || applicationParameters.length != 8 ){
			log("\tArray ABNORMAL!");

			this.setAbnormalReinit( "Params null or length!=8" );
			if( yacsLog != null ) this.logReinit();
					
			return;
		}
		if( yacsLog != null ) this.logReinit();
		
		watchdog = 				(applicationParameters[0]==null?null:(NicheId)applicationParameters[0]);
		masterAggregator = 		(applicationParameters[1]==null?null:(NicheId)applicationParameters[1]);
		masterGroup = 			(applicationParameters[2]==null?null:(GroupId)applicationParameters[2]);
		globalMasterGroup = 	(applicationParameters[3]==null?null:(GroupId)applicationParameters[3]);
		globalWorkerGroup = 	(applicationParameters[4]==null?null:(GroupId)applicationParameters[4]);
		resourceServiceGroup = 	(applicationParameters[5]==null?null:(GroupId)applicationParameters[5]);
		jobCP = 				(applicationParameters[6]==null?null:(JobCheckpoint)applicationParameters[6]);
		eventHistory = 			(applicationParameters[7]==null?null:(EventHistory)applicationParameters[7]);
	}
	
	// @Override
	protected void doInitCallsPostprocessing(){
		// TODO: MasterWatcher, what about subscriptions if not active replica? 
		if( !isActiveReplica() ){
			log("\tNOT active replica. Quitting!");
			return;
		}
		YacsTimer timer = new YacsTimer( tuid++ );
		
		// subscribe to (master) resource departure events
		if( !this.isReinited() ) // subscriptions don't need to be re-inited
		{
			log("\tSubscribing events...");
			dks.niche.wrappers.Subscription subLeave=null, subFail=null, subAggregator=null;
						
			// TODO: subLeave = actuator.subscribe(this.masterGroup, this.myGlobalId, ResourceLeaveEvent.class.getName() );
			timer.reset();
			subFail =  actuator.subscribe(this.masterGroup, this.myGlobalId, ComponentFailEvent.class.getName() );
			timefx("MW",""+timer.getTtid(),timer.getTtid(),"FS",null,timer.elapsed(),null); // Fail Subscription
			
			if( masterAggregator != null ){ 
				timer.reset();
				subAggregator = actuator.subscribe(this.myGlobalId, this.masterAggregator, MasterManagementEvent.class.getName() );
				timefx("MW",""+timer.getTtid(),timer.getTtid(),"MAS",null,timer.elapsed(),null); // Master Aggregator Subscription
			}				
						
			if( subLeave != null ) subscriptions.add( subLeave );
			if( subFail != null ) subscriptions.add( subFail );
			if( subAggregator != null ) subscriptions.add( subAggregator );
			
			log("\tSubscriptions completed: {"+subLeave+", "+subFail+", "+subAggregator+"}");
		}
		else
			log("Re-inited MasterWatcher. Subscriptions already in place.");
		
		log("\tBinding MasterWatcher("+this.myGlobalId+") to ResourceService: " + resourceServiceGroup.getId());
		{
			timer.reset();
			BindId bid = actuator.bind(	this.myGlobalId,
										YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE,
										resourceServiceGroup,
										YACSNames.RESOURCE_SERVICE_REQUEST_SERVER_INTERFACE, 
										JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE );
			timefx("MW",""+timer.getTtid(),timer.getTtid(),"RSBC",null,timer.elapsed(),null); // Resource Service Binding Called
			bindings.add( bid );
		}
		
		
		// for management of (entire) master job-group
		log("\tBinding workerGroupInterface to group: " + masterGroup.getId());
		{
			timer.reset();
			BindId bid = actuator.bind(	this.myGlobalId,
										YACSNames.MASTER_CLIENT_INTERFACE,
										masterGroup,
										YACSNames.MASTER_SERVER_INTERFACE, 
										JadeBindInterface.ONE_TO_MANY );
			timefx("MW",""+timer.getTtid(),timer.getTtid(),"MJGBC",null,timer.elapsed(),null); // Master Job Group Binding Called
			
			// since re-deployment need to get latest state from workers 
			if( isReinited() ){
				log("\tRedeployment! Instruct master(s) to send latest state...");
				while( this.masterGroupInterface == null )
					YacsUtils.ignorantSleep(10);
				timefx("MW",""+timer.getTtid(),timer.getTtid(),"MJGBD",null,timer.elapsed(),null); // Master Job Group Binding Called
				
				timer.reset();
				masterGroupInterface.publishState(YACSNames.DUMMY_PARAM);
				timefx("MW",""+timer.getTtid(),timer.getTtid(),"PS",null,timer.elapsed(),null); // Publish State
			}
			bindings.add( bid );
		}
		
		log("\tMasterWatcher init post-processing done!");
	}
	
	private void deployWatchdog(){
		
		if( !USE_WATCHDOG ){
			log("MasterWatcher.deployWatchdog: NOT using watchdogs!");
			return;
		}
		
		if( this.watchdog != null ){
			log("MasterWatcher.deployWatchdog: Watchdog already in place: " + watchdog);
			return;
		}
		
		ManagementDeployParameters params = new ManagementDeployParameters();
		
		log("\tDescribing Watchdog...");
		params.describeWatcher(
				YACSNames.WATCHDOG_CLASS_NAME,
				YACSNames.WATCHDOG_ADL_NAME,
				actuator.getComponentType(YACSNames.WATCHDOG_CLASS_NAME),
				new Object[]{ 	this.myGlobalId, YACSNames.MASTER_COMPONENT, masterGroup, 
								globalMasterGroup, globalWorkerGroup, masterAggregator, null,
								resourceServiceGroup },
				masterGroup.getId()
			);
		
		watchdog = actuator.deploy(params, null);
		/*log("\tDeploying Watchdog WITH NULL CO-LOCATION...");
		//ManagementElementId watchdog = actuator.deploy(params, masterGroup);
		ManagementElementId watchdog = actuator.deploy(params, null);
		log("\tSubstribing Watchdog to MasterWatcher...");
		actuator.subscribe(myGlobalId, watchdog, ResourceLeaveEvent.class.getName() );
		actuator.subscribe(myGlobalId, watchdog, ResourceFailEvent.class.getName() );
		actuator.subscribe(myGlobalId, watchdog, ManagementEvent.class.getName() );
		
		log("\tSubstribing MasterWatcher to Watchdog...");
		actuator.subscribe(watchdog, myGlobalId, ResourceLeaveEvent.class.getName() );
		actuator.subscribe(watchdog, myGlobalId, ResourceFailEvent.class.getName() );
		actuator.subscribe(watchdog, myGlobalId, ManagementEvent.class.getName() );*/
		log("\tMaster-Watchdog setup done! Id: " + watchdog.getId());
	}

	public Object[] getAttributes() {
		log("MasterWatcher.getAttributes");
		return new Object[]{
				watchdog, masterAggregator,				
				masterGroup,
				globalMasterGroup, globalWorkerGroup, resourceServiceGroup,
				jobCP,				
				eventHistory
			};
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	public String[] listFc() {
		log("MasterWatcher.listFc");
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE,
				YACSNames.DEPLOY_SENSOR,
				YACSNames.MASTER_CLIENT_INTERFACE,
				YACSNames.JOB_MANAGEMENT_CLIENT_INTERFACE,
				YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {
		log("MasterWatcher.lookupFc: "+itfName);
		if (itfName.equals(FractalInterfaceNames.COMPONENT))
			return myself;
		else if (itfName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return actuator;
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			return triggerInterface;
		else if(itfName.equals(YACSNames.DEPLOY_SENSOR))
			return deploySensor;		
		else if(itfName.equals(YACSNames.MASTER_CLIENT_INTERFACE))
			return this.masterGroupInterface;
		else if(itfName.equals(YACSNames.JOB_MANAGEMENT_CLIENT_INTERFACE))
			return this.jobManagement;
		else if(itfName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE))
			return this.resourceServiceRequest;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		log("MasterWatcher.bindFc: " + itfName );
		if (itfName.equals("component"))
			myself = (Component) itfValue;
		else if (itfName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			actuator = (NicheActuatorInterface) itfValue;
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			triggerInterface = (TriggerInterface)itfValue;
		else if (itfName.equals(YACSNames.DEPLOY_SENSOR))
			deploySensor = (DeploySensorsInterface)itfValue;
		else if (itfName.equals(YACSNames.MASTER_CLIENT_INTERFACE))
			masterGroupInterface = (JobMasterGroupInterface)itfValue;
		else if (itfName.equals(YACSNames.JOB_MANAGEMENT_CLIENT_INTERFACE))
			jobManagement = (JobManagementInterface)itfValue;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE))
			resourceServiceRequest = (ResourceServiceRequestInterface)itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		log("MasterWatcher.unbindFc: " + itfName);
		if (itfName.equals(FractalInterfaceNames.COMPONENT))
			myself = null;
		else if (itfName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			actuator = null;
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			triggerInterface = null;
		else if (itfName.equals(YACSNames.DEPLOY_SENSOR))
			deploySensor = null;
		else if (itfName.equals(YACSNames.MASTER_CLIENT_INTERFACE))
			masterGroupInterface = null;
		else if (itfName.equals(YACSNames.JOB_MANAGEMENT_CLIENT_INTERFACE))
			jobManagement = null;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE))
			resourceServiceRequest = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {
		log("MasterWatcher.getFcState");
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		log("MasterWatcher.startFc");
		status = true;
		log("MW started. Version: " + YACSSettings.YACS_VERSION);
	}

	public void stopFc() throws IllegalLifeCycleException {
		log("MasterWatcher.stopFc");
		status = false;

	}
}
