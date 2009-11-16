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

package org.objectweb.jasmine.jade.service.resourcediscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.jasmine.jade.service.allocator.Allocator;
import org.objectweb.jasmine.jade.service.allocator.NodeNotFoundException;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.service.resourcediscoverybackend.ResourceDiscoveryBackEnd;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;
import org.objectweb.jasmine.jade.util.UtilityComponentFactory;

import dks.niche.ids.ResourceId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.wrappers.NodeRef;
import eu.grid4all.service.reservation.ResourceSpec;

/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos
 *         Parlavantzas
 * 
 */
public class ResourceDiscoveryImpl implements LifeCycleController,
		BindingController, ResourceDiscovery {

	/**
	 * 
	 */
	private boolean started = false;

	/**
	 * 
	 */
	private final String[] bindingList = { "overlayAccess" };

	/**
	 * 
	 */
	private Component myself;

	/**
	 * 
	 */
	private OverlayAccess overlayAccess;

	private ResourceSpec resourceSpec;

	private NicheActuatorInterface niche;

	/**
	 * 
	 */
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	public ResourceDiscoveryImpl() {
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

			started = true;
			niche = overlayAccess.getOverlay().getJadeSupport();
			Logger.println("[ResourceDiscovery] started");

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
			return overlayAccess;
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
		else if (itfName.equals("overlayAccess"))
			overlayAccess = (OverlayAccess) itfValue;
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
		} else if (itfName.equals("component"))
			myself = null;
		else if (itfName.equals("overlayAccess"))
			overlayAccess = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.jasmine.jade.service.resourcediscovery.ResourceDiscovery#discoverAllocate(java.lang.String)
	 */
	public Component discover(String requirements, String virtualNodeAllocationProperties, String virtualNodeCardinality) {

		Component result = null;
		int cardinality = 0;
		
		// if there are no requirements, any node can be selected
		if ((requirements == null) || (requirements.equals("")))
			requirements = "(jadeNode=*)"; 
		
		ArrayList<Object[]> discovered = niche.discover(requirements);
		int count = discovered.size();
		
		if ((virtualNodeCardinality==null) || (virtualNodeCardinality.equals(""))) {
			cardinality=1;
		} else if (virtualNodeCardinality.equals("ALL"))	{
			cardinality=count;
		} else {	
			cardinality = Integer.parseInt(virtualNodeCardinality);
		}
		
		if ((count==0) || (count<cardinality)) {
			Logger.println(DebugService.info,
					"[ResourceDiscovery] Did not find resources for requirements: "+requirements);
			return null;
		}
		ArrayList<NodeRef> nodes = new ArrayList<NodeRef>();
		for (int i = 0; i<cardinality; i++) {
			nodes.add((NodeRef) ((discovered.get(i))[1]));
		}
		Map<String,Object> map = new Hashtable<String, Object>();
		
		if (virtualNodeAllocationProperties!=null)
			map.put("Allocation properties", virtualNodeAllocationProperties);
		map.put("NodeRefs", nodes);
		result = UtilityComponentFactory.createHolderComponent(map);
		
		return result;

	}
	

}