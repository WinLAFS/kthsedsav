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

package org.objectweb.jasmine.jade.service.nodemanager;

import java.lang.management.ManagementFactory;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.JadeException;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.ungoverned.oscar.FilterImpl;

import com.sun.management.OperatingSystemMXBean;

/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos
 *         Parlavantzas
 * 
 */
public class NodeManagerImpl implements LifeCycleController, BindingController,
		NodeManager {

	private boolean started;

	private final String[] bindingList = {};

	private Component myself;

	private OperatingSystemMXBean mxbean = (OperatingSystemMXBean) ManagementFactory
	.getOperatingSystemMXBean();
	
	private static final int TOTAL_STORAGE=1000;
	int freeStorage = TOTAL_STORAGE;

	private Map<String,Allocation> allocations= new Hashtable<String, Allocation>();
	

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public NodeManagerImpl() throws JadeException {

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
			started = true;


			
			Logger.println("[NodeManager] started");
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
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public boolean satisfies(String Requirements) {

		// requirements have LDAP filter syntax
		Filter f = null;
		try {
			f = new FilterImpl(Requirements);
		} catch (InvalidSyntaxException e) {
			Logger.println("[NodeManager] Invalid syntax for filter: "+Requirements);
			return false;
		}
		return (f.match((Dictionary)getProperties()));
	}

	// ------------------------------------------------------------------------
	// Private Methods
	// ------------------------------------------------------------------------
	private String getJadeNodeName() {
		String result = "";
		try {
			Component jadeNode = FractalUtil
					.getFirstFoundSuperComponentByServerInterfaceSignature(
							myself,
							"org.objectweb.fractal.deployment.local.api.Installer");
			result = Fractal.getNameController(jadeNode).getFcName();
		} catch (NoSuchComponentException e) {
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		return result;
	}

	public String allocate(String allocationProperties) {
		
		int storageShare=0;
		if (allocationProperties!=null) {
			assert allocationProperties.startsWith("storageShare="): "Assertion properties must have format: storageShare=<number>";
			storageShare = Integer.parseInt(allocationProperties.substring(13));
		}
		if ((freeStorage-storageShare)<0) return null; 
		freeStorage=freeStorage-storageShare;
		Allocation alloc=new Allocation(storageShare);
		String ref=alloc.getAllocationRef();
		allocations.put(ref, alloc);
		return ref;
	}

	public void deallocate(String allocRef) {
		Allocation alloc= allocations.get(allocRef);
		if (alloc==null) return;
		freeStorage=freeStorage+alloc.getStorageShare();
		allocations.remove(allocRef);
	}
	
	
	public Map<String, Object> getProperties() {

		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		
		
		// Memory measured in megabytes
		properties.put("freePhysicalMemory", mxbean.getFreePhysicalMemorySize()/1048576);
		
		properties.put("totalPhysicalMemory", mxbean
				.getTotalPhysicalMemorySize()/1048576);
		properties.put("availableProcessors", mxbean.getAvailableProcessors());
		properties.put("OSName", mxbean.getName());
		
		properties.put("totalStorage", TOTAL_STORAGE);
		properties.put("freeStorage", freeStorage);
		
		properties.put("jadeName", getJadeNodeName());
		properties.put("networkSpeed", "medium");
		
		return properties;
	}
	
	public String getPropertiesAsString() {
		
		String result="";
		Map<String, Object> map=getProperties();
		for (Map.Entry<String, Object> entry : map.entrySet())
		{
		    result=result+entry.getKey() + " : " + entry.getValue()+"\n";
		}
		return result;
	}
	

}
