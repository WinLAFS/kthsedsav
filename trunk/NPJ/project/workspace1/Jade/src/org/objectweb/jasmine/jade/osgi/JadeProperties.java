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

import org.objectweb.fractal.adl.FactoryFactory;
import org.objectweb.fractal.rmi.registry.Registry;
import org.objectweb.jasmine.jade.service.remotenodeaddition.RNADetails;
import org.objectweb.jasmine.jade.service.remotenodeaddition.RNAServer;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class JadeProperties {

	/**
	 * The JadeProperties singleton
	 */
	private static JadeProperties singleton;

	// ------------------------------------------------------------------------
	// Bundle properties
	// ------------------------------------------------------------------------

	/**
	 * Flag Reflex-mode
	 */
	private static boolean jadeReflex = true;

	/**
	 * Flag automatic Reflex-mode
	 */
	private static boolean jadeReflexAuto = true;

	/**
	 * Name of the JadeBoot/Node in Reflex-mode
	 */
	private static String jadeReflexName = null;

	/**
	 * Name of the dual JadeBoot/Node in Reflex-mode
	 */
	private static String jadeReflexDualname = null;

	/**
	 * Fractal RMI registry host
	 */
	private static String jadeRegistryHost = null;

	/**
	 * Fractal RMI registry port
	 */
	private static String jadeRegistryPort;

	/**
	 * Node Discovery service host
	 */
	private static String jadeDiscoveryHost = null;

	/**
	 * Node Discovery service port
	 */
	private static String jadeDiscoveryPort = null;

	/**
	 * JNDI port
	 */
	private static String jadeJndiPort;

	/**
	 * Urls of deployable ADL file
	 */
	private static String jadeUrlsDeployableFile = null;

	/**
	 * Heartbeat pulse period in second
	 */
	//private static String jadeHeartbeatPulse = null;

	// ------------------------------------------------------------------------
	// Jade properties
	// ------------------------------------------------------------------------

	/**
	 * System Representation component name
	 */
	private final String systemRepresentationName = "system_representation";

	/**
	 * Name of the composite component containing JadeBoot and JadeNodes
	 */
	private final String jadePlatformName = "jade_platform";

	// ------------------------------------------------------------------------
	// Fractal factory properties
	// ------------------------------------------------------------------------

	/**
	 * name of the ADL factory used to boot jade
	 */
	public final static String JADE_BOOT = "fr.jade.fractal.adl.JadeFactory";

	/**
	 * name of the ADL factory used to boot jade in Reflex mode
	 */
	public final static String JADE_BOOT_REFLEX = "fr.jade.fractal.adl.JadeFactory";

	// public final static String JADE_BOOT_REFLEX =
	// "fr.jade.reflex.adl.ReflexFactory";

	/**
	 * name of the backend used to create jade component
	 */
	public final static String BOOT_BACKEND = "fr.jade.fractal.adl.BootBackend";

	/**
	 * name of the backend used to create jade component in Reflex mode
	 */
	public final static String BOOT_BACKEND_REFLEX = FactoryFactory.FRACTAL_BACKEND;

	/**
	 * name of the Fractal ADL describing the JadeBoot
	 */
	public final static String JADE_BOOT_ADL = "org.objectweb.jasmine.jade.boot.jadeboot.JadeBoot";

	/**
	 * name of the Fractal ADL describing the JadeNode
	 */
	public final static String JADE_NODE_ADL = "org.objectweb.jasmine.jade.boot.jadenode.JadeNode";

	// ------------------------------------------------------------------------
	// Fractal properties
	// ------------------------------------------------------------------------

	/**
	 * Default Jade Julia loader
	 */
	private final static String JULIA_LOADER = "org.objectweb.fractal.julia.loader.DynamicLoader";
	/**
	 * Default Jade Fractal provider (bootstrap-with-name)
	 */
	private final static String FRACTAL_PROVIDER = "fr.jade.fractal.julia.JuliaJade";

	/**
	 * Jade Fractal provider in Reflex mode (reflex-bootstrap)
	 */
	// private final static String FRACTAL_PROVIDER_REFLEX =
	// "org.objectweb.fractal.julia.Julia";
	private final static String FRACTAL_PROVIDER_REFLEX = "fr.jade.reflex.util.ReflexJulia";

	/**
	 * Default Jade Julia configuration files
	 */
	private final static String JULIA_CONFIG = "etc/julia.cfg,etc/julia-fractal-rmi.cfg,etc/julia-jade.cfg,etc/julia-deploy.cfg";

	/**
	 * Jade Julia configuration files for reflex mode
	 */
	private final static String JULIA_CONFIG_REFLEX = "etc/julia.cfg,etc/julia-fractal-rmi.cfg,etc/julia-jade.cfg,etc/julia-deploy.cfg,etc/reflex-julia-fractal-rmi.cfg,etc/reflex-julia-jade.cfg,etc/reflex-julia-deploy.cfg";

	/**
	 * Default Jade julia.loader.use-context-class-loader property value
	 */
	private final static String JULIA_LOADER_USE_CONTEXT_CLASS_LOADER = "false";

	/**
	 * Default Jade Java security policy file
	 */
	private final static String JAVA_SECURITY_POLICY = "etc/java.policy";

	private static final String DEFAULT_RNA_RMI_PORT = "1099";

	private static final String DEFAULT_RNA_RMI_NAME = "RNAService";

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	/**
	 * 
	 */
	public JadeProperties() {
	}

	/**
	 * @param context
	 */
	public JadeProperties(BundleContext context) {
		processBundleContext(context);
	}

	// ------------------------------------------------------------------------
	// Singleton
	// ------------------------------------------------------------------------

	/**
	 * @return The JadeProperties singleton
	 */
	public static synchronized JadeProperties getInstance() {
		if (singleton == null) {
			singleton = new JadeProperties();
		}
		return singleton;
	}

	/**
	 * @return The JadeProperties singleton
	 */
	public static synchronized JadeProperties getInstance(BundleContext context) {
		if (singleton == null) {
			singleton = new JadeProperties(context);
		} else {
			processBundleContext(context);
		}
		return singleton;
	}

	// ------------------------------------------------------------------------
	// Getter / Setter
	// ------------------------------------------------------------------------

	public String getJadeDiscoveryPort() {
		return jadeDiscoveryPort;
	}

//	public String getJadeHeartbeatPulse() {
//		return jadeHeartbeatPulse;
//	}

	public String getJadeJndiPort() {
		return jadeJndiPort;
	}

	public boolean isJadeReflex() {
		return jadeReflex;
	}

	public boolean isJadeReflexAuto() {
		return jadeReflexAuto;
	}

	public String getJadeReflexDualname() {
		return jadeReflexDualname;
	}

	public String getJadeReflexName() {
		return jadeReflexName;
	}

	public String getJadeRegistryHost() {
		return jadeRegistryHost;
	}

	public String getJadeRegistryPort() {
		return jadeRegistryPort;
	}

	public String getJadeUrlsDeployableFile() {
		return jadeUrlsDeployableFile;
	}

	public String getSystemRepresentationName() {
		return systemRepresentationName;
	}

	public String getJadePlatformName() {
		return jadePlatformName;
	}

	// ------------------------------------------------------------------------
	// Private Methods
	// ------------------------------------------------------------------------

	private static void processBundleContext(BundleContext context) {

		String property = null;

		/*
		 * julia.loader
		 */
		property = context.getProperty("julia.loader");
		if (property != null)
			System.setProperty("julia.loader", property);
		else
			System.setProperty("julia.loader", JULIA_LOADER);
		/*
		 * java.security.policy
		 */
		property = null;
		property = context.getProperty("java.security.policy");
		if (property != null)
			System.setProperty("java.security.policy", property);
		else
			System.setProperty("java.security.policy", JAVA_SECURITY_POLICY);

		/*
		 * julia.loader.use-context-class-loader
		 */
		property = null;
		property = context.getProperty("julia.loader.use-context-class-loader");
		if (property != null)
			System.setProperty("julia.loader.use-context-class-loader",
					property);
		else
			System.setProperty("julia.loader.use-context-class-loader",
					JULIA_LOADER_USE_CONTEXT_CLASS_LOADER);

		// Handle Remote Method Addition
		if ("true".equals(context
				.getProperty("jadenode.remoteNodeAddition.enabled"))) {

			// Get RNA properties
			String rmiPort = context
					.getProperty("jadenode.remoteNodeAddition.rmi.port");
			if (rmiPort == null || "".equals(rmiPort)) {
				rmiPort = DEFAULT_RNA_RMI_PORT;
			}
			String nameRNA = context
					.getProperty("jadenode.remoteNodeAddition.rmi.name");
			if (nameRNA == null || "".equals(nameRNA)) {
				nameRNA = DEFAULT_RNA_RMI_NAME;
			}

			RNAServer server = RNAServer.createInstance(Integer.parseInt(rmiPort), nameRNA);
			server.start();
			// Blocks until the service is invoked
			RNADetails details = null;
			try {
				details = server.getRNADetails();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			jadeRegistryHost = details.getVORegistryHost();
			jadeRegistryPort = new Integer(details.getVORegistryPort())
					.toString();
			jadeDiscoveryHost = jadeRegistryHost;

		} else {
			// No remote method addition

			/*
			 * Fractal rmi registry host
			 */
			jadeRegistryHost = context.getProperty("jadeboot.registry.host");
			if (jadeRegistryHost == null
					|| "UNDEFINED".equals(jadeRegistryHost)) {
				jadeRegistryHost = "localhost";
			}
			/*
			 * Fractal rmi registry port
			 */
			jadeRegistryPort = context.getProperty("jadeboot.registry.port");
			if (jadeRegistryPort == null
					|| "UNDEFINED".equals(jadeRegistryPort)) {
				jadeRegistryPort = new Integer(Registry.DEFAULT_PORT)
						.toString();
			}

			/*
			 * get Discovery service host
			 */
			jadeDiscoveryHost = context.getProperty("jadeboot.discovery.host");
			if (jadeDiscoveryHost == null
					|| "UNDEFINED".equals(jadeDiscoveryHost)) {
				jadeDiscoveryHost = "localhost";
			}

		}

		/*
		 * set the System properties (useful for the FractalBootstrap bundle &
		 * for the Fractal-Reflex service)
		 */
		System.setProperty("registry.host", jadeRegistryHost);
		System.setProperty("registry.port", new Integer(jadeRegistryPort)
				.toString());

		/*
		 * get Discovery service host
		 */
		jadeDiscoveryPort = context.getProperty("jadeboot.discovery.port");
		if (jadeDiscoveryPort == null || "UNDEFINED".equals(jadeDiscoveryPort)) {
			jadeDiscoveryPort = "9998";
		}

		/*
		 * get the JNDI port
		 */
		jadeJndiPort = context.getProperty("jadeboot.jndi.port");
		if (jadeJndiPort == null || "UNDEFINED".equals(jadeJndiPort)) {
			jadeJndiPort = "1239";
		}

		/*
		 * get heartbeat pulse frenquency in second
		 */
//		jadeHeartbeatPulse = context.getProperty("jadenode.heartbeat.pulse");
//		if (jadeHeartbeatPulse == null
//				|| "UNDEFINED".equals(jadeHeartbeatPulse)) {
//			jadeHeartbeatPulse = "3";
//		}

		/*
		 * get the URLs of deployable ADL file
		 */
		jadeUrlsDeployableFile = context
				.getProperty("jadeboot.urls.deployable.file");
		if (jadeUrlsDeployableFile == null
				|| "UNDEFINED".equals(jadeUrlsDeployableFile)) {

			// StringBuffer sb = new StringBuffer();
			// sb.append("file://");
			// sb.append(System.getProperty("user.dir"));
			//
			// if (!sb.toString().endsWith(File.separator)) {
			// sb.append(File.separator);
			// }
			//
			// sb.append("examples");
			// sb.append(File.separator);
			//
			// jadeUrlsDeployableFile = sb.toString();

			jadeUrlsDeployableFile = "http://proton.inrialpes.fr/~jlegrand/jade/examples/";
		}

		/*
		 * get Reflex-mode flag
		 */
		property = null;
		property = context.getProperty("jade.reflex");
		if (property != null) {
			jadeReflex = Boolean.parseBoolean(property);
		}

		if (jadeReflex) {
			/*
			 * jade.reflex.name
			 */
			property = null;
			property = context.getProperty("jade.reflex.name");
			if (property != null && !"UNDEFINED".equals(property)) {
				System.setProperty("reflex-fractal.name", property);
			}
			/*
			 * jade.reflex.dual-name
			 */
			property = null;
			property = context.getProperty("jade.reflex.dual-name");
			if (property != null && !"UNDEFINED".equals(property)) {
				System.setProperty("reflex-fractal.dual-name", property);
			}
			/*
			 * jade.reflex.auto
			 */
			property = null;
			property = context.getProperty("jade.reflex.auto");
			if (property == null || "UNDEFINED".equals(property)) {
				System.setProperty("reflex-fractal.auto", "true");
			} else {
				System.setProperty("reflex-fractal.auto", property);
			}
		}

		/*
		 * julia.config
		 */
		property = null;
		property = context.getProperty("julia.config");
		if (property != null) {
			System.setProperty("julia.config", property);
		} else {
			if (jadeReflex) {
				System.setProperty("julia.config", JULIA_CONFIG_REFLEX);
			} else {
				System.setProperty("julia.config", JULIA_CONFIG);
			}
		}

		/*
		 * fractal.provider
		 */
		property = null;
		property = context.getProperty("fractal.provider");
		if (property != null) {
			System.setProperty("fractal.provider", property);
		} else {
			if (jadeReflex) {
				System.setProperty("fractal.provider", FRACTAL_PROVIDER_REFLEX);
			} else {
				System.setProperty("fractal.provider", FRACTAL_PROVIDER);
			}
		}

	}
}
