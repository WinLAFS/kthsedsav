/*
 * Created on 10 oct. 06
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.jasmine.jade.control.repair.reactor;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.control.repair.util.event.Event;
import org.objectweb.jasmine.jade.control.repair.util.event.EventBody;
import org.objectweb.jasmine.jade.control.repair.util.event.EventHandler;
import org.objectweb.jasmine.jade.control.repair.util.event.EventType;

import fr.jade.reflex.api.control.ContentNotificationController;
import fr.jade.reflex.util.Reflex;

import org.objectweb.jasmine.jade.osgi.JadeProperties;
import org.objectweb.jasmine.jade.service.allocator.Allocator;
import org.objectweb.jasmine.jade.util.FractalUtil;

public class BasicPolicyImpl implements BindingController, EventHandler {

	/**
	 * 
	 */
	private EventHandler eventOut;

	/**
	 * 
	 */
	private Allocator allocator;

	/**
	 * 
	 */
	private NamingService ns;

	// ------------------------------------------------------------------------
	// Implementation of BindingController interface
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String,
	 *      java.lang.Object)
	 */
	public void bindFc(String clientItfName, Object serverItf)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if (clientItfName.equals("event-out"))
			eventOut = (EventHandler) serverItf;
		else if (clientItfName.equals("allocator"))
			allocator = (Allocator) serverItf;
		else if (clientItfName.equals("registry"))
			ns = (NamingService) serverItf;
		else
			throw new NoSuchInterfaceException(clientItfName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#listFc()
	 */
	public String[] listFc() {
		return new String[] { "event-out", "allocator", "registry" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
	 */
	public Object lookupFc(String clientItfName)
			throws NoSuchInterfaceException {
		if (clientItfName.equals("event-out"))
			return eventOut;
		if (clientItfName.equals("allocator"))
			return allocator;
		if (clientItfName.equals("registry"))
			return ns;
		else
			throw new NoSuchInterfaceException(clientItfName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
	 */
	public void unbindFc(String clientItfName) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if (clientItfName.equals("event-out"))
			eventOut = null;
		else if (clientItfName.equals("allocator"))
			allocator = null;
		else if (clientItfName.equals("registry"))
			ns = null;
		else
			throw new NoSuchInterfaceException(clientItfName);
	}

	// ------------------------------------------------------------------------
	// Implementation of EventHandler interface
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.jade.service.repair.util.event.EventHandler#handleEvent(fr.jade.service.repair.util.event.Event)
	 */
	public void handleEvent(Event e) throws Exception {

		if (e.getEventType().equals(EventType.NODEFAILURE)) {

			EventBody eb = e.getEventBody();

			Component sr = ns.lookup(JadeProperties.getInstance().getSystemRepresentationName());
			Component failedNode_M = FractalUtil.getDirectSubComponentByName(
					sr, (String) eb.get("failedNode"));

			Component managedResources = FractalUtil
					.getFirstFoundSubComponentByName(failedNode_M,
							"managed_resources");

			Component subCmps_M[] = Fractal.getContentController(
					managedResources).getFcSubComponents();

			/*
			 * if there's something to repair
			 */
			if (subCmps_M.length != 0) {

				Component newNode_E = allocator.alloc();
				Component newNode_M = Reflex.getDualComponent(newNode_E);

				eb.put("newNode_M", newNode_M);

				/*
				 * compute target architecture
				 */

				ContentNotificationController newNodeCCN_M = Reflex
						.getContentNotificationController(newNode_M);

				for (Component subCmp_M : subCmps_M) {
					newNodeCCN_M.addFcSubComponentNotification(subCmp_M);
				}

				/*
				 * deploy
				 */
				eventOut.handleEvent(e);
				
			} else {
				System.out
						.println("[RepairManager] There's nothing to repair on node : "
								+ Fractal.getNameController(failedNode_M)
										.getFcName());
			}

			System.out.println("[RepairManager] Reparation done ");
		}
	}

}
