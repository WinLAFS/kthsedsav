package helloworld.frontend;

import helloworld.interfaces.HelloAllInterface;
import helloworld.interfaces.HelloAnyInterface;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

public class FrontendComponent implements BindingController, LifeCycleController {

    // Client Interfaces
    private HelloAnyInterface helloAny;
    private HelloAllInterface helloAll;

    // The Niche reference to this component.
    private Component myself;

    // Is this component active or not?
    private boolean status;

    // ///////////////////////////////////////////////////////////////
    // ///////////////// Called from the user interface. /////////////
    // ///////////////////////////////////////////////////////////////

    public synchronized void helloAny() {
        helloAny.helloAny("HelloWorld");
    }

    public synchronized void helloAll() {
        helloAll.helloAll("HelloWorld");
    }

    // ////////////////////////////////////////////////////////////////////////
    // ////////////////////////// Fractal Stuff ///////////////////////////////
    // ////////////////////////////////////////////////////////////////////////

    public String[] listFc() {
        return new String[] { "component", "helloAny", "helloAll" };
    }

    public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
        if (itfName.equals("helloAny")) {
            return helloAny;
        } else if (itfName.equals("helloAll")) {
            return helloAll;
        } else if (itfName.equals("component")) {
            return myself;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    public void bindFc(final String itfName, final Object itfValue) throws NoSuchInterfaceException {
        if (itfName.equals("helloAny")) {
            helloAny = (HelloAnyInterface) itfValue;
        } else if (itfName.equals("helloAll")) {
            helloAll = (HelloAllInterface) itfValue;
        } else if (itfName.equals("component")) {
            myself = (Component) itfValue;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    public void unbindFc(final String itfName) throws NoSuchInterfaceException {
        if (itfName.equals("helloAny")) {
            helloAny = null;
        } else if (itfName.equals("helloAll")) {
            helloAll = null;
        } else if (itfName.equals("component")) {
            myself = null;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    public String getFcState() {
        return status ? "STARTED" : "STOPPED";
    }

    public void startFc() throws IllegalLifeCycleException {
        // Create the GUI.
        new UserInterface(this);
        status = true;
        System.err.println("Frontend component started.");
    }

    public void stopFc() throws IllegalLifeCycleException {
        status = false;
    }
}
