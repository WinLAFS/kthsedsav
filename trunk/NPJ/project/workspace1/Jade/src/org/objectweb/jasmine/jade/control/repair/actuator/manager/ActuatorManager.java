/**
 * Jade_self-healing
 * fr.jade.service.repair.actuator.manager
 * ActuatorManager.java
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * Nov 29, 2006
 */
package org.objectweb.jasmine.jade.control.repair.actuator.manager;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.jasmine.jade.control.repair.actuator.reifier.Reifier;

import org.objectweb.jasmine.jade.control.repair.util.event.Event;
import org.objectweb.jasmine.jade.control.repair.util.event.EventBody;
import org.objectweb.jasmine.jade.control.repair.util.event.EventHandler;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class ActuatorManager implements BindingController, EventHandler {

    /**
     * 
     */
    private Reifier reifier;

    // ------------------------------------------------------------------------
    // Implementation of BindingController interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String,
     *      java.lang.Object)
     */
    public void bindFc(String clientItfName, Object serverItf)
            throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {

        if (clientItfName.equals("reifier"))
            reifier = (Reifier) serverItf;
        else
            throw new NoSuchInterfaceException(clientItfName);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        return new String[] { "reifier" };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItfName)
            throws NoSuchInterfaceException {
        if (clientItfName.equals("reifier"))
            return reifier;
        else
            throw new NoSuchInterfaceException(clientItfName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals("reifier"))
            reifier = null;
        else
            throw new NoSuchInterfaceException(clientItfName);
    }

    // ------------------------------------------------------------------------
    // Implementation of EventHandler interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see fr.jade.service.repair.util.event.EventHandler#handleEvent(fr.jade.service.repair.util.event.Event)
     */
    public void handleEvent(Event e) throws Exception {

        EventBody eb = e.getEventBody();
        Component newNode_M = (Component)eb.get("newNode_M");
        reifier.propagate(newNode_M);
        
    }

}
