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

package org.objectweb.jasmine.jade.service.deployer.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.deployment.local.api.UndeploymentController;
import org.objectweb.fractal.deployment.local.api.UndeploymentException;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.resource.Resource;
import org.objectweb.jasmine.jade.service.Service;
import org.objectweb.jasmine.jade.service.deployer.DeployerService;
import org.objectweb.jasmine.jade.service.deployer.DeploymentException;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.JadeException;
import org.objectweb.jasmine.jade.util.Logger;

/**
 * This class launch an application adl file. The adlfile to launch is
 * configured either in the attribute adlFile or either in a property file. The
 * name of the property file is defined by the attribute adlFileProperty. In
 * this latter case, the name of the key is :
 * "deploymentService.applicationLauncher.adlFile". The name of this key is not
 * configurable for now. The adlFileProperty is an argument of
 * ApplicationLauncher.fractal.
 * 
 * @author <a href="mailto:noel.depalma@inrialpes.fr">Noel De Palma
 * @author <a href="mailto:jakub.kornas@inrialpes.fr">Jakub Kornas
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 */
public class DeployerManagerImpl implements DeployerService,
		LifeCycleController, BindingController {

	/**
	 * 
	 */
	private boolean started = false;

	/**
	 * 
	 */
	final static String[] attList = {};

	/**
	 * 
	 */
	private final String[] bindingList = { "registry", "deployer" };

	/**
	 * 
	 */
	private Component myself;

	/**
	 * 
	 */
	private NamingService registry = null;

	/**
	 * The deployer used by this launcher to instanciate the adlfile. (it is an
	 * adl factory)
	 */
	private Factory deployer;

	/**
	 * record the name and the application root cmp deployed by this apps.
	 */
	private Map deployedApps = new TreeMap();

	/**
	 * Identifier of a deployment
	 */
	private int deploymentId = 0;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public DeployerManagerImpl() {
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
		if (itfName.equals("deployer"))
			return deployer;
		else if (itfName.equals("registry"))
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
		if (itfName.equals("component"))
			myself = (Component) itfValue;
		else if (itfName.equals("deployer"))
			deployer = (Factory) itfValue;
		else if (itfName.equals("registry"))
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
		if (itfName.equals("component"))
			myself = null;
		else if (itfName.equals("deployer"))
			deployer = null;
		else if (itfName.equals("registry"))
			registry = null;
		else
			throw new NoSuchInterfaceException(itfName);
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
		if (started)
			throw new IllegalLifeCycleException("Component already started");
		else {
			try {
				Component supers[] = Fractal.getSuperController(myself)
						.getFcSuperComponents();
				if (supers.length > 1)
					Logger
							.println(DebugService.on,
									"Can't register Deployer. More than one Super Component found");
				else {
					/*
					 * FIXME bind or rebind ?
					 */
					registry.bind("deployer", supers[0]);
				}

			} catch (NoSuchInterfaceException e) {
				Logger.println(DebugService.on,
						"Can't register Deployer. No Super Component found");
			}
			started = true;

			Logger.println(DebugService.info, "[Deployer] started");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
	 */
	public void stopFc() throws IllegalLifeCycleException {
		if (!started)
			throw new IllegalLifeCycleException("Component already stopped");
		else {
			started = false;
			Logger.println(DebugService.info, "[Deployer] stopped");
		}

	}

	// ------------------------------------------------------------------------
	// Implementation of ManagerService interface
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.jasmine.jade.service.ManagerService#deploy(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public int deploy(String adlId, Map context) throws DeploymentException {

		if (adlId != null) {

			// File adlFile = null;
			String adlFileName = null;

			// if(adlId.startsWith("file://")){
			// adlFileName = adlId.replaceFirst("file://", "");
			// adlFile = new File(adlFileName);
			// adlFileName = adlFile.getName();
			// }
			// else{
			adlFileName = adlId;
			// }

			/*
			 * verify if the component already exists
			 */
			// if (!(deployedApps.containsKey(adlId))) {
			Logger
					.println(DebugService.info,
							"*********************************  DEPLOYER  ******************************");
			Logger.println(DebugService.on, "[DeployerManager] ");
			Logger.println(DebugService.info, "Deploying \"" + adlFileName
					+ "\" ...");

			/*
			 * create the component
			 */
			try {
				if (context==null)
					context = new HashMap();

				Component myapps = null;
				// if(adlFile != null){
				// myapps = (Component) deployer.newComponent(adlFile, context);
				// }
				// else{
				context.put("classloader", this.getClass().getClassLoader());
				
				context.put("nicheIdRegistryContext", adlId + "_" + deploymentId );
				
				
				myapps = (Component) deployer
						.newComponent(adlFileName, context);
				// }

				/*
				 * register the root component of the application in the
				 * registry. adlId is the name of the root component
				 */
				registry.rebind(adlId + "_" + deploymentId, myapps);

				Logger.println(DebugService.info,
						"****************************************");
				Logger.println(DebugService.on, "[DeployerManager] ");
				Logger.println(DebugService.info, "component \"" + adlId
						+ "\" registered");
				Logger.println(DebugService.info,
						"****************************************");
				/*
				 * locally store component
				 */
				deployedApps.put(deploymentId, new Deployment(adlId, myapps,
						new Date(), null));

				Logger.println(DebugService.on, "[DeployerManager] ");
				Logger.println(DebugService.info, "\"" + adlId
						+ "\" successfully deployed");

				Logger
						.println(DebugService.info,
								"***************************************************************************");

			} catch (Exception e) {
				this.printDeploymentCancelled(e);
				throw new DeploymentException("Unable to deploy application \""
						+ adlId + "\" : " + e.getMessage());
			}

			// } else {
			// DeploymentException e = new DeploymentException(
			// "Unable to deploy application \"" + adlId
			// + "\" : application already deployed");
			//
			// this.printDeploymentCancelled(e);
			// throw new DeploymentException(e);
			// }
		}

		return deploymentId++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.jasmine.jade.service.ManagerService#start(java.lang.String)
	 */
	public Component start(int adlId) throws DeploymentException {

		Deployment deployment = (Deployment) deployedApps.get(adlId);
		if (deployment == null)
			throw new DeploymentException(
					"[DeployerManager] Unable to start application defined by the ID \""
							+ adlId + "\" : application not found");
		try {

			Component cmp = deployment.getComponent();

			Fractal.getLifeCycleController(cmp).startFc();

			/*
			 * call standard service if the root composite provide a Ressource
			 * itf, start it
			 */
			// Interface[] lfItf =
			// FractalUtil.getServerInterfaceBySignature(cmp,
			// "org.objectweb.jasmine.jade.service.Service");
			//
			// if (lfItf != null) {
			// if (lfItf.length > 1)
			// throw new JadeException(
			// "[DeployerManager] Unable to start application defined by the ID
			// \""
			// + adlId
			// + "\" : multiple Service interface found");
			//
			// ((Service) lfItf[0]).start();
			// }
			
			Object sItf = null;
			Object rItf = null;
			try {
				sItf = cmp.getFcInterface("service");
			} catch (NoSuchInterfaceException ignored) {
				// TODO: Find a better solution than one based on itf name
				try {
					rItf = cmp.getFcInterface("rsrc");
				} catch (NoSuchInterfaceException ignored2) {
				}
			}

			if (sItf != null && sItf instanceof Service) {
				((Service) sItf).start();
			}
			
			if (rItf != null && rItf instanceof Resource) {
				((Resource) rItf).start();
			}

			return cmp;

		} catch (Exception e) {
			e.printStackTrace();
			throw new DeploymentException(
					"[DeployerManager] Unable to start application defined by the ID \""
							+ adlId + "\" : " + e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.jasmine.jade.service.ManagerService#stop(java.lang.String)
	 */
	public void stop(int adlId) throws DeploymentException {

		Deployment deployment = (Deployment) deployedApps.get(adlId);
		if (deployment == null)
			throw new DeploymentException(
					"[DeployerManager] Unable to stop application defined by the ID \""
							+ adlId + "\" : application not found");

		try {
			Component cmp = deployment.getComponent();

			if (cmp != null) {
				try {
					Interface[] lfItf = FractalUtil
							.getServerInterfaceBySignature(cmp,
									"org.objectweb.jasmine.jade.service.Service");
					if (lfItf != null) {
						if (lfItf.length > 1)
							throw new JadeException(
									"[DeployerManager] Unable to stop application \""
											+ adlId
											+ "\" : multiple Service interface found");
						else
							((Service) lfItf[0]).stop();
					}

				} catch (Exception e) {
					throw new DeploymentException(
							"[DeployerManager] Unable to stop application defined by the ID \""
									+ adlId + "\" : " + e.getLocalizedMessage());
				}
			} else
				throw new DeploymentException(
						"[DeployerManager] Unable to stop application defined by the ID \""
								+ adlId + "\" : application not found");

		} catch (Exception e) {
			throw new DeploymentException(
					"[DeployerManager] Unable to stop application defined by the ID \""
							+ adlId + "\" : " + e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.jasmine.jade.service.ManagerService#undeploy(java.lang.String)
	 */
	public void undeploy(int adlId) throws DeploymentException {

		Logger
				.println(DebugService.info,
						"*********************************  DEPLOYER  ******************************");
		Logger.print(DebugService.on, "[DeployerManager] ");
		Logger.println(DebugService.info, "Undeploying \"" + adlId + "\" ...");

		/*
		 * get deployment information
		 */
		Deployment deployment = (Deployment) deployedApps.get(adlId);
		if (deployment == null)
			throw new DeploymentException(
					"[DeployerManager] Unable to stop application defined by the ID \""
							+ adlId + "\" : application not found");

		/*
		 * get component
		 */
		Component cmp = deployment.getComponent();

		if (cmp != null) {

			/*
			 * unregister the root component of the application in the registry.
			 */
			try {
				registry.unbind(Fractal.getNameController(cmp).getFcName());
			} catch (NoSuchInterfaceException ignored) {
			}

			/*
			 * undeploy component
			 */
			UndeploymentController uc = null;
			try {
				uc = (UndeploymentController) cmp
						.getFcInterface("undeployment-controller");
			} catch (NoSuchInterfaceException e) {
				Logger.print(DebugService.on, "[DeployerManager] ");
				Logger.println(DebugService.info,
						"This component can't be undeployed");
				return;
			}

			try {
				uc.undeploy();
			} catch (UndeploymentException e) {
				throw new DeploymentException("[DeployerManager] "
						+ e.getMessage());
			}

			/*
			 * remove locally store component
			 */
			deployedApps.remove(adlId);

		} else {
			throw new DeploymentException(
					"[DeployerManager] Unable to stop application defined by the ID \""
							+ adlId + "\" : application not found");
		}

		Logger
				.println(DebugService.info,
						"***************************************************************************");
	}

	// ------------------------------------------------------------------------
	// Public Methods
	// ------------------------------------------------------------------------
	public void listDeployment() {

		System.out.println("Id | ADL name | Date | Comment");

		Iterator it = deployedApps.keySet().iterator();
		Deployment d = null;
		int id;
		StringBuffer buffer = null;
		while (it.hasNext()) {
			buffer = new StringBuffer();
			id = (Integer) it.next();
			d = (Deployment) deployedApps.get(id);
			buffer.append(id);
			buffer.append(" | ");
			buffer.append(d.getAdlId());
			buffer.append(" | ");
			buffer.append(d.getDate());
			buffer.append(" | ");
			buffer.append(d.getComment());

			System.out.println(buffer.toString());
		}
		System.out.println("\n");
	}

	public void listDeploymentByAdlId(String adlId) {

		System.out.println("Id | ADL name | Date | Comment");

		Iterator it = deployedApps.keySet().iterator();
		Deployment d = null;
		int id;
		StringBuffer buffer = null;
		while (it.hasNext()) {
			buffer = new StringBuffer();
			id = (Integer) it.next();
			d = (Deployment) deployedApps.get(id);
			if (d.getAdlId().equals(adlId)) {
				buffer.append(id);
				buffer.append(" | ");
				buffer.append(d.getAdlId());
				buffer.append(" | ");
				buffer.append(d.getDate());
				buffer.append(" | ");
				buffer.append(d.getComment());

				System.out.println(buffer.toString());
			}
		}
		System.out.println("\n");
	}

	// ------------------------------------------------------------------------
	// Private Methods
	// ------------------------------------------------------------------------

	// private void uninstall(Component app) throws NoSuchInterfaceException,
	// JadeException, NoSuchComponentException, NoSuchAttributeException {
	//
	// /*
	// * DEBUG begin 26 avr. 2006 jlegrand
	// */
	// String appName = Fractal.getNameController(app).getFcName();
	// /*
	// * end
	// */
	//
	// try {
	// ContentController cc = (ContentController) Fractal
	// .getContentController(app);
	// for (Component c : cc.getFcSubComponents()) {
	// uninstall(c);
	// }
	//
	// } catch (NoSuchInterfaceException e) {
	//
	// Component managed_resources = FractalUtil
	// .getFirstFoundSuperComponentByName(app, "managed_resources");
	//
	// /*
	// * DEBUG begin 26 avr. 2006 jlegrand
	// */
	// String mrName = Fractal.getNameController(managed_resources)
	// .getFcName();
	// /*
	// * end
	// */
	//
	// Component jadenode = FractalUtil
	// .getFirstFoundSuperComponentByServerInterfaceSignature(
	// managed_resources,
	// "org.objectweb.jasmine.jade.service.installer.Installer");
	// /*
	// * DEBUG begin 26 avr. 2006 jlegrand
	// */
	// String jdName = Fractal.getNameController(jadenode).getFcName();
	// /*
	// * end
	// */
	//
	// Installer installer = (Installer) jadenode
	// .getFcInterface("installer");
	//
	// try {
	//
	// GenericAttributeController gac = (GenericAttributeController) app
	// .getFcInterface("attribute-controller");
	//
	// if (Arrays.asList(gac.listFcAtt()).contains("bundleIds")) {
	// String dirLocal = gac.getAttribute("dirLocal");
	// String bundleIds = gac.getAttribute("bundleIds");
	// StringTokenizer tokenizer = new StringTokenizer(bundleIds,
	// ":");
	// while (tokenizer.hasMoreTokens()) {
	// installer.uninstall(new Long(tokenizer.nextToken()));
	// }
	// }
	// } catch (NoSuchInterfaceException ignored) {
	//
	// }
	// }
	//
	// }

	/**
	 * @param exception
	 */
	private void printDeploymentCancelled(Exception exception) {
		Logger.println(DebugService.info,
				"****************************************");
		Logger.println(DebugService.info, "ERROR DURING DEPLOYMENT : ");
		Logger.print(DebugService.on, "[DeployerManager] ");
		Logger.println(DebugService.info, exception.getMessage());
		if (DebugService.on)
			exception.printStackTrace();
		Logger
				.println(DebugService.info,
						"***************************************************************************");

	}

	// ------------------------------------------------------------------------
	// Inner class
	// ------------------------------------------------------------------------

	class Deployment {

		/**
		 * 
		 */
		private String adlId;

		/**
		 * 
		 */
		private Component component;

		/**
		 * 
		 */
		private Date date;

		/**
		 * 
		 */
		private String comment;

		// ------------------------------------------------------------------------
		// Constructor
		// ------------------------------------------------------------------------
		public Deployment() {
			// TODO Auto-generated constructor stub
		}

		public Deployment(String adlId, Component component, Date date,
				String comment) {
			this.adlId = adlId;
			this.component = component;
			this.date = date;
			this.comment = comment;
		}

		// ------------------------------------------------------------------------
		// Accessors
		// ------------------------------------------------------------------------

		public String getAdlId() {
			return adlId;
		}

		public void setAdlId(String adlId) {
			this.adlId = adlId;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public Component getComponent() {
			return component;
		}

		public void setComponent(Component component) {
			this.component = component;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}
	}
}