package counter.frontend;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import counter.interfaces.CounterInterface;
import counter.service.ServiceComponent;

/**
 * The front end component of the application. It has a {@link CounterInterface}
 * as a client interface and use it to make calls to {@link ServiceComponent} At
 * startup {@link FrontendComponent}. creates {@link UserInterface}.
 * 
 */
public class FrontendComponent implements BindingController,
		LifeCycleController {

	// Client Interfaces
	private CounterInterface counter;

	// The Niche reference to this component.
	private Component myself;

	// Is this component active or not?
	private boolean status;

	// ///////////////////////////////////////////////////////////////
	// ///////////////// Called from the user interface. /////////////
	// ///////////////////////////////////////////////////////////////

	/**
	 * The method calls increaseCounter method of the client {@link CounterInterface}
	 * 
	 * @param a
	 */
	public synchronized void increaseCounter(int a) {
		counter.inreaseCounter(a);
	}

	// ////////////////////////////////////////////////////////////////////////
	// ////////////////////////// Fractal Stuff ///////////////////////////////
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#listFc()
	 */
	public String[] listFc() {
		return new String[] { "component", "counter" };
	}

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
	 */
	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {
		if (itfName.equals("counter")) {
			return counter;
		} else if (itfName.equals("component")) {
			return myself;
		} else {
			throw new NoSuchInterfaceException(itfName);
		}
	}

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String, java.lang.Object)
	 */
	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		System.out.println(">> >>Binding " + itfName + " : null ? "
				+ (itfValue == null));
		System.out.println(">> >>Value: " + (itfValue.toString()));
		if (itfName.equals("component")) {
			myself = (Component) itfValue;
		} else if (itfName.equals("counter")) {
			counter = (CounterInterface) itfValue;
		} else {
			throw new NoSuchInterfaceException(itfName);
		}
	}

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
	 */
	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals("component")) {
			myself = null;
		} else if (itfName.equals("counter")) {
			counter = null;
		} else {
			throw new NoSuchInterfaceException(itfName);
		}
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
		// Create the GUI.
		new UserInterface(this);
		status = true;
		System.err.println("Frontend component started.");
	}

	/**
	 * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
	 */
	public void stopFc() throws IllegalLifeCycleException {
		status = false;
	}
}
