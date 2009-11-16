/**
 * Jade_self-healing
 * fr.jade.service.repair.actuator.reifier
 * AbstractReifier.java
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * Nov 28, 2006
 */
package org.objectweb.jasmine.jade.control.repair.actuator.reifier;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 *
 */
public abstract class AbstractReifier implements Reifier, BindingController {

    /**
     * 
     */
    protected Reifier clientReifier;
    
    // ------------------------------------------------------------------------
    // Implementation of BindingController interface
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String, java.lang.Object)
     */
    public void bindFc(String clientItfName, Object serverItf)
            throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {

        if (clientItfName.equals("client-reifier"))
            clientReifier = (Reifier) serverItf;
        else
            throw new NoSuchInterfaceException(clientItfName);
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        return new String[]{"client-reifier"};
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.equals("client-reifier"))
            return clientReifier;
        else
            throw new NoSuchInterfaceException(clientItfName);
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {

        if (clientItfName.equals("client-reifier"))
            clientReifier = null;
        else
            throw new NoSuchInterfaceException(clientItfName);
    }

}
