package yacs.resources.aggregators;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.*;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.IdentifierInterface;

import yacs.interfaces.YACSSettings;
import yacs.job.Job;
import yacs.resources.events.*;
import yacs.resources.data.AvailabilityInformation;

import yacs.utils.EventHistory;
import yacs.utils.monitoring.*;

import java.util.Hashtable;

public class ServiceAggregator 	extends yacs.YacsManagementElement
								implements	EventHandlerInterface, MovableInterface,
											BindingController, LifeCycleController {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = 1L;

	// ///////////////////
	Component mySelf;
	TriggerInterface triggerInterface;
	NicheId myId;

	private NicheActuatorInterface actuator;
	private NicheAsynchronousInterface logger;
	private boolean status;
	
	private long MINIMUM_ACTIVE_SERVICES = (System.getProperty("yacs.availability.minimum.res.services") instanceof String ? 
												Long.parseLong(System.getProperty("yacs.availability.minimum.res.services"))
												: 1 );
	
	private long MINIMUM_FREE_MASTERS = (System.getProperty("yacs.availability.minimum.free.masters") instanceof String ? 
												Long.parseLong(System.getProperty("yacs.availability.minimum.free.masters"))
												: 0 );

	private long MINIMUM_FREE_WORKERS = (System.getProperty("yacs.availability.minimum.free.workers") instanceof String ? 
												Long.parseLong(System.getProperty("yacs.availability.minimum.free.workers"))
												: 1 );
	
	public static final long STABILIZE_TIME = (System.getProperty("yacs.availability.stabilize.time.msek") instanceof String ? 
												Long.parseLong(System.getProperty("yacs.availability.stabilize.time.msek"))
												: 30000 );
	
	// members for replication - begin
	private AvailabilityInformation lastKnownState;
	
	private long lastNeedResourceService;
	private long lastNeedMaster;
	private long lastNeedWorker;
	
	private Hashtable<String,String> resourceComponents = new Hashtable<String,String>();
	// members for replication - end
	
	private MProducer monitor = new MProducer();
	
	// timing vars
	private long tuid = 0;
	private long timeRequestRS = 0;
	private long timeRequestMaster = 0, timeMasterRefCount = 0;
	private long timeRequestWorker = 0, timeWorkerRefCount = 0;
	
	// empty constructor always needed!
	public ServiceAggregator() {}
	
	// EventHandlerInterface
	public void eventHandler(Object e, int flag) {
		log("ServiceAggregator.eventHandler: " + e);
		
		if( !(e instanceof ServiceManagementEvent) ){
			log("\tUnknown event type: " + e.getClass().getName() );
			return;
		}
		
		ServiceManagementEvent sme = (ServiceManagementEvent)e;
		
		if( sme.getEventType() == ServiceManagementEvent.TYPE.SERVICE_ADDED )
			handleNewServiceComponent(sme);
		else if( sme.getEventType() == ServiceManagementEvent.TYPE.SERVICE_DEPARTED )
			handleServiceComponentDeparted( sme );
		else if( sme.getEventType() == ServiceManagementEvent.TYPE.SERVICE_HIGH_LOAD )
			handleServiceHighLoad( sme );
		else if( sme.getEventType() == ServiceManagementEvent.TYPE.AVAILABILITY_INFORMATION )
			handleAvailabilityInformation( sme );
		else {
			log("\tUnknown SME.event_type: " + sme.getEventType());
		}
		
		// TODO: availability info event, analyze and decide on add|remove resources (master,worker,service)
	}
	
	// CUSTOM EVENT HANDLERS begin
	private void handleAvailabilityInformation( ServiceManagementEvent sme ){
		if( sme.getAvailability() == null ){
			log("\tAvailability is null!");
			return;
		}
		
		AvailabilityInformation latest = sme.getAvailability();
		
		// Time of Add, i.e. how long since first Master request until fulfilled
		if( timeRequestMaster != 0 && (latest.getBusyMasterComponents()+latest.getFreeMasterComponents()>timeMasterRefCount) ){
			timefx( "SA", null, tuid++, "MARF", null, (System.currentTimeMillis()-timeRequestMaster), null ); // Master Add Request Fulfilled
			this.timeRequestMaster = 0;
		}
		if( timeRequestWorker != 0 && (latest.getBusyWorkerComponents()+latest.getFreeWorkerComponents()>timeWorkerRefCount) ){
			timefx( "SA", null, tuid++, "WARF", null, (System.currentTimeMillis()-timeRequestWorker), null ); // Worker Add Request Fulfilled
			this.timeRequestWorker = 0;
		}
		
		this.compare( latest );
		long currentTime = System.currentTimeMillis();
		
		// too few masters
		if( latest.getFreeMasterComponents() < MINIMUM_FREE_MASTERS ){
			if( !isStabilizedSinceLastMaster(currentTime,latest) )
				log("\tService not stabilized since last MASTER_NEEDED!");
			else {
				log("\tNeed "+(MINIMUM_FREE_MASTERS-latest.getFreeMasterComponents())+" master(s)!");
				trigger( new ServiceManagementEvent(sme.getId(),ServiceManagementEvent.TYPE.AVAILABILITY_MASTER_NEEDED) );
				this.lastNeedMaster = currentTime;
				if( timeRequestMaster == 0 ){ timeRequestMaster = System.currentTimeMillis(); timeMasterRefCount=latest.getBusyMasterComponents()+latest.getFreeMasterComponents(); }
			}
		}
		
		// too few workers
		if( latest.getFreeWorkerComponents() < MINIMUM_FREE_WORKERS ){
			if( !isStabilizedSinceLastWorker(currentTime,latest) )
				log("\tService not stabilized since last WORKER_NEEDED!");
			else {
				log("\tNeed "+(MINIMUM_FREE_WORKERS-latest.getFreeWorkerComponents())+" worker(s)!");
				trigger( new ServiceManagementEvent(sme.getId(),ServiceManagementEvent.TYPE.AVAILABILITY_WORKER_NEEDED) );
				this.lastNeedWorker = currentTime;
				if( timeRequestWorker == 0 ){ timeRequestWorker = System.currentTimeMillis(); timeWorkerRefCount=latest.getBusyWorkerComponents()+latest.getFreeWorkerComponents(); }
			}
		}
		
		// store last view of system for comparison next time
		this.lastKnownState = latest;
		this.monitorAvailability();
		
	}
	private void handleNewServiceComponent( ServiceManagementEvent sme ){
		updateServiceList( sme );
		
		// Time of Add, i.e. how long since first RS request until fulfilled
		if( this.timeRequestRS !=0 ){
			timefx( "SA", null, tuid++, "RARF", null, (System.currentTimeMillis()-timeRequestRS), null ); // Rs Add Request Fulfilled
			this.timeRequestRS = 0;
		}
		
		// if still too few, ask for more
		if( resourceComponents.size() < MINIMUM_ACTIVE_SERVICES ){
			log("\tStill too few services! " + resourceComponents.size()+" vs. " +MINIMUM_ACTIVE_SERVICES);
			trigger( new ServiceManagementEvent(sme.getId(),ServiceManagementEvent.TYPE.SERVICE_HIGH_LOAD) );
			if( this.timeRequestRS == 0 ) this.timeRequestRS = System.currentTimeMillis();
		}
	}
	private void handleServiceComponentDeparted( ServiceManagementEvent sme ){
		String id = sme.getKey();
		if( resourceComponents.containsKey(id) )
			resourceComponents.remove(id);
		
		// check if too few remaining
		if( resourceComponents.size() < MINIMUM_ACTIVE_SERVICES ){
			log("\tLoss => too few services! " + resourceComponents.size()+" vs. " +MINIMUM_ACTIVE_SERVICES);
			trigger( new ServiceManagementEvent(sme.getId(),ServiceManagementEvent.TYPE.SERVICE_HIGH_LOAD) );
			if( this.timeRequestRS == 0 ) this.timeRequestRS = System.currentTimeMillis();
		}
	}
	private void handleServiceHighLoad( ServiceManagementEvent sme ){
		updateServiceList( sme );
		
		long currentTime = System.currentTimeMillis();
		
		// give the system a minute to settle before asking for even more services
		if( !isStabilizedSinceLastService(currentTime) ){
			log("\tService not stabilized since last SERVICE_HIGH_LOAD");
			return;
		}
		lastNeedResourceService = currentTime; 
		
		// services complaining of high-load, lets increase the minimum number needed
		MINIMUM_ACTIVE_SERVICES++;
		trigger( new ServiceManagementEvent(sme.getId(),ServiceManagementEvent.TYPE.SERVICE_HIGH_LOAD) );
		if( this.timeRequestRS == 0 ) this.timeRequestRS = System.currentTimeMillis();
	}
	// CUSTOM EVENT HANDLERS end
	
	// HELPERS begin
	private void updateServiceList( ServiceManagementEvent sme ){
		String id = sme.getKey();
		if( !resourceComponents.containsKey(id) )
			resourceComponents.put( id, id );
	}
	
	private void compare( AvailabilityInformation latest ){
		if( this.lastKnownState == null )
			return;
		
		/*log("\tAvailability comparison:");
		log("\tFRC-delta: " + (latest.getFreeResourceComponents()+" vs. "+lastKnownState.getFreeResourceComponents()));
		log("\tBRC-delta: " + (latest.getBusyResourceComponents()+" vs. "+lastKnownState.getBusyResourceComponents()));
		log("\tFMC-delta: " + (latest.getFreeMasterComponents()+  " vs. "+lastKnownState.getFreeMasterComponents()));
		log("\tBMC-delta: " + (latest.getBusyMasterComponents()+  " vs. "+lastKnownState.getBusyMasterComponents()));
		log("\tFWC-delta: " + (latest.getFreeWorkerComponents()+  " vs. "+lastKnownState.getFreeWorkerComponents()));
		log("\tBWC-delta: " + (latest.getBusyWorkerComponents()+  " vs. "+lastKnownState.getBusyWorkerComponents()));*/
		
		/*log("\tAvailability comparison:");
		log("\tFRC-delta: " + (latest.getFreeResourceComponents()-lastKnownState.getFreeResourceComponents()));
		log("\tBRC-delta: " + (latest.getBusyResourceComponents()-lastKnownState.getBusyResourceComponents()));
		log("\tFMC-delta: " + (latest.getFreeMasterComponents()-lastKnownState.getFreeMasterComponents()));
		log("\tBMC-delta: " + (latest.getBusyMasterComponents()-lastKnownState.getBusyMasterComponents()));
		log("\tFWC-delta: " + (latest.getFreeWorkerComponents()-lastKnownState.getFreeWorkerComponents()));
		log("\tBWC-delta: " + (latest.getBusyWorkerComponents()-lastKnownState.getBusyWorkerComponents()));*/
	}
	
	// monitoring helpers
	private void monitorAvailability(){
		
		// let it suffice to have the active one send the monitoring message
		if( !isActiveReplica() ){
			return;
		}
		
		AvailabilityMessage a = new AvailabilityMessage();
		
		a.setSender(this.toString());
		a.setSendername("ServiceAggregator");
		a.setSendertype("SA");
		a.setMinFreeResourceComponents(MINIMUM_ACTIVE_SERVICES);
		a.setMinFreeMasterComponents(MINIMUM_FREE_MASTERS);
		a.setMinFreeWorkerComponents(MINIMUM_FREE_WORKERS);
		
		a.setFreeResourceComponents( this.resourceComponents.size() );
		a.setBusyResourceComponents( this.resourceComponents.size() );
		
		if( lastKnownState != null ){
			a.setFreeMasterComponents( lastKnownState.getFreeMasterComponents() );
			a.setBusyMasterComponents( lastKnownState.getBusyMasterComponents() );
			
			a.setFreeWorkerComponents( lastKnownState.getFreeWorkerComponents() );
			a.setBusyWorkerComponents( lastKnownState.getBusyWorkerComponents() );
		}
		
		monitor.send(a);
	}
	
	// stabilization checkers
	private boolean isStabilizedSinceLastMaster( long currentTime, AvailabilityInformation latest ){
		// if enough time has passed...
		if( (this.lastNeedMaster+STABILIZE_TIME) < currentTime )
			return true;
		
		// if last request has been fulfilled but still asking for more then prompt immediate request
		// TODO: why not just ask for all that remains right away, i.e. in the first request ask for X instead of always asking for at a time?
		if( lastKnownState!=null && 
			latest.getFreeMasterComponents() == (this.lastKnownState.getFreeMasterComponents()+1) )
			return true;
		
		return false;		
	}
	private boolean isStabilizedSinceLastWorker( long currentTime, AvailabilityInformation latest ){
		// if enough time has passed...
		if( (this.lastNeedWorker+STABILIZE_TIME) < currentTime )
			return true;
		
		// if last request has been fulfilled but still asking for more then prompt immediate request
		// TODO: why not just ask for all that remains right away, i.e. in the first request ask for X instead of always asking for at a time?
		if(	this.lastKnownState != null &&
			latest.getFreeWorkerComponents() == (this.lastKnownState.getFreeWorkerComponents()+1) )
			return true;
		
		return false;
	}
	private boolean isStabilizedSinceLastService( long currentTime ){
		return (lastNeedResourceService+STABILIZE_TIME) < currentTime;
	}
	
	// replication aware triggering
	private void trigger( Object event ){
		if( !isActiveReplica() )
			return;
		log( "Triggering: " + event );
		triggerInterface.trigger( event );			
	}
	// HELPERS end

	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// Attributes
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	public Object[] getAttributes() {
		log("ServiceAggregator.getAttributes");
		return new Object[]{
				lastKnownState,				
				new Long(lastNeedResourceService),
				new Long(lastNeedMaster),
				new Long(lastNeedWorker),
				resourceComponents
			};
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// init
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected void doInit(Object[] parameters) {
	}

	protected void doInit(NicheActuatorInterface actuator) {
		this.actuator = actuator;
		this.logger = actuator.testingOnly();
		
		if( myId != null ){
			this.createYacsLogger( "ServiceAggregator", String.valueOf(myId.getReplicaNumber()), true, true, logger );
		}
		else {
			this.createYacsLogger( "ServiceAggregator", null, true, true, logger );
		}
		this.logReinit(); // will log only if re-inited
	}

	protected void doInitId(Object id) {
		log("ServiceAggregator.initId: " + id + ", rep#: " + (id!=null&&(id instanceof NicheId)?((NicheId)id).getReplicaNumber():"NULL"));
		myId = (NicheId) id;
		setActiveReplica( myId.getReplicaNumber() == 0 );
		log("\tSA-Rep.#: " + myId.getReplicaNumber());
		
		if( yacsLog != null ){
			yacsLog.setId( String.valueOf(myId.getReplicaNumber()) );
		}
	}
	
	protected void doReinit(Object[] applicationParameters) {
		log("ServiceAggregator.REinit(Object[]): "  + applicationParameters);
		
		this.setReinited(true);
		
		if( applicationParameters == null || applicationParameters.length != 5 ){
			log("\tArray ABNORMAL!");
			
			this.setAbnormalReinit( "Params null or length!=5" );
			if( yacsLog != null ) this.logReinit();
			
			return;
		}
		if( yacsLog != null ) this.logReinit();
		
		lastKnownState = 			(applicationParameters[0]==null?null:(AvailabilityInformation)applicationParameters[0]);
		lastNeedResourceService = 	(applicationParameters[1]==null?0:(Long)applicationParameters[1]);
		lastNeedMaster = 			(applicationParameters[2]==null?0:(Long)applicationParameters[2]);
		lastNeedWorker = 			(applicationParameters[3]==null?0:(Long)applicationParameters[3]);
		resourceComponents = 		(applicationParameters[4]==null?null:(Hashtable<String,String>)applicationParameters[4]);
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	public String[] listFc() {
		return new String[] { 	FractalInterfaceNames.COMPONENT,
								FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
								FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE };
	}

	public Object lookupFc(String interfaceName) throws NoSuchInterfaceException {
		if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			return mySelf;
		else if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return null;
		else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			return triggerInterface;
		else
			throw new NoSuchInterfaceException(interfaceName);
	}

	public void bindFc(String interfaceName, Object stub) throws NoSuchInterfaceException, IllegalBindingException,	IllegalLifeCycleException {
		log("ServiceAggregator.bindFc: " + interfaceName );
		if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			mySelf = (Component) stub;
		else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			triggerInterface = (TriggerInterface) stub;
		else
			throw new NoSuchInterfaceException(interfaceName);
	}

	public void unbindFc(String interfaceName) throws NoSuchInterfaceException,	IllegalBindingException, IllegalLifeCycleException {
		if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			mySelf = null;
		else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			triggerInterface = null;
		else
			throw new NoSuchInterfaceException(interfaceName);

	}

	public String getFcState() {
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		status = true;
		//this.monitorAvailability();
		log("SA started. Version: " + YACSSettings.YACS_VERSION);
	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;
	}
}
