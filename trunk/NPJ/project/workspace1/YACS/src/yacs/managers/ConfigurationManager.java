package yacs.managers;

import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.jasmine.jade.util.Serialization;
import org.objectweb.jasmine.jade.service.componentdeployment.DeploymentParams;

import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.ResourceRef;
import dks.niche.ids.*;

import yacs.interfaces.YACSSettings;
import yacs.resources.events.*;
import yacs.utils.YacsTimer;

public class ConfigurationManager 	extends yacs.YacsManagementElement
									implements 	EventHandlerInterface, MovableInterface, 
												BindingController, LifeCycleController {

	//Client interfaces
	private NicheActuatorInterface myManagementInterface;
	private Component mySelf;
	private boolean status;
	private NicheId myGlobalId;
		
	// members for replication - begin
	private GroupId resourceServiceGroup, masterGroup, workerGroup;
	private DeploymentParams resourceServiceDepParams, masterDepParams, workerDepParams;
	private long lastResourceService = 0;
	// members for replication - end
	
	// timing vars
	private long tuid = 0;
	
	public ConfigurationManager() {}
	
	// EventHandlerInterface
	public void eventHandler(Object e, int flag){
		log("ConfigurationManager.eventHandler: "+e);
		
		/*long currTime = System.currentTimeMillis();
		if( (lastResourceService+30000) > currTime )
			return;*/
		
		long tstart = System.currentTimeMillis();
		long ttuid = tuid++;
		
		try {
			if( e instanceof ServiceManagementEvent ){
				this.handleServiceManagementEvent( (ServiceManagementEvent)e, tstart, ttuid );
			}
			else {
				log("\tUnhandled event: " + e);
			}
		}
		catch( Exception ex ){
			log("ConfigurationManager.eventHandler: EXCEPTION: " + ex.getMessage() );
			if( isActiveReplica() ) time("CM",""+ttuid,"EX",null,(System.currentTimeMillis()-tstart),null ); // EXception
			ex.printStackTrace();
			//throw ex;
		}
		
	}
	
	// CUSTOM "EVENT" HANDLER begin
	private synchronized void handleServiceManagementEvent( ServiceManagementEvent sme, long tstart, long ttuid ){
		log("ConfigurationManager.handleServiceManagementEvent: " + sme);
			
		if( !isActiveReplica() ){
			log("\tNOT active replica. Quitting!");
			return;
		}
		
		time("CM",""+ttuid,"SHSME",null,(System.currentTimeMillis()-tstart),null ); // Start Handling Service Management Event
		
		if( sme.getEventType() == ServiceManagementEvent.TYPE.SERVICE_HIGH_LOAD )
			deployResourceServiceComponent( sme, ttuid );
		else if( sme.getEventType() == ServiceManagementEvent.TYPE.AVAILABILITY_MASTER_NEEDED )
			deployMasterComponent( sme, ttuid );
		else if( sme.getEventType() == ServiceManagementEvent.TYPE.AVAILABILITY_WORKER_NEEDED )
			deployWorkerComponent( sme, ttuid );
		else {
			log("\tUnhandled SME event: " + sme);
		}
		time("CM",""+ttuid,"FHSME",null,(System.currentTimeMillis()-tstart),null ); // Finish Handling Service Management Event
		
	}
	// CUSTOM "EVENT" HANDLER end
	
	// HELPERS begin
	private void deployResourceServiceComponent( ServiceManagementEvent sme, long ttuid  ){
		long tstart = System.currentTimeMillis();
		log("ConfigurationManager.deploy: ResourceService component...");
		findResourcesAndDeploy( resourceServiceDepParams, resourceServiceGroup, YACSSettings.RESOURCE_REQUIREMENTS_RESOURCE_SERVICE, ttuid );
		timefx("CM",null,ttuid,"DRC",null,(System.currentTimeMillis()-tstart),null ); // Deploy Resource-service Component
	}
	private void deployMasterComponent( ServiceManagementEvent sme, long ttuid  ){
		long tstart = System.currentTimeMillis();
		log("ConfigurationManager.deploy: Master component...");
		findResourcesAndDeploy( masterDepParams, masterGroup, YACSSettings.RESOURCE_REQUIREMENTS_MASTER, ttuid );
		timefx("CM",null,ttuid,"DMC",null,(System.currentTimeMillis()-tstart),null ); // Deploy Master Component
	}
	private void deployWorkerComponent( ServiceManagementEvent sme, long ttuid  ){
		long tstart = System.currentTimeMillis();
		log("ConfigurationManager.deploy: Worker component...");
		findResourcesAndDeploy( workerDepParams, workerGroup, YACSSettings.RESOURCE_REQUIREMENTS_WORKER, ttuid );
		timefx("CM",null,ttuid,"DWC",null,(System.currentTimeMillis()-tstart),null ); // Deploy Worker Component
	}
	private void findResourcesAndDeploy( DeploymentParams depParamsObj, GroupId addGroup, Long resReq, long ttuid  ){
		YacsTimer timer = new YacsTimer( ttuid );
		
		NodeRef newResource = myManagementInterface.oneShotDiscoverResource("dynamic:" + String.valueOf(resReq) );
		timefx("CM",null,ttuid,"NRD",null,timer.elapsed(),null ); // New Resource Discovered
		if( newResource == null ){
			log("\tNewResource is null. Quitting!");
			return;
		}
		
		timer.reset();
		ArrayList at = myManagementInterface.allocate(newResource, null); //specification(null, newRid));
		timefx("CM",null,ttuid,"RA",null,timer.elapsed(),null ); // Resource Allocated
		log("\tArrayList: " + at + ", size: " + at.size());
		ResourceRef allocatedResource = (ResourceRef)at.get(0);
		
		String depParams = null;
		try {
			depParams = Serialization.serialize(depParamsObj);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		log("\tDep.params.len(): " + depParams.length());
		
		timer.reset();
		at = myManagementInterface.deploy(allocatedResource, depParams);
		timefx("CM",null,ttuid,"RD",null,timer.elapsed(),null ); // Resource Deployed
		log("\tDeployed: " + at);
		ComponentId cid = (ComponentId)((Object[])at.get(0))[1]; //0 is "result", 1 is cid
		log("\tCid: " + cid.getId());
		
		timer.reset();
		myManagementInterface.update(addGroup, cid, NicheComponentSupportInterface.ADD_TO_GROUP_AND_START);
		timefx("CM",null,ttuid,"CAGS",null,timer.elapsed(),null ); // Component Added to Group and Started		
	}
	
	// HELPERS end
	
	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// init
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	protected void doInit(Object [] parameters) {
		//new Object[] { 	resourceServiceGroup, masterGroup, workerGroup, 
		// 					rsDepParams, mDepParams, wDepParams }
		resourceServiceGroup = 		parameters[0] == null ? null : (GroupId)parameters[0]; 
		masterGroup = 				parameters[1] == null ? null : (GroupId)parameters[1];
		workerGroup = 				parameters[2] == null ? null : (GroupId)parameters[2];
		resourceServiceDepParams = 	parameters[3] == null ? null : (DeploymentParams)parameters[3];
		masterDepParams = 			parameters[4] == null ? null : (DeploymentParams)parameters[4];
		workerDepParams = 			parameters[5] == null ? null : (DeploymentParams)parameters[5];
	}
	
	protected void doInit(NicheActuatorInterface managementInterface) {
		myManagementInterface = managementInterface;
		
		if( myGlobalId != null ){
			this.createYacsLogger( "ConfigurationManager", String.valueOf(myGlobalId.getReplicaNumber()), true, true, myManagementInterface.testingOnly() );
		}
		else {
			this.createYacsLogger( "ConfigurationManager", null, true, true, myManagementInterface.testingOnly() );
		}
		this.logReinit(); // will log only if re-inited
	}
	
	protected void doInitId(Object id) {
		log("ConfigurationManager.initId: "+id + ", rep#: " + (id!=null&&(id instanceof NicheId)?((NicheId)id).getReplicaNumber():"NULL"));
		myGlobalId = (NicheId)id;
		setActiveReplica( myGlobalId.getReplicaNumber() == 0 );
		log("\tCM-Rep.#: " + myGlobalId.getReplicaNumber());
		
		if( yacsLog != null ){
			yacsLog.setId( String.valueOf(myGlobalId.getReplicaNumber()) );
		}
	}
	protected void doReinit(Object[] applicationParameters) {
		log("ConfigurationManager.REinit(Object[]): "  + applicationParameters);
		this.setReinited(true);
				
		if( applicationParameters == null || applicationParameters.length < 7 ){
			log("\tArray ABNORMAL!");
			
			this.setAbnormalReinit( "Params null or length<7" );
			if( yacsLog != null ) this.logReinit();
			
			return;
		}
		if( yacsLog != null ) this.logReinit();
		
		resourceServiceGroup = 		(applicationParameters[0]==null?null:(GroupId)applicationParameters[0]);
		masterGroup = 				(applicationParameters[1]==null?null:(GroupId)applicationParameters[1]);
		workerGroup = 				(applicationParameters[2]==null?null:(GroupId)applicationParameters[2]);
		resourceServiceDepParams = 	(applicationParameters[3]==null?null:(DeploymentParams)applicationParameters[3]);
		masterDepParams = 			(applicationParameters[4]==null?null:(DeploymentParams)applicationParameters[4]);
		workerDepParams = 			(applicationParameters[5]==null?null:(DeploymentParams)applicationParameters[5]);
		lastResourceService = 		(applicationParameters[6]==null?0:(Long)applicationParameters[6]);
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// Attributes
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	public Object[] getAttributes() {
		return new Object[]{
				resourceServiceGroup, masterGroup, workerGroup,
				resourceServiceDepParams, masterDepParams, workerDepParams,
				new Long(lastResourceService)
			};
	}

	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	public String[] listFc() {
		return new String[] {	FractalInterfaceNames.COMPONENT, 
								FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE, 
								FractalInterfaceNames.EVENT_HANDLER_CLIENT_INTERFACE };
	}


	public Object lookupFc(String interfaceName) throws NoSuchInterfaceException {
		if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return myManagementInterface;
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			return mySelf;
		else
			throw new NoSuchInterfaceException(interfaceName);
	}
	
	public void bindFc(String interfaceName, Object stub) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		log("ConfigurationManager.bindFc: " + interfaceName );
		if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			myManagementInterface = (NicheActuatorInterface) stub;
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			mySelf = (Component) stub;
		else
			throw new NoSuchInterfaceException(interfaceName);
	}

	public void unbindFc(String interfaceName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			myManagementInterface = null;
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			mySelf = null;
		else
			throw new NoSuchInterfaceException(interfaceName);	
	}

	public String getFcState() {
		return status ? "STARTED": "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		status = true;
		log("CM started. Version: " + YACSSettings.YACS_VERSION);
	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;
	}	
}
