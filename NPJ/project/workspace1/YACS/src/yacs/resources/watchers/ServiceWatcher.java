package yacs.resources.watchers;

import java.util.*;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import dks.niche.events.ComponentFailEvent;
import dks.niche.events.ResourceLeaveEvent;
import dks.niche.events.MemberAddedEvent;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.fractal.interfaces.DeploySensorsInterface;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;

import yacs.interfaces.YACSNames;
import yacs.interfaces.YACSSettings;
import yacs.resources.events.*;
import yacs.utils.EventHistory;
import yacs.resources.sensors.LoadSensor;
import yacs.utils.YacsTimer;

public class ServiceWatcher extends yacs.YacsManagementElement
							implements 	EventHandlerInterface, MovableInterface, 
										BindingController, LifeCycleController
{
	private Component myself;	
	private NicheActuatorInterface actuator;
	private NicheAsynchronousInterface logger;
	
	private TriggerInterface triggerInterface;
	private DeploySensorsInterface deploySensor;

	private boolean status;
	private NicheId myGlobalId;
	
	// members for replication - begin
	private GroupId serviceGroup;
	private Hashtable<String,String> services = new Hashtable<String,String>();
	private EventHistory eventHistory = new EventHistory();
	private boolean isSystemSelfManagementActive = false;
	// members for replication - end	
	
	// for timing
	private int tuid;
		
	public ServiceWatcher(){
		log("ServiceWatcher created!");
	}

	// EventHandlerInterface
	public void eventHandler(Object e, int flag) {
		log("ServiceWatcher.eventHandler: "+e);
		
		// new service component added
		if( e instanceof MemberAddedEvent ){
			MemberAddedEvent add = (MemberAddedEvent)e;
			String id = add.getSNR().getId().toString();
			
			if( !eventHistory.record("MemberAddedEvent:"+id,null) ){
				log("\tMemberAddedEvent event seen before! ID: " + id);
				return;
			}
			
			// add to my system view
			services.put( id, id );
			
			// notify higher level MEs, i.e. aggregators and managers			
			if( isSystemSelfManagementActive )
				trigger( new ServiceManagementEvent(add.getSNR().getId(),ServiceManagementEvent.TYPE.SERVICE_ADDED) );
		}
		else if( e instanceof ComponentFailEvent ){
			ComponentFailEvent fe = (ComponentFailEvent)e;

			String id = fe.getNicheId().toString();
			
			if( !eventHistory.record("ComponentFailEvent:"+id,null) ){
				log("\tComponentFailEvent event seen before! ID: " + id);
				return;
			}			
			
			// remove from my system view
			if( this.services.containsKey(id) )
				this.services.remove(id);

			// notify higher level MEs, i.e. aggregators and managers
			if( isSystemSelfManagementActive )
				trigger( new ServiceManagementEvent(fe.getNicheId(),ServiceManagementEvent.TYPE.SERVICE_DEPARTED) );
		}
		else if( e instanceof ResourceLeaveEvent ){
			ResourceLeaveEvent le = (ResourceLeaveEvent)e;
			
			String id = le.getNicheId().toString();
			
			if( !eventHistory.record("ResourceLeaveEvent:"+id,null) ){
				log("\tResourceLeaveEvent event seen before! ID: " + id);
				return;
			}
			
			// remove from my system view
			if( this.services.containsKey(id) )
				this.services.remove(id);
			
			// notify higher level MEs, i.e. aggregators and managers
			if( isSystemSelfManagementActive )
				trigger( new ServiceManagementEvent(le.getNicheId(),ServiceManagementEvent.TYPE.SERVICE_DEPARTED) );
		}
		else if( e instanceof ServiceManagementEvent ){
			ServiceManagementEvent sme = (ServiceManagementEvent)e;
			
			String id = sme.getKey();
			
			// add service to my system view if not seen before 
			if( !this.services.containsKey(id) ){
				// add to my system view
				services.put(id, id);
				
				// notify higher level MEs, i.e. aggregators and managers
				if( isSystemSelfManagementActive )
					trigger( new ServiceManagementEvent(sme.getId(),ServiceManagementEvent.TYPE.SERVICE_ADDED) );
			}
			
			// then forward the event to higher level MEs. Should be a Service-High-Load event or Functional-Resource-Availability-Information event
			if( isSystemSelfManagementActive )
				trigger( sme );
		}
		else {
			log("\tUnknown event type: " + e.getClass().getName() );
		}
	}
	
	// helpers
	private void trigger( Object event ){
		if( !isActiveReplica() )
			return;
		log( "Triggering: " + event );
		triggerInterface.trigger( event );			
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// Attributes
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	protected void doInit(Object[] parameters) {
		log("ServiceWatcher.init(Object[]): "+parameters);
		
		serviceGroup = parameters[0] == null ? null : (GroupId)parameters[0];
		isSystemSelfManagementActive = parameters[1] == null ? isSystemSelfManagementActive : (Boolean)parameters[1];
		//serviceGroup.
		
		// TODO: is a sensor deployed on every single component in the group, including future-added? Or is there only one sensor?
		YacsTimer timer = new YacsTimer( tuid++ );
		Object[] sensorParameters = new Object[2];
		deploySensor.deploySensor(	LoadSensor.class.getName(),
									ServiceManagementEvent.class.getName(), sensorParameters, 
									
									// client interface
									null,
									//new String[] { YACSNames.STATE_CHANGE_CLIENT_INTERFACE },
									
									// server interface
									new String[] { YACSNames.LOAD_STATE_SERVER_INTERFACE }
									//null
								);
		timefx("SW",""+timer.getTtid(),timer.getTtid(),"SWLSD",null,timer.elapsed(),null); // Worker Watcher Load Sensor Deployed
		
		log("ServiceWatcher.init(Object[]): LoadSensor deployed!");
	}

	protected void doInit(NicheActuatorInterface actuator) {
		log("ServiceWatcher.init(NicheActuatorInterface): "+actuator);
		this.actuator = actuator;
		this.logger = actuator.testingOnly();
		
		if( myGlobalId != null ){
			this.createYacsLogger( "ServiceWatcher", String.valueOf(myGlobalId.getReplicaNumber()), true, true, logger );
		}
		else {
			this.createYacsLogger( "ServiceWatcher", null, true, true, logger );
		}
		this.logReinit(); // will log only if re-inited
	}

	protected void doInitId(Object id) {
		log("ServiceWatcher.initId: "+id + ", rep#: " + (id!=null&&(id instanceof NicheId)?((NicheId)id).getReplicaNumber():"NULL"));
		myGlobalId = (NicheId)id;
		setActiveReplica( myGlobalId.getReplicaNumber() == 0 );
		log("\tSW-Rep.#: " + myGlobalId.getReplicaNumber());
		
		if( yacsLog != null ){
			yacsLog.setId( String.valueOf(myGlobalId.getReplicaNumber()) );
		}
	}
	
	public Object[] getAttributes() {
		log("ServiceWatcher.getAttributes");
		return new Object[]{
				serviceGroup,
				new Boolean(isSystemSelfManagementActive),
				services,
				eventHistory
			};
	}
	
	protected void doReinit(Object[] applicationParameters) {
		log("ServiceWatcher.REinit(Object[]): "  + applicationParameters);
		
		this.setReinited(true);
			
		if( applicationParameters == null || applicationParameters.length != 4 ){
			log("\tArray ABNORMAL!");
			
			this.setAbnormalReinit( "Params null or length!=4" );
			if( yacsLog != null ) this.logReinit();
					
			return;
		}
		if( yacsLog != null ) this.logReinit();
		
		serviceGroup = 					(applicationParameters[0]==null?null:(GroupId)applicationParameters[0]);
		isSystemSelfManagementActive = 	(applicationParameters[1]==null?false:(Boolean)applicationParameters[1]);
		services = 						(applicationParameters[2]==null?null:(Hashtable<String,String>)applicationParameters[2]);
		eventHistory = 					(applicationParameters[3]==null?null:(EventHistory)applicationParameters[3]);
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	public String[] listFc() {
		log("ServiceWatcher.listFc");
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE,
				YACSNames.DEPLOY_SENSOR };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {
		log("ServiceWatcher.lookupFc: "+itfName);
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
		log("ServiceWatcher.bindFc: " + itfName );
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
		log("ServiceWatcher.unbindFc: " + itfName);
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
		log("ServiceWatcher.getFcState");
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		log("ServiceWatcher.startFc");
		status = true;
		log("SW started. Version: " + YACSSettings.YACS_VERSION);
	}

	public void stopFc() throws IllegalLifeCycleException {
		log("ServiceWatcher.stopFc");
		status = false;

	}
}
