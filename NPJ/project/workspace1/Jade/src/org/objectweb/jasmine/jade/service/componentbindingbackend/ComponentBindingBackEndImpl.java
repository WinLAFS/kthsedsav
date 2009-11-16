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

package org.objectweb.jasmine.jade.service.componentbindingbackend;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.componentdeploymentbackend.DeployedComponents;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.Invocation;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.jasmine.jade.util.Serialization;

import dks.niche.NicheOSSupport;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.messages.SendThroughBindingMessage;

/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos
 *         Parlavantzas
 * 
 */
public class ComponentBindingBackEndImpl implements LifeCycleController,
		BindingController, ComponentBindingBackEnd {

	/**
	 * 
	 */
	private boolean started = false;

	/**
	 * 
	 */
	private final String[] bindingList = { "overlayAccess",
			"deployedComponents" };

	/**
	 * 
	 */
	private Component myself;

	/**
	 * 
	 */
	private OverlayAccess overlay;

	private NicheOSSupport niche;

	private NicheAsynchronousInterface logger;

	private DeployedComponents deployed;

	private static int localBindIds = 0;

	private HashMap<Integer, Object> targetsMap;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public ComponentBindingBackEndImpl() {
		targetsMap = new HashMap<Integer, Object>();
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
			niche.registerBindHandler(this);
			niche.registerDeliverHandler(this);
			logger = niche.getNicheAsynchronousSupport();
			started = true;
			// Logger.println("[ComponentBindingBackEnd] started");
			/*#%*/ logger.log("[ComponentBindingBackEnd] started");
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
	 * @see
	 * org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang
	 * .String)
	 */
	public Object lookupFc(String itfName) throws NoSuchInterfaceException {
		if (itfName.equals("component"))
			return myself;
		else if (itfName.equals("overlayAccess"))
			return overlay;
		else if (itfName.equals("deployedComponents"))
			return deployed;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.
	 * String, java.lang.Object)
	 */
	public void bindFc(String itfName, Object itfValue)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if (started) {
			throw new IllegalLifeCycleException(itfName);
		}
		if (itfName.equals("component"))
			myself = (Component) itfValue;
		else if (itfName.equals("overlayAccess"))
			overlay = (OverlayAccess) itfValue;
		else if (itfName.equals("deployedComponents"))
			deployed = (DeployedComponents) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang
	 * .String)
	 */
	public void unbindFc(String itfName) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if (started) {
			throw new IllegalLifeCycleException(itfName);
		}
		if (itfName.equals("component"))
			myself = null;
		else if (itfName.equals("overlayAccess"))
			overlay = null;
		else if (itfName.equals("deployedComponents"))
			deployed = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public Object[] bind(Object localComponentID, Object description)
			throws IOException {
		// Niche bind upcall

		String descr = (String) description;

		/*#%*/ logger.log("[ComponentBindingBackEnd]: received bind upcall with component: "
		/*#%*/ 				+ localComponentID + " and description: " + descr);

		int i1 = descr.indexOf('-');
		int i2 = descr.indexOf(':');
		String mode = descr.substring(0, i1);
		String itf;
		String id = null;

		if (i2 > 0) {
			itf = descr.substring(i1 + 1, i2);
			id = descr.substring(i2 + 1);
		} else {
			itf = descr.substring(i1 + 1);
		}

		Component c = (Component) localComponentID; // deployed.getDeployedComponent(refStr);

		assert (c != null) : "[ComponentBindingBackEnd]: c is null";

		// Logger.println("[ComponentBindingBackEnd]: mode: " + mode);
		/*#%*/ logger.log("[ComponentBindingBackEnd]: mode: " + mode);

		localBindIds++;
		Integer generatedLocalBindId = new Integer(localBindIds);

		// Component c = deployed.getDeployedComponent(refStr);

		assert (c != null) : "[ComponentBindingBackEnd]: c is null";

		if ("SEND".equals(mode)) {
			try {
				Object stub =
					Stub.newInstance(
							c,
							itf,
							generatedLocalBindId,
							niche
					);
				/*#%*/ logger.log("[ComponentBindingBackEnd]: stub obtained: "); // Logger.println("[ComponentBindingBackEnd]:
				// stub
				// obtained:
				// ");
				assert (stub != null) : "[ComponentBindingBackEnd]: stub is null";
				Fractal.getBindingController(c).bindFc(itf, stub);

			} catch (NoSuchInterfaceException e1) {
				e1.printStackTrace();
			} catch (IllegalBindingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalLifeCycleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if ("SEND_WITH_REPLY".equals(mode)) {
			try {

				Object stub = ReplyStub.newInstance(c, itf, id,
						generatedLocalBindId, niche);
				/*#%*/ logger.log("[ComponentBindingBackEnd]: reply-stub obtained: ");
				assert (stub != null) : "[ComponentBindingBackEnd]: stub is null";
				Fractal.getBindingController(c).bindFc(itf, stub);

			} catch (NoSuchInterfaceException e1) {
				e1.printStackTrace();
			} catch (IllegalBindingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalLifeCycleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if ("RECV".equals(mode)) {
			Object target = null;
			try {
				target = (Interface) c.getFcInterface(itf);
			} catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}
			assert (target != null) : "[ComponentBindingBackEnd]: target is null";
			targetsMap.put(generatedLocalBindId, target);
		}
		// I assume it returns {status, localbindId}
		return new Object[] { "OK", generatedLocalBindId };
	}

	public void deliver(Object localBindId, Object message) {
		// Niche deliver upcall

		Object delivered = ((SendThroughBindingMessage) message).getMessage();
		/*#%*/ logger.log("[ComponentBindingBackEnd]: Message " + delivered
		/*#%*/ 		+ " with local bind id " + localBindId + "was delivered.");
		Object target = targetsMap.get(localBindId);
		assert (target != null);
		Invocation inv = (Invocation) delivered;
		try {

			/*#%*/ logger.log("Delivering: before method " + inv.getName());
			try {
				inv.invoke(target);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			throw new RuntimeException("unexpected invocation exception: "
					+ e.getMessage());
		} /*#%*/ finally {
		/*#%*/ 	logger.log("Delivering: after method " + inv.getName());
		/*#%*/ }

	}

	public Object deliver(Object localBindId, Object message,
			Boolean returnValueDesired) {
		// Niche deliver upcall

		Object delivered = ((SendThroughBindingMessage) message).getMessage();
		/*#%*/ logger.log("[ComponentBindingBackEnd]: RequestMessage " + delivered
		/*#%*/ 		+ " with local bind id " + localBindId + "was delivered.");
		/*#%*/ 
		Object target = targetsMap.get(localBindId);
		Object returnValue = null;
		assert (target != null);
		Invocation inv = (Invocation) delivered;
		try {

			/*#%*/ logger.log("Delivering with expected return value: before method "
			/*#%*/ 		+ inv.getName());
			try {
				returnValue = inv.invoke(target);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			throw new RuntimeException("unexpected invocation exception: "
					+ e.getMessage());
		}/*#%*/  finally {
		/*#%*/ 	logger.log("Delivering: after method " + inv.getName());
		/*#%*/ }
		return returnValue;
	}

}

class Stub implements InvocationHandler {

	private Object serverRef;

	private NicheAsynchronousInterface logger;
	private NicheActuatorInterface niche;

	//private Component owner;

	private String itf;

	private String signature;

	private Component comp;

	// preloaded Method objects for the methods in java.lang.Object
	private static Method hashCodeMethod;

	private static Method equalsMethod;

	private static Method toStringMethod;

	private static final Class[] EMPTYCL = new Class[0];

	static {
		try {
			hashCodeMethod = Object.class.getMethod("hashCode", EMPTYCL);
			equalsMethod = Object.class.getMethod("equals",
					new Class[] { Object.class });
			toStringMethod = Object.class.getMethod("toString", EMPTYCL);
		} catch (NoSuchMethodException e) {
			throw new NoSuchMethodError(e.getMessage());
		}
	}

	Stub(Component c, String itf, Object serverRef, NicheOSSupport niche,
			String signature) {

		this.comp = c;
		this.itf = itf;
		this.serverRef = serverRef;
		this.logger = niche.getNicheAsynchronousSupport();
		this.niche = niche.getComponentSupport(c);
		this.signature = signature;
	}

	public static Object newInstance(Component c, String itf, Object serverRef,
			NicheOSSupport niche) {

		Interface iface = null;
		Class cl = null;
		String signature = null;

		try {
			iface = (Interface) c.getFcInterface(itf);
			InterfaceType itfType = (InterfaceType) iface.getFcItfType();
			signature = itfType.getFcItfSignature();
			cl = Class.forName(signature);
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}

		return java.lang.reflect.Proxy.newProxyInstance(cl.getClassLoader(),
				new Class[] { cl, Interface.class }, new Stub(c, itf,
						serverRef, niche, signature));

	}

	public Object invoke(Object proxy, Method m, Object[] args)
			throws Throwable {
		/*#%*/ try {
			
			// Logger.println("Stub: before method " + m.getName());

			Class declaringClass = m.getDeclaringClass();

			if (declaringClass == Object.class) {
				if (m.equals(hashCodeMethod)) {
					return proxyHashCode(proxy);
				} else if (m.equals(equalsMethod)) {
					return proxyEquals(proxy, args[0]);
				} else if (m.equals(toStringMethod)) {
					return proxyToString(proxy);
				} else {
					throw new InternalError(
							"unexpected Object method dispatched: " + m);
				}
			}
			if ("getFcItfType".equals(m.getName())) {
				Component boot = Fractal.getBootstrapComponent();
				TypeFactory tf = Fractal.getTypeFactory(boot);
				return tf.createFcItfType(this.itf, this.signature, false,
						false, false);
			}
			if ("getFcItfOwner".equals(m.getName())) {

				/* to emulate local binding */
				return this.comp;
			}
			Invocation inv = new Invocation(m, args);
			
			/*#%*/ logger.log("Stub: Handling method "
			/*#%*/ 		+ m.getName()
			/*#%*/ 		+ " - sending invocation through Niche."
			/*#%*/ 		+ inv.getName()
			/*#%*/ );
			
			if (args.length > 0) {
				Object lastArgument = args[args.length - 1];
				if (lastArgument instanceof ComponentId) {
					niche.sendOnBinding(serverRef, inv, (ComponentId) lastArgument);
					// NPB //FIXME
				} else {
					niche.sendOnBinding(serverRef, inv, null);
				}
			} else {
				niche.sendOnBinding(serverRef, inv, null);
			}

			/*#%*/ } finally {
			/*#%*/ 	logger.log("Stub: after method " + m.getName());
			/*#%*/ 	}
		return null;
	}

	protected Integer proxyHashCode(Object proxy) {
		return new Integer(System.identityHashCode(proxy));
	}

	protected Boolean proxyEquals(Object proxy, Object other) {
		return (proxy == other ? Boolean.TRUE : Boolean.FALSE);
	}

	protected String proxyToString(Object proxy) {
		return proxy.getClass().getName() + '@'
				+ Integer.toHexString(proxy.hashCode());
	}
}

class ReplyStub implements InvocationHandler {

	private Object serverRef;

	private NicheAsynchronousInterface logger;

	private NicheActuatorInterface niche;

	private Component owner;

	private String itf;

	private String signature;

	private Component comp;

	// preloaded Method objects for the methods in java.lang.Object
	private static Method hashCodeMethod;

	private static Method equalsMethod;

	private static Method toStringMethod;

	private static final Class[] EMPTYCL = new Class[0];

	static {
		try {
			hashCodeMethod = Object.class.getMethod("hashCode", EMPTYCL);
			equalsMethod = Object.class.getMethod("equals",
					new Class[] { Object.class });
			toStringMethod = Object.class.getMethod("toString", EMPTYCL);
		} catch (NoSuchMethodException e) {
			throw new NoSuchMethodError(e.getMessage());
		}
	}

	ReplyStub(Component c, String itf, Object serverRef, NicheOSSupport niche,
			String id, String signature) {

		this.comp = c;
		this.itf = itf;
		this.serverRef = serverRef;
		this.logger = niche.getNicheAsynchronousSupport();
		this.niche = niche.getComponentSupport(c);
		this.signature = signature;
	}

	public static Object newInstance(Component c, String itf, String id,
			Object serverRef, NicheOSSupport niche) {

		Interface iface = null;
		Class cl = null;
		String signature = null;

		try {
			iface = (Interface) c.getFcInterface(itf);
			InterfaceType itfType = (InterfaceType) iface.getFcItfType();
			signature = itfType.getFcItfSignature();
			cl = Class.forName(signature);
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}

		return java.lang.reflect.Proxy.newProxyInstance(cl.getClassLoader(),
				new Class[] { cl, Interface.class }, new ReplyStub(c, itf,
						serverRef, niche, id, signature));

	}

	public Object invoke(Object proxy, Method m, Object[] args)
			throws Throwable {
		/*#%*/ try {
			/*#%*/ logger.log("Reply-stub: before method " + m.getName());
			// Logger.println("Stub: before method " + m.getName());

			Class declaringClass = m.getDeclaringClass();

			if (declaringClass == Object.class) {
				if (m.equals(hashCodeMethod)) {
					return proxyHashCode(proxy);
				} else if (m.equals(equalsMethod)) {
					return proxyEquals(proxy, args[0]);
				} else if (m.equals(toStringMethod)) {
					return proxyToString(proxy);
				} else {
					throw new InternalError(
							"unexpected Object method dispatched: " + m);
				}
			}
			if ("getFcItfType".equals(m.getName())) {
				Component boot = Fractal.getBootstrapComponent();
				TypeFactory tf = Fractal.getTypeFactory(boot);
				return tf.createFcItfType(this.itf, this.signature, false,
						false, false);
			}
			if ("getFcItfOwner".equals(m.getName())) {

				/* to emulate local binding */
				return this.comp;
			}
			Invocation inv = new Invocation(m, args);
			/*#%*/ 	logger.log("Reply-stub: sending through Niche invocation: "
			/*#%*/ 			+ inv.getName());
			// Logger.println("Stub: sending through Niche invocation: "
			// + inv.getName());

			return niche.sendWithReply(serverRef, inv);

			/*#%*/ } finally {
			// Logger.println("Stub: after method " + m.getName());
			/*#%*/ 	logger.log("Reply-stub: after method " + m.getName());
			/*#%*/ }

	}

	protected Integer proxyHashCode(Object proxy) {
		return new Integer(System.identityHashCode(proxy));
	}

	protected Boolean proxyEquals(Object proxy, Object other) {
		return (proxy == other ? Boolean.TRUE : Boolean.FALSE);
	}

	protected String proxyToString(Object proxy) {
		return proxy.getClass().getName() + '@'
				+ Integer.toHexString(proxy.hashCode());
	}
}
