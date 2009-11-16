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

package org.objectweb.jasmine.jade.osgi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.adl.FactoryFactory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.rmi.RemoteException;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.rmi.registry.Registry;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.basicresourcemanagement.BasicResourceManagement;
import org.objectweb.jasmine.jade.service.nodediscovery.listener.AbstractNodeDiscoveryListener;
import org.objectweb.jasmine.jade.service.remotenodeaddition.RNAServer;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.JadeException;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;
import org.objectweb.jonathan.apis.binding.NamingContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import dks.niche.wrappers.NodeRef;

import fr.jade.fractal.api.control.GenericAttributeController;
import fr.jade.fractal.api.control.OSGiContextController;
import fr.jade.reflex.api.ReflexFactoryFactory;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class JadeNodeActivator implements BundleActivator {

	
	static final boolean JADE_HEARTBEAT = 
		System.getProperty("jade.heartbeat") instanceof String ?
				System.getProperty("jade.heartbeat").equals("1")
			:
				false;

	/**
	 * 
	 */
	private Component jadenode = null;

	/**
	 * The Fractal bootstap for Reflex mode
	 */
	private Component bootstrap = null;

	/**
	 * The Jadeboot component
	 */
	private Component jadeboot = null;

	/**
	 * 
	 */
	private String name = null;

	/**
	 * 
	 */
	private NamingService ns = null;

	/**
	 * Singleton containing Jade properties
	 */
	private JadeProperties properties = null;

	/**
	 * Fractal RMI ORB
	 */
	private Component registry = null;

	// ------------------------------------------------------------------------
	// Implementation of BundleActivator interface
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception {

		/*
		 * Initialize java security manager
		 */
		System.setSecurityManager(new SecurityManager());
		
		/*
		 * set system properties
		 */
		properties = JadeProperties.getInstance(bc);
		/*
		 * 
		 */
		System.out.println("JadeNode starting ...");
		/*
		 * connect to fractal rmi registry
		 */
		connectFractalRmiRegistry();
		/*
		 * Verify if a node is already running on this host
		 */
		verifyPreCondition();
		/*
		 * Initialize Fractal bootstrap
		 */
		initializeFractalBootstrap();
		/*
		 * Create the Fractal component Node
		 */
		jadenode = createComponent(bc);
		/*
		 * register Node in the Fractal RMI registry
		 */
		registerNode(jadenode);
		/*
		 * add Jadeboot th the composite component Jade Platform
		 */
//		addJadeBoot2JadePlatform(jadenode);
		/*
		 * add Fractal bootstrap to jadeboot/controllers
		 */
		addFractalBootstrap(jadenode);
		/*
		 * configure heartbeat
		 */

		if(JADE_HEARTBEAT) {
			configureHeartBeat(bc, jadenode);
		}
		/*
		 * configure installer
		 */
		configureInstaller(jadenode);
		/*
		 * Start the node
		 */
		startJadeNode(jadenode);
		/*
		 * 
		 */
		System.out.println("JadeNode started");
		
		// Configure remote node addition with my NodeRef 
		if ("true".equals(bc.getProperty("jadenode.remoteNodeAddition.enabled"))) {

				Component bRM = FractalUtil.getFirstFoundSubComponentByName(jadenode, "basicResourceManagement");
				BasicResourceManagement itf=(BasicResourceManagement) bRM.getFcInterface("basicResourceManagement");
				NodeRef myNodeRef = itf.getMyNode();
				String name= Fractal.getNameController(jadenode).getFcName();
				myNodeRef.setJadeNode(name);
				RNAServer.getInstance().setNodeRef(myNodeRef);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bc) throws Exception {
		if (jadenode != null) {
			((LifeCycleController) Fractal.getLifeCycleController(jadenode))
					.stopFc();
		}
	}

	// -----------------------------------------------------------------------
	// Private Methods
	// ------------------------------------------------------------------------

	/**
	 * extract arguments host & port from the list of arguments and connect to
	 * fractal rmi registry
	 * 
	 * @param argList
	 *            list containing arguments of NodeLauncher command and
	 *            specially informations about fractal rmi registry (host &
	 *            port)
	 */
	private void connectFractalRmiRegistry() {

		ClassLoader cl = JadeNodeActivator.class.getClassLoader();

		try {

			/*
			 * connect
			 */
			String registryHost = properties.getJadeRegistryHost();
			String registryPort = properties.getJadeRegistryPort();

			System.out.println("Fractal Registry: " + registryHost + ":"
					+ registryPort);

			Map<String, ClassLoader> hints = new HashMap<String, ClassLoader>();
			hints.put("registry-classloader", cl);
			hints.put("component-classloader", cl);
			
			if (registryPort == null) {
				registry = Registry.getRegistryComponent(registryHost,
						Registry.DEFAULT_PORT, hints);
				NamingContext binder = (NamingContext) registry
						.getFcInterface("context");
				ns = Registry.getRegistry(registryHost, Registry.DEFAULT_PORT,
						binder);
			} else {
				registry = Registry.getRegistryComponent(registryHost, Integer
						.parseInt(registryPort), hints);
				NamingContext binder = (NamingContext) registry
						.getFcInterface("context");
				ns = Registry.getRegistry(registryHost, Integer
						.parseInt(registryPort), binder);
			}

		} catch (Exception e) {
			e.printStackTrace();
			fatalError(new JadeException(
					"[JadeNodeActivator] can't connect to Fractal RMI registry"));
		}
	}

	/**
	 * this method test if a JadeBoot node is already running. If it is and if
	 * we are trying to launch an other JadeBoot node, stop launcher. If it
	 * isn't and we are trying to launch a JadeNode, stop the launcher
	 * 
	 * @param argList
	 *            list containing arguments of NodeLauncher command
	 */
	private void verifyPreCondition() {
		try {
			/*
			 * search a Jadeboot on the Fractal Rmi registry
			 */
			jadeboot = ns.lookup("jadeboot");

		} catch (RemoteException ignored) {
			/*
			 * if the node to launch is a JadeNode, failed
			 */
			fatalError(new JadeException(
					"Can't start a node. No JadeBoot (fractal rmi registry) found"));
		}
	}

	private void initializeFractalBootstrap() throws Exception {

		/*
		 * get Fractal Bootstrap
		 */
		bootstrap = Fractal.getBootstrapComponent();
		System.out.println("[JadeNodeActivator] Bootstrap : "
				+ bootstrap.toString());

		/*
		 * get Jade platform.
		 */
//		Component jadePlatform = ns.lookup(properties.getJadePlatformName());
		/*
		 * add bootstrap to Jade platform to allow binding with JadeBoot
		 */
//		Fractal.getContentController(jadePlatform).addFcSubComponent(bootstrap);

		/*
		 * introspect JadeBoot to find Fractal RMI registry
		 */
		// Component registryWrapper =
		// FractalUtil.getFirstFoundSubComponentByName(jadeboot,
		// "fractal_rmi_registry");
		/*
		 * get the registry interface
		 */
		Object registryInterface = jadeboot.getFcInterface("registry");
		/*
		 * get binding controller of the Fractal Bootstrap
		 */
		BindingController bc = Fractal.getBindingController(bootstrap);
		/*
		 * bind the Fractal Bootstrap to Fractal RMI registry
		 */
		bc.bindFc("registry", registryInterface);
	}

	/**
	 * Create the JadeNode component
	 * 
	 * @param bc
	 * 
	 * @param pargs
	 * @return
	 * @throws Exception
	 */
	private Component createComponent(BundleContext bc) {
		Component res = null;

		try {

			Map<String, String> context = new HashMap<String, String>();
//			context.put("heartbeat_pulse_period_in_second", properties
//					.getJadeHeartbeatPulse());
			context.put("heartbeat_pulse_period_in_second", "10000");
			//TODO

			/*
			 * get Fractal factory
			 */
			Factory f = null;

			/*
			 * get bootstrap
			 */
			bootstrap = Fractal.getBootstrapComponent();

			if (properties.isJadeReflex()) {

				/*
				 * get reflex-bootstrap
				 */
				// bootstrap = ReflexFractal.getBootstrapComponent();
				f = ReflexFactoryFactory.getFactory(
						JadeProperties.JADE_BOOT_REFLEX,
						JadeProperties.BOOT_BACKEND_REFLEX, new HashMap());

			} else {

				/*
				 * get reflex-bootstrap
				 */
				// bootstrap = Fractal.getBootstrapComponent();
				f = FactoryFactory.getFactory(JadeProperties.JADE_BOOT,
						JadeProperties.BOOT_BACKEND, new HashMap());
			}

			/*
			 * create JadeNode component
			 */
			res = (Component) f.newComponent(JadeProperties.JADE_NODE_ADL,
					context);

			/*
			 * register BundleContext in the JadeBoot installer
			 */
			((OSGiContextController) FractalUtil
					.getFirstFoundSubComponentByName(res, "installer")
					.getFcInterface("osgi-context-controller"))
					.setBundleContext(bc);

		} catch (Exception e) {
			fatalError(e);
		}

		return res;
	}

	/**
	 * method which generate the name of the Jade node formatted like this :
	 * hostname[_nodeNumber] where nodeNumber is the number of node already
	 * running on this host
	 */
	private int generateNodename(int nodeNumber) {

		try {
			name = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			fatalError(e);
		}
		boolean nodeNumberOk = false;
		Component c = null;

		while (!nodeNumberOk) {
			try {
				c = ns.lookup(name + "_" + nodeNumber);
			} catch (RemoteException ignored) {
			}
			if (c != null) {
				nodeNumber++;
			} else {
				nodeNumberOk = true;
				name += "_" + nodeNumber;
			}
		}

		return nodeNumber;
	}

	/**
	 * register the JadeNode in the fractal Rmi registry
	 * 
	 * @param argList
	 *            list containing arguments of NodeLauncher command and
	 *            specially informations about node type (JadeBoot or JadeNode)
	 * @param component
	 *            the JadeNode to register
	 */
	private void registerNode(Component component) {

		boolean isBound = false;
		int nodeNumber = 0;
		while (!isBound) {
			nodeNumber = generateNodename(nodeNumber);
			isBound = ns.bind(name, component);
		}

		try {
			Fractal.getNameController(component).setFcName(name);
		} catch (NoSuchInterfaceException e) {
			fatalError(e);
		}

		System.out.println("[NodeLauncher] Node \"" + name + "\" registered");
	}

	/**
	 * 
	 */
	private void addJadeBoot2JadePlatform(Component component) throws Exception {
		/*
		 * get Jade platform.
		 */
		Component jadePlatform = ns.lookup(properties.getJadePlatformName());

		/*
		 * set JadeNode sub-component of JadePlatform
		 */
		Fractal.getContentController(jadePlatform).addFcSubComponent(component);

	}

	/**
	 * 
	 */
	private void addFractalBootstrap(Component component) {

		try {

			/*
			 * name Fractal bootstrap
			 */
			Fractal.getNameController(bootstrap).setFcName(name + "_factory");
			/*
			 * add Fractal bootstrap to the jadeboot/controllers
			 */
			Component controllers = FractalUtil
					.getFirstFoundSubComponentByName(component, "controllers");

			Fractal.getContentController(controllers).addFcSubComponent(
					bootstrap);

			Component fractalRmiRegistry = FractalUtil
					.getFirstFoundSubComponentByName(ns.lookup("jadeboot"),
							"fractal_rmi_registry");

			Fractal.getContentController(controllers).addFcSubComponent(
					fractalRmiRegistry);

			/*
			 * unbind bootstrap.registry from jadeboot.registry
			 */
			BindingController bc = Fractal.getBindingController(bootstrap);
			bc.unbindFc("registry");
			/*
			 * bind bootstrap.registry to jadenode/controllers.registry
			 */
			// bc.bindFc("registry",
			// controllers.getFcInterface("registry"));
			/*
			 * bind jadenode.registry to jadeboot.registry
			 */
			// Object jadebootRegistry =
			// ns.lookup("jadeboot").getFcInterface("registry");
			// Fractal.getBindingController(component).bindFc("registry",
			// jadebootRegistry);
			bc
					.bindFc("registry", fractalRmiRegistry
							.getFcInterface("registry"));
			
			/*
			 * bind Fractal bootstrap to installer and garbage collector
			 */
			Fractal.getBindingController(controllers).bindFc(
					"generic-installing-factory",
					bootstrap.getFcInterface("generic-installing-factory"));

			Component installer = FractalUtil.getFirstFoundSubComponentByName(
					controllers, "installer");
			Component gc = FractalUtil.getFirstFoundSubComponentByName(
					controllers, "gc");
			Fractal.getBindingController(bootstrap).bindFc("installer",
					installer.getFcInterface("installer"));
			Fractal.getBindingController(bootstrap).bindFc("gc",
					gc.getFcInterface("gc"));

			/*
			 * get Jade platform.
			 */
//			Component jadePlatform = ns
//					.lookup(properties.getJadePlatformName());
//			/*
			 /* remove bootstrap from Jade platform (see
			 * initializeFractalBootstrap)
			 */
//			Fractal.getContentController(jadePlatform).removeFcSubComponent(
//					bootstrap);

			/*
			 * add Fractl RMI ORB used by genreic-installing-factory in
			 * jadeboot/controllers
			 */
			Fractal.getContentController(controllers).addFcSubComponent(
					registry);

		} catch (NoSuchComponentException e) {
			fatalError(e);
		} catch (IllegalBindingException e) {
			fatalError(e);
		} catch (IllegalContentException e) {
			fatalError(e);
		} catch (IllegalLifeCycleException e) {
			fatalError(e);
		} catch (NoSuchInterfaceException e) {
			fatalError(e);
		}
	}

	/**
	 * Configure the heartbeat by settings attibutes NodeDiscovery host &
	 * NodeDiscovery port
	 * 
	 * @param argList
	 *            list containing arguments of NodeLauncher command and
	 *            specially informations about NodeDiscovery service (host &
	 *            port)
	 * @param comp
	 *            component containing the HeartBeat to configure
	 */
	private void configureHeartBeat(BundleContext bc, Component comp) {

		/*
		 * get node discovery service parameters
		 */
		String discoveryHost = bc.getProperty("jadeboot.discovery.host");
		if (discoveryHost == null) {
			discoveryHost = AbstractNodeDiscoveryListener.DEFAULT_DISCOVERY_SERVICE_HOST;
		}

		String discoveryPort = bc.getProperty("jadeboot.discovery.port");
		if (discoveryPort == null) {
			discoveryPort = AbstractNodeDiscoveryListener.DEFAULT_DISCOVERY_SERVICE_PORT;
		}

		/*
		 * Introspect JadeNode component to find the HeartBeat component
		 */
		Component heartbeat = null;
		try {
			heartbeat = FractalUtil.getSubComponentByPath((Component) comp,
					"controllers/heartbeat");
		} catch (NoSuchComponentException e) {
			fatalError(e);
		}

		/*
		 * set HeartBeat component attributes
		 */
		GenericAttributeController gac;
		try {
			gac = fr.jade.fractal.util.FractalUtil
					.getGenericAttributeController(heartbeat);
			gac.setAttribute("discoveryHost", discoveryHost);
			gac.setAttribute("discoveryPort", discoveryPort);
			gac.setAttribute("nodeName", name);
		} catch (Exception e) {
			fatalError(e);
		}
	}

	/**
	 * Configure the heartbeat by settings attibutes NodeDiscovery host &
	 * NodeDiscovery port
	 * 
	 * @param argList
	 *            list containing arguments of NodeLauncher command and
	 *            specially informations about NodeDiscovery service (host &
	 *            port)
	 * @param comp
	 *            component containing the HeartBeat to configure
	 */
	private void configureInstaller(Component comp) {

		/*
		 * Introspect JadeNode component to find the Installer component
		 */
		Component osgiInstaller = null;
		try {
			osgiInstaller = FractalUtil.getSubComponentByPath((Component) comp,
					"controllers/installer");
		} catch (NoSuchComponentException e) {
			fatalError(e);
		}
		/*
		 * set HeartBeat component attributes
		 */
		try {
			GenericAttributeController gac = fr.jade.fractal.util.FractalUtil
					.getGenericAttributeController(osgiInstaller);
			gac.setAttribute("profileName", name);
		} catch (Exception e) {
			fatalError(e);
		}

	}

	/**
	 * start the Jade node component
	 * 
	 * @param comp
	 *            ref to the Jade node to start
	 */
	private void startJadeNode(Component comp) {
		try {
			LifeCycleController lc = Fractal.getLifeCycleController(comp);
			if (lc != null) {

				try {
					lc.startFc();
				} catch (IllegalLifeCycleException e) {
					fatalError(e);
				}
			}
//			Fractal.getBindingController(registry).bindFc("component-factory",
//					bootstrap.getFcInterface("generic-installing-factory"));

		} catch (NoSuchInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * method which deals with fatal errors of the NodeLauncher
	 * 
	 * @param e
	 *            exception catched which caused a fatal error
	 */
	private void fatalError(Exception e) {
		Logger.println("[NodeLauncher] Can't start node : "
				+ e.getLocalizedMessage());

		if (DebugService.on)
			e.printStackTrace();

		// System.exit(0);
	}

}
