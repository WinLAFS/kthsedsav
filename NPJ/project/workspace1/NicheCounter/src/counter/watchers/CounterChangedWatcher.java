package counter.watchers;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import counter.aggregators.ServiceSupervisor;
import counter.events.CounterChangedEvent;
import counter.sensors.CounterStatusSensor;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.DeploySensorsInterface;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;

/**
 * The watcher class that initializes the {@link CounterStatusSensor},
 * receives the event when a state (counter value) of a component changed 
 * by the sensors and forwards the event to the {@link ServiceSupervisor}.
 *
 */
public class CounterChangedWatcher implements EventHandlerInterface,
		MovableInterface, BindingController, LifeCycleController, InitInterface {

	private Component myself;
	private NicheActuatorInterface actuator;
	private DeploySensorsInterface deploySensor; //the interface used to deploy sensors
	private TriggerInterface trigger; //the interface used to trigger events
	
	private boolean status;

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.EventHandlerInterface#eventHandler(java.io.Serializable, int)
	 */
	public void eventHandler(Serializable event, int flag) {
		if (event instanceof CounterChangedEvent) {
			int value = ((CounterChangedEvent)event).getCounterNumber();
			int lamport = ((CounterChangedEvent)event).getLamport();
			ComponentId cid = ((CounterChangedEvent)event).getCid();
			trigger.trigger(new CounterChangedEvent(cid, value, lamport)); //promote the event to ServiceSupervisor
		}

	}

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.MovableInterface#getAttributes()
	 */
	public Serializable[] getAttributes() {
		return new Serializable[] {};
	}

	/* (non-Javadoc)
	 * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String, java.lang.Object)
	 */
	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		if (itfName.equals("component"))
			myself = (Component) itfValue;
		else if (itfName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			actuator = (NicheActuatorInterface) itfValue;
		else if (itfName.equals(FractalInterfaceNames.DEPLOY_SENSOR_CLIENT_INTERFACE))
			deploySensor = (DeploySensorsInterface) itfValue;
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			trigger = (TriggerInterface) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.fractal.api.control.BindingController#listFc()
	 */
	public String[] listFc() {
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE,
				FractalInterfaceNames.DEPLOY_SENSOR_CLIENT_INTERFACE,
				FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE};
	}

	/* (non-Javadoc)
	 * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
	 */
	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {
		if (itfName.equals(FractalInterfaceNames.COMPONENT))
			return myself;
		else if (itfName
				.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return actuator;
		else if (itfName
				.equals(FractalInterfaceNames.DEPLOY_SENSOR_CLIENT_INTERFACE))
			return deploySensor;
		else if (itfName
				.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			return trigger;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
	 */
	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals(FractalInterfaceNames.COMPONENT))
			myself = null;
		else if (itfName
				.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			actuator = null;
		else if (itfName
				.equals(FractalInterfaceNames.DEPLOY_SENSOR_CLIENT_INTERFACE))
			deploySensor = null;
		else if (itfName
				.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			trigger = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.fractal.api.control.LifeCycleController#getFcState()
	 */
	public String getFcState() {
		return status ? "STARTED" : "STOPPED";
	}

	/* (non-Javadoc)
	 * @see org.objectweb.fractal.api.control.LifeCycleController#startFc()
	 */
	public void startFc() throws IllegalLifeCycleException {

		status = true;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
	 */
	public void stopFc() throws IllegalLifeCycleException {
		status = false;

	}

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.InitInterface#init(java.io.Serializable[])
	 */
	public void init(Serializable[] parameters) {
		initSensor() ;
	}

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.InitInterface#init(dks.niche.interfaces.NicheActuatorInterface)
	 */
	public void init(NicheActuatorInterface actuator) {
		initSensor() ;
	}

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.InitInterface#initId(dks.niche.ids.NicheId)
	 */
	public void initId(NicheId id) {
		initSensor() ;
	}

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.InitInterface#reinit(java.io.Serializable[])
	 */
	public void reinit(Serializable[] parameters) {
		initSensor() ;
	}

	private void initSensor() {
		Serializable[] sensorParameters = new Serializable[2];

		//deploy the sensors
		deploySensor.deploySensor(CounterStatusSensor.class.getName(),
				CounterChangedEvent.class.getName(), sensorParameters, null,
				new String[] { "counterStatus" });
	}

}