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

package org.objectweb.jasmine.jade.service.resourcediscoverybackend;

import dks.niche.NicheOSSupport;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.deployment.local.api.GenericInstallingFactory;
import org.objectweb.fractal.deployment.local.api.Installer;
import org.objectweb.fractal.deployment.local.api.Loader;
import org.objectweb.fractal.deployment.local.api.PackageDescription;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.service.nodemanager.NodeManager;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.JadeException;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;
import org.objectweb.jasmine.jade.util.Serialization;

import dks.niche.NicheOSSupport;

/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos
 *         Parlavantzas
 * 
 */
public class ResourceDiscoveryBackEndImpl implements LifeCycleController,
		BindingController, ResourceDiscoveryBackEnd {

	private boolean started;

	private final String[] bindingList = { "overlayAccess",
			"nodeManager" };

	private Component myself;

	private OverlayAccess overlayAccess;

	private NodeManager nodeManager;

	public String jdName;

	private Component jadeNode;

	// private int myStorage

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public ResourceDiscoveryBackEndImpl() throws JadeException {

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
	 * @see org.objectweb.fractal.api.control.LifeCycleController#startFc()
	 */
	public void startFc() throws IllegalLifeCycleException {
		if (!started) {
			overlayAccess.getOverlay().registerResourceEnquiryHandler(this);
			started = true;

			// Stores reference to enclosing node and its name
			try {
				jadeNode = FractalUtil
						.getFirstFoundSuperComponentByServerInterfaceSignature(
								myself,
								"org.objectweb.fractal.deployment.local.api.Installer");
				jdName = Fractal.getNameController(jadeNode).getFcName();
			} catch (NoSuchComponentException e) {
				e.printStackTrace();
			} catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}

			Logger.println("[ResourceDiscoveryBackEnd] started");
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

	/**
	 * Receives discovery upcall from Niche
	 * 
	 * @param requirements
	 * @return
	 */
	public Object[] resourceEnquiry(Object requirements) {

		int result;

		if (((String)requirements).startsWith("(")) {
			//LDAP filter syntax
			result = nodeManager.satisfies((String) requirements) ? 1 : 0;
		} else
			result = overlayAccess.getOverlay().getResourceManager()
					.checkFreeSpace((String) requirements);
		if (result > 0) {
			return new Object[] { jdName, result, jadeNode };
		} else
			return new Object[] { null, 0 };

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
	 * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang
	 *      .String)
	 */
	public Object lookupFc(String itfName) throws NoSuchInterfaceException {
		if (itfName.equals("component"))
			return myself;
		else if (itfName.equals("overlayAccess"))
			return overlayAccess;
		else if (itfName.equals("nodeManager"))
			return nodeManager;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.
	 *      String, java.lang.Object)
	 */
	public void bindFc(String itfName, Object itfValue)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if (itfName.equals("component"))
			myself = (Component) itfValue;
		else if (itfName.equals("overlayAccess"))
			overlayAccess = (OverlayAccess) itfValue;
		else if (itfName.equals("nodeManager"))
			nodeManager = (NodeManager) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang
	 *      .String)
	 */
	public void unbindFc(String itfName) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if (itfName.equals("component"))
			myself = null;
		else if (itfName.equals("overlayAccess"))
			overlayAccess = null;
		else if (itfName.equals("nodeManager"))
			nodeManager = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	// ------------------------------------------------------------------------
	// Private Methods
	// ------------------------------------------------------------------------

}
