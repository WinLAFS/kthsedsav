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

package org.objectweb.jasmine.jade.service.factory;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.deployment.local.api.GarbageCollector;
import org.objectweb.fractal.deployment.local.api.GenericInstallingFactory;
import org.objectweb.fractal.deployment.local.api.Installer;
import org.objectweb.fractal.deployment.local.api.Loader;
import org.objectweb.fractal.deployment.local.api.PackageDescription;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.util.JadeException;

import fr.jade.fractal.api.control.GenericInstallingFactoryController;
import fr.jade.fractal.julia.loader.ClassLoaderWrapper;

/**
 * @author <a href="mailto:jakub.kornas@inrialpes.fr">Jakub Kornas
 * 
 */
public class JadeLocalFactoryImpl implements GenericInstallingFactory,
        BindingController {

	private final String INSTALLER_ITF = "installer";

	private final String GC_ITF = "gc";

	private Component bootstrap = null;

	private GenericFactory fact = null;

	private Installer installer = null;
	
	private GarbageCollector gc = null;
	
	private Map<Component, Loader> cmpsToLdr = new HashMap<Component,Loader>();
	
	private Component myself = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

	public JadeLocalFactoryImpl() throws JadeException {
		// this.cmpsToPkgs = new HashMap<Component, PackageDescription>();
		try {
			this.bootstrap = Fractal.getBootstrapComponent();
			this.fact = Fractal.getGenericFactory(bootstrap);
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}

    // ------------------------------------------------------------------------
    // Implementation of GenericInstallingFactory interface
    // ------------------------------------------------------------------------

    public Component newFcInstance(Type type, Object controllerDesc,
            Object contentDesc, PackageDescription packageDescription)
            throws InstantiationException {

		Loader ldr = installer.install(packageDescription);

		ClassLoader cl = null;
		if (ldr != null) {
			cl = ((ClassLoaderWrapper) ldr).getClassLoader();
		} else {
			cl = this.getClass().getClassLoader();
		}

		GenericFactory componentFactory = fact;

		if (controllerDesc instanceof Map) {
			Map params = (Map) controllerDesc;
			String juliaCfg = params.get("juliaconfig").toString();
			String controllerDsc = params.get("controller").toString();
			String juliaConfig = System.getProperty("julia.config");
			try {
				org.objectweb.fractal.julia.loader.Loader juliaLoader = (org.objectweb.fractal.julia.loader.Loader) bootstrap
						.getFcInterface("loader");
				Map<String, Object> hints = new HashMap<String, Object>();
				hints.put("classloader", cl);
				hints.put("julia.config", juliaCfg);
				juliaLoader.init(hints);

				hints = new HashMap<String, Object>();
				hints.put("classloader", cl);

				Component result = componentFactory.newFcInstance(type,
						new Object[] { cl, controllerDsc }, contentDesc);

				// TODO: refactor (the GF controller should not have a set
				// method)
				setGenericFactory(result);
				cmpsToLdr.put(result,ldr);
				gc.notifyComponentCreated(result, packageDescription);

				// reconfigure the loader to the previous state
				ClassLoader juliaCl = getClass().getClassLoader();
				hints.put("classloader", juliaCl);
				hints.put("julia.config", juliaConfig);
				juliaLoader.init(hints);

				return result;
			} catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Component result = componentFactory.newFcInstance(type, new Object[] {
				cl, controllerDesc }, contentDesc);

		// TODO: refactor (the GF controller should not have a set method)
		setGenericFactory(result);
		cmpsToLdr.put(result,ldr);
		gc.notifyComponentCreated(result, packageDescription);
		return result;
    }

    public Component newFcInstance(Type type, Object controllerDesc,
            Object contentDesc) throws InstantiationException {

        Component result = fact
                .newFcInstance(type, controllerDesc, contentDesc);

        // TODO: refactor (the GF controller should not have a set method)
        setGenericFactory(result);
        return result;
    }

    public void undeployFcComponent(Component cmp) {
        cmpsToLdr.remove(cmp);
		gc.notifyComponentDestroyed(cmp);
    }

    // ------------------------------------------------------------------------
    // Implementation of BindingController interface
    // ------------------------------------------------------------------------

	public void bindFc(String clientItfName, Object serverItf)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if (clientItfName.equals(this.INSTALLER_ITF)) {
			this.installer = (Installer) serverItf;
		} else if (clientItfName.equals(this.GC_ITF)) {
			this.gc = (GarbageCollector) serverItf;
		} else if (clientItfName.equals("component")) {
			this.myself = (Component) serverItf;
		}
	}

	public String[] listFc() {
		return new String[] { this.INSTALLER_ITF, this.GC_ITF };
	}

	public Object lookupFc(String clientItfName)
			throws NoSuchInterfaceException {
		if (clientItfName.equals(this.INSTALLER_ITF)) {
			return this.installer;
		} else if (clientItfName.equals(this.GC_ITF)) {
			return this.gc;
		}
		return null;
	}

	public void unbindFc(String clientItfName) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if (clientItfName.equals(this.INSTALLER_ITF)) {
			this.installer = null;
		} else if (clientItfName.equals(this.GC_ITF)) {
			this.gc = null;
		}
	}

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

	private void setGenericFactory(Component result) {
		try {
			((GenericInstallingFactoryController) result
					.getFcInterface("generic-installing-factory-controller"))
					.setGenericFactory((GenericInstallingFactory) myself
							.getFcInterface("factory"));
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
	}

	public Loader getFcLoaderForComponent(Component cmp) {
		return cmpsToLdr.get(cmp);
	}
}
