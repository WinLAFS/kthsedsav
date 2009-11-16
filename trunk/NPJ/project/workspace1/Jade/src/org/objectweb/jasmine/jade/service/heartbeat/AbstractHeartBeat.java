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

package org.objectweb.jasmine.jade.service.heartbeat;

import java.net.UnknownHostException;

import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.Logger;

import fr.jade.fractal.api.control.GenericAttributeController;
import fr.jade.fractal.api.control.NoSuchAttributeException;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public abstract class AbstractHeartBeat implements LifeCycleController,
        GenericAttributeController, Runnable {

	static final boolean JADE_HEARTBEAT = 
		System.getProperty("jade.heartbeat") instanceof String ?
				System.getProperty("jade.heartbeat").equals("1")
			:
				false;

    /**
     * 
     */
    private boolean started = false;

    /**
     * 
     */
    private boolean firstStart = true;

    /**
     * 
     */
    private Thread thread;

    /**
     * 
     */
    final static String[] attList = { "pulsePeriodInSec", "discoveryHost",
            "discoveryPort", "nodeName" };

    /**
     * 
     */
    protected String pulsePeriodInSec = "2";

    /**
     * 
     */
    protected String discoveryHost = "localhost";

    /**
     * 
     */
    protected String discoveryPort = "9998";

    /**
     * 
     */
    protected String nodeName = "PULSE";

    // ------------------------------------------------------------------------
    // Abstract Methods
    // ------------------------------------------------------------------------

    /**
     * initialize the connection beetween the HeartBeat and the discovery
     * service
     * 
     * @throws SocketExceptionU
     * @throws UnknownHostException
     *             nknownHostException
     */
    protected abstract void init() throws Exception;

    /**
     * send the heartbeat
     * 
     * @throws Exception
     */
    protected abstract void pulse() throws Exception;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public AbstractHeartBeat() {

    }

    // ------------------------------------------------------------------------
    // Implementation of LifeCycleController interface
    // ------------------------------------------------------------------------

    public String getFcState() {
        if (started)
            return LifeCycleController.STARTED;
        return LifeCycleController.STOPPED;
    }

    public void startFc() throws IllegalLifeCycleException {

    	if(JADE_HEARTBEAT) {
        if (firstStart) {
            try {
                this.init();
                firstStart = false;
            } catch (Exception e) {
                throw new IllegalLifeCycleException(
                        "[Hearbeat] Unable to start the component : error during connection to discovery service");
            }
        }

        if (!started) {
            thread = new Thread(this, "HeartBeat");
            thread.setDaemon(true);
            thread.start();
            started = true;
            Logger.println(DebugService.info, "[Heartbeat] started");
            
            //Do a couple of pulses just in case..
            for (int i=1; i<=3; i++) {
            	try {
            		pulse();
            	} catch (Exception e) {
            		System.out.println("[HeartBeat] Error during sending heartbeat : "
            				+ e.getLocalizedMessage());
            	}
            }
        	
        }
    	}
    }

    public void stopFc() throws IllegalLifeCycleException {
        if (started) {
            thread.interrupt();
            started = false;
            
            Logger.println(DebugService.info, "[Heartbeat] stopped");
        }
    }

    // ------------------------------------------------------------------------
    // Implementation of GenericAttributeController interface
    // ------------------------------------------------------------------------

    public String getAttribute(String name) throws NoSuchAttributeException {
        if (name.equals("pulsePeriodInSec"))
            return pulsePeriodInSec;
        if (name.equals("discoveryHost"))
            return discoveryHost;
        if (name.equals("discoveryPort"))
            return discoveryPort;
        if (name.equals("nodeName"))
            return nodeName;
        throw new NoSuchAttributeException(name);
    }

    public void setAttribute(String name, String value)
            throws NoSuchAttributeException {
        if (name.equals("pulsePeriodInSec"))
            pulsePeriodInSec = value;
        else if (name.equals("discoveryHost"))
            discoveryHost = value;
        else if (name.equals("discoveryPort"))
            discoveryPort = value;
        else if (name.equals("nodeName"))
            nodeName = value;
        else
            throw new NoSuchAttributeException(name);

    }

    public String[] listFcAtt() {
        return attList;
    }

    // ------------------------------------------------------------------------
    // Implementation of Runnable interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
//        while (started && !thread.isInterrupted()) {
//            try {
//                try {
//                    pulse();
//                } catch (Exception e) {
//                	System.out.println("[HeartBeat] Error during sending heartbeat : "
//                                    + e.getLocalizedMessage());
//                }
//                Thread.sleep(new Integer(pulsePeriodInSec).intValue() * 1000);
//            } catch (InterruptedException x) {
//                Thread.currentThread().interrupt();
//            }
//        }
    }
}
