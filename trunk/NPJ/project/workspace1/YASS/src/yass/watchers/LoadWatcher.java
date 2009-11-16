package yass.watchers;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import yass.events.ComponentStateChangeEvent;
import yass.sensors.LoadSensor;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.fractal.interfaces.DeploySensorsInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;

public class LoadWatcher implements InitInterface, EventHandlerInterface, MovableInterface,
		LoadWatcherAttributeController, BindingController, LifeCycleController {

	// Client Interfaces
	TriggerInterface trigger;

	DeploySensorsInterface deploySensor;

	// ///////////////
	Component mySelf;

	private boolean status;

	// local variables

	//private NicheActuatorInterface myNicheActuatorInterface;
	private NicheAsynchronousInterface logger; 

	private Object myId;

	// //////////////////////////////////////////////////////////////////////////
	// ///////////////////////// Server Interfaces
	// //////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	// ////// InitInterface
	// /////////////////////////////////////////////////////
	public void init(Object[] parameters) {

		// read your parameters here and process them & extract sensor
		// parameters
		// for example we assume here that parameters[2 & 3] is an array
		// containing the sensor parameters
		Object[] sensorParameters = new Object[2];
		
		sensorParameters[0] = parameters[2];
		sensorParameters[1] = parameters[3];
	
		deploySensor.deploySensor(
				LoadSensor.class.getName(),
				ComponentStateChangeEvent.class.getName(),
				sensorParameters,
				null, //no clientInterfaces
				new String[] { "pushLoadChange" } //just one server interface
		);
	}
	
	public void reinit(Object[] parameters) {

		// sensors are already deployed!
	
	}

	public void init(NicheActuatorInterface actuator) {
		//myNicheActuatorInterface = actuator;
		logger = actuator.testingOnly();

	}

	public void initId(Object id) {
		myId = id;
	}

	// ////// EventHandlerInterface
	// /////////////////////////////////////////////
	public void eventHandler(Object event, int flag) {
		//System.out.println("$$$$$$$$$$$$ LoadWatcher says: I got "+event.getClass().getSimpleName());
		
		if (event instanceof ComponentStateChangeEvent) {
			//ComponentStateChangeEvent e = (ComponentStateChangeEvent) event;
			
			/*#%*/ logger.log("LoadWatcher got a ComponentStateChangeEvent and is triggering");
			trigger.trigger(event);
		} /*#%*/ else {
			/*#%*/ String logMessage = 
			/*#%*/ 	"LoadWatcher got an event it cannot understand: "
			/*#%*/ 	+ event.getClass().getName()
			/*#%*/ 	+ " and is not triggering";
			/*#%*/ 
			/*#%*/ logger.log(logMessage);
			/*#%*/ System.err.println(logMessage);
			
		/*#%*/ }
		

	}

	// //////////////////////////////////////////////////////////////////////////
	// /////////////////////////// Attributes
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public Object[] getAttributes() {
		return new Object[] {true}; //true for re-init... 
	}
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public String[] listFc() {

		return new String[] { "component", "trigger", "deploySensor" };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {

		if (itfName.equals("trigger"))
			return trigger;
		else if (itfName.equals("deploySensor"))
			return deploySensor;
		else if (itfName.equals("component"))
			return mySelf;
		else
			throw new NoSuchInterfaceException(itfName);

	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		if (itfName.equals("trigger"))
			trigger = (TriggerInterface) itfValue;
		else if (itfName.equals("deploySensor"))
			deploySensor = (DeploySensorsInterface) itfValue;
		else if (itfName.equals("component"))
			mySelf = (Component) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals("trigger"))
			trigger = null;
		else if (itfName.equals("deploySensor"))
			deploySensor = null;
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
		System.err.println("LoadWatcher Started");
	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;

	}

}
