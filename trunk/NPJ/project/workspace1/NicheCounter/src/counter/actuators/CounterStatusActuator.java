package counter.actuators;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import counter.events.ComponentOutOfSyncEvent;
import counter.executors.CounterStateChangedExecutor;
import counter.interfaces.CounterResyncInterface;
import counter.service.ServiceComponent;
import dks.niche.fractal.interfaces.ActuatorInitInterface;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;

/**
 * The actuator to re-synchronize {@link ServiceComponent} components. Actuator
 * receives {@link ComponentOutOfSyncEvent} and calls
 * {@link CounterResyncInterface#reSynchronize(int, int)} method of the
 * component
 * 
 */
public class CounterStatusActuator implements ActuatorInitInterface,
		BindingController, LifeCycleController, EventHandlerInterface {

	Component mySelf;
	private boolean status;

	// Local variables
	private NicheId myId;
	private ComponentId myComponentId;
	private NicheActuatorInterface myNicheActuatorInterface;

	private CounterResyncInterface counterResync;

	/**
	 * Default constructor
	 */
	public CounterStatusActuator() {
		mySelf = null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Server Interfaces
	// ///////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

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
	}

	/**
	 * Init method
	 * 
	 * @param id
	 *            NicheId of the actuator
	 */
	public void initId(Object id) {
		myId = (NicheId) id;
	}

	/**
	 * @see dks.niche.fractal.interfaces.ActuatorInitInterface#initComponentId(dks.niche.ids.ComponentId)
	 */
	public void initComponentId(ComponentId cid) {
		myComponentId = cid;

	}

	/**
	 * @param applicationParameters
	 */
	public void reinit(Object[] applicationParameters) {
	}

	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#listFc()
	 */
	public String[] listFc() {
		return new String[] { "component", "counterResync" };
	}

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
	 */
	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {
		if (itfName.equals("component"))
			return mySelf;
		else if (itfName.equals("counterResync")) {
			return counterResync;
		} else
			throw new NoSuchInterfaceException(itfName);

	}

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String,
	 *      java.lang.Object)
	 */
	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		if (itfName.equals("component"))
			mySelf = (Component) itfValue;
		else if (itfName.equals("counterResync")) {
			counterResync = (CounterResyncInterface) itfValue;
		} else
			throw new NoSuchInterfaceException(itfName);
	}

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
	 */
	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals("component"))
			mySelf = null;
		else if (itfName.equals("counterResync")) {
			counterResync = null;
		} else
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

	/**
	 * The method handles {@link ComponentOutOfSyncEvent} event from
	 * {@link CounterStateChangedExecutor}
	 * 
	 * @see dks.niche.fractal.interfaces.EventHandlerInterface#eventHandler(java.io.Serializable,
	 *      int)
	 */
	public void eventHandler(Serializable event, int flag) {
		if (event instanceof ComponentOutOfSyncEvent) {
			ComponentOutOfSyncEvent evt = (ComponentOutOfSyncEvent) event;
			counterResync.reSynchronize(evt.getCounterNumber(), evt
					.getLamport());
		} else {
			System.out.println("[actuator]> uknown event: "
					+ event.getClass().getName());
		}

	}

}