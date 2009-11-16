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

package org.objectweb.jasmine.jade.examples.managerimpl;


import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;

import org.objectweb.jasmine.jade.service.componentdeployment.NicheIdRegistry;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.JadeException;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;

import dks.niche.ids.SNR;


/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos Parlavantzas</a>
 *
 */
public class ManagerImpl implements LifeCycleController, BindingController {

    

    private boolean started;
    
    private final String[] bindingList = {"overlayAccess","nicheIdRegistry"};

	private Component myself;
	
	private OverlayAccess overlay;
	private NicheIdRegistry nicheIdRegistry;
	

	
	// ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public ManagerImpl() throws JadeException {
        
    }
//  ------------------------------------------------------------------------
    // Implementation of LifecycleController interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.LifeCycleController#getFcState()
     */
    public String getFcState() {
        if (started)
            return LifeCycleController.STARTED;
        return LifeCycleController.STOPPED;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.LifeCycleController#startFc()
     */
    public void startFc() throws IllegalLifeCycleException {
        if (!started) {
            // verify that client interfaces are bound
            assert (overlay!=null) : "[ManagerImpl] overlay is null";
            assert (nicheIdRegistry!=null) : "[ManagerImpl] nicheIdRegistry is null";

            // place manager code here
            // for example
            // SNR tmp= nicheIdRegistry.lookup("MyHelloGroup_0/client");
            // assert (tmp!=null) : "[ManagerImpl] name: MyHelloGroup_0/client not found in registry";
            
    		started = true;            
            Logger.println("[ManagerImpl] started");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
     */
    public void stopFc() throws IllegalLifeCycleException {
        if (started) {
            started = false;
        }
    }
    
    // ------------------------------------------------------------------------
    // Implementation of BindingController interface
    // ------------------------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#listFc()
	 */
	public String[] listFc() {
		return bindingList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
	 */
	public Object lookupFc(String itfName) throws NoSuchInterfaceException {
		if (itfName.equals("component"))
			return myself;
		else if (itfName.equals("overlayAccess"))
			return overlay;
		else if (itfName.equals("nicheIdRegistry"))
			return nicheIdRegistry;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String,
	 *      java.lang.Object)
	 */
	public void bindFc(String itfName, Object itfValue)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if (itfName.equals("component"))
			myself = (Component) itfValue;
		else if (itfName.equals("overlayAccess"))
			overlay = (OverlayAccess) itfValue;
		else if (itfName.equals("nicheIdRegistry"))
			nicheIdRegistry = (NicheIdRegistry) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
	 */
	public void unbindFc(String itfName) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if (itfName.equals("component"))
			myself = null;
		else if (itfName.equals("overlayAccess"))
			overlay = null;
		else if (itfName.equals("nicheIdRegistry"))
			nicheIdRegistry = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}


	
    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------
    
}
