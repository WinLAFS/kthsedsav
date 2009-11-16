/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package yass.aggregators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.LogManager;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import yass.events.ReplicaChangeEvent;
import dks.addr.DKSRef;
import dks.niche.events.ComponentFailEvent;
import dks.niche.events.ResourceLeaveEvent;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;

/**
 * The <code>FileReplicaAggregator</code> class
 *
 * @author Joel
 * @version $Id: FileReplicaAggregator.java 294 2006-05-05 17:14:14Z joel $
 */
public class FileReplicaAggregator implements EventHandlerInterface, MovableInterface, InitInterface, BindingController, LifeCycleController {

	//There is one FileReplicaAggregator for each "file group"

	/////////////////////
	Component mySelf;
	TriggerInterface triggerInterface;
	NicheAsynchronousInterface logger;
	
	GroupId gid;
	
	NicheId myId;
	
	//HashMap<String, Boolean> leavingResources;
	ArrayList<String> leavingResources;
	
	private boolean status;
	private boolean hasLoggedStartup = false;
	
	public FileReplicaAggregator() {
		
	}


	public void init(NicheActuatorInterface actuator) {
		logger = actuator.testingOnly();
		if(myId != null && !hasLoggedStartup) {
			hasLoggedStartup = true;
			//System.out.println("FileReplicaAggregator with id " + myId + " created for the group "+gid.getId());
			/*#%*/ logger.log("FileReplicaAggregator with id " + myId + " created for the group "+gid.getId());
		}

	}

	public synchronized void eventHandler(Object e, int flag) {
		//System.out.println("FRA says: I got event concerning a lost member of my group: "+gid.getId());
		
		
		String temp;
		/*#%*/ String logMessage = "FRA " + myId + " says: I got event concerning a lost member of my group: "+gid.getId();
		
		if(e instanceof ResourceLeaveEvent) {
			temp = ((ResourceLeaveEvent)e).getDKSRef().getId().toString();
			
			//now, things are just stored and never deleted. garbage collection must be added!
			//to be able to check whether a failed resource previously did trigger a leave
			
			//leavingResources.put(temp, true);
			leavingResources.add(temp);
			triggerInterface.trigger(new ReplicaChangeEvent(temp, gid));
		}
		else if(e instanceof ComponentFailEvent) {

			temp = ((ComponentFailEvent)e).getDKSRef().getId().toString();
			
			//event is only triggered if the resource did not signal a leave before failing
			if(!leavingResources.contains(temp)) {
				leavingResources.add(temp);
				
				triggerInterface.trigger(new ReplicaChangeEvent(temp, gid));
				/*#%*/ logMessage += "\nTriggering for " + gid.getId() + " since " + temp + " is leaving the system";
				
			}/*#%*/  else {
			/*#%*/ 	logMessage += "\nNOT triggering for " + gid.getId() + " since " + temp + " has already left the system";
			/*#%*/ }
	
		}
		
		/*#%*/ logger.log(logMessage);
		//System.out.println(logMessage);

				
	}
	

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheEventHandlerInterface#init(java.lang.Object[])
	 */
	public void init(Object[] parameters) {
		gid = (GroupId) parameters[0];
		leavingResources = new ArrayList();
		//System.out.println("FileReplicaAggregator created! for the group "+gid.getId());		

	}
	public void reinit(Object[] parameters) {
		gid = (GroupId) parameters[0];
		leavingResources = (ArrayList<String>)parameters[1];
		//System.out.println("FileReplicaAggregator created! for the group "+gid.getId());		

	}
	

	public void initId(Object id) {
		//Used, Not used?
		myId = (NicheId) id; 
		
		if(logger != null && !hasLoggedStartup) {
			hasLoggedStartup = true;
			/*#%*/ logger.log("FileReplicaAggregator with id " + myId + " created for the group "+gid.getId());
		}
	}
	
	public Object[] getAttributes() {
		return new Object [] {
				gid,
				leavingResources
		};
	}

	//FRACTAL STUFF

	public String[] listFc() {
		return new String[] {FractalInterfaceNames.COMPONENT, FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE, FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE};
	}


	public Object lookupFc(String interfaceName) throws NoSuchInterfaceException {
		if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			return mySelf;
		else if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return null;
		else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			return triggerInterface;
		else
			throw new NoSuchInterfaceException(interfaceName);
	}

	
	public void bindFc(String interfaceName, Object stub) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
			mySelf = (Component) stub;
			//System.err.println("setting the component interface");
		} else if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE)) {
			//nuffin			
		} else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE)) {
			triggerInterface = (TriggerInterface)stub;			
		} else
			throw new NoSuchInterfaceException(interfaceName);
	}


	public void unbindFc(String interfaceName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
			mySelf = null;
		} else if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE)) {
			//nuffin			
		} else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE)) {
			triggerInterface = null;			
		} else
			throw new NoSuchInterfaceException(interfaceName);

		
	}


	public String getFcState() {
		return status ? "STARTED": "STOPPED";
	}


	public void startFc() throws IllegalLifeCycleException {
		status = true;		
	}


	public void stopFc() throws IllegalLifeCycleException {
		status = false;
		
	}



}
