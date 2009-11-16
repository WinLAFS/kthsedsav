package yacs.managers;

import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.adl.FactoryFactory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.componentdeployment.DeploymentParams;
import org.objectweb.jasmine.jade.service.componentdeployment.NicheIdRegistry;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;
import org.objectweb.jasmine.jade.util.Serialization;

import dks.niche.events.*;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.DCMRegistryInterface;
import dks.niche.fractal.interfaces.DCMServiceInterface;
import dks.niche.ids.BindId;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.ids.ManagementElementId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.wrappers.ManagementDeployParameters;
import dks.niche.wrappers.ScriptInfo;
import dks.niche.wrappers.SimpleResourceManager;

import yacs.interfaces.YACSNames;
import yacs.interfaces.YACSSettings;
import yacs.interfaces.YACSTemplates;
import yacs.job.events.*;
import yacs.resources.events.*;

public class StartManager 	extends yacs.YacsComponent
							implements BindingController, LifeCycleController {

	// Client Interfaces
	private OverlayAccess dcmService;

	// private NicheAsynchronousInterface logger;

	// private DCMRegistryInterface DCMRegistry;
	private NicheIdRegistry idRegistry;

	// ///////////////
	Component mySelf;

	private boolean status;

	// Local variables
	// PROGRAM STANDARD SETTINGS, if none can be loaded
	static final boolean staticADL = System.getProperty("yass.test.staticADL") instanceof String ?
					(0 < Integer.parseInt(System.getProperty("yass.test.staticADL")) ? true : false) 
					: false;

	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// startScript
	// ///////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	NicheActuatorInterface myActuatorInterface;
	NicheAsynchronousInterface myAsynchronousInterface;
	NicheManagementInterface myNiche;
	SimpleResourceManager myRM;

	public StartManager() {
	}

	public StartManager(NicheManagementInterface nicheInstance, ScriptInfo si){
	}
	
	private void startScript() {

		log("$$$$$$$$$$$$$$$$$$$$$$$$$ Starting YACS StartManager $$$$$$$$$$$$$$$$$$$$$$$$$$$");
		log("CODE3_YACS - Version sanity check: " + YACSSettings.YACS_VERSION);
		log("Niche-replication.degree: " + System.getProperty("niche.replicationDegree"));
		log("YACS-NFS-base: '" + System.getProperty("yacs.nfs.base") + "'");
		
		this.myActuatorInterface = dcmService.getOverlay().getJadeSupport();
		this.myAsynchronousInterface = dcmService.getOverlay().getNicheAsynchronousSupport(); // nicheInstance.getNicheAsynchronousSupport();
		this.createYacsLogger( "StartManager", null, true, true, myAsynchronousInterface );
		this.myRM = myAsynchronousInterface.getResourceManager();
		log("SRM.getReplicationFactor: " + this.myRM.getReplicationFactor() );
		
		ArrayList<ComponentId> masterComponents = new ArrayList<ComponentId>();
		ArrayList<ComponentId> workerComponents = new ArrayList<ComponentId>();
		ArrayList<ComponentId> frontEnds = new ArrayList<ComponentId>();
		ArrayList<ComponentId> resourceServiceComponents = new ArrayList<ComponentId>();
		
		GroupId frontendGroup=null, resourceServiceGroup=null, masterGroup=null, workerGroup=null, storageGroup=null;
		
		ComponentId s;
		int id=1;
		
		// frontends
		do {
			log("Lookup of: " + YACSNames.APPLICATION_PREFIX + YACSNames.FRONTEND_COMPONENT + id );
			s = (ComponentId) idRegistry.lookup( YACSNames.APPLICATION_PREFIX + YACSNames.FRONTEND_COMPONENT + (id++) );
			if( s != null )	frontEnds.add(s);
			log("Found: " + s + ": " + (s!=null?s.getComponentName():"null"));
		} while( s != null );
		
		log("About to create frontend component group of size: " + frontEnds.size());
		frontendGroup = myActuatorInterface.createGroup(	YACSTemplates.frontendGroup(myActuatorInterface),
															frontEnds );
		log("\tCreated frontend component group: " + frontendGroup.getId());
		
		// ResourceService
		id=1;
		do {
			log("Lookup of: " + YACSNames.APPLICATION_PREFIX + YACSNames.RESOURCE_SERVICE_COMPONENT + id );
			s = (ComponentId) idRegistry.lookup( YACSNames.APPLICATION_PREFIX + YACSNames.RESOURCE_SERVICE_COMPONENT + (id++) );
			if( s != null )	resourceServiceComponents.add(s);
			log("Found: " + s + ": " + (s!=null?s.getComponentName():"null"));
		} while( s != null );
	
		log("About to create global RS component group of size: " + resourceServiceComponents.size());
		resourceServiceGroup = myActuatorInterface.createGroup(	YACSTemplates.resourceServiceGroup(myActuatorInterface),
																resourceServiceComponents );
		log("\tCreated RS component group: " + resourceServiceGroup.getId());
		
		// masters
		id = 1;
		do{
			log("Lookup of: " + YACSNames.APPLICATION_PREFIX + YACSNames.MASTER_COMPONENT + id );
			s = (ComponentId) idRegistry.lookup( YACSNames.APPLICATION_PREFIX + YACSNames.MASTER_COMPONENT + (id++) );
			if( s != null ) masterComponents.add(s);
			log("Found: " + s + ": + " + (s!=null?s.getComponentName():"null"));
		}
		while( s != null );
		
		log("About to create global master component group of size: " + masterComponents.size());
		masterGroup = myActuatorInterface.createGroup(	YACSTemplates.masterGroup(myActuatorInterface),
														masterComponents);
		log("\tCreated master component group: " + masterGroup.getId());
		
		// Workers
		id = 1;
		do {
			log("Lookup of: " + YACSNames.APPLICATION_PREFIX + YACSNames.WORKER_COMPONENT + id );
			s = (ComponentId) idRegistry.lookup( YACSNames.APPLICATION_PREFIX + YACSNames.WORKER_COMPONENT + (id++) );
			if( s != null )	workerComponents.add(s);
			log("Found: " + s + ": " + (s!=null?s.getComponentName():"null"));
		} while( s != null );
	
		log("About to create global worker component group of size: " + workerComponents.size());
		workerGroup = myActuatorInterface.createGroup(	YACSTemplates.workerGroup(myActuatorInterface),
														workerComponents );
		log("\tCreated worker component group: " + workerGroup.getId());

		
		// functional bindings
				
		// functional resource service bindings
		{
			// resource state reporting
			{
				BindId bid  = myActuatorInterface.bind(	masterGroup, 			YACSNames.RESOURCE_SERVICE_STATE_MASTER_CLIENT_INTERFACE,
													 	resourceServiceGroup, 	YACSNames.RESOURCE_SERVICE_STATE_MASTER_SERVER_INTERFACE,
													 	JadeBindInterface.ONE_TO_ANY );
				log("Masters-RS state.binding: " + bid.getId());
				
				bid 		= myActuatorInterface.bind(	workerGroup, 			YACSNames.RESOURCE_SERVICE_STATE_CLIENT_INTERFACE,
					 									resourceServiceGroup, 	YACSNames.RESOURCE_SERVICE_STATE_SERVER_INTERFACE,
					 									JadeBindInterface.ONE_TO_ANY );
				log("Workers-RS state.binding: " + bid.getId());
			}
			// resource request
			{
				BindId bid  = myActuatorInterface.bind(	masterGroup, 			YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE,
					 									resourceServiceGroup, 	YACSNames.RESOURCE_SERVICE_REQUEST_SERVER_INTERFACE,
					 									JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE );
				log("Masters-RS request.binding: " + bid.getId());
			}
			{
				BindId bid  = myActuatorInterface.bind(	frontendGroup, 			YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE,
					 									resourceServiceGroup, 	YACSNames.RESOURCE_SERVICE_REQUEST_SERVER_INTERFACE,
					 									JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE );
				log("Frontend-RS request.binding: " + bid.getId());
			}
			// intra RS group cooperative communication 
			{
				BindId bid  = myActuatorInterface.bind(	resourceServiceGroup,	YACSNames.RESOURCE_MANAGEMENT_CLIENT_INTERFACE,
					 									resourceServiceGroup, 	YACSNames.RESOURCE_MANAGEMENT_SERVER_INTERFACE,
					 									JadeBindInterface.ONE_TO_MANY | JadeBindInterface.NO_SEND_TO_SENDER );
				log("RS management binding: " + bid.getId());
			}
		}
		
		
		/** END OF BIND SECTION **** */
		// global MEs
		ManagementDeployParameters params;
		ComponentType currentComponentType;
		
		NicheId serviceWatcher=null, serviceAggregator=null, workerAggregator=null, masterAggregator=null, configurationManager=null;
		
		// resource service watcher 
		if( YACSSettings.SYSTEM_SELF_MANAGEMENT || YACSSettings.RESOURCE_SERVICE_SELF_MANAGEMENT ){
			if( !YACSSettings.RESOURCE_SERVICE_SELF_MANAGEMENT ){
				log("INFO! RS self-mgmt ACTIVATED! Due to system self-mgmt activation.");
			}
			
			currentComponentType = staticADL ? null : myActuatorInterface.getComponentType(YACSNames.SERVICE_WATCHER_CLASS_NAME);
			params = new ManagementDeployParameters();
			params.describeWatcher(
					YACSNames.SERVICE_WATCHER_CLASS_NAME,
					YACSNames.SERVICE_WATCHER_ADL_NAME,
					currentComponentType,
					new Object[] {resourceServiceGroup, new Boolean(YACSSettings.SYSTEM_SELF_MANAGEMENT)},
					resourceServiceGroup.getId()
				);
			params.setReliable( YACSSettings.SELF_MANAGEMENT_REPLICATION>1 && !YACSSettings.BLOCK_RELIABLE_SERVICE_WATCHER );
			serviceWatcher = myActuatorInterface.deploy(params, (YACSSettings.COLOCATE_SERVICE_WATCHER?resourceServiceGroup:null));
			log("ServiceWatcher deployed: " + serviceWatcher.getId());
			
			myActuatorInterface.subscribe( resourceServiceGroup, serviceWatcher, ComponentFailEvent.class.getName() );
			// TODO: myActuatorInterface.subscribe( resourceServiceGroup, serviceWatcher, ResourceLeaveEvent.class.getName() );
			myActuatorInterface.subscribe( resourceServiceGroup, serviceWatcher, MemberAddedEvent.class.getName() );
		}
		
		// ServiceAggregator
		if( YACSSettings.SYSTEM_SELF_MANAGEMENT ){
			currentComponentType = staticADL ? null : myActuatorInterface.getComponentType(YACSNames.SERVICE_AGGREGATOR_CLASS_NAME);
			params = new ManagementDeployParameters();
			params.describeAggregator(
					YACSNames.SERVICE_AGGREGATOR_CLASS_NAME,
					YACSNames.SERVICE_AGGREGATOR_ADL_NAME,
					currentComponentType,
					new Object[] {serviceWatcher}
				);
			params.setReliable( YACSSettings.SELF_MANAGEMENT_REPLICATION>1 && !YACSSettings.BLOCK_RELIABLE_SERVICE_AGGREGATOR );
			serviceAggregator = myActuatorInterface.deploy(params, (YACSSettings.COLOCATE_SERVICE_AGGREGATOR?resourceServiceGroup:null));
			log("ServiceAggregator deployed: " + serviceAggregator.getId());
			
			if( serviceWatcher  != null )
				myActuatorInterface.subscribe( serviceWatcher, serviceAggregator, ServiceManagementEvent.class.getName() );
		}
		
		// MasterAggregator
		if( YACSSettings.SYSTEM_SELF_MANAGEMENT && masterGroup!=null ){
			currentComponentType = staticADL ? null : myActuatorInterface.getComponentType(YACSNames.MASTER_AGGREGATOR_CLASS_NAME);
			params = new ManagementDeployParameters();
			params.describeAggregator(
					YACSNames.MASTER_AGGREGATOR_CLASS_NAME,
					YACSNames.MASTER_AGGREGATOR_ADL_NAME,
					currentComponentType,
					new Object[] {}
				);
			params.setReliable( YACSSettings.SELF_MANAGEMENT_REPLICATION>1 && !YACSSettings.BLOCK_RELIABLE_MASTER_AGGREGATOR );
			masterAggregator = myActuatorInterface.deploy(params, (YACSSettings.COLOCATE_MASTER_AGGREGATOR?masterGroup:null) );
			log("MasterAggregator deployed: " + masterAggregator.getId());
		}
		
		// WorkerAggregator
		if( YACSSettings.SYSTEM_SELF_MANAGEMENT && workerGroup!=null ){
			currentComponentType = staticADL ? null : myActuatorInterface.getComponentType(YACSNames.WORKER_AGGREGATOR_CLASS_NAME);
			params = new ManagementDeployParameters();
			params.describeAggregator(
					YACSNames.WORKER_AGGREGATOR_CLASS_NAME,
					YACSNames.WORKER_AGGREGATOR_ADL_NAME,
					currentComponentType,
					new Object[] {}
				);
			params.setReliable( YACSSettings.SELF_MANAGEMENT_REPLICATION>1 && !YACSSettings.BLOCK_RELIABLE_WORKER_AGGREGATOR );
			workerAggregator = myActuatorInterface.deploy(params, (YACSSettings.COLOCATE_WORKER_AGGREGATOR?workerGroup:null));
			log("WorkerAggregator deployed: " + workerAggregator.getId());
		}
		
		// Deploy CreateGroupManager
		// TODO: any way to get rid of the CreateGroupManager, at least not have it store state?
		{
			currentComponentType = staticADL ? null : myActuatorInterface.getComponentType(YACSNames.CREATE_JOB_GROUP_MANAGER_CLASS_NAME);
			params = new ManagementDeployParameters();
			params.describeManager(
					YACSNames.CREATE_JOB_GROUP_MANAGER_CLASS_NAME,
					YACSNames.CREATE_JOB_GROUP_MANAGER_ADL_NAME,
					currentComponentType,
					new Object[] {	masterGroup, workerGroup, resourceServiceGroup,
									serviceAggregator==null? null:serviceAggregator.getId(),
									masterAggregator==null ? null:masterAggregator.getId(),
									workerAggregator==null ? null:workerAggregator.getId() }
				);
			params.setReliable( YACSSettings.SELF_MANAGEMENT_REPLICATION>1 && !YACSSettings.BLOCK_RELIABLE_CREATE_JOB_GROUP_MANAGER );
			NicheId createFileGroupManager = myActuatorInterface.deploy(params, (YACSSettings.COLOCATE_CREATE_JOB_GROUP_MANAGER?masterGroup:null));
			if( masterGroup != null )
				myActuatorInterface.subscribe(masterGroup, createFileGroupManager, CreateGroupEvent.class.getName());
		}
		
		// ConfigurationManager
		if( YACSSettings.SYSTEM_SELF_MANAGEMENT ){
			DeploymentParams rsDepParams=null, mDepParams=null, wDepParams=null;
			try {
				if( resourceServiceComponents.size() > 0 )
					rsDepParams = (DeploymentParams) Serialization.deserialize(resourceServiceComponents.get(0).getSerializedDeployParameters());
				if( masterComponents.size() > 0 )
					mDepParams = (DeploymentParams) Serialization.deserialize(masterComponents.get(0).getSerializedDeployParameters());
				if( workerComponents.size() > 0 )
					wDepParams = (DeploymentParams) Serialization.deserialize(workerComponents.get(0).getSerializedDeployParameters());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			currentComponentType = staticADL ? null : myActuatorInterface.getComponentType(YACSNames.CONFIGURATION_MANAGER_CLASS_NAME);
			params = new ManagementDeployParameters();
			params.describeManager(
					YACSNames.CONFIGURATION_MANAGER_CLASS_NAME,
					YACSNames.CONFIGURATION_MANAGER_ADL_NAME,
					currentComponentType,
					new Object[] { resourceServiceGroup, masterGroup, workerGroup, rsDepParams, mDepParams, wDepParams }
				);
			params.setReliable( YACSSettings.SELF_MANAGEMENT_REPLICATION>1 && !YACSSettings.BLOCK_RELIABLE_CONFIGURATION_MANAGER );
			configurationManager = myActuatorInterface.deploy(params, (YACSSettings.COLOCATE_CONFIGURATION_MANAGER?workerGroup:null));
			log("ConfigurationManager deployed: " + configurationManager.getId());
			
			if( serviceAggregator != null )
				myActuatorInterface.subscribe( serviceAggregator, 	configurationManager, ServiceManagementEvent.class.getName() );
			if( workerAggregator != null )
				myActuatorInterface.subscribe( workerAggregator, 	configurationManager, WorkerManagementEvent.class.getName() );
			if( masterAggregator != null )
				myActuatorInterface.subscribe( masterAggregator, 	configurationManager, MasterManagementEvent.class.getName() );	
		}
		
		if( YACSSettings.SYSTEM_SELF_MANAGEMENT )
			log("Startup done WITH system self-management!");
		else
			log("Startup done WITHOUT system self-management!");
	}

	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public String[] listFc() {

		// Client interfaces list
		return new String[] { 	FractalInterfaceNames.COMPONENT,
								FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
								FractalInterfaceNames.ID_REGISTRY };

	}

	public Object lookupFc(final String itfName) throws NoSuchInterfaceException {

		if (itfName.equals(FractalInterfaceNames.OVERLAY_ACCESS))
			return dcmService;
		else if (itfName.equals(FractalInterfaceNames.ID_REGISTRY))
			return idRegistry;
		else if (itfName.equals("component"))
			return mySelf;
		else
			throw new NoSuchInterfaceException(itfName);

	}

	public void bindFc(final String itfName, final Object itfValue) throws NoSuchInterfaceException {
		if (itfName.equals(FractalInterfaceNames.OVERLAY_ACCESS))
			dcmService = (OverlayAccess) itfValue;
		else if (itfName.equals(FractalInterfaceNames.ID_REGISTRY))
			idRegistry = (NicheIdRegistry) itfValue;
		else if (itfName.equals("component"))
			mySelf = (Component) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals(FractalInterfaceNames.OVERLAY_ACCESS))
			dcmService = null;
		else if (itfName.equals(FractalInterfaceNames.ID_REGISTRY))
			idRegistry = null;
		else if (itfName.equals("component"))
			mySelf = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		status = true;

		log("StartManager.startFc() - Starting script!");
		startScript();
	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;
	}
}
