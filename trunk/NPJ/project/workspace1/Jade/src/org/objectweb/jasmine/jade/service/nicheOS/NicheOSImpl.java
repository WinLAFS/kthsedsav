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

package org.objectweb.jasmine.jade.service.nicheOS;
import dks.niche.NicheOSSupport;
import dks.niche.ids.ComponentId;
import dks.niche.ids.ResourceId;
import fr.jade.fractal.api.control.GenericAttributeController;
import fr.jade.fractal.api.control.NoSuchAttributeException;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import org.objectweb.fractal.rmi.io.Ref;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.jasmine.jade.util.RmiRefManagement;





/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos Parlavantzas
 * 
 */
public class NicheOSImpl implements LifeCycleController, BindingController,
OverlayAccess, GenericAttributeController{


	/**
	 * 
	 */
	private boolean started = false;

	/**
	 * 
	 */
	private final String[] bindingList = {};

	/**
	 * 
	 */
	private Component myself;

	/**
	 * 
	 */
	
	private NicheOSSupport overlay;
	
	
    final static String[] attList = { "mode" };
	
    private String mode;
    
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public NicheOSImpl() {

	}

	// ------------------------------------------------------------------------
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
		if (started) {
			throw new IllegalLifeCycleException(itfName);
		}
		if (itfName.equals("component"))
			myself = (Component) itfValue;
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
		if (started) {
			throw new IllegalLifeCycleException(itfName);
		}
		if (itfName.equals("component"))
			myself = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	    public void startFc() throws IllegalLifeCycleException {
			if (!started) {

				ClassLoader cl = Thread.currentThread()
                .getContextClassLoader();
				//needed to be able to use Class.forName(className) in the niche communication component
				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); 
				
				if (mode.equals("BOOT")) {
					overlay = new NicheOSSupport("0", 22345, NicheOSSupport.BOOT);
					overlay.boot();
					started = true;
				}
				else {
				
					Random myRandom = new Random();

					int port = 10000+(int)(15000*myRandom.nextDouble());
					overlay= new NicheOSSupport("0", port, NicheOSSupport.JOINING);

					overlay.join();
					started = true;
				}
				
				
				//try to get managed_resources and register it
				Component jadeNode = null;
		        Component comps[] = null;
				try {
					comps = Fractal.getSuperController(myself).getFcSuperComponents();
				} catch (NoSuchInterfaceException e) {
					e.printStackTrace();
				}
		        for (int i = 0; i < comps.length; i++) {
					try {
						if (Fractal.getNameController(comps[i]).getFcName().equals("managed_resources")) {
							jadeNode = comps[i];
							break;
						}
					} catch (NoSuchInterfaceException e) {
						e.printStackTrace();
					};
				}
		        
		        // done registering managed_resources
				
				overlay.getResourceManager().registerManagedResourcesComponent(jadeNode);
				overlay.getResourceManager().registerNicheComponent(myself);
				Logger.println("[NicheOS] started");
			}
}

		public NicheOSSupport getOverlay() {
			
			return overlay;
		}
		
		 public String getAttribute(String name) throws NoSuchAttributeException {
		        if (name.equals("mode"))
		            return mode;
		        throw new NoSuchAttributeException(name);
		    }

		    public void setAttribute(String name, String value)
		            throws NoSuchAttributeException {
		        if (name.equals("mode")) {
		        	if (!(value.equals("JOIN") || value.equals("BOOT")))
		        		throw new RuntimeException("Illegal value for mode attribute");
		        	mode = value;
		        }
		        else
		            throw new NoSuchAttributeException(name);
		    }

		    public String[] listFcAtt() {
		        return attList;
		    }
}
