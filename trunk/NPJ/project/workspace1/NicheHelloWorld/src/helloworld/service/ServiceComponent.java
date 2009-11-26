package helloworld.service;

import helloworld.interfaces.HelloAllInterface;
import helloworld.interfaces.HelloAnyInterface;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

public class ServiceComponent implements HelloAnyInterface, HelloAllInterface, BindingController,
    LifeCycleController {

    private Component myself;
    private boolean status;

    public ServiceComponent() {
        System.err.println("HelloService created");
    }

    // /////////////////////////////////////////////////////////////////////
    // //////////////////////// Server interfaces //////////////////////////
    // /////////////////////////////////////////////////////////////////////

    public void helloAny(String s) {
        System.out.println(s);
    }

    public void helloAll(String s) {
        System.out.println(s);
    }

    // /////////////////////////////////////////////////////////////////////
    // //////////////////////// Fractal Stuff //////////////////////////////
    // /////////////////////////////////////////////////////////////////////

    public String[] listFc() {
        return new String[] { "component" };
    }

    public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
        if (itfName.equals("component")) {
            return myself;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    public void bindFc(final String itfName, final Object itfValue) throws NoSuchInterfaceException {
        if (itfName.equals("component")) {
            myself = (Component) itfValue;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    public void unbindFc(final String itfName) throws NoSuchInterfaceException {
        if (itfName.equals("component")) {
            myself = null;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    public String getFcState() {
        return status ? "STARTED" : "STOPPED";
    }

    public void startFc() throws IllegalLifeCycleException {
        status = true;
        System.err.println("Service component started.");
    }

    public void stopFc() throws IllegalLifeCycleException {
        status = false;
    }
}