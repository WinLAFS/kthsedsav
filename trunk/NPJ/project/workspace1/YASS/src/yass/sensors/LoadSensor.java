package yass.sensors;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import yass.events.ComponentStateChangeEvent;
import dks.niche.fractal.interfaces.SensorInitInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;

public class LoadSensor implements SensorInitInterface, LoadChangeInterface, LoadSensorAttributeController, BindingController, LifeCycleController {
	
	
	// Client Interfaces
	TriggerInterface trigger;
	
	/////////////////
	Component mySelf;
	private boolean status;
	private boolean deltaIsInitialized;
	
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

	////////////////////////////////////////////////////////////////////////////
	////////////////////////// Server Interfaces ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	//////// InitInterface
	public void init(Object[] parameters) {
		delta = (Integer)parameters[0];
		if(logger != null) {
			/*#%*/ logger.log("LoadSensor initialized with delta = " + delta);
		} else {
			deltaIsInitialized = true;
		}
		//System.err.println("$$$$$$$$$$$$ LoadSensor: delta = " + delta);
		
		
	}
	
	public void reinit(Object[] parameters) {
		delta = (Integer)parameters[0];
		//System.err.println("$$$$$$$$$$$$ LoadSensor: delta = " + delta);
		
		
	}

	public void init(NicheActuatorInterface actuator) {
		myNicheActuatorInterface = actuator;	
		logger = actuator.testingOnly();
		if(deltaIsInitialized) {
			/*#%*/ logger.log("LoadSensor initialized with delta = " + delta);
		}
	}


	public void initId(Object id) {
		myId = (NicheId)id;
	}
	
	
	public void initComponentId(ComponentId cid) {
		myComponentId = cid;
		
	}


	//////// LoadChangeInterface
	public void newLoad(int load) {
		previousLoad = currentLoad;
		currentLoad = load;
		
		currentDelta = currentDelta + (int)(currentLoad - previousLoad);
		//System.err.println("$$$$$$$$$$$$ LoadSensor: new load is " + load + " and currentDelta is " + currentDelta);
		
		
		if(Math.abs(currentDelta) > delta) {
			currentDelta = 0;
			/*#%*/ logger.log("LoadSensor is triggering new ComponentStateChangeEvent with previousLoad: " + previousLoad + " currentLoad: " + currentLoad);
			ComponentStateChangeEvent e = new ComponentStateChangeEvent(myComponentId, previousLoad, currentLoad);
			trigger.trigger(e);
		} /*#%*/ else {
		/*#%*/ 	logger.log("LoadSensor says currentDelta is too small: " + currentDelta);
		/*#%*/ }

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
		//System.err.println("LoadSensor Started: Total = " + totalStorage + ", Load = " + currentLoad + ", Delta = " + delta);
	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;
		
	}

}
