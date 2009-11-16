package yacs.resources;

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
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.TriggerInterface;

import java.util.ArrayList;

import yacs.interfaces.*;
import yacs.resources.data.*;
import yacs.resources.interfaces.*;
import yacs.utils.YacsUtils;

public class ResourceService	extends yacs.YacsComponent 
								implements
										ResourceServiceRequestInterface, ResourceServiceStateInterface, 
										ResourceManagementInitialization, ResourceManagementInterface,
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
	
	//
	private ResourceManagementInitialization resourceManagementInitialization;
	private ResourceManagementInterface resourceManagement;
	private LoadStateInterface loadStateInterface;
	
	private ResourceInfo myInfo;
	private ResourceState systemState = new ResourceState();
	private PeriodicManager mgmtThread = null;
	
	/**
	 * How long to collect events from the system before assuming that a stable enough view has been reached
	 * to be able to make reasonable judgments about it. In milliseconds.
	 */
	private final long INIT_STABILIZATION_TIME = 	(System.getProperty("yacs.rs.init.stabilization.time.msek") instanceof String ? 
														Long.parseLong(System.getProperty("yacs.rs.init.stabilization.time.msek"))
														: 30000);
	
	/**
	 * If the service has not heard from a resource within this period of time it will assume the
	 * resource has left the system. In milliseconds.
	 */
	private final long STATE_UPDATE_TIMEOUT = 		(System.getProperty("yacs.rs.timeout.msek") instanceof String ? 
														Long.parseLong(System.getProperty("yacs.rs.timeout.msek"))
														: 60000);
	/**
	 * How long to wait/stabilize before issuing another HIGH_LOAD event, i.e. how long to wait
	 * to give the system time to react to a previous HIGH_LOAD event.
	 */
	private final long LOAD_EVENT_MIN_INTERVAL = 	(System.getProperty("yacs.rs.load.event.min.interval") instanceof String ? 
														Long.parseLong(System.getProperty("yacs.rs.load.event.min.interval"))
														: 30000);
	
	public ResourceService(){
		log("ResourceService created: "+this + " " + YACSSettings.YACS_VERSION);
	}
	
	// ResourceServiceRequestInterface
	public ResourceRequest request( ResourceRequest specs ){
		/*
		 * TODO: opportunistic system view improvement.
		 * Requestors might have more up to date information about resources because of  RS state-reporting delay and, in case of failures, timeout delays.
		 * This can happen if the RS gives a resource that upon further querying/innvocation by requestor turns out to be busy or failed.
		 * Piggybacking this information in the next request might help the RS. Maybe have different levels of resource state, 
		 * e.g. free, suspected busy, suspected failed, busy, failed
		 * 
		 * Also have some randomization of "suitable workers". If many ask at the same time they will all get the same resource now and that
		 * resource might at best be able to take on one anyway.
		 */
		
		this.checkLoad();
		
		ResourceInfo bestMatch = null;
		synchronized( this.systemState ){
			
			ArrayList<ResourceHolder> resources = null;
			if( specs.getComponentType().equals(YACSNames.MASTER_COMPONENT) )
				resources = systemState.getMasters();
			else
				resources = systemState.getWorkers();
			
			for( ResourceHolder holder : resources ){
				ResourceInfo held = holder.getResource();
				if( held.getStatus() == YACSNames.AVAILABILITY_STATUS__FREE && 
					(bestMatch==null || bestMatch.getCpuSpeed()<held.getCpuSpeed()) )
					bestMatch = holder.getResource();
			}
			
			if( bestMatch != null ){
				log("\tFound: " + bestMatch.getComponentId().getId().toString()+", cpu: " + ((long)bestMatch.getCpuSpeed()));
				specs.getAvailableComponents().add( bestMatch.getComponentId() );
				bestMatch.setStatus(YACSNames.AVAILABILITY_STATUS__PRELIMINARILY_ASSIGNED);
				systemState.update(bestMatch);
				
				return specs;
			}
			
			// try suggesting resources already suspected of being busy
			for( ResourceHolder holder : resources ){
				ResourceInfo held = holder.getResource();
				if( held.getStatus() == YACSNames.AVAILABILITY_STATUS__PRELIMINARILY_ASSIGNED && 
					(bestMatch==null || bestMatch.getCpuSpeed()<held.getCpuSpeed()) )
					bestMatch = holder.getResource();
			}
			if( bestMatch != null ){
				log("\tFound: " + bestMatch.getComponentId().getId().toString()+", cpu: " + bestMatch.getCpuSpeed());
				specs.getAvailableComponents().add( bestMatch.getComponentId() );
			}
		}
		
		
		
		return specs;		
		
	}
	
	// ResourceServiceStateInterface
	public void advertise( ResourceInfo resourceInfo ){
		// TODO: ResourceService.advertise - availability analysis. Do in separate thread?
		
		log("RS.ad#"+(adCounter++)+": " + resourceInfo.getComponentId().getId().toString()+", t:"+resourceInfo.getComponentType()+", s:" + resourceInfo.getStatus() + ", cpu: "+((long)resourceInfo.getCpuSpeed()));
		
		/*while( this.loadStateInterface == null ){
			YacsUtils.ignorantSleep(10);
		}*/
		
		this.checkLoad();
		
		synchronized( systemState ){
			/*log("ad: Working on: " + systemState.hashCode() 
									+ ", " + systemState.getMasters().hashCode() + ":{"+systemState.getMasters().size()+"}"
									+ ", " + systemState.getWorkers().hashCode() + ":{"+systemState.getWorkers().size()+"}" );*/	
			this.systemState.update( resourceInfo );
		}
		
		/*this.checkLoad();
		
		synchronized( systemState ){
			this.systemState.update( resourceInfo );
		}*/
		/*if( (testCounter++==10) ){
			this.loadStateInterface.loadState( new LoadInformation(LoadInformation.LOAD.HIGH) );
		}*/
		/*else if( (testCounter%2)==0 )
			this.loadStateInterface.availabilityState( new AvailabilityInformation() );*/
	}
	private long adCounter = 1;
	private long testCounter = 0;
	
	// ResourceManagementInitialization (interface)
	public ResourceState getState(){ 
		return systemState;
	}
	
	// ResourceManagement (interface)
	public void newResource( ResourceInfo resource, ResourceInfo sender ){
		// TODO: ResourceService.newResource - availability analysis
		systemState.update(resource);
		systemState.update(sender);
	}
	public void updatedResource( ResourceInfo resource, ResourceInfo sender ){
		// TODO: ResourceService.updatedResource - availability analysis
		systemState.update(resource);
		systemState.update(sender);
	}
	public void departedResource( ResourceInfo resource, ResourceInfo sender ){
		// TODO: ResourceService.departedResource - availability analysis
		systemState.remove(resource);
		systemState.update(sender);
	}
	public void systemResourceState( ResourceState state, ResourceInfo sender ){
		//log("ResourceService("+lid()+").sysResState: sender " + sender.getComponentId().getId().toString() );
		
		/*logger.log("-----     BEGIN      -----");
		logger.log("-----   HELD STATE   -----");
		logger.log(systemState.state2String());
		logger.log("----- RECEIVED STATE -----");
		logger.log(state.state2String());
		logger.log("-----      END       -----");
		
		if( systemState.merge(state) )
			log("\tState updated!");
		else
			log("\tState already in sync!");*/
		
		systemState.merge(state);
	}
	
	// helpers
	private String lid(){
		return this.myGlobalId.getId().toString();
	}
	
	// component load analysis 
	private void checkLoad(){
		//checkLoad_timecheckpoints();		
	}
	private long lastRequestTime = 0;
	
	private long lastLoadCheckpoint = 0;
	private long checkpointHitCount = 0;
	private long TT_HITCOUNT_THRESHOLD_UPPER = 3;
	private synchronized void checkLoad_timecheckpoints(){
		
		long now = System.currentTimeMillis();
		
		// if new checkpoint crossed
		if( (lastLoadCheckpoint+60000) < now ){
			lastLoadCheckpoint = now;
			checkpointHitCount = 1;
		}
		else
			checkpointHitCount++;
		
		if( checkpointHitCount>=TT_HITCOUNT_THRESHOLD_UPPER && ((lastRequestTime+LOAD_EVENT_MIN_INTERVAL)<now) ){
			log("\tHIGH-LOAD! Triggered request for help.");
			this.loadStateInterface.loadState( new LoadInformation(LoadInformation.LOAD.HIGH) );
			lastRequestTime = now;
		}
		else if( checkpointHitCount>=TT_HITCOUNT_THRESHOLD_UPPER ){
			log("\tHIGH-LOAD! But too short since last asked for help!");
		}
	}
	
	private long requestCounter = 0;
	private double avgReqDelta = 0;
	private long lastLoadEvent = 0;
	private synchronized void checkLoad_avgPerMsek(){
		// TODO: ResourceService.advertise
		/*log("ResourceService("+lid()+").advertise: " + resourceInfo.getComponentId().getId()
								+ ", type: " + resourceInfo.getComponentType()
								+ ", busy: " + (resourceInfo.getStatus() == YACSNames.AVAILABILITY_STATUS__BUSY ? true : false) );*/
		
		/*if( testCounter++ == 5 ){
			this.loadStateInterface.loadState( new LoadInformation() );
			log("\tLoadState event triggered!");
		}*/
		long currentRequestTime = System.currentTimeMillis();
		long timeSinceLast = currentRequestTime - lastRequestTime;
		
		avgReqDelta = ( (avgReqDelta*requestCounter) + timeSinceLast ) / (++requestCounter);
		
		log("RS("+lid()+"): RC: " + requestCounter + ", AVG: " + avgReqDelta +", DLT: " + timeSinceLast );
		
		// if the average time between requests is less than a second
		// and there have been at least 30 secs since last requested another ResourceService
		if( avgReqDelta < 1000 && (lastLoadEvent+LOAD_EVENT_MIN_INTERVAL)<currentRequestTime && (requestCounter>=5) ){
			this.loadStateInterface.loadState( new LoadInformation(LoadInformation.LOAD.HIGH) );
			log("\tLoadState event triggered!");
			lastLoadEvent = currentRequestTime;
		}		
		
		lastRequestTime = currentRequestTime;
	}
	
	// analyze view functional resource state
	private long aid = 0;
	private AvailabilityInformation analyze(){
		AvailabilityInformation info = new AvailabilityInformation(aid++);
		int[] counts = new int[2];
		
		countFreeAndBusy( systemState.getResourceServices(), counts );
		info.setFreeResourceComponents(counts[0]);
		info.setBusyResourceComponents(counts[1]);
		
		countFreeAndBusy( systemState.getMasters(), counts );
		info.setFreeMasterComponents(counts[0]);
		info.setBusyMasterComponents(counts[1]);
		
		countFreeAndBusy( systemState.getWorkers(), counts );
		info.setFreeWorkerComponents(counts[0]);
		info.setBusyWorkerComponents(counts[1]);
		
		return info;
	}
	private void countFreeAndBusy( ArrayList<ResourceHolder> list, int[] counts ){
		int cfree=0, cbusy=0;
		
		for( ResourceHolder holder : list ){
			ResourceInfo info = holder.getResource();
			
			if( info.getStatus() == YACSNames.AVAILABILITY_STATUS__BUSY )
				cbusy++;
			else
				cfree++;
		}
		
		counts[0] = cfree;
		counts[1] = cbusy;
	}
	
	// clear timed out entries
	private void removeTimeouts(){
		
		ArrayList<ResourceHolder> rems = null;
		
		/*log("rt: Working on: " + systemState.hashCode() 
								+ ", " + systemState.getMasters().hashCode() + ":{"+systemState.getMasters().size()+"}"
								+ ", " + systemState.getWorkers().hashCode() + ":{"+systemState.getWorkers().size()+"}" );*/
		
		rems = checkListForTimeouts( systemState.getMasters() );
		for( ResourceHolder rem : rems )
			systemState.remove( rem.getResource() );
		
		rems = checkListForTimeouts( systemState.getWorkers() );
		for( ResourceHolder rem : rems )
			systemState.remove( rem.getResource() );		
	}
	private ArrayList<ResourceHolder> checkListForTimeouts( ArrayList<ResourceHolder> list ){
		ArrayList<ResourceHolder> rems = new ArrayList<ResourceHolder>();
		
		long currentTime = System.currentTimeMillis();
		for( ResourceHolder holder : list ){
			ResourceInfo resource = holder.getResource();
			
			long diff = (resource.getUpdateTime()+STATE_UPDATE_TIMEOUT) - currentTime;
			
			if( (resource.getUpdateTime()+STATE_UPDATE_TIMEOUT) < currentTime ){
				/*log(	"\tRes: " + resource.getComponentId().getId().toString()
									+", h:" +resource.hashCode()
									+", lu:"+resource.getUpdateTime()
									+", t:" +resource.getComponentType() 
									+", to:"+diff );*/
				log("\tRS-timeout: " + resource.getComponentId().getId() +", type: " + resource.getComponentType());
				rems.add( holder );
			}
		}
		
		return rems;
	}
	private boolean isStabilizationDone(){
		return (myInfo.getCreationTime()+INIT_STABILIZATION_TIME) < System.currentTimeMillis();
	}
	
	/**
	 * For periodic management work such as reporting availability number to the self-management part  
	 * and disseminating state information to other ResourceService components 
	 * @author LTDATH
	 */
	class PeriodicManager extends Thread {
		public void run(){
			int counter=0; 
			
			while( status ){
				try {
					sleep( 5000 );
					if( !status ) // if component was told to stop while Mgr-thread was sleeping
						return;
					
					// take turn reporting availability and disseminating to other resource service components
					// report availability
					if( counter++ % 2 == 0 && isStabilizationDone() ){
						removeTimeouts();
						if( loadStateInterface != null ){
							AvailabilityInformation ai = analyze();
							loadStateInterface.availabilityState( ai );
							nlog( "AvInfo sent: R:("+(ai.getFreeResourceComponents()+ai.getBusyResourceComponents())+"),"
												+"M:("+ai.getFreeMasterComponents()+"/"+(ai.getFreeMasterComponents()+ai.getBusyMasterComponents())+"),"
												+"W:("+ai.getFreeWorkerComponents()+"/"+(ai.getFreeWorkerComponents()+ai.getBusyWorkerComponents())+")" );
						}
					}
					// disseminate system knowledge to other services
					else {
						if( resourceManagement != null ){
							synchronized( systemState ){
								myInfo.updateLastUpdateTime();
								resourceManagement.systemResourceState(systemState, myInfo);
							}
						}
					}
				}
				catch( Exception e ){
					e.printStackTrace();
				}
			}
		}
	}
	
	
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public String[] listFc() {
		log("ResourceService.listFc");
		return new String[] { 	"component",
								YACSNames.RESOURCE_MANAGEMENT_CLIENT_INTERFACE,
								YACSNames.RESOURCE_MANAGEMENT_INIT_CLIENT_INTERFACE,
								YACSNames.LOAD_STATE_CLIENT_INTERFACE };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {
		log("ResourceService.lookupFc: "+itfName);
		if (itfName.equals(YACSNames.RESOURCE_MANAGEMENT_CLIENT_INTERFACE))
			return this.resourceManagement;
		else if (itfName.equals(YACSNames.RESOURCE_MANAGEMENT_INIT_CLIENT_INTERFACE))
			return this.resourceManagementInitialization;
		else if (itfName.equals(YACSNames.LOAD_STATE_CLIENT_INTERFACE))
			return this.loadStateInterface;
		else if (itfName.equals("component"))
			return myself;
		else{
			log("\tInterface not bindable");
			throw new NoSuchInterfaceException(itfName);
		}
	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		log("ResourceService.bindFc: " + itfName );
		if (itfName.equals(YACSNames.RESOURCE_MANAGEMENT_CLIENT_INTERFACE))
			resourceManagement = (ResourceManagementInterface) itfValue;
		else if (itfName.equals(YACSNames.RESOURCE_MANAGEMENT_INIT_CLIENT_INTERFACE))
			resourceManagementInitialization = (ResourceManagementInitialization) itfValue;
		else if (itfName.equals(YACSNames.LOAD_STATE_CLIENT_INTERFACE))
			loadStateInterface = (LoadStateInterface) itfValue;
		else if (itfName.equals("component"))
			myself = (Component) itfValue;
		else {
			log("\tInterface not bindable");
			throw new NoSuchInterfaceException(itfName);
		}
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		log("ResourceService.unbindFc: " + itfName);
		if (itfName.equals(YACSNames.RESOURCE_MANAGEMENT_CLIENT_INTERFACE))
			resourceManagement = null;
		else if (itfName.equals(YACSNames.RESOURCE_MANAGEMENT_INIT_CLIENT_INTERFACE))
			resourceManagementInitialization = null;
		else if (itfName.equals(YACSNames.LOAD_STATE_CLIENT_INTERFACE))
			loadStateInterface = null;
		else if (itfName.equals("component"))
			myself = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {
		log("ResourceService.getFcState");
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		log("ResourceService.startFc");
		
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
			} catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}
		}

		try {
			niche = FractalUtil.getFirstFoundSubComponentByName(jadeNode,"nicheOS");
		} catch (NoSuchComponentException e1) {
			e1.printStackTrace();
		}

		try {
			overlayAccess = (OverlayAccess) niche.getFcInterface("overlayAccess");
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		nicheOSSupport = overlayAccess.getOverlay().getComponentSupport(myself);
		logger = overlayAccess.getOverlay().getNicheAsynchronousSupport();
		this.createYacsLogger( "ResourceService", null, true, true, logger );
		status = true;

		myGlobalId = nicheOSSupport.getResourceManager().getComponentId(myself);
		myLocation = "" + myGlobalId.getResourceRef().getDKSRef().getId();

		totalSpace = freeSpace = nicheOSSupport.getResourceManager().getTotalStorage(myself);
		nicheOSSupport.setOwner(myGlobalId);

		lastRequestTime = System.currentTimeMillis();
		myInfo = new ResourceInfo( this.myGlobalId, YACSNames.RESOURCE_SERVICE_COMPONENT, System.currentTimeMillis(), YACSNames.AVAILABILITY_STATUS__BUSY );
		// TODO: re-enable periodic management
		mgmtThread = new PeriodicManager();
		mgmtThread.start();
		log("Started YACS RESOURCE_SERVICE component = " + myGlobalId.getId() + " at " + myLocation + " with totalspace: " + totalSpace + ". Name: "+ myGlobalId.getComponentName());
	}

	public void stopFc() throws IllegalLifeCycleException {
		log("ResourceService.stopFc");
		status = false;
	}
}