package yacs.managers;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.type.ComponentType;

import yacs.interfaces.YACSNames;

import dks.niche.events.ComponentFailEvent;
import dks.niche.events.ResourceLeaveEvent;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.fractal.interfaces.DeploySensorsInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.ids.ManagementElementId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.wrappers.ManagementDeployParameters;
import dks.niche.ids.*;

import yacs.interfaces.*;
import yacs.job.*;
import yacs.job.events.*;


import java.util.ArrayList;

public class Watchdog 	extends yacs.YacsComponent
						implements 	EventHandlerInterface, MovableInterface, 
										InitInterface, BindingController, 
										LifeCycleController
{
	private Component myself;	
	private NicheActuatorInterface actuator;
	private NicheAsynchronousInterface logger;
	
	private TriggerInterface triggerInterface;
	private DeploySensorsInterface deploySensor;
	
	ComponentType workerWatcherComponentType = null;
	ComponentType masterWatcherComponentType = null;
		
	private boolean status;
	private NicheId myGlobalId;
	
	private NicheId watcherId;
	private String groupType;
	private GroupId watchedGroup;
	private GroupId globalMasterGroup, globalWorkerGroup, resourceServiceGroup;
	
	private NicheId masterAggregator, workerAggregator;
	
	public Watchdog(){
		log("Watchdog created!");
	}

	public void eventHandler(Object e, int flag) {
		
		// TODO: how to reconnect sensors to new/re-deployed Watchers?
		
		log("Watchdog.eventHandler: "+e);
		if(e instanceof ResourceLeaveEvent) {
			ManagementDeployParameters params = new ManagementDeployParameters();
			
			params.describeWatcher(
					YACSNames.WORKER_WATCHER_CLASS_NAME,
					YACSNames.WORKER_WATCHER_ADL_NAME,
					workerWatcherComponentType,
					new Object[]{	watchedGroup, globalMasterGroup, globalWorkerGroup, 
									myGlobalId, workerAggregator, resourceServiceGroup },
					watchedGroup.getId()
				);
	
			log("\tDeploying WorkerWatcher...");
			NicheId workerWatcher = actuator.deploy(params, watchedGroup);
	
			log("\tSubscribing WorkerWatcher to Workers");
			actuator.subscribe(watchedGroup, workerWatcher, ResourceLeaveEvent.class.getName() );
			actuator.subscribe(watchedGroup, workerWatcher, ComponentFailEvent.class.getName() );
			
			log("\tSubscribing WorkerWatcher to Watchdog");
			actuator.subscribe(this.myGlobalId, workerWatcher, ResourceLeaveEvent.class.getName() );
			actuator.subscribe(this.myGlobalId, workerWatcher, ComponentFailEvent.class.getName() );
		}
		else if(e instanceof ComponentFailEvent) {
			// TODO: lookup task from resource.id and redeploy
		}
		else if(e instanceof ManagementEvent){
		}
	}	
	private Object watchdog;
	
	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// Attributes
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public void init(Object[] parameters) {
		// No params to set
		log("Watchdog.init(Object[]): "+parameters);
		watcherId = (NicheId)parameters[0];
		groupType = (String)parameters[1];
		watchedGroup = (GroupId)parameters[2];
		globalMasterGroup = (GroupId)parameters[3];
		globalWorkerGroup = (GroupId)parameters[4];
		masterAggregator = parameters[5]==null ? null : (NicheId)parameters[5];
		workerAggregator = parameters[6]==null ? null : (NicheId)parameters[6];
		resourceServiceGroup = (GroupId)parameters[7];
		
		log("\tGroup: "+watchedGroup.getId()+" of type: " + groupType + " watched by: " + watcherId);
		log("\tGlobals, M: "+globalMasterGroup.getId()+", W: " + globalWorkerGroup.getId());
		
		/*for( Object o : parameters ){
			log("\tParam: "+o);
		}*/
		
		/*log("\tTriggering ManagementEvent to notify watched component...");
		triggerInterface.trigger(new ManagementEvent());
		log("\t\tDone triggering ManagementEvent");*/
	}

	public void init(NicheActuatorInterface actuator) {
		log("Watchdog.init(NicheActuatorInterface): "+actuator);
		this.actuator = actuator;
		this.logger = actuator.testingOnly();
		this.createYacsLogger( "Watchdog", null, true, true, logger );
		
		workerWatcherComponentType = actuator.getComponentType(YACSNames.WORKER_WATCHER_CLASS_NAME);
		masterWatcherComponentType = actuator.getComponentType(YACSNames.MASTER_WATCHER_CLASS_NAME);
	}

	public void initId(Object id) {
		log("Watchdog.initId: "+id + ", type: " + id.getClass().getName());
		myGlobalId = (NicheId)id;
		
		dks.niche.wrappers.Subscription rle=null, rfe=null, me=null;
		
		log("\tDeploying Watchdog WITH NULL CO-LOCATION...");
		//ManagementElementId watchdog = actuator.deploy(params, workerGroup);
		log("\tSubscribing Watchdog: " + this.myGlobalId.getId() +" to Watcher: " + watcherId + " of type: " + this.groupType);
		// TODO: rle = actuator.subscribe(watcherId, myGlobalId, ResourceLeaveEvent.class.getName() );
		rfe = actuator.subscribe(watcherId, myGlobalId, ComponentFailEvent.class.getName() );
		me  = actuator.subscribe(watcherId, myGlobalId, ManagementEvent.class.getName() );
		log("\tWD-2-W.subs: " + rle +", " + rfe + ", " + me);
		
		log("\tSubstribing Watcher to Watchdog...");
		// TODO: rle = actuator.subscribe(myGlobalId, watcherId, ResourceLeaveEvent.class.getName() );
		rfe = actuator.subscribe(myGlobalId, watcherId, ComponentFailEvent.class.getName() );
		me  = actuator.subscribe(myGlobalId, watcherId, ManagementEvent.class.getName() );
		log("\tW-2-WD.subs: " + rle +", " + rfe + ", " + me);
	}
	
	public void reinit(Object[] applicationParameters) {
		log("Watchdog.REinit(Object[]): "  + applicationParameters);
	}

	public Object[] getAttributes() {
		log("Watchdog.getAttributes");
		return null; //Ok,since it is stateless
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	public String[] listFc() {
		log("Watchdog.listFc");
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE,
				YACSNames.DEPLOY_SENSOR };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {
		log("Watchdog.lookupFc: "+itfName);
		if (itfName.equals(FractalInterfaceNames.COMPONENT))
			return myself;
		else if (itfName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return actuator;		
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			return triggerInterface;
		else if(itfName.equals(YACSNames.DEPLOY_SENSOR))
			return deploySensor;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		log("Watchdog.bindFc: " + itfName );
		if (itfName.equals("component"))
			myself = (Component) itfValue;
		else if (itfName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			actuator = (NicheActuatorInterface) itfValue;
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			triggerInterface = (TriggerInterface)itfValue;
		else if (itfName.equals(YACSNames.DEPLOY_SENSOR))
			deploySensor = (DeploySensorsInterface)itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		log("Watchdog.unbindFc: " + itfName);
		if (itfName.equals(FractalInterfaceNames.COMPONENT))
			myself = null;
		else if (itfName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			actuator = null;
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			triggerInterface = null;
		else if (itfName.equals(YACSNames.DEPLOY_SENSOR))
			deploySensor = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {
		log("Watchdog.getFcState");
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		log("Watchdog.startFc");
		status = true;
	}

	public void stopFc() throws IllegalLifeCycleException {
		log("Watchdog.stopFc");
		status = false;

	}
}
