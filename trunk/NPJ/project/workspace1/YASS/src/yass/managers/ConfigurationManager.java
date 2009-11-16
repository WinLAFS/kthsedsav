/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package yass.managers;

import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.jasmine.jade.service.componentdeployment.DeploymentParams;
import org.objectweb.jasmine.jade.util.Serialization;

import dks.niche.exceptions.OperationTimedOutException;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.ResourceRef;
import yass.events.StorageAvailabilityChangeEvent;
import yass.frontend.FrontendImpl;

/**
 * The <code>ConfigurationManager</code> class
 *
 * @author Joel
 * @version $Id: ConfigurationManager.java 294 2006-05-05 17:14:14Z joel $
 */
public class ConfigurationManager implements EventHandlerInterface, MovableInterface, InitInterface, BindingController, LifeCycleController {


	//Client interfaces
	NicheActuatorInterface myManagementInterface;
	
	NicheAsynchronousInterface logger;
	/////////////////////
	Component mySelf;
	NicheId myId;

	//This is/should be the order of the parameters:
	//0
	//NicheId componentGroupId;
	//not sent
	GroupId componentGroup;
	//1
	Object deploymentParams ;
	//2
	//BundleDescription preferenceHolder;
	Object preferenceHolder;
	//3
	int capacityLowThreshold;
	//4
	double loadHighThreshold;
	//5
	double loadLowThreshold;

	//local stuff:
	
	static final int TEST_MODE = System.getProperty("yass.test.mode") instanceof String ? Integer.parseInt(System.getProperty("yass.test.mode")) : -1;
	
	private static String discoverPrefix = "dynamic:";
	private boolean executing = false;
	
	final int numberOfArguments = 6;

	private boolean status, hasShownMessage = false;
	
	private Object[] param;
	
	String helloMessage = null;
	
	/*
	  Let's agree on an ordering of things concerning ME and SNRs:
	  
	  1 - constructors
	  2 - activation, sending itself to receiver side
	  3 - connection, connecting itself to MEContainer on receiver side
	  4 - saved for migration, store/restore state!
	  5 - add/remove sources
	  6 - add/remove sinks
	  7 - other helper methods
	  n - getters and setters, last
	
	*/
	
	
	public ConfigurationManager() {
//		if(TEST_MODE == FrontendImpl.DEMO_MODE) {
//			System.out.println(">>>>>>>>> ConfigurationManager says: CREATED");
//		}
	}
	

	public void init(Object [] parameters) {
		
		helloMessage = "ConfigurationManager is initialized: ";
		this.param = parameters; //no need to modify it, the object is close-enough to stateless
		if(myId != null) {
			init(parameters, helloMessage + myId.getReplicaNumber());
		}
	}
	
	public void reinit(Object [] parameters) {
		
		//Since it is kept stateless, we can do this:
		helloMessage = "ConfigurationManager is re-initialized: ";
		this.param = parameters; //no need to modify it, the object is close-enough to stateless
		if(myId != null) {
			init(parameters, helloMessage + myId.getReplicaNumber());
		}

	}
	
	private void init(Object [] parameters, String message) {
		
		this.componentGroup = (GroupId)parameters[0];
		//Test hack!
		if(parameters.length > numberOfArguments) {
			this.deploymentParams = (String)parameters[1];
		} else {
			this.deploymentParams = (DeploymentParams)parameters[1];
		}
		this.preferenceHolder = discoverPrefix + parameters[2];
		this.capacityLowThreshold = (Integer)parameters[3];
		this.loadHighThreshold = (Double)parameters[4];
		this.loadLowThreshold = (Double)parameters[5];
		if(TEST_MODE == FrontendImpl.DEMO_MODE || TEST_MODE == FrontendImpl.FAIL_TEST) {
			System.out.println(message);
		}

	}
	public void init(NicheActuatorInterface managementInterface) {
		myManagementInterface = managementInterface;
		logger = managementInterface.testingOnly();
	}
	
	public void initId(Object id) {
		myId = (NicheId)id;
		if(param != null) {
			init(param, helloMessage + myId.getReplicaNumber());
		}

	}
	
