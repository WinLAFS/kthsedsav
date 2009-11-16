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

import java.util.HashMap;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import yass.frontend.FrontendImpl;
import dks.niche.events.ComponentFailEvent;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.wrappers.ManagementDeployParameters;

/**
 * The <code>ConfigurationManager</code> class
 *
 * @author Joel
 * @version $Id: ConfigurationManager.java 294 2006-05-05 17:14:14Z joel $
 */
public class MetaManager implements EventHandlerInterface, MovableInterface, InitInterface, BindingController, LifeCycleController {


	//Client interfaces
	NicheActuatorInterface myManagementInterface;
	
	NicheAsynchronousInterface logger;
	/////////////////////
	Component mySelf;
	boolean status;
	
	//we need to store the niche-ids, 
	//for instance to tell if they were 
	//reliable or not...
	HashMap<String, ManagementDeployParameters> reDeployParameters;
	HashMap<String, String> reDeployNames;
	
	
	static final int TEST_MODE =
		System.getProperty("yass.test.mode") instanceof String ?
				Integer.parseInt(System.getProperty("yass.test.mode"))
			:
				-1;
	
	private Object[] param;
	
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
	
	
	public MetaManager() {
//		if(TEST_MODE == FrontendImpl.DEMO_MODE) {
//			System.out.println(">>>>>>>>> ConfigurationManager says: CREATED");
//		}
	}
	

	public void init(Object [] parameters) {
		
		this.param = parameters; //no need to modify it, the object is close-enough to stateless
		
		reDeployParameters = (HashMap<String, ManagementDeployParameters>)parameters[0];
		reDeployNames = (HashMap<String, String>)parameters[1];
		
		//if(TEST_MODE == FrontendImpl.DEMO_MODE) {
			System.out.println("MetaManager is initialized");
		//}

	}
	public void reinit(Object [] parameters) {
		
		this.param = parameters; //no need to modify it, the object is close-enough to stateless
		
		reDeployParameters = (HashMap<String, ManagementDeployParameters>)parameters[0];
		reDeployNames = (HashMap<String, String>)parameters[1];
		
		//if(TEST_MODE == FrontendImpl.DEMO_MODE) {
			System.out.println("MetaManager is re-initialized");
		//}

	}
	
	public void init(NicheActuatorInterface managementInterface) {
		myManagementInterface = managementInterface;
		logger = managementInterface.testingOnly();
	}
	
	public void initId(Object id) {
		//Not used
	}
	
	public Object[] getAttributes() {
		return this.param; //no need to modify it, the object is close-enough to stateless
	}
	
	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheEventHandlerInterface#eventHandler(dks.arch.Event)
	 */
	public void eventHandler(Object e, int flag) {
		
		ComponentFailEvent evert = (ComponentFailEvent)e; 
		NicheId failedComponentId = evert.getNicheId();
		String failedComponentIdAsString = evert.getNicheId().toString();
		/*#%*/ String logMessage =
		/*#%*/ 	"The MetaManager received a fail-event: It's the "
		/*#%*/ 	+ reDeployNames.get(failedComponentIdAsString)
		/*#%*/ 	+ " that kicked the bucket.";
		
		
		if (failedComponentId.isReliable()) {
			/*#%*/ logMessage += " But the ME was marked as reliable, so the system should restore it automatically";
		} else {
			/*#%*/ logMessage += " So I will try to re-deploy it";
			
			myManagementInterface.redeploy(
					reDeployParameters.get(failedComponentIdAsString),
					failedComponentId
			);
			
		}
		
		/*#%*/ logger.log(logMessage);
		//if(TEST_MODE == FrontendImpl.DEMO_MODE) {
		/*#%*/ System.out.println(logMessage);
		//}

		
		
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
