package counter.sensors;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import counter.events.CounterChangedEvent;
import counter.interfaces.CounterStatusInterface;
import dks.niche.fractal.interfaces.SensorInitInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;

/**
 * The sensor management elements that are responsible to "get" (get notified)
 * the value of the counter from the component each time it is increased and 
 * trigger a {@link CounterChangedEvent} in order to inform the watchers for
 * this change. They are informed about counter value by the {@link CounterStatusInterface}.
 *
 */
public class CounterStatusSensor implements SensorInitInterface,
		CounterStatusInterface, BindingController, LifeCycleController {

	TriggerInterface trigger; //the interface used to trigger event to watcher

	Component mySelf;
	private boolean status;

	private NicheId myId;
	private ComponentId myComponentId;
	private NicheActuatorInterface myNicheActuatorInterface;
	private NicheAsynchronousInterface logger;

	/**
	 * Default constructor.
	 */
	public CounterStatusSensor() {
		mySelf = null;
		trigger = null;
	}

	// //////////////////////// Server Interfaces

	/**
	 * @param parameters
	 */
	public void init(Object[] parameters) {

	}

	/**
	 * @see dks.niche.fractal.interfaces.InitInterface#init(dks.niche.interfaces.NicheActuatorInterface)
	 */
	public void init(NicheActuatorInterface actuator) {
		myNicheActuatorInterface = actuator;
		logger = actuator.testingOnly();
	}

	/**
	 * @param id
	 */
	public void initId(Object id) {
		myId = (NicheId) id;
	}

	/**
	 * @see dks.niche.fractal.interfaces.SensorInitInterface#initComponentId(dks.niche.ids.ComponentId)
	 */
	public void initComponentId(ComponentId cid) {
		myComponentId = cid;

	}

	/**
	 * @param applicationParameters
	 */
	public void reinit(Object[] applicationParameters) {
	}
	
	/**
	 * @see counter.interfaces.CounterStatusInterface#informCounterValue(dks.niche.ids.ComponentId, int, int)
	 */
	public void informCounterValue(ComponentId cid, int value, int lamport) {
			this.trigger.trigger(new CounterChangedEvent(cid, value, lamport));			
	}

	// //////////////////////// Fractal Stuff

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#listFc()
	 */
	public String[] listFc() {

		return new String[] { "component", "trigger" };
	}

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
	 */
	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {

		if (itfName.equals("trigger"))
			return trigger;
		else if (itfName.equals("component"))
			return mySelf;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String, java.lang.Object)
	 */
	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		if (itfName.equals("trigger"))
			trigger = (TriggerInterface) itfValue;
		else if (itfName.equals("component"))
			mySelf = (Component) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
	 */
	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals("trigger"))
			trigger = null;
		else if (itfName.equals("component"))
			mySelf = null;
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
	}

	/**
	 * @see dks.niche.fractal.interfaces.InitInterface#initId(dks.niche.ids.NicheId)
	 */
	public void initId(NicheId id) {
	}

	/**
	 * @see dks.niche.fractal.interfaces.InitInterface#reinit(java.io.Serializable[])
	 */
	public void reinit(Serializable[] parameters) {
	}

}
