/**
 * Copyright (C) : INRIA - Domaine de Voluceau, Rocquencourt, B.P. 105, 
 * 78153 Le Chesnay Cedex - France 
 * 
 * contributor(s) : SARDES project - http://sardes.inrialpes.fr
 *
 * Contact : jade@inrialpes.fr
 *
 * This software is a computer program whose purpose is to provide a framework
 * to build autonomic systems, following an architecture-based approach.
 *
 * This software is governed by the CeCILL-C license under French law and 
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as 
 * circulated by CEA, CNRS and INRIA at the following URL 
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a 
 * limited warranty and the software's author, the holder of the economic 
 * rights, and the successive licensors have only limited liability. 
 *
 * In this respect, the user's attention is drawn to the risks associated with 
 * loading,  using,  modifying and/or developing or reproducing the software by 
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate,  and  that  also therefore means  that it is
 * reserved for developers  and  experienced professionals having in-depth 
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling 
 * the security of their systems and/or data to be ensured and,  more generally,
 * to use and operate it in the same conditions as regards security. 
 *
 * The fact that you are presently reading this means that you have had 
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package org.objectweb.jasmine.jade.service.nodediscovery.listener;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.jasmine.jade.service.nodediscovery.analyser.NodeDiscoveryAnalyser;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.Logger;

import fr.jade.fractal.api.control.GenericAttributeController;
import fr.jade.fractal.api.control.NoSuchAttributeException;


/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 *
 */
public abstract class AbstractNodeDiscoveryListener implements
        NodeDiscoveryListener, LifeCycleController, BindingController,
        GenericAttributeController, Runnable {

    /**
     * Default Discovery service host
     */
    public final static String DEFAULT_DISCOVERY_SERVICE_HOST = "localhost";
    /**
     * Default Discovery service host
     */
    public final static String DEFAULT_DISCOVERY_SERVICE_PORT = "9998";
    /**
     * 
     */
    protected boolean started = false;
    /**
     * 
     */
    final static String[] attList = { "port" };
    /**
     * 
     */
    protected String port = "9999";
    /**
     * 
     */
    private final static String[] bindingList = { "analyser", "registry" };
    /**
     * 
     */
    protected NodeDiscoveryAnalyser analyser;
    /**
     * 
     */
    protected NamingService registry;
    /**
     * 
     */
    protected Thread thread = null;

    // ------------------------------------------------------------------------
    // Implementation of LifeCycleController interface
    // ------------------------------------------------------------------------

    public String getFcState() {
        if (started)
            return LifeCycleController.STARTED;
        return LifeCycleController.STOPPED;

    }
    
    public void startFc() throws IllegalLifeCycleException {
        if (!started) {
            thread = new Thread(this, "NodeDiscoveryListener");
            thread.setDaemon(true);
            thread.start();
            started = true;
            Logger.println(DebugService.info, "[NodeDiscovery service] started");
        } else
            throw new IllegalLifeCycleException(
                    "NodeDiscoveryListener already started");
    }

    public void stopFc() throws IllegalLifeCycleException {
        if (started) {
            thread.interrupt();
            started = false;
            Logger.println(DebugService.info, "[NodeDiscovery service] stopped");
        }
    }

    // ------------------------------------------------------------------------
    // Implementation of BindingController interface
    // ------------------------------------------------------------------------

    public String[] listFc() {
        return bindingList;
    }

    public Object lookupFc(String itfName) throws NoSuchInterfaceException {
        if (itfName.equals("analyser"))
            return analyser;
        if (itfName.equals("registry"))
            return registry;
        throw new NoSuchInterfaceException(itfName);
    }

    public void bindFc(String itfName, Object itfValue)
            throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (started) {
            throw new IllegalLifeCycleException(
                    "Component NodeDiscovery Started");
        }
        if (itfName.equals("analyser"))
            analyser = (NodeDiscoveryAnalyser) itfValue;
        else if (itfName.equals("registry"))
            registry = (NamingService) itfValue;
        else
            throw new NoSuchInterfaceException(itfName);

    }

    public void unbindFc(String itfName) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (started) {
            throw new IllegalLifeCycleException(
                    "Component NodeDiscovery Started");
        }
        if (itfName.equals("analyser"))
            analyser = null;
        else if (itfName.equals("registry"))
            registry = null;
        else
            throw new NoSuchInterfaceException(itfName);
    }

    // ------------------------------------------------------------------------
    // Implementation of GenericAttributeController interface
    // ------------------------------------------------------------------------

    public String getAttribute(String name) throws NoSuchAttributeException {
        if (name.equals("port"))
            return port;
        throw new NoSuchAttributeException(name);
    }

    public void setAttribute(String name, String value)
            throws NoSuchAttributeException {
        if (name.equals("port"))
            port = value;
        else
            throw new NoSuchAttributeException(name);
    }

    public String[] listFcAtt() {
        return attList;
    }
}
