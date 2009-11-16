package yacs.managers;

import java.util.ArrayList;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.type.ComponentType;

import dks.niche.events.CreateGroupEvent;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.ids.GroupId;
import dks.niche.ids.BindId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.wrappers.ManagementDeployParameters;

import yacs.interfaces.YACSNames;
import yacs.interfaces.YACSSettings;
import yacs.job.interfaces.InformationInterface;

// testing
import yacs.resources.data.ResourceRequest;
import yacs.resources.interfaces.ResourceServiceRequestInterface;
import yacs.utils.YacsUtils;
import dks.niche.ids.ComponentId;


public class CreateJobGroupManager 	extends yacs.YacsManagementElement
									implements 	EventHandlerInterface, MovableInterface, 
												BindingController, LifeCycleController {

	private NicheActuatorInterface myManagementInterface;

	private NicheAsynchronousInterface myPrivateManagementInterface;

	// ///////////////////
	Component mySelf;
	ComponentType workerWatcherComponentType = null;
	ComponentType masterWatcherComponentType = null;
	
	private InformationInterface informationInterface;
	
	private boolean status;
	private NicheId myGlobalId;
	
	// members for replication - begin
	private GroupId globalMasterGroup;
	private GroupId globalWorkerGroup;
	private GroupId resourceServiceGroup;
	
	private NicheId serviceAggregator, masterAggregator, workerAggregator;
	// members for replication - end
	
	// for timing
	private long tuid;
		
	// for testing
	private int eventsReceived = 0;
	private ResourceServiceRequestInterface resourceServiceRequestInterface;
	
	// SETTINGS
	static final boolean staticADL = System.getProperty("yass.test.staticADL") instanceof String ?
			(0 < Integer.parseInt(System.getProperty("yass.test.staticADL")) ? true : false)
			: false;
	
	// REMEMBER THE EMPTY CONSTRUCTOR
	public CreateJobGroupManager() {
		log("CreateJobGroupManager created");
		status = false;
	}

	public synchronized void eventHandler(Object e, int flag) {
		long tstart = System.currentTimeMillis();
		tuid++;
		
		log("CreateJobGroupManager.eventHandler: " + e);
		eventsReceived++;
		
		//log("\tEvent.#: "+eventsReceived);
		
		CreateGroupEvent cge = (CreateGroupEvent) e;

		GroupId newGroup = cge.getGroupId();
		log("\tGCE for: " + newGroup.getId());
		
		if( !isActiveReplica() ){
			// TODO: or does DCMS take care of "not" doing what I'm actually doing below, i.e. creating MEs?
			// I am binding so I need to take care of this!
			log("\tNOT active replica. Quitting!");
			return;
		}
	
		// find out type of group
		BindId b = myManagementInterface.bind( 	this.myGlobalId, 	YACSNames.INFORMATION_CLIENT_INTERFACE,
												newGroup, 			YACSNames.INFORMATION_SERVER_INTERFACE,
												JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE );
		time("CJGM",""+tuid,"CJGMIBC",null,(System.currentTimeMillis()-tstart),null); // CJGM Information Binding Called
		
		while( informationInterface == null ){
			YacsUtils.ignorantSleep(10);
		}
		time("CJGM",""+tuid,"CJGMIBD",null,(System.currentTimeMillis()-tstart),null); // CJGM Information Binding Done
		
		String type = informationInterface.componentType();
		time("CJGM",""+tuid,"TFC",null,(System.currentTimeMillis()-tstart),null); // Type Function Called
		log("\tType is: " + type);
		
		// don't need the binding anymore, get rid of it
		myManagementInterface.unbind(b);
		this.informationInterface = null;
		
		// spawn appropriate management elements
		if( type.equals(YACSNames.MASTER_COMPONENT) ){
			ManagementDeployParameters params = new ManagementDeployParameters();
			
			params.describeWatcher(
					YACSNames.MASTER_WATCHER_CLASS_NAME,
					YACSNames.MASTER_WATCHER_ADL_NAME,
					masterWatcherComponentType,
					new Object[]{	newGroup, globalMasterGroup, globalWorkerGroup, 
									null, masterAggregator, resourceServiceGroup },
					newGroup.getId()
				);
			params.setReliable( YACSSettings.SELF_MANAGEMENT_REPLICATION > 1 && !YACSSettings.BLOCK_RELIABLE_MASTER_WATCHER );
			log("\tDeploying MasterWatcher...");
			NicheId masterWatcher = myManagementInterface.deploy(params, (YACSSettings.COLOCATE_MASTER_WATCHER?newGroup:null) );
			time("CJGM",""+tuid,"MWD",null,(System.currentTimeMillis()-tstart),masterWatcher.toString()); // Master Watcher Deployed
			log("\tDone deploying MasterWatcher: " + masterWatcher);
		}
		else {
			ManagementDeployParameters params = new ManagementDeployParameters();
			
			params.describeWatcher(
					YACSNames.WORKER_WATCHER_CLASS_NAME,
					YACSNames.WORKER_WATCHER_ADL_NAME,
					workerWatcherComponentType,
					new Object[]{	newGroup, globalMasterGroup, globalWorkerGroup, 
									null, workerAggregator, resourceServiceGroup },
					newGroup.getId()
				);
			params.setReliable( YACSSettings.SELF_MANAGEMENT_REPLICATION > 1 && !YACSSettings.BLOCK_RELIABLE_WORKER_WATCHER );
			log("\tDeploying WorkerWatcher...");
			NicheId workerWatcher = myManagementInterface.deploy(params, (YACSSettings.COLOCATE_WORKER_WATCHER?newGroup:null) );
			time("CJGM",""+tuid,"WWD",null,(System.currentTimeMillis()-tstart),workerWatcher.toString()); // Worker Watcher Deployed
			log("\tDone deploying WorkerWatcher: " + workerWatcher);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.ManagementElementInterface#init(java.lang.Object[])
	 */
	protected void doInit(Object[] applicationParameters) {
		log("CreateJobGroupManager.init(Object[]): "  + applicationParameters);
		
		/*if( applicationParameters != null ){
			log("\tParams: " + applicationParameters.length );
			int i = 0;
			for( Object obj : applicationParameters ){
				log("\t\t"+i+": " + (obj==null?"NULL":obj.getClass()));
				i++;
			}
		}*/
		
		if( applicationParameters == null || applicationParameters.length < 6 ){
			log("\tWARNING! Init param[] NOT normal.");
			return;
		}
		
		this.globalMasterGroup = 	applicationParameters[0] == null ? null : (GroupId)applicationParameters[0];
		this.globalWorkerGroup = 	applicationParameters[1] == null ? null : (GroupId)applicationParameters[1];
		this.resourceServiceGroup = applicationParameters[2] == null ? null : (GroupId)applicationParameters[2];
		serviceAggregator  = 		applicationParameters[3] == null ? null : (NicheId)applicationParameters[3];
		masterAggregator = 			applicationParameters[4] == null ? null : (NicheId)applicationParameters[4];
		workerAggregator = 			applicationParameters[5] == null ? null : (NicheId)applicationParameters[5];
		
		if( applicationParameters.length > 8 ){
			int priorCount = eventsReceived;
			this.eventsReceived = applicationParameters[6] == null ? 0 : (Integer)applicationParameters[6];
			log( "\tEvent.count changed from: " + priorCount + " to " + eventsReceived );
			//log( "\tID: " + applicationParameters[7] );
			//log( "\tCt: " + applicationParameters[8] );
		}
		else
			log("\tEvent counter not in array. Now: " + eventsReceived);
		
		log("CreateJobGroupManager initialized");
	}
	protected void doReinit(Object[] applicationParameters) {
		log("CreateJobGroupManager.REinit(Object[]): "  + applicationParameters);
		this.setReinited(true);
		
		// TODO: make this reinit like others, including abnormality check!
		
		// If init has been called then the logger is proper and we can log this important event.
		// If not then isReInited will be checked in init() and reinit will be logged there 
		if( yacsLog != null ){
			this.logReinit();
		}
		
		this.init(applicationParameters);
	}

	protected void doInit(NicheActuatorInterface actuator) {
		log("CreateJobGroupManager.init(Actuator): "  + actuator);
		
		myManagementInterface = actuator;
		myPrivateManagementInterface = myManagementInterface.testingOnly();
		
		if( myGlobalId != null ){
			this.createYacsLogger( "CreateJobGroupManager", String.valueOf(myGlobalId.getReplicaNumber()), true, true, myPrivateManagementInterface );
		}
		else {
			this.createYacsLogger( "CreateJobGroupManager", null, true, true, myPrivateManagementInterface );
		}
		this.logReinit(); // will log only if re-inited
		
		if(!staticADL) {
			workerWatcherComponentType = actuator.getComponentType(YACSNames.WORKER_WATCHER_CLASS_NAME);
			masterWatcherComponentType = actuator.getComponentType(YACSNames.MASTER_WATCHER_CLASS_NAME);
		}
	}

	protected void doInitId(Object id) {
		// Not used
		log("CreateJobGroupManager.initId(id): "  + id + ", rep#: " + (id!=null&&(id instanceof NicheId)?((NicheId)id).getReplicaNumber():"NULL"));
		this.myGlobalId = (NicheId)id;
		setActiveReplica( myGlobalId.getReplicaNumber() == 0 );
		log("\tCJGM-Rep.#: " + myGlobalId.getReplicaNumber());
		
		if( yacsLog != null ){
			yacsLog.setId( String.valueOf(myGlobalId.getReplicaNumber()) );
		}
		
		// TODO: REMOVE, only for testing!
		/*BindId bid  = myManagementInterface.bind(	this.myGlobalId, 		YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE,
													resourceServiceGroup, 	YACSNames.RESOURCE_SERVICE_REQUEST_SERVER_INTERFACE,
													JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE );
		log("CJGM-RS request.binding: " + bid.getId() );*/
	}

	public Object[] getAttributes() {
		log("CreateJobGroupManager.getAttributes()");
		return new Object[]{	globalMasterGroup, globalWorkerGroup, resourceServiceGroup,
								serviceAggregator, masterAggregator, workerAggregator,
								new Integer(eventsReceived),
								this.myGlobalId.getId().toString(),
								this.toString() };
	}
	// FRACTAL STUFF

	public String[] listFc() {
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.EVENT_HANDLER_CLIENT_INTERFACE,
				YACSNames.INFORMATION_CLIENT_INTERFACE,
				// TODO: interface only for testing
				YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE };
	}

	public Object lookupFc(String interfaceName)
			throws NoSuchInterfaceException {
		log("CreateJobGroupManager.lookupFc: "+interfaceName);
		if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return myManagementInterface;
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			return mySelf;
		else if (interfaceName.equals(YACSNames.INFORMATION_CLIENT_INTERFACE))
			return informationInterface;
		else if (interfaceName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE)) // TODO: interface only for testing
			return this.resourceServiceRequestInterface;
		else
			throw new NoSuchInterfaceException(interfaceName);

	}

	public void bindFc(String interfaceName, Object stub)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		log("CreateJobGroupManager.bindFc: " + interfaceName );
		
		if (interfaceName.equals(YACSNames.INFORMATION_CLIENT_INTERFACE))
			informationInterface = (InformationInterface)stub;
		else if (interfaceName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE)) // TODO: interface only for testing
			resourceServiceRequestInterface = (ResourceServiceRequestInterface)stub;
		else if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			log("Managers says ERROR, use initinterface instead");
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
			mySelf = (Component) stub;
			// log("setting the component interface");
		} else
			throw new NoSuchInterfaceException(interfaceName);

	}

	public void unbindFc(String interfaceName) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		log("CreateJobGroupManager.unbindFc: " + interfaceName);
		
		if (interfaceName.equals(YACSNames.INFORMATION_CLIENT_INTERFACE))
			this.informationInterface = null;
		else if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			myManagementInterface = null;
		else if (interfaceName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE)) // TODO: interface only for testing
			resourceServiceRequestInterface = null;
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
			mySelf = null;
		} else
			throw new NoSuchInterfaceException(interfaceName);

	}

	public String getFcState() {
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		status = true;
		log("CJGM started. Version: " + YACSSettings.YACS_VERSION);
	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;

	}

}
