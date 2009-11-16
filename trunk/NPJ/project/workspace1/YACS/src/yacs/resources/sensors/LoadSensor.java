package yacs.resources.sensors;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import dks.niche.fractal.interfaces.SensorInitInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;

import yacs.resources.data.*;
import yacs.resources.interfaces.*;
import yacs.resources.events.*;

public class LoadSensor extends yacs.YacsComponent 
						implements 	LoadStateInterface, SensorInitInterface, 
									BindingController, LifeCycleController {
	// Client Interfaces
	TriggerInterface trigger;
	
	/////////////////
	Component mySelf;
	private boolean status;
	
	// Local variables
	private int delta;
	private int currentLoad;
	private int previousLoad;
	private int totalStorage;
	private int currentDelta;
	private NicheId myId;
	private ComponentId myComponentId;
	private NicheActuatorInterface myNicheActuatorInterface;
	private NicheAsynchronousInterface logger;
	
	
	public LoadSensor() {
		mySelf = null;
		trigger = null;
		currentDelta = 0;
	}
	
	// LoadStateInterface
	public void loadState( LoadInformation load ){
		nlog("loadState: " + load);
		trigger.trigger( new ServiceManagementEvent(myComponentId.getId(), ServiceManagementEvent.TYPE.SERVICE_HIGH_LOAD) );
	}
	public void availabilityState( AvailabilityInformation availability ){
		nlog("availabilityState: " + availability );
		ServiceManagementEvent sme = new ServiceManagementEvent( myComponentId.getId(), ServiceManagementEvent.TYPE.AVAILABILITY_INFORMATION );
		sme.setAvailability(availability);
		trigger.trigger( sme );
	}

	////////////////////////////////////////////////////////////////////////////
	////////////////////////// Server Interfaces ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	//////// InitInterface
	public void init(Object[] parameters) {
		//delta = (Integer)parameters[0];
		//log("$$$$$$$$$$$$ LoadSensor: delta = " + delta);
		
		
	}

	public void init(NicheActuatorInterface actuator) {
		myNicheActuatorInterface = actuator;	
		logger = actuator.testingOnly();
		this.createYacsLogger( "LoadSensor", null, true, true, logger );
	}


	public void initId(Object id) {
		myId = (NicheId)id;
	}
	
	
	public void initComponentId(ComponentId cid) {
		myComponentId = cid;
		
	}
	
	public void reinit(Object[] applicationParameters) {
		log("LoadSensor.REinit(Object[]): "  + applicationParameters);
	}
	
	////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Attributes ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	
	public int getCurrentLoad() {
		return currentLoad;
	}

	public int getDelta() {
		return delta;
	}

	public int getTotalStorage() {
		return totalStorage;
	}

	public void setCurrentLoad(int load) {
		currentLoad = load;
	}

	public void setDelta(int delta) {
		this.delta = delta;		
	}

	public void setTotalStorage(int total) {
		totalStorage = total;
	}
	
	
	////////////////////////////////////////////////////////////////////////////
	////////////////////////// Fractal Stuff ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	public String[] listFc () {

		return new String[] {"component", "trigger"};
	}
	
	
	public Object lookupFc (final String itfName) throws NoSuchInterfaceException {

		if (itfName.equals("trigger"))
			return trigger;
		else if (itfName.equals("component"))
			return mySelf;
		else
			throw new NoSuchInterfaceException(itfName);

	}
	
	public void bindFc (final String itfName, final Object itfValue) throws NoSuchInterfaceException {
		if (itfName.equals("trigger"))
			trigger = (TriggerInterface) itfValue;
		else if (itfName.equals("component"))
			mySelf = (Component) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc (final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals("trigger"))
			trigger = null;
		else if (itfName.equals("component"))
			mySelf = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {
		
		return status ? "STARTED": "STOPPED";
	}
	
	public void startFc() throws IllegalLifeCycleException {
		status = true;
		//log("LoadSensor Started: Total = " + totalStorage + ", Load = " + currentLoad + ", Delta = " + delta);
	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;
		
	}

}
