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

package org.objectweb.jasmine.jade.service.deployer.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.rmi.RemoteException;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.rmi.registry.Registry;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.osgi.JadeNodeActivator;
import org.objectweb.jasmine.jade.osgi.JadeProperties;
import org.objectweb.jasmine.jade.service.deployer.DeployerService;
import org.objectweb.jasmine.jade.service.deployer.DeploymentException;
import org.objectweb.jasmine.jade.service.deployer.adl.classloader.UrlManager;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.JadeException;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;
import org.objectweb.jonathan.apis.binding.NamingContext;

import fr.jade.fractal.api.control.GenericAttributeController;
import fr.jade.fractal.api.control.NoSuchAttributeException;

/**
 * Implementation of deployer proxy
 * 
 * Forwards to deployer on jadeboot.
 * 
 * @author Nikos Parlavantzas
 * 
 */
public class ResourceDeployerProxyImpl implements DeployerService,
		LifeCycleController, BindingController {

	/**
	 * 
	 */
	private boolean started = false;

	/**
	 * 
	 */
	private final String[] bindingList = { "registry" };

	private NamingService registry;

	private DeployerService deployerservice = null;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public ResourceDeployerProxyImpl() {

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
			Logger.println(DebugService.info, "[Deployer] started");
		}
	}

	private void connectDeployer() {
		Component jadeboot = registry.lookup("jadeboot");
		Component deployerComp = null;
		try {
			deployerComp = FractalUtil.getSubComponentByPath(jadeboot,
					"managed_resources/resource_deployer");
			deployerservice = (DeployerService) deployerComp
					.getFcInterface("deployer_service");
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		} catch (NoSuchComponentException e) {
			e.printStackTrace();
		}
		assert (deployerservice != null) : "Cannot connect to deployer service";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
	 */
	public void stopFc() throws IllegalLifeCycleException {
		if (started) {
			started = false;
			Logger.println(DebugService.info, "[Deployer] stopped");
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

		if (itfName.equals("registry"))
			return registry;
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
		} else if (itfName.equals("registry"))
			registry = (NamingService) itfValue;
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
		} else if (itfName.equals("registry"))
			registry = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public int deploy(String adlId, Map context) throws DeploymentException {
		if (deployerservice == null)
			connectDeployer();
		return deployerservice.deploy(adlId, context);
	}

	public void listDeployment() {
		if (deployerservice == null)
			connectDeployer();
		deployerservice.listDeployment();
	}

	public void listDeploymentByAdlId(String adlId) {
		if (deployerservice == null)
			connectDeployer();
		deployerservice.listDeploymentByAdlId(adlId);
	}

	public Component start(int deploymentId) throws DeploymentException {
		if (deployerservice == null)
			connectDeployer();
		return deployerservice.start(deploymentId);
	}

	public void stop(int deploymentId) throws DeploymentException {
		if (deployerservice == null)
			connectDeployer();
		deployerservice.stop(deploymentId);

	}

	public void undeploy(int deploymentId) throws DeploymentException {
		if (deployerservice == null)
			connectDeployer();
		deployerservice.undeploy(deploymentId);

	}

}
