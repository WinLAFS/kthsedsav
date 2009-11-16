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

package org.objectweb.jasmine.jade.service.basicresourcemanagement;

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
import dks.niche.wrappers.ResourceRef;
import eu.grid4all.service.reservation.ResourceSpec;

/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos
 *         Parlavantzas
 * 
 */
public class BasicResourceManagementImpl implements LifeCycleController,
		BindingController, BasicResourceManagement {

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
	public BasicResourceManagementImpl() {
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
			Logger.println("[BasicResourceManager] started");
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
	 * @see
	 * org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang
	 * .String)
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
	 * @see
	 * org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.
	 * String, java.lang.Object)
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
	 * @see
	 * org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang
	 * .String)
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

	public List<NodeRef> discoverRSpec(ResourceSpec rspec, String memberId) {

		String requirements = rspec.getMatchString();
		ArrayList<Object[]> discovered = niche.discover(requirements);

		ArrayList<NodeRef> nodes = new ArrayList<NodeRef>();
		for (int i = 0; i < discovered.size(); i++) {
			nodes.add((NodeRef) ((discovered.get(i))[1]));
		}
		return nodes;
	}
	
	public List<NodeRef> discover(String filter, String memberId) {

		ArrayList<Object[]> discovered = niche.discover(filter);

		ArrayList<NodeRef> nodes = new ArrayList<NodeRef>();
		for (int i = 0; i < discovered.size(); i++) {
			nodes.add((NodeRef) ((discovered.get(i))[1]));
		}
		return nodes;
	}


	public Map<String, String> query(NodeRef node, String memberId) {

		// Uses special form of allocation to query resources
		String allocationProperties = "__GET__";
		ArrayList<ResourceId> result = niche.allocate(node,
				allocationProperties);
		return (Map<String, String>) result.get(0).getJadeNode();
	}


	public NodeRef discoverOne(String filter, String memberId) {

		ArrayList<Object[]> discovered = niche.discover(filter);
		if (discovered == null || discovered.size() == 0) {
			return null;
		}
		NodeRef node=(NodeRef) (discovered.get(0))[1];
		return node;
	}

	public ResourceId allocate(NodeRef node,
			Map<String, String> allocationProperties, String memberId) {
		
		// Error handling must be improved
		ArrayList<ResourceId> allocated=niche.allocate(node, allocationProperties);
		if (allocated==null || allocated.size() == 0) {
			return null;
		}
		return allocated.get(0);
	}

	public void deallocate(ResourceId resource, String memberId) {
		// TODO implement deallocation
		
	}

	public NodeRef getMyNode() {
		return niche.getResourceManager().getNodeRef();
	}
	
}