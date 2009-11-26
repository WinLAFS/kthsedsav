/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package counter.managers;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.jasmine.jade.util.Serialization;

import counter.events.ComponentOutOfSyncEvent;
import counter.events.CounterChangedEvent;
import counter.events.InformOutOfSyncEvent;
import counter.events.MaxCounterChangedEvent;
import counter.interfaces.SynchronizeInterface;

import dks.niche.exceptions.OperationTimedOutException;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.ResourceRef;

/**
 * The <code>ConfigurationManager</code> class
 * 
 * @author Joel
 * @version $Id: ConfigurationManager.java 294 2006-05-05 17:14:14Z joel $
 */
public class ConfigurationManager implements EventHandlerInterface, MovableInterface,
    InitInterface, BindingController, LifeCycleController {

    private static final String DISCOVER_PREFIX = "dynamic:";

    // Reference to the Niche API.
    private NicheActuatorInterface myManagementInterface;

    private Component mySelf;

    // The group of service components.
    private GroupId componentGroup;

    // A service component's properties.
    private Object serviceCompProps;

    // A node that hosts a service component must meet these requirements.
    private Serializable nodeRequirements;

    // True if we are processing an event.
    private boolean executing = false;

    // Tells whether this component is running.
    private boolean status;

    // Initialization attributes.
    private Serializable[] initAttributes;

    // Our Niche id.
    private NicheId myId;
    
    private int maxCounterNumber=0;
    
    private int lastRoundId = 0;
    
    private SynchronizeInterface synchronize;
    
    private TriggerInterface eventTrigger;
    
    //end of variables

    public int getMaxCounterNumber() {
		return maxCounterNumber;
	}

	public void setMaxCounterNumber(int maxCounterNumber) {
		this.maxCounterNumber = maxCounterNumber;
	}

	// Empty constructor always needed!
    public ConfigurationManager() {
    }

    // //////////////////////////////////////////////////////////////////
    // //////// InitInterface methods, gives us init attributes. ////////
    // //////////////////////////////////////////////////////////////////

    public void init(Serializable[] parameters) {
        initAttributes = parameters;
        init();
    }

    public void reinit(Serializable[] parameters) {
        initAttributes = parameters;
        init();
    }

    private void init() {
        componentGroup = (GroupId) initAttributes[0];
        serviceCompProps = initAttributes[1];
        nodeRequirements = DISCOVER_PREFIX + initAttributes[2];
        System.out.println("ConfigurationManager is initialized");
    }

    public void init(NicheActuatorInterface managementInterface) {
        myManagementInterface = managementInterface;
    }

    public void initId(NicheId id) {
        myId = id;
    }

    // //////////////////////////////////////////////////////////////////
    // EventHandlerInterface method, called when we receive an event. ///
    // //////////////////////////////////////////////////////////////////

    public void eventHandler(Serializable e, int flag) {
    	
    	if (e instanceof ComponentOutOfSyncEvent) {
    		int maxNumber = ((ComponentOutOfSyncEvent)e).getCounterNumber();
    		int lamport = ((ComponentOutOfSyncEvent)e).getLamport();
    		setMaxCounterNumber(maxNumber);
//    		System.out.println("[configuration]> ComponentOutOfSyncEvent received."+
//    				"Value: " + getMaxCounterNumber());
    		eventTrigger.trigger(new InformOutOfSyncEvent(maxNumber, lamport));
    		lastRoundId = lamport;
    	}
    	else {
    		
        if (!(myId.getReplicaNumber() < 1)) {
            // I am not the master replica, do nothing.
            return;
        }

        if (executing) {
            System.out.println("ConfigurationManager received an event but must wait "
                + "for previous operation to finish");
            return;
        }
        executing = true;
        boolean notDone = true;

        while (notDone) {
            System.out.println("ConfigurationManager received an event and started executing");

            // Find a node that meets the requirements for a service component.
            NodeRef newNode = null;
            try {
                newNode = myManagementInterface.oneShotDiscoverResource(nodeRequirements);
            } catch (OperationTimedOutException err) {
                System.out.println("Discover operation timed out, retry a bit later");
                continue;
            }
            if (newNode == null) {
                System.out.println("ConfigurationManager could not currently get a new resource. "
                    + "Application must operate in a scaled down version until more resources "
                    + "become available");
                break;
            }
            System.out.println("ConfigurationManager was given a resource at "
                + newNode.getDKSRef() + ". Allocate it");

            // Allocate resources for a service component at the found node.
            List allocatedResources = null;
            try {
                allocatedResources = myManagementInterface.allocate(newNode, null);
            } catch (OperationTimedOutException err) {
                System.out.println("Allocate operation timed out, retry a bit later");
                continue;
            }
            ResourceRef allocatedResource = (ResourceRef) allocatedResources.get(0);

            // Deploy a new service component instance at the allocated
            // resource.
            String deploymentParams = null;
            try {
                deploymentParams = Serialization.serialize(serviceCompProps);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            List deployedComponents = null;
            try {
                deployedComponents =
                    myManagementInterface.deploy(allocatedResource, deploymentParams);
            } catch (OperationTimedOutException err) {
                System.out.println("Deploy operation timed out, retry a bit later");
                continue;
            }

            ComponentId cid = (ComponentId) ((Object[]) deployedComponents.get(0))[1];
            System.out.println("ConfigurationManager is adding new component with id "
                + cid.getId() + " to the group");

            // Add the newly deployed component to the service component group
            // and start it.
            myManagementInterface.update(componentGroup, cid,
                                         NicheComponentSupportInterface.ADD_TO_GROUP_AND_START);
            System.out.println("ConfigurationManager says: All done!");
            
            
            System.out.println("[configuration]> New node joined. Resynchronizing.."+ "Value: " + getMaxCounterNumber());
            eventTrigger.trigger(new InformOutOfSyncEvent(getMaxCounterNumber(),this.lastRoundId));

            notDone = false;
        } // end while notDone - after a continue we get here

        executing = false;}
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MovableInterface method, called when we are about to be moved or copied.
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public Serializable[] getAttributes() {
        return initAttributes;
    }

    // ////////////////////////////////////////////////////////////
    // //////////////////////// FRACTAL STUFF /////////////////////
    // ////////////////////////////////////////////////////////////

    public String[] listFc() {
        return new String[] { FractalInterfaceNames.COMPONENT, "synchronize", FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE };
    }

    public Object lookupFc(String interfaceName) throws NoSuchInterfaceException {
        if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
            return mySelf;
        } else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE)) {
            return eventTrigger;
        }else if (interfaceName.equals("synchronize")) {
        	return synchronize;
        } else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    public void bindFc(String interfaceName, Object stub) throws NoSuchInterfaceException,
        IllegalBindingException, IllegalLifeCycleException {
        if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
            mySelf = (Component) stub;
        } else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE)) {
            eventTrigger = (TriggerInterface) stub;
        } else if (interfaceName.equals("synchronize")) {
        	synchronize = (SynchronizeInterface) stub;
        }
        else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    public void unbindFc(String interfaceName) throws NoSuchInterfaceException,
        IllegalBindingException, IllegalLifeCycleException {
        if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
            mySelf = null;
        } else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE)) {
            eventTrigger = null;
        } else if (interfaceName.equals("synchronize")) {
        	synchronize = null;
        }
        else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    public String getFcState() {
        return status ? "STARTED" : "STOPPED";
    }

    public void startFc() throws IllegalLifeCycleException {
        status = true;
    }

    public void stopFc() throws IllegalLifeCycleException {
        status = false;
    }
}
