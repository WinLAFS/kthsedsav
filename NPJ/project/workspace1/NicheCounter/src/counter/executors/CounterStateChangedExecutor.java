package counter.executors;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import counter.actuators.CounterStatusActuator;
import counter.events.ComponentOutOfSyncEvent;
import counter.events.CounterChangedEvent;
import counter.events.InformOutOfSyncEvent;
import counter.interfaces.CounterResyncInterface;
import counter.sensors.CounterStatusSensor;

import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.DeployActuatorsInterface;
import dks.niche.fractal.interfaces.DeploySensorsInterface;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;

public class CounterStateChangedExecutor implements EventHandlerInterface,
MovableInterface, BindingController, LifeCycleController, InitInterface {

	private Component myself;
	private NicheActuatorInterface actuator;
//	private DeploySensorsInterface deploySensor;
	private DeployActuatorsInterface deployActuator;
	private TriggerInterface trigger;
	
	

	private boolean status;

	@Override
	public void eventHandler(Serializable event, int flag) {
		if (event instanceof InformOutOfSyncEvent) {
			int value = ((InformOutOfSyncEvent)event).getCounterNumber();
			//ComponentId cid = ((CounterChangedEvent)event).getCid();
			System.out.println("[executor] InformOutOfSyncEvent received. Value: "+value);
			trigger.trigger(new ComponentOutOfSyncEvent(value));
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
				.equals(FractalInterfaceNames.DEPLOY_ACTUATOR_CLIENT_INTERFACE))
			deployActuator = (DeployActuatorsInterface) itfValue;
		else if (itfName
				.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			trigger = (TriggerInterface) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String[] listFc() {
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.DEPLOY_ACTUATOR_CLIENT_INTERFACE,
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
				.equals(FractalInterfaceNames.DEPLOY_ACTUATOR_CLIENT_INTERFACE))
			return deployActuator;
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
				.equals(FractalInterfaceNames.DEPLOY_ACTUATOR_CLIENT_INTERFACE))
			deployActuator = null;
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
		initSmth() ;
	}

	@Override
	public void init(NicheActuatorInterface actuator) {
		initSmth() ;
	}

	@Override
	public void initId(NicheId id) {
		initSmth() ;
	}

	@Override
	public void reinit(Serializable[] parameters) {
		initSmth() ;
	}

	private void initSmth() {
		System.out.println("[executor]> Initialize");
		Serializable[] actuatorParameters = new Serializable[2];

//		deploySensor.deploySensor(CounterStatusSensor.class.getName(),
//				CounterChangedEvent.class.getName(), sensorParameters, null,
//				new String[] { "counterStatus" });
		deployActuator.deployActuator(CounterStatusActuator.class.getName(), 
				ComponentOutOfSyncEvent.class.getName(), actuatorParameters, new String[] { "counterResync" }, null);
	}

}
