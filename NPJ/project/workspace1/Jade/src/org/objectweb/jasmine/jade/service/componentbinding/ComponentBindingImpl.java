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

package org.objectweb.jasmine.jade.service.componentbinding;

import java.util.ArrayList;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.rmi.io.Ref;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.componentdeployment.LowerComponents;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.jasmine.jade.util.Serialization;

import dks.niche.NicheOSSupport;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;
import dks.niche.ids.BindId;


/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos
 *         Parlavantzas
 * 
 */
public class ComponentBindingImpl implements LifeCycleController,
		BindingController, ComponentBinding {

	/**
	 * 
	 */
	private boolean started = false;

	/**
	 * 
	 */
	private final String[] bindingList = { "lowerComponents", "overlayAccess" };

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
	private LowerComponents lowercomps;

	private OverlayAccess overlay;

	private NicheOSSupport niche;

	private NicheAsynchronousInterface myAsynchronousInterface;

	private NicheComponentSupportInterface nicheComponents;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public ComponentBindingImpl() {

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
			this.niche = overlay.getOverlay();
			this.myAsynchronousInterface = niche.getNicheAsynchronousSupport();
			nicheComponents = niche.getJadeSupport();
			started = true;
			Logger.println("[ComponentBinding] started");
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
		else if (itfName.equals("lowerComponents"))
			return lowercomps;
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
		else if (itfName.equals("lowerComponents"))
			lowercomps = (LowerComponents) itfValue;
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
		else if (itfName.equals("lowerComponents"))
			lowercomps = null;
		else if (itfName.equals("overlayAccess"))
			overlay = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void bindComponent(int type, Object client, String clientItf,
			Object server, String serverItf, String bindingType, Object context)
			throws Exception {

		Object itf = null;
		try {
			itf = ((Component) client).getFcInterface("groupMarkerInterface");

		} catch (NoSuchInterfaceException e1) {

			// Client is not a group

			if ("oneway".equals(bindingType)) {
				onewayBind(type, client, clientItf, server, serverItf,
						bindingType, context);
			} else if ("groupAny".equals(bindingType)) {
				groupClientBind(type, client, clientItf, server, serverItf,
						bindingType, context, JadeBindInterface.ONE_TO_ANY);
			} else if ("groupAll".equals(bindingType)) {
				groupClientBind(type, client, clientItf, server, serverItf,
						bindingType, context, JadeBindInterface.ONE_TO_MANY);
			} else
				rmiBind(type, client, clientItf, server, serverItf,
						bindingType, context);
			return;
		}
		// Client is a group
		groupMemberBind(type, client, clientItf, server, serverItf,
				bindingType, context);

	}

	public void rmiBind(int type, Object client, String clientItf,
			Object server, String serverItf, String bindingType, Object context)
			throws Exception {

		BindingController bc = Fractal.getBindingController((Component) client);
		Object itf;
		if (type == IMPORT_BINDING) {
			itf = Fractal.getContentController((Component) server)
					.getFcInternalInterface(serverItf);
		} else {
			itf = ((Component) server).getFcInterface(serverItf);
		}
		bc.bindFc(clientItf, itf);
	}

	private void onewayBind(int type, Object client, String clientItf,
			Object server, String serverItf, String bindingType, Object context)
			throws Exception {

		ComponentId clientCid = (ComponentId) lowercomps
				.getLowerComponent((Component) client);
		ComponentId serverCid = (ComponentId) lowercomps
				.getLowerComponent((Component) server);

		ArrayList<ComponentId> receivers = new ArrayList<ComponentId>();
		receivers.add(serverCid);

		String sendDescr = "SEND-" + clientItf + ":"; // +
														// Serialization.serialize(clientRef);
		String receiveDescr = "RECV-" + serverItf + ":"; // +
															// Serialization.serialize(serverRef);
		
		// binding should be synchronous, how?
		nicheComponents.bind(clientCid, sendDescr, serverCid, receiveDescr, JadeBindInterface.ONE_TO_ONE);
	}

	private void groupClientBind(int type, Object client, String clientItf,
			Object server, String serverItf, String bindingType,
			Object context, int mode) throws Exception {

		Logger.println("[ComponentBinding] groupClientBind");
		ComponentId clientCid = (ComponentId) lowercomps
				.getLowerComponent((Component) client);
		GroupId groupId = null;

		groupId = (GroupId) lowercomps.getLowerComponent((Component) server);

		String sendDescr = "SEND-" + clientItf + ":";
		String receiveDescr = "RECV-" + serverItf + ":";

		// binding should be synchronous, how?
//		BindId b = new BindId(myAsynchronousInterface, clientCid, groupId,
//				sendDescr, receiveDescr, mode);
//
//		b.activateOneToGroup();
		
		nicheComponents.bind(clientCid, sendDescr, groupId, receiveDescr, mode);
		
		// nicheComponents.dynamicBind(clientCid, groupId, sendDescr,
		// receiveDescr,mode);
	}

	private void groupMemberBind(int type, Object client, String clientItf,
			Object server, String serverItf, String bindingType, Object context)
			throws Exception {
		Logger.println("[ComponentBinding] groupMemberBind");
		GroupId groupId = null;
		groupId = (GroupId) lowercomps.getLowerComponent((Component) client);
		ComponentId serverCid = (ComponentId) lowercomps
				.getLowerComponent((Component) server);


		NicheComponentSupportInterface nicheComponents = niche.getJadeSupport();


		nicheComponents.addToGroup(serverCid, groupId);

	}
}