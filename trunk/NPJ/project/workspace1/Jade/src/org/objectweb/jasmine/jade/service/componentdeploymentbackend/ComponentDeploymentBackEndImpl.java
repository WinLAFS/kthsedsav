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

package org.objectweb.jasmine.jade.service.componentdeploymentbackend;

// import dks.niche.JadeDeploymentInterface;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.deployment.local.api.GenericInstallingFactory;
import org.objectweb.fractal.deployment.local.api.PackageDescription;
import org.objectweb.fractal.rmi.io.Ref;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.componentdeploymentbackend.DeployedComponents;
import org.objectweb.jasmine.jade.service.componentdeployment.DeploymentParams;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.service.nodemanager.NodeManager;
import org.objectweb.jasmine.jade.util.DebugAdl;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.JadeException;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;
import org.objectweb.jasmine.jade.util.RmiRefManagement;
import org.objectweb.jasmine.jade.util.Serialization;

import dks.niche.NicheOSSupport;
import dks.niche.ids.ComponentId;
import dks.niche.interfaces.NicheAsynchronousInterface;

/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos
 *         Parlavantzas
 * 
 */
public class ComponentDeploymentBackEndImpl implements LifeCycleController,
		BindingController, ComponentDeploymentBackEnd, DeployedComponents {

	private boolean started;

	private final String[] bindingList = { "overlayAccess", "nodeManager" };

	private Component myself;

	private OverlayAccess overlay;

	private NodeManager nodeManager;

	private NicheOSSupport niche;

	// private NicheAsynchronousInterface logger;

	private Map<String, Component> deployed;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public ComponentDeploymentBackEndImpl() throws JadeException {
		deployed = new HashMap<String, Component>();
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

			niche = overlay.getOverlay();
			niche.registerDeploymentHandler(this);
			niche.registerAllocationHandler(this);

			started = true;
			Logger.println("[ComponentDeploymentBackEnd] started");
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

	public Component doDeploy(Object type, String name, String definition,
			Object controllerDesc, Object contentDesc, Object[] packageDesc) {

		Component res = null;
		Type fType = (Type) type;

		Component jadeNode = null;
		try {
			jadeNode = FractalUtil
					.getFirstFoundSuperComponentByServerInterfaceSignature(
							myself,
							"org.objectweb.fractal.deployment.local.api.GenericInstallingFactory");
		} catch (NoSuchComponentException e1) {
			e1.printStackTrace();
		}
		GenericInstallingFactory physicalNodeFactoryItf = null;
		try {
			physicalNodeFactoryItf = (GenericInstallingFactory) jadeNode
					.getFcInterface("generic-installing-factory");
		} catch (NoSuchInterfaceException e1) {
			e1.printStackTrace();
		}

		try {
			if (packageDesc == null) {
				res = physicalNodeFactoryItf.newFcInstance(fType,
						controllerDesc, contentDesc);
			} else {
				res = physicalNodeFactoryItf.newFcInstance(fType,
						controllerDesc, contentDesc,
						(PackageDescription) packageDesc[0]);
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		/*
		 * Configure the component
		 */
		try {
			Fractal.getNameController(res).setFcName(name);
		} catch (NoSuchInterfaceException ignored) {
		}

		/*
		 * add the component created as a sub-component of the component
		 * managed_resources of the jade node.
		 */

		Component comps[];
		try {
			comps = Fractal.getContentController(jadeNode).getFcSubComponents();
			for (Component c : comps) {
				if (Fractal.getNameController(c).getFcName().equals(
						"managed_resources")) {
					Fractal.getContentController(c).addFcSubComponent(res);
				}
			}

		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		} catch (IllegalContentException e) {
			e.printStackTrace();
		} catch (IllegalLifeCycleException e) {
			e.printStackTrace();
		}

		// Logger.println(DebugService.on, "[ComponentDeploymentBackEnd] ");
		// Logger.println(DebugAdl.info, "created component \"" + name);
		// logger.log("[ComponentDeploymentBackEnd] created component "+ name);

		return res;
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
		else if (itfName.equals("nodeManager"))
			return nodeManager;
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
		else if (itfName.equals("nodeManager"))
			nodeManager = (NodeManager) itfValue;
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
		else if (itfName.equals("nodeManager"))
			nodeManager = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public Component deployComponentBackEnd(String arg) {

		// For deployment through RMI
		DeploymentParams params = null;
		try {
			params = (DeploymentParams) Serialization.deserialize(arg);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return doDeploy(params.type, params.name, params.definition,
				params.controllerDesc, params.contentDesc, params.packageDesc);

	}

	/**
	 * Receives deployment upcall from Niche
	 * 
	 * @param arg
	 * @return
	 */
	public Object[] deploy(Object arg) {
		// Logger.println("[ComponentDeploymentBackEnd] deployment request
		// received: "+arg.getClass().getSimpleName());
		DeploymentParams params = null;
		try {
			params = (DeploymentParams) Serialization.deserialize((String) arg);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// FIXME: hack for obtaining allocation token
		// Should be explicitly passed as upcall argument
		String allocationRef = params.definition;

		if (allocationRef == null) {
			Logger
					.println("[ComponentDeploymentBackEnd] Cannot deploy because of lack of resources. Current node properties:\n"+nodeManager.getPropertiesAsString());
			// FIXME: What is the return value for error?
			return new Object[] { null, null, null };
		}

		Component c = doDeploy(params.type, params.name, params.definition,
				params.controllerDesc, params.contentDesc, params.packageDesc);

		// System.out.println("[ComponentDeploymentBackEnd] says: params are:
		// "+params.type+" "+ params.name+" "+ params.definition+" "+
		// params.controllerDesc+" "+ params.contentDesc+" "+
		// params.packageDesc);

		Ref ref = RmiRefManagement.generateRef(c);
		try {
			this.deployed.put(Serialization.serialize(ref), c);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Object[] res = { ref, 1, c }; // Was: Object[] res={ref, 1};

		return res;

	}

	public Component getDeployedComponent(String localCID) {
		return this.deployed.get(localCID);
	}

	// {LRID, result} allocate(Object requirements) !!
	/**
	 * Receives allocation upcall from Niche
	 * 
	 * @param requirements
	 * @return
	 */
	public Object[] allocate(Object allocationProperties) {

		if ("__GET__".equals(allocationProperties)){
			// properties query
			Map properties=nodeManager.getProperties();
			return new Object[] {properties, ""};
		}
		String allocationRef = nodeManager.allocate((String) allocationProperties);
		return new Object[] { allocationRef, "" };

	}

}
