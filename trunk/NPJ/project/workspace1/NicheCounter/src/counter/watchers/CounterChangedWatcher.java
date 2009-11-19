package counter.watchers;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import counter.events.CounterChangedEvent;
import counter.sensors.CounterStatusSensor;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.DeploySensorsInterface;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;

public class CounterChangedWatcher implements EventHandlerInterface,
		MovableInterface, BindingController, LifeCycleController, InitInterface {

	private Component myself;
	private NicheActuatorInterface actuator;
	private DeploySensorsInterface deploySensor;
	private TriggerInterface trigger;
	
	

	private boolean status;

	@Override
	public void eventHandler(Serializable event, int flag) {
		if (event instanceof CounterChangedEvent) {
			int value = ((CounterChangedEvent)event).getCounterNumber();
//			System.out.println("value in watcher = "+value);
			trigger.trigger(new CounterChangedEvent(value));
		}

	}

	public Serializable[] getAttributes() {
		return new Serializable[] {};
	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		if (itfName.equals("component"))
			myself = (Component) itfValue;
		else if (itfName
				.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			actuator = (NicheActuatorInterface) itfValue;
		else if (itfName
				.equals(FractalInterfaceNames.DEPLOY_SENSOR_CLIENT_INTERFACE))
			deploySensor = (DeploySensorsInterface) itfValue;
		else if (itfName
				.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			trigger = (TriggerInterface) itfValue;
		// else if
		// (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
		// triggerInterface = (TriggerInterface) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String[] listFc() {
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE,
				FractalInterfaceNames.DEPLOY_SENSOR_CLIENT_INTERFACE,
				FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE};
	}

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
		// else if
		// (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
		// return triggerInterface;
		else
			throw new NoSuchInterfaceException(itfName);
	}

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
		// else if
		// (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
		// triggerInterface = null;

		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {

		status = true;
	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;

	}

	@Override
	public void init(Serializable[] parameters) {
		// TODO Auto-generated method stub
		initSmth() ;
	}

	@Override
	public void init(NicheActuatorInterface actuator) {
		// TODO Auto-generated method stub
		initSmth() ;
	}

	@Override
	public void initId(NicheId id) {
		// TODO Auto-generated method stub
		initSmth() ;
	}

	@Override
	public void reinit(Serializable[] parameters) {
		// TODO Auto-generated method stub
		initSmth() ;
	}

	private void initSmth() {
		System.out.println("INIT called!!!");
		Serializable[] sensorParameters = new Serializable[2];

		deploySensor.deploySensor(CounterStatusSensor.class.getName(),
				CounterChangedEvent.class.getName(), sensorParameters, null,
				new String[] { "counterStatus" });
	}

}