	public Object[] getAttributes() {
		return this.param; //no need to modify it, the object is close-enough to stateless
	}
	
	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheEventHandlerInterface#eventHandler(dks.arch.Event)
	 */
	public void eventHandler(Object ev, int mode) {
		
		/*#%*/ String logMessage = "The ConfigurationManager received an event";
		
		if(!executing && myId.getReplicaNumber() < 1) {
			
			executing = true;
			boolean notDone = true;
			
			/*#%*/ logMessage += ", and started executing";
			/*#%*/ if(TEST_MODE == FrontendImpl.DEMO_MODE || TEST_MODE == FrontendImpl.FAIL_TEST ) {
			/*#%*/ 		System.out.println(logMessage);
			/*#%*/ }
			
			StorageAvailabilityChangeEvent event = (StorageAvailabilityChangeEvent)ev;
			//System.out.println("ConfigurationManager says: Start event handling. Stats: "+event.getTotalLoad() +" "+ loadHighThreshold +" "+ event.getTotalCapacity()+ " "+ capacityLowThreshold);
			
			if(event.getTotalLoad() > loadHighThreshold || event.getTotalCapacity() < capacityLowThreshold) {
				
				while(notDone) {
					
				//find, allocate & add to group
				
				/*#%*/ logMessage += ", trying to grab a new resource"; 
				/*#%*/ logger.log(logMessage);
				
				NodeRef newResource = null;
				try {
					newResource = myManagementInterface.oneShotDiscoverResource(preferenceHolder);
				} catch (OperationTimedOutException e) {
					/*#%*/ System.out.println("Discover operation timed out, retry a bit later");
					break;
				}
				
				if(newResource != null) {
					
					hasShownMessage = false;
					
					/*#%*/ logMessage = "ConfigurationManager was given a resource at "+ newResource.getDKSRef() + ". Allocate it";
					/*#%*/ if(TEST_MODE == FrontendImpl.DEMO_MODE || TEST_MODE == FrontendImpl.FAIL_TEST) {
					/*#%*/ 	System.out.println(logMessage);
					/*#%*/ }
					
					ArrayList at = null;
					try {
						at = myManagementInterface.allocate(newResource, null); //specification(null, newRid));
					} catch (OperationTimedOutException e) {
						System.out.println("Allocate operation timed out, retry a bit later");
						break;
					}
					ResourceRef allocatedResource = (ResourceRef)at.get(0);
					
//					if(TEST_MODE == FrontendImpl.DEMO_MODE) {
//						System.out.println("ConfigurationManager is deploying a new component");
//					}
					
					String depParams = null;
					try {
						depParams = Serialization.serialize(deploymentParams);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					try {
						at = myManagementInterface.deploy(allocatedResource, depParams);
					} catch (OperationTimedOutException e) {
						System.out.println("Deploy operation timed out, retry a bit later");
						break;
					}
					
					ComponentId cid = (ComponentId)((Object[])at.get(0))[1]; //0 is "result", 1 is cid
					/*#%*/ logMessage = "ConfigurationManager is adding new component with id "+cid.getId()+" to the group";
					
					/*#%*/ logger.log(logMessage);
					/*#%*/ if(TEST_MODE == FrontendImpl.DEMO_MODE) {
					/*#%*/ 	System.out.println(logMessage);
					/*#%*/ }
					myManagementInterface.update(componentGroup, cid, NicheComponentSupportInterface.ADD_TO_GROUP_AND_START);
					//System.out.println("ConfigurationManager says: All done!");
				}
				
				/*#%*/ else {
				/*#%*/ logMessage = "ConfigurationManager could not currently get a new resource. Application must operate in a scaled down version until more resources become available";
				/*#%*/ logger.log(logMessage);
				/*#%*/ 	if(TEST_MODE == FrontendImpl.DEMO_MODE || TEST_MODE == FrontendImpl.FAIL_TEST) {
				/*#%*/ 		//if(!hasShownMessage) {
				/*#%*/ 			System.out.println(logMessage);
				/*#%*/ 			hasShownMessage = true;
				/*#%*/ 		//}
				/*#%*/ 	}
				/*#%*/ }
				
					notDone = false;
					
				} //end while notDone - after a break we get here
			}/*#%*/ 	else if(event.getTotalLoad() < loadLowThreshold) {
				//kick some resource out!
		        // can we assume the component is communication directly with the others in the group to inform about its leave...? : invoke(groupName, leaveMessage)
			/*#%*/ } else {
			/*#%*/ 	logMessage += ", but decided not to try to do anyting";
			/*#%*/ }
			
			executing = false;
		} //end if(!executing) 
		/*#%*/ else if(myId.getReplicaNumber() < 1){
			//System.out.println("ConfigurationManager says: waiting for previous operation to finish");
		/*#%*/ 	logMessage += ", but is already executing and must wait for the previous operation to finish";
		/*#%*/ 	if(TEST_MODE == FrontendImpl.DEMO_MODE || TEST_MODE == FrontendImpl.FAIL_TEST) {
		/*#%*/ 		System.out.println(logMessage);
		/*#%*/ 	}
		/*#%*/ }
		/*#%*/ logger.log(logMessage);
		
		
	}

	//GETTERS & SETTERS
	
	public int getCapacityLowThreshold() {
		return capacityLowThreshold;
	}

	public void setCapacityLowThreshold(int capacityLow) {
		this.capacityLowThreshold = capacityLow;
	}

	public GroupId getComponentGroup() {
		return componentGroup;
	}

	public void setComponentGroup(GroupId componentGroup) {
		this.componentGroup = componentGroup;
	}

	public double getLoadHighThreshold() {
		return loadHighThreshold;
	}

	public void setLoadHighThreshold(double loadHighThreshold) {
		this.loadHighThreshold = loadHighThreshold;
	}

	public double getLoadLowThreshold() {
		return loadLowThreshold;
	}

	public void setLoadLowThreshold(double loadLowThreshold) {
		this.loadLowThreshold = loadLowThreshold;
	}

	public NicheActuatorInterface getManagementInterface() {
		return myManagementInterface;
		
	}

	public void setManagementInterface(NicheActuatorInterface  myManagementInterface) {
		this.myManagementInterface = myManagementInterface;
	}

	//FRACTAL STUFF

	public String[] listFc() {
		return new String[] {FractalInterfaceNames.COMPONENT, FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE, FractalInterfaceNames.EVENT_HANDLER_CLIENT_INTERFACE};
	}


	public Object lookupFc(String interfaceName) throws NoSuchInterfaceException {
		if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return myManagementInterface;
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			return mySelf;
		else
			throw new NoSuchInterfaceException(interfaceName);
	}

	
	public void bindFc(String interfaceName, Object stub) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			myManagementInterface = (NicheActuatorInterface) stub;
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
			mySelf = (Component) stub;
			//System.err.println("setting the component interface");
		} else
			throw new NoSuchInterfaceException(interfaceName);
	}


	public void unbindFc(String interfaceName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			myManagementInterface = null;
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
			mySelf = null;
		} else
			throw new NoSuchInterfaceException(interfaceName);

		
	}


	public String getFcState() {
		return status ? "STARTED": "STOPPED";
	}


	public void startFc() throws IllegalLifeCycleException {
		//System.err.println("Trying to start the manager" );
		status = true;
		
	}


	public void stopFc() throws IllegalLifeCycleException {
		status = false;
		
	}
	
	
	

	
}
