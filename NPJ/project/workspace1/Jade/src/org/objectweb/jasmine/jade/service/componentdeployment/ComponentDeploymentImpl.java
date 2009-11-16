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

package org.objectweb.jasmine.jade.service.componentdeployment;

import dks.niche.NicheOSSupport;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.ids.ResourceId;
import dks.niche.ids.SNR;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.ResourceRef;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.deployment.local.api.GenericInstallingFactory;
import org.objectweb.fractal.deployment.local.api.PackageDescription;

import org.objectweb.fractal.julia.type.BasicComponentType;
import org.objectweb.fractal.julia.type.BasicInterfaceType;
import org.objectweb.fractal.rmi.io.Ref;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.DebugAdl;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;
import org.objectweb.jasmine.jade.util.RmiRefManagement;
import org.objectweb.jasmine.jade.util.Serialization;
import org.objectweb.jasmine.jade.util.UtilityComponentFactory;

/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos
 *         Parlavantzas
 * 
 */
public class ComponentDeploymentImpl implements LifeCycleController,
		BindingController, ComponentDeployment, LowerComponents,
		NicheIdRegistry {

	static final boolean NICHE_DEPLOYMENT = 
		System.getProperty("niche.deployment.mode") instanceof String ?
				System.getProperty("niche.deployment.mode").equals("1")
			:
				false;

	/**
	 * 
	 */
	private boolean started = false;

	/**
	 * 
	 */
	private final String[] bindingList = { "registry", "overlayAccess" };

	/**
	 * 
	 */
	private Component myself;

	/**
	 * 
	 */
	private NamingService registry;

	/**
	 * 
	 */

	private OverlayAccess overlay;

	private NicheOSSupport niche;

	private NicheAsynchronousInterface logger;

	private HashMap<Component, SNR> componentsMap;

	private Map<Component, Object> localCompIdMap;

	private Map<String, SNR> naming;
	
	private Component localComponent;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public ComponentDeploymentImpl() {

		componentsMap = new HashMap<Component, SNR>();
		localCompIdMap = new HashMap<Component, Object>();
		naming = new HashMap<String, SNR>();
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
			logger = overlay.getOverlay().getNicheAsynchronousSupport();
			started = true;
			Logger.println("[ComponentDeployment] started");
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
		if (itfName.equals("registry"))
			return registry;
		else if (itfName.equals("overlayAccess"))
			return overlay;
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
		else if (itfName.equals("registry"))
			registry = (NamingService) itfValue;
		else if (itfName.equals("overlayAccess"))
			overlay = (OverlayAccess) itfValue;
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
		}
		if (itfName.equals("component"))
			myself = null;
		else if (itfName.equals("registry"))
			registry = null;
		else if (itfName.equals("overlayAccess"))
			overlay = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	// public Component deployComponent(Object type, String name, String
	// definition, Object controllerDesc, Object contentDesc, Object[]
	// packageDesc, Object context) {
	//	
	// Using RMI
	// DeploymentParams params = new DeploymentParams();
	//		
	// params.type=type;
	// params.name=name;
	// params.definition=definition;
	// params.controllerDesc=controllerDesc;
	// params.contentDesc=contentDesc;
	// params.packageDesc=packageDesc;
	//		
	// String factoryName = (String) ((Map) context).get("factoryName");
	//        
	// Component jadeNode = registry.lookup(factoryName);
	//        
	// if (jadeNode == null)
	// throw new RuntimeException("No Fractal Factory found");
	//
	// Component backend = null;
	// try {
	// backend =
	// FractalUtil.getSubComponentByPath(jadeNode,"managed_resources/ComponentDeploymentBackEnd");
	// } catch (NoSuchComponentException e) {
	// e.printStackTrace();
	// }
	//		
	// ComponentDeploymentBackEnd backendItf = null;
	// try {
	// backendItf =
	// (ComponentDeploymentBackEnd)backend.getFcInterface("componentDeploymentBackEnd");
	// } catch (NoSuchInterfaceException e) {
	// e.printStackTrace();
	// }
	//		
	//		
	// try {
	// return backendItf.deployComponentBackEnd(serialize(params));
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//	
	// return null;
	// }

	public Component deployComponent(Object type, String name,
			String definition, Object controllerDesc, Object contentDesc,
			Object[] packageDesc, Object context) {

		if ((contentDesc != null) && ("GROUP".equals((String) contentDesc))) {
			return createGroup(name);
		}
		if (("org.objectweb.jasmine.jade.ManagementType"
				.equals((String) definition))) {
			return createManagementComponent(type, name, definition,
					controllerDesc, contentDesc, packageDesc, context);
		}
		DeploymentParams params = new DeploymentParams();

		params.type = type;
		params.name = name;
		params.definition = definition;
		params.controllerDesc = controllerDesc;
		params.contentDesc = contentDesc;
		params.packageDesc = packageDesc;

		Map value = (Map) ((Map) context).get("value");
		ArrayList<NodeRef> nodes = (ArrayList<NodeRef>) value.get("NodeRefs");
		String allocationProperties = (String) value
				.get("Allocation properties");

		assert (nodes != null) : "Node not found";

		ArrayList<ResourceId> allocatedResources = niche.getJadeSupport()
				.allocate(nodes, allocationProperties);

		ArrayList<String> descriptions = new ArrayList<String>();

		for (int i = 0; i < allocatedResources.size(); i++) {

			// FIXME: hack for passing allocation token to deploy upcall
			// Should be explicitly passed as upcall argument
			params.definition = (String) allocatedResources.get(i)
					.getJadeNode();

			// names for components _with the same name_ deployed on multiple nodes
			//FIXME future work - now the code only works for single components with individual names
			params.name = name;
			//+ "_"+ String.valueOf(i+1);
			try {
				descriptions.add(Serialization.serialize(params));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ArrayList<Object[]> results = niche.getJadeSupport().deploy(
				allocatedResources, descriptions);

		//FIXME: must store other component ids too!

		ComponentId cid = (ComponentId) ((results.get(0))[1]);

		/*#%*/ logger.log("Jade says Done Deploying! ComponentId was " + cid.getId());

		Component comp;
		Ref ref;
		
//		if(NICHE_DEPLOYMENT) {
//			if(2 < (results.get(0)).length) {
//			
//				comp = ((Component)(results.get(0))[2]);
//				localComponent = comp;
//				System.out.println("storing component for " + params.name);
//			} else {
//				System.out.println("reusing old component for " + params.name);
//				comp = localComponent;
//			}
//			ref =  null;
//		} else
		
		{
			
			ref = (Ref) ((results.get(0))[0]);
	
			if (ref==null) return null;
			/*#%*/ logger.log("Jade says Ref was " + ref.toString());
	
			comp = RmiRefManagement.resolveRef(ref);
		}
		
		componentsMap.put(comp, cid);
		localCompIdMap.put(comp, ref);
	
		String ctx = (String) ((Map) context).get("nicheIdRegistryContext");
		naming.put(ctx + "/" + name, cid);
		/*#%*/ logger.log("Jade says resolved Ref & is now returning "
		/*#%*/ 			+ comp.toString());
		
		
		return comp;

	}

	private Component createGroup(String name) {
		Logger.println("[ComponentDeployment] Group creation: " + name);

		NicheComponentSupportInterface nicheComponents = niche
				.getJadeSupport();

		GroupId gid = nicheComponents.createGroup("", new ArrayList());

		//		Create a Component to represent the group
		Component tmp = UtilityComponentFactory
				.createMarkerComponent("groupMarkerInterface");
		componentsMap.put(tmp, gid);

		return tmp;
	}

	public SNR getLowerComponent(Component comp) {

		return this.componentsMap.get(comp);

	}

	public Object getLocalComponentID(Component comp) {

		return this.localCompIdMap.get(comp);
	}

	public SNR lookup(String name) {
		return naming.get(name);
	}

	private Component createManagementComponent(Object type, String name,
			String definition, Object controllerDesc, Object contentDesc,
			Object[] packageDesc, Object context) {

		// Deploy component locally
		Component res = null;
		Type fType = (Type) type;

		Component node = null;
		try {
			node = FractalUtil
					.getFirstFoundSuperComponentByServerInterfaceSignature(
							myself,
							"org.objectweb.fractal.deployment.local.api.GenericInstallingFactory");
		} catch (NoSuchComponentException e1) {
			e1.printStackTrace();
		}
		GenericInstallingFactory physicalNodeFactoryItf = null;
		try {
			physicalNodeFactoryItf = (GenericInstallingFactory) node
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

		try {
			Fractal.getNameController(res).setFcName(name);
		} catch (NoSuchInterfaceException ignored) {
		}

		/*
		 * add the component created as a sub-component of the component
		 * managed_resources of the node.
		 */

		Component comps[];
		try {
			comps = Fractal.getContentController(node).getFcSubComponents();
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

		Logger.println(DebugService.on, "[ComponentDeployment] ");
		Logger.println(DebugAdl.info, "Created management component: " + name);

		//Bind client interfaces of management component
		try {
			Component nicheOS = FractalUtil.getFirstFoundSubComponentByName(
					node, "nicheOS");
			Fractal.getBindingController(res).bindFc("overlayAccess",
					nicheOS.getFcInterface("overlayAccess"));
			Component deployment = FractalUtil.getFirstFoundSubComponentByName(
					node, "componentDeployment");
			Fractal.getBindingController(res).bindFc("nicheIdRegistry",
					deployment.getFcInterface("nicheIdRegistry"));

		} catch (NoSuchComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBindingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalLifeCycleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;
	}

}