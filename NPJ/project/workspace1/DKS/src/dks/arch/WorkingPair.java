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

/**
 * The <code>WorkingPair</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: WorkingPair.java 266 2007-03-29 16:34:14Z Roberto $
 */
public class WorkingPair {

	private Class event;

	private Class component;

	/**
	 * @param event
	 * @param component
	 */
	public WorkingPair(Class event, Class component) {
		super();
		this.event = event;
		this.component = component;
	}

	/**
	 * @return Returns the component.
	 */
	public Class getComponentClass() {
		return component;
	}

	/**
	 * @return Returns the event.
	 */
	public Class getEventClass() {
		return event;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result
				+ ((component == null) ? 0 : component.hashCode());
		result = PRIME * result + ((event == null) ? 0 : event.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final WorkingPair other = (WorkingPair) obj;
		if (component == null) {
			if (other.component != null)
				return false;
		} else if (!component.equals(other.component))
			return false;
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (!event.equals(other.event))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return event.getSimpleName() + "-" + component.getSimpleName();
	}

}
