package counter.actuators;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import counter.events.ComponentOutOfSyncEvent;
import counter.events.CounterChangedEvent;
import counter.events.SynchronizeCompononentEvent;
import counter.interfaces.CounterResyncInterface;
import counter.interfaces.CounterStatusInterface;
import counter.interfaces.SynchronizeInterface;
import dks.niche.fractal.interfaces.ActuatorInitInterface;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.SensorInitInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;

public class CounterStatusActuator implements ActuatorInitInterface,
		BindingController, LifeCycleController, EventHandlerInterface {


	// ///////////////
	Component mySelf;
	private boolean status;

	// Local variables
	private NicheId myId;
	private ComponentId myComponentId;
	private NicheActuatorInterface myNicheActuatorInterface;
	
	private CounterResyncInterface counterResync;

	public CounterStatusActuator() {
		mySelf = null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Server Interfaces
	// ///////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	// ////// InitInterface
	public void init(Object[] parameters) {
		// delta = (Integer)parameters[0];
		// log("$$$$$$$$$$$$ LoadSensor: delta = " + delta);

	}

	public void init(NicheActuatorInterface actuator) {
		myNicheActuatorInterface = actuator;
	}

	public void initId(Object id) {
		myId = (NicheId) id;
	}

	public void initComponentId(ComponentId cid) {
		myComponentId = cid;

	}

	public void reinit(Object[] applicationParameters) {
	}

//	public void informCounterValue(ComponentId cid, int value) {
//		this.trigger.trigger(new CounterChangedEvent(cid, value));
//		
//	}

	// //////////////////////////////////////////////////////////////////////////
	// /////////////////////////// Attributes
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////


	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public String[] listFc() {

		return new String[] { "component", "counterResync" };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {

		if (itfName.equals("component"))
			return mySelf;
		else if (itfName.equals("counterResync")) {
			return counterResync;
		}
		else
			throw new NoSuchInterfaceException(itfName);

	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		if (itfName.equals("component"))
			mySelf = (Component) itfValue;
		else if (itfName.equals("counterResync")) {
			counterResync = (CounterResyncInterface) itfValue;
		}
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals("component"))
			mySelf = null;
		else if (itfName.equals("counterResync")) {
			counterResync = null;
		}
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
		
	}

	@Override
	public void initId(NicheId id) {
		
	}

	@Override
	public void reinit(Serializable[] parameters) {
		
	}

	@Override
	public void eventHandler(Serializable event, int flag) {
		if (event instanceof ComponentOutOfSyncEvent) {
			ComponentOutOfSyncEvent evt = (ComponentOutOfSyncEvent) event;
			System.out.println("[actuator]> ComponentOutOfSyncEvent received. Value: " + evt.getCounterNumber());
			counterResync.reSynchronize(evt.getCounterNumber());
		}
		else {
			System.out.println("[actuator]> uknown event: " + event.getClass().getName());
		}
		
	}

}