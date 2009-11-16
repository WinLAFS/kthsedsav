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

package org.objectweb.jasmine.jade.service.registry.fractalrmi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.rmi.registry.Registry;
import org.objectweb.jasmine.jade.osgi.JadeNodeActivator;
import org.objectweb.jasmine.jade.osgi.JadeProperties;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.JadeException;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.jonathan.apis.binding.NamingContext;

import fr.jade.fractal.api.control.GenericAttributeController;
import fr.jade.fractal.api.control.NoSuchAttributeException;

/**
 * Implementation class of the Fractal RMI registry wrapper
 *
 * Encapsulates a Fractal RMI registry on the local host, or a proxy to an Fractal RMI registry
 * on another host (if attribute IsProxy=true)
 * 
 * @author <a href="mailto:noel.depalma@inrialpes.fr">Noel De Palma
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * @author Nikos Parlavantzas
 *
 */
public class RegistryImpl implements NamingService, GenericAttributeController {

    /**
     * 
     */
    private NamingService ns = null;

    /**
     * 
     */
    final static String[] attList = { "port" };

    /**
     * 
     */
    private String host = null;

    /**
     * 
     */
    private int port = 0;


	private String isProxy="false";

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public RegistryImpl() throws JadeException {

        try {

            host = InetAddress.getLocalHost().getHostName();

        } catch (Exception e) {
            throw new JadeException("cannot set registry host", e);
        }

    }

    // ------------------------------------------------------------------------
    // Implementation of GenericAttributeController interface
    // ------------------------------------------------------------------------

    public String getAttribute(String name) throws NoSuchAttributeException {

        if (name.equals("host")) {
                return host;
            } else if (name.equals("port")) {
                return String.valueOf(port);
            } else if (name.equals("isProxy")) {
                return isProxy;} 
            else {
                throw new NoSuchAttributeException(name);
        }
    }

    /**
     * Basic implementation of GenericAttributeController
     */
    public void setAttribute(String name, String value)
			throws NoSuchAttributeException {

		if (name.equals("port")) {
			port = new Integer(value).intValue();
			if (ns == null) {
				createRegistry();
			}
		} else if (name.equals("isProxy")) {
			isProxy = value;
			if (isProxy.equals("true") && (ns == null)) {
				connectFractalRmiRegistry();
			}
		} else
			throw new NoSuchAttributeException(name);

	}

    public String[] listFcAtt() {
        return attList;
    }

    // ------------------------------------------------------------------------
    // Implementation of NamingService interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.rmi.registry.NamingService#bind(java.lang.String,
     *      org.objectweb.fractal.api.Component)
     */
    public boolean bind(String name, Component comp) {
        if (ns == null) {
            createRegistry();
        }
        return ns.bind(name, comp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.rmi.registry.NamingService#list()
     */
    public String[] list() {
        if (ns == null) {
            createRegistry();
        }
        return ns.list();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.rmi.registry.NamingService#lookup(java.lang.String)
     */
    public Component lookup(String name) {
        if (ns == null) {
            createRegistry();
        }
        return ns.lookup(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.rmi.registry.NamingService#rebind(java.lang.String,
     *      org.objectweb.fractal.api.Component)
     */
    public Component rebind(String name, Component comp) {
        if (ns == null) {
            createRegistry();
        }
        return ns.rebind(name, comp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.rmi.registry.NamingService#unbind(java.lang.String)
     */
    public Component unbind(String name) {
        if (ns == null) {
            createRegistry();
        }
        return ns.unbind(name);
    }

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

    private void createRegistry() {

        try {
            Map<String,ClassLoader> hints = new HashMap<String,ClassLoader>();
            ClassLoader cl = this.getClass().getClassLoader();
            hints.put("registry-classloader", cl);
            hints.put("component-classloader", cl);
            ns = Registry.getRegistry(host, port, hints);
            // ns = Registry.getRegistry(host, port,
            // this.getClass().getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ns.list();
        } catch (Exception e) {
            try {
                org.objectweb.fractal.rmi.registry.Registry.createRegistry(
                        port, this.getClass().getClassLoader());
            } catch (Exception e1) {
                Logger.println(DebugService.info, "cannot create registry on "
                        + host + ":" + port);
                e1.printStackTrace();
                System.exit(0);
            }
        }
    }
    
    private void connectFractalRmiRegistry() {

		ClassLoader cl = JadeNodeActivator.class.getClassLoader();
		Component registry = null;

		try {

			/*
			 * connect
			 */
			String registryHost = JadeProperties.getInstance().getJadeRegistryHost();
			String registryPort = JadeProperties.getInstance().getJadeRegistryPort();

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
		}
	}
}
