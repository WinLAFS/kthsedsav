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

package org.objectweb.jasmine.jade.service.installer.osgi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.deployment.local.api.Installer;
import org.objectweb.fractal.deployment.local.api.Loader;
import org.objectweb.fractal.deployment.local.api.PackageDescription;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.JadeException;
import org.objectweb.jasmine.jade.util.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ungoverned.oscar.BundleImpl;
import org.ungoverned.oscar.BundleInfo;
import org.ungoverned.oscar.Oscar;
import org.ungoverned.osgi.service.shell.ShellService;

import fr.jade.fractal.api.control.GenericAttributeController;
import fr.jade.fractal.api.control.NoSuchAttributeException;
import fr.jade.fractal.api.control.OSGiContextController;
import fr.jade.fractal.julia.loader.ClassLoaderWrapper;

/**
 * 
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * @author <a href="mailto:jakub.kornas@inrialpes.fr">Jakub Kornas
 * 
 */
public class OSGiInstallerImpl implements Installer, OSGiContextController,
		LifeCycleController, GenericAttributeController {

	/**
	 * 
	 */
	// private String profileName;
	/**
	 * 
	 */
	private boolean started = false;

	/**
	 * 
	 */
	private Oscar oscar = null;

	/**
	 * 
	 */
	private ShellService shell = null;

	/**
	 * 
	 */
	private BundleContext bc;

	/**
	 * 
	 */
	private Map<PackageDescription, LoaderAndBundleID> pkgToLoader = null;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	public OSGiInstallerImpl() {
		pkgToLoader = new HashMap<PackageDescription, LoaderAndBundleID>();
	}

	// ------------------------------------------------------------------------
	// Implementation of LifeCycleController interface
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
			try {
				ServiceReference[] services = bc.getServiceReferences(
						"org.ungoverned.oscar.Oscar", "(Oscar=Oscar)");
				oscar = (Oscar) bc.getService(services[0]);

			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
			}

			getShellService();
			started = true;

		} else {
			throw new IllegalLifeCycleException("[OSGI] already started");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
	 */
	public void stopFc() throws IllegalLifeCycleException {
		if (started) {
			Logger.println(DebugService.info, "[OSGI] oscar stopped");
			started = false;
		} else
			throw new IllegalLifeCycleException("[OSGI] already stopped");
	}

	// ------------------------------------------------------------------------
	// Implementation of GenericAttributeControllerController interface
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.jasmine.jade.meta.api.control.GenericAttributeController#getAttribute(java.lang.String)
	 */
	public String getAttribute(String name) throws NoSuchAttributeException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.jasmine.jade.meta.api.control.GenericAttributeController#setAttribute(java.lang.String,
	 *      java.lang.String)
	 */
	public void setAttribute(String name, String value)
			throws NoSuchAttributeException {
		// TODO: remove this method?
		// if (name.equals("profileName"))
		// profileName = value;
		// else
		// throw new NoSuchAttributeException(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.jasmine.jade.meta.api.control.GenericAttributeController#listFcAtt()
	 */
	public String[] listFcAtt() {
		// return attList;
		return null;
	}

	// ------------------------------------------------------------------------
	// Implementation of Installer interface
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.deployment.local.api.Installer#install(org.objectweb.fractal.deployment.local.api.PackageDescription)
	 */
	public Loader install(PackageDescription packageDescription) {
		Loader result;

		/*
		 * 
		 */
		LoaderAndBundleID labid = pkgToLoader.get(packageDescription);
		if (labid != null && (result = labid.getLoader()) != null) {
			return result;
		}

		/*
		 * Set package properties
		 */
		Map pkgProps = (Map) packageDescription.getPackageProperties();

		if (pkgProps != null && pkgProps.keySet().size() > 0) {

			Iterator it = pkgProps.keySet().iterator();

			while (it.hasNext()) {
				String key = it.next().toString();
				String propValue = null;

				try {

					propValue = (String) pkgProps.get(key);
					shell.executeCommand("sp " + key + " " + propValue,
							System.out, System.err);

				} catch (Exception e) {
					// throw new JadeException("[OSGI] Unable to set property
					// \"" +
					// key +
					// "\" with value \""
					// + propValue + "\" : " +
					// e.getMessage());
				}
			}
		}

		/*
		 * install the bundle defined by packageName (& its dependency)
		 */
		String resourceName = packageDescription.getPackageID();
		try {
			shell.executeCommand("obr start '" + resourceName + "'",
					System.out, System.err);

		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * 
		 */

		long id = oscar.getBundleId()-1;
		Bundle bnd = oscar.getBundle(id);
		if (bnd != null) {
			BundleInfo binf = ((BundleImpl) bnd).getInfo();
			ClassLoader cl = binf.getCurrentModule().getClassLoader();
			result = new ClassLoaderWrapper(cl);
		} else {
			result = new ClassLoaderWrapper(this.getClass().getClassLoader());
		}

		/*
		 * 
		 */
		pkgToLoader.put(packageDescription, new LoaderAndBundleID(result, id));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.deployment.local.api.Installer#installAndMark(org.objectweb.fractal.deployment.local.api.PackageDescription)
	 */
	public void installAndMark(PackageDescription packageDescription) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.deployment.local.api.Installer#uninstall(org.objectweb.fractal.deployment.local.api.PackageDescription)
	 */
	public void uninstall(PackageDescription packageDescription)
			throws JadeException {
		LoaderAndBundleID labid = pkgToLoader.get(packageDescription);
		if (labid == null) {
			return;
		}

		long bundleId = labid.getID();
		try {
			shell.executeCommand("uninstall " + bundleId, System.out,
					System.err);
		} catch (Exception e) {
			throw new JadeException(
					"[OSGI] Unable to uninstall bundle defined by the ID \""
							+ bundleId + "\" : " + e.getLocalizedMessage());
		}
		pkgToLoader.remove(packageDescription);		
	}

	class LoaderAndBundleID {
		private Loader loader;

		private long bundleID;

		public LoaderAndBundleID(Loader loader, long bundleID) {
			this.loader = loader;
			this.bundleID = bundleID;
		}

		public Loader getLoader() {
			return this.loader;
		}

		public long getID() {
			return this.bundleID;
		}
	}

	// -----------------------------------------------------------------------
	// Private methods
	// -----------------------------------------------------------------------

	/**
	 * Get the shell service from Oscar.
	 */
	private void getShellService() {

		try {
			ServiceReference shellRef[] = oscar.getServiceReferences(
					ShellService.class.getName(), null);

			if (shellRef == null) {
				System.out.println("No shell service is available.");
			}

			shell = (ShellService) oscar.getService((BundleImpl) shellRef[0]
					.getBundle(), shellRef[0]);

		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param resourceName
	 * @return the id defining the bundle passed in parameter.
	 */
	// private long getBundleId(String resourceName) {
	// Bundle bundles[] = oscar.getBundles();
	// for (Bundle b : bundles) {
	// Dictionary dico = b.getHeaders();
	// if (dico.get("Bundle-Name").equals(resourceName)) {
	// return b.getBundleId();
	// }
	// }
	// return -1;
	// }
	/**
	 * delete the Oscar cache directory of the JadeNode
	 * 
	 * @return
	 */
	// private boolean deleteOscarCache() {
	// String oscarCache = System.getProperty("user.home");
	// oscarCache = oscarCache.endsWith(File.separator) ? oscarCache
	// : oscarCache + File.separator;
	// oscarCache = oscarCache + ".oscar" + File.separator + profileName;
	//
	// File oscarCacheDirectory = new File(oscarCache);
	// return deleteDir(oscarCacheDirectory);
	// }
	/**
	 * delete the directory
	 * 
	 * @param dir
	 * @return
	 */
	// private static boolean deleteDir(File dir) {
	// if (dir.isDirectory()) {
	// String[] children = dir.list();
	// for (int i = 0; i < children.length; i++) {
	// boolean success = deleteDir(new File(dir, children[i]));
	// if (!success) {
	// return false;
	// }
	// }
	// }
	// return dir.delete();
	// }
	public void addBundleId(Component component, long bundleId) {

	}

	public BundleContext getBundleContext() {
		return this.bc;
	}

	public long getBundleId(Component component) {
		return 0;
	}

	public void setBundleContext(BundleContext context) {
		this.bc = context;
	}

	public PackageDescription[] getInstalledPackages() {
		return pkgToLoader.keySet().toArray(new PackageDescription[] {});
	}

}
