/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.arch;

import java.lang.reflect.Method;

/**
 * The <code>Subscription</code> class
 * 
 * @author Roberto Roverso
 * @version $Id: Subscription.java 220 2007-03-03 17:54:27Z Roberto $
 */
public class Subscription {

	private Component component;

	private Method method;

	private Class event;

	/*
	 * A subscription is made by the object of the component that is responsible
	 * for that subscription and the method to be called for handling that
	 * event, plus the vent itself
	 */
	public Subscription(Component component, Method method, Class event) {
		this.component = component;
		this.method = method;
		this.event = event;
	}

	public Class getEventClass() {
		return event;
	}

	public Method getMethod() {
		return method;
	}

	public Component getComponent() {
		return component;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Subscription other = (Subscription) obj;
		if (event == null) {
			if (other.event != null) {
				return false;
			}
		} else if (!event.equals(other.event)) {
			return false;
		}
		if (method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!method.equals(other.method)) {
			return false;
		}
		if (component != other.component) {
			return false;
		}
		return true;
	}

}
