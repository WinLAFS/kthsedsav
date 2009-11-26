package counter.executors;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import counter.actuators.CounterStatusActuator;
import counter.events.ComponentOutOfSyncEvent;
import counter.events.InformOutOfSyncEvent;
import counter.managers.ConfigurationManager;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.DeployActuatorsInterface;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;

/**
 * Executor handles {@link InformOutOfSyncEvent} triggered by
 * {@link ConfigurationManager} and then triggers
 * {@link ComponentOutOfSyncEvent} to {@link CounterStatusActuator} actuator.
 * Executor also deploys actuators {@link CounterStatusActuator}
 * 
 */
public class CounterStateChangedExecutor implements EventHandlerInterface,
		MovableInterface, BindingController, LifeCycleController, InitInterface {

	private Component myself;
	private NicheActuatorInterface actuator;
	private DeployActuatorsInterface deployActuator;
	private TriggerInterface trigger;

	private boolean status;

	/**
	 * The method handles {@link InformOutOfSyncEvent}
	 * 
	 * @see dks.niche.fractal.interfaces.EventHandlerInterface#eventHandler(java.io.Serializable,
	 *      int)
	 */
	public void eventHandler(Serializable event, int flag) {
		if (event instanceof InformOutOfSyncEvent) {
			int value = ((InformOutOfSyncEvent) event).getCounterNumber();
			int lamport = ((InformOutOfSyncEvent) event).getLamport();
			trigger.trigger(new ComponentOutOfSyncEvent(value, lamport));
		}

	}

	/**
	 * @see dks.niche.fractal.interfaces.MovableInterface#getAttributes()
	 */
	public Serializable[] getAttributes() {
		return new Serializable[] {};
	}

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String,
	 *      java.lang.Object)
	 */
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
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			trigger = (TriggerInterface) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#listFc()
	 */
	public String[] listFc() {
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.DEPLOY_ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE };
	}

	/**
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
				.equals(FractalInterfaceNames.DEPLOY_ACTUATOR_CLIENT_INTERFACE))
			return deployActuator;
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			return trigger;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
	 */
	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals(FractalInterfaceNames.COMPONENT))
			myself = null;
		else if (itfName
				.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			actuator = null;
		else if (itfName
				.equals(FractalInterfaceNames.DEPLOY_ACTUATOR_CLIENT_INTERFACE))
			deployActuator = null;
		else if (itfName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			trigger = null;

		else
			throw new NoSuchInterfaceException(itfName);
	}

	/**
	 * @see org.objectweb.fractal.api.control.LifeCycleController#getFcState()
	 */
	public String getFcState() {
		return status ? "STARTED" : "STOPPED";
	}

	/**
	 * @see org.objectweb.fractal.api.control.LifeCycleController#startFc()
	 */
	public void startFc() throws IllegalLifeCycleException {

		status = true;
	}

	/**
	 * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
	 */
	public void stopFc() throws IllegalLifeCycleException {
		status = false;

	}

	/**
	 * @see dks.niche.fractal.interfaces.InitInterface#init(java.io.Serializable[])
	 */
	public void init(Serializable[] parameters) {
		initActuators();
	}

	/**
	 * @see dks.niche.fractal.interfaces.InitInterface#init(dks.niche.interfaces.NicheActuatorInterface)
	 */
	public void init(NicheActuatorInterface actuator) {
		initActuators();
	}

	/**
	 * @see dks.niche.fractal.interfaces.InitInterface#initId(dks.niche.ids.NicheId)
	 */
	public void initId(NicheId id) {
		initActuators();
	}

	/**
	 * @see dks.niche.fractal.interfaces.InitInterface#reinit(java.io.Serializable[])
	 */
	public void reinit(Serializable[] parameters) {
		initActuators();
	}

	/**
	 * The method deploys actuators {@link CounterStatusActuator}. Actuators are
	 * sybscribed to {@link ComponentOutOfSyncEvent}
	 */
	private void initActuators() {
		System.out.println("[executor]> Initialize");
		Serializable[] actuatorParameters = new Serializable[2];
		deployActuator.deployActuator(CounterStatusActuator.class.getName(),
				ComponentOutOfSyncEvent.class.getName(), actuatorParameters,
				new String[] { "counterResync" }, null);
	}

}
