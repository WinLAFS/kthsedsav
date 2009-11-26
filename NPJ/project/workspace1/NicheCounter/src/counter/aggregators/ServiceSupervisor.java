/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package counter.aggregators;

import counter.events.AvailabilityTimerTimeoutEvent;
import counter.events.ComponentOutOfSyncEvent;
import counter.events.CounterChangedEvent;
import counter.events.MaxCounterChangedEvent;
import counter.events.ServiceAvailabilityChangeEvent;
import counter.interfaces.CounterInterface;
import counter.interfaces.CounterStatusInterface;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import dks.niche.events.ComponentFailEvent;
import dks.niche.events.MemberAddedEvent;
import dks.niche.exceptions.OperationTimedOutException;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;

/**
 * The <code>ServiceSupervisor</code> class
 * 
 * @author Joel
 * @version $Id: ServiceSupervisor.java 294 2006-05-05 17:14:14Z joel $
 */
public class ServiceSupervisor implements CounterStatusInterface, EventHandlerInterface, InitInterface, MovableInterface,
    BindingController, LifeCycleController {

    private static final long serialVersionUID = -9008437658170151593L;

    // This is the time we wait for enough service components to become active.
    private transient static final int CHECK_NR_OF_COMPONENTS_AFTER_THIS_TIME = 20000;

    // There should be at least this many active server components.
    private static final int MINIMUM_ALLOCATED_SERVICE_COMPONENTS = 3;

    private Component mySelf;

    // This component is used when we want to trigger an event.
    private TriggerInterface eventTrigger;

    // Our Niche id.
    private NicheId myId;

    // When this timer goes off there should be enough service components.
    private transient long availabilityTimerId;

    // Reference to the Niche API.
    private NicheActuatorInterface actuator;

    // Tells whether this component is running.
    private boolean status;

    // The number of currently active server components
    private int currentAllocatedServiceComponents;

    // We can not run initialization code until all init parameters are set.
    private boolean gotCurrentComponents;
    private boolean gotNicheAPI;

    // The group of service components.
    private NicheId componentGroup;

    // The members of the group of service components.
    private HashMap<String, Integer> currentComponents;
    
    private int checkStep = 0;
    private int numberOfNodes;
    private int roundId = 0;
    
    private static final boolean isCalculationStrict=true;
    
    // Empty constructor always needed!
    public ServiceSupervisor() {
    }
    
    private int maxedReceivedvalue=0;

	public int getMaxedReceivedvalue() {
		return maxedReceivedvalue;
	}

	public void setMaxedReceivedvalue(int maxedReceivedvalue) {
		this.maxedReceivedvalue = maxedReceivedvalue;
	}
    
    // //////////////////////////////////////////////////////////////////
    // //////// Methods called by the ServiceComponent //////////////////
    // //////////////////////////////////////////////////////////////////
    
    @Override
	public void informCounterValue(ComponentId cid, int value, int lamport) {
		System.out.println("Received a new counter value: " + value);
	}

    // //////////////////////////////////////////////////////////////////
    // //////// InitInterface methods, gives us init attributes. ////////
    // //////////////////////////////////////////////////////////////////

    public void init(Serializable[] parameters) {
        componentGroup = (NicheId) parameters[0];
        currentComponents = new HashMap<String, Integer>();
        gotCurrentComponents = true;

        // We don't know the order the init-methods are invoked. Do not run init
        // before all parameters are set.
        if (gotCurrentComponents && gotNicheAPI) {
            init();
        }
    }

	public void reinit(Serializable[] parameters) {
        componentGroup = (NicheId) parameters[0];
        currentComponents = (HashMap<String, Integer>) parameters[1];
        gotCurrentComponents = true;

        // We don't know the order the init-methods are invoked. Do not run init
        // before all parameters are set.
        if (gotCurrentComponents && gotNicheAPI) {
            init();
        }
    }

    public void init(NicheActuatorInterface actuator) {
        this.actuator = actuator;
        gotNicheAPI = true;

        // We don't know the order the init-methods are invoked. Do not run init
        // before all parameters are set.
        if (gotCurrentComponents && gotNicheAPI) {
            init();
        }
    }

    public void initId(NicheId id) {
        myId = id;
    }

    private void init() {
        // Get the service components. They are all members of the service
        // component group so we can look them up from there.
        Object[] currentMembers = null;
        while (null == currentMembers) {
            try {
                currentMembers =
                    (Object[]) actuator.query(componentGroup,
                                              NicheComponentSupportInterface.GET_CURRENT_MEMBERS);
            } catch (OperationTimedOutException e) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        for (Object currentMember : currentMembers) {
            currentComponents.put(((ComponentId) currentMember).getId().toString(), 0);
        }

        currentAllocatedServiceComponents = currentMembers.length;

        System.out.println("ServiceSupervisor is initialized. Current number of assingned service components = "
            + currentAllocatedServiceComponents);

        if (currentAllocatedServiceComponents < MINIMUM_ALLOCATED_SERVICE_COMPONENTS) {
            System.out.println("Right now there are insufficient number of service components!");
            // Don't tell ConfigurationManager now, just set the timer &
            // give the system some more time to stabilize. If there are still
            // too few service components when the timer goes off we will tell
            // ConfigurationManager.
            availabilityTimerId =
                actuator.registerTimer(this, AvailabilityTimerTimeoutEvent.class,
                                       CHECK_NR_OF_COMPONENTS_AFTER_THIS_TIME);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // EventHandlerInterface method, called when we receive an event. ///
    // //////////////////////////////////////////////////////////////////

    public void eventHandler(Serializable e, int flag) {
        if (e instanceof ComponentFailEvent) {
            handleComponentFailEvent((ComponentFailEvent) e);
        } else if (e instanceof MemberAddedEvent) {
            handleMemberAddedEvent((MemberAddedEvent) e);
        } else if (e instanceof AvailabilityTimerTimeoutEvent) {
            handlerAvailabilityTimerTimeout();
        } else if (e instanceof CounterChangedEvent) {
            handlerCounterChanged((CounterChangedEvent)e);
        } else {
            System.out.println("[ServiceSupervisor]: Unknown event type, error.");
        }
    }
    
    private synchronized void handlerCounterChanged(CounterChangedEvent e){
    	int resNumber = e.getCounterNumber();
    	int roundIdNew = e.getLamport();
    	String id = e.getCid().getId().toString();
    	
    	System.out.println("[aggregator]>>>>> MAX: " + getMaxedReceivedvalue() + " | LAMPORT: " + roundIdNew);
    	
    	int exValue = currentComponents.get(id);
    	if (resNumber > exValue) {

    		currentComponents.put(id, resNumber);
    		
    		if(resNumber>getMaxedReceivedvalue()){
    			setMaxedReceivedvalue(resNumber);
    			roundId = roundIdNew;
    		}
    		
    		int margin=1;
    		
    		if(!isCalculationStrict){
	    		margin = currentAllocatedServiceComponents - checkStep;
	    		System.out.println("[aggregator]> Check step: " + checkStep + "\tMargin: " + margin);
	    		checkStep = (checkStep + 1) % currentAllocatedServiceComponents;
	    		
	    		if (margin == currentAllocatedServiceComponents) {
	    			return;
	    		}
	    	}
    		
    		
    		Iterator iterator = currentComponents.keySet().iterator();  
    		
    		System.out.print("[aggregator]> ====" + margin + "=== CHECK: ");
//    		System.out.println("**Testing stored values against value: " + getMaxedReceivedvalue());
    		boolean outOfSync = false;
    		while (iterator.hasNext()) {  
    			String key = iterator.next().toString();  
    			Integer value = (Integer) currentComponents.get(key);
    			System.out.print(value.intValue() + " | "); 
    			int maxValue = getMaxedReceivedvalue();
    			if(value.intValue()<=(maxValue-margin)){
//    				System.out.println("*Component "+key+" is inconsistent! Value: " + value.intValue());
    				outOfSync = true;
    			}
    		}
    		System.out.println();
    		if (outOfSync) {
//    			
    			iterator = currentComponents.keySet().iterator();
    			while (iterator.hasNext()) {  
        			String key = iterator.next().toString();
        			currentComponents.put(key, getMaxedReceivedvalue());
    			}
    			System.out.println("[aggregator]> triggering ComponentOutOfSyncEvent. Value: " + getMaxedReceivedvalue() + " | " + roundId);
    			eventTrigger.trigger(new ComponentOutOfSyncEvent(getMaxedReceivedvalue(), roundId));
    		}
//    		System.out.println("[aggregator]> ======== End");
    	}
    	else {
    		System.out.println("[aggregator]> NO CHECK! From: " + id + "Value:\t" + resNumber + "\t. Ex Value:\t" + exValue);
    	}
    	
    	
    }

    /**
     * Called when a service component has failed.
     * 
     * @param failedEvent
     *            Tells which component failed.
     */
    private void handleComponentFailEvent(ComponentFailEvent failedEvent) {
        String idAsString = failedEvent.getFailedComponentId().getId().toString();

        if (!currentComponents.containsKey(idAsString)) {
            // The failed component was not in our list of active service
            // components.
            return;
        }

        // Remove failed component from list of active components.
        currentComponents.remove(idAsString);
        
        currentAllocatedServiceComponents--;
        //TODO
        checkStep = 0;

        if (myId.getReplicaNumber() < 1) {
            System.out.print("ServiceSupervisor ");
        } else {
            System.out.print("ServiceSupervisor REPLICA ");
        }
        System.out.println("has received a ComponentFailEvent!\n"
            + "The result is that there are now " + currentAllocatedServiceComponents
            + " service components");

        if (currentAllocatedServiceComponents < MINIMUM_ALLOCATED_SERVICE_COMPONENTS) {
            // There are too few service components.
            if (myId.getReplicaNumber() < 1) {
                System.out.println("ServiceSupervisor triggering!");
            }

            // Cancel eventual running timers since we are telling
            // ConfigurationManager now.
            actuator.cancelTimer(availabilityTimerId);

            // Tell ConfigurationManager there are too few service components.
            eventTrigger.trigger(new ServiceAvailabilityChangeEvent());

            // When the timer goes off we will check if enough service
            // components have become active.
            availabilityTimerId =
                actuator.registerTimer(this, AvailabilityTimerTimeoutEvent.class,
                                       CHECK_NR_OF_COMPONENTS_AFTER_THIS_TIME);
        }
    }

    /**
     * Called when a new service component has joined the service component
     * group.
     * 
     * @param newMemberEvent
     *            Tells who is the new member.
     */
    private void handleMemberAddedEvent(MemberAddedEvent newMemberEvent) {
        String idAsString = newMemberEvent.getSNR().getId().toString();

        if (currentComponents.containsKey(idAsString)) {
            // We already new about this component.
            return;
        }
        currentComponents.put(idAsString, 0);
        currentAllocatedServiceComponents++;
        checkStep = 0;
    }

    /**
     * Called when the timer goes off. Now there should be enough service
     * components, but we check anyway.
     */
    private void handlerAvailabilityTimerTimeout() {
        if (currentAllocatedServiceComponents < MINIMUM_ALLOCATED_SERVICE_COMPONENTS) {
            // Still to few service components, tell ConfigurationManager again.
            if (myId.getReplicaNumber() < 1) {
                System.out.println("ServiceSupervisor re-triggering!");
            }

            // Send event to ConfigurationManager.
            eventTrigger.trigger(new ServiceAvailabilityChangeEvent());

            // We will check once again after the timeout period.
            availabilityTimerId =
                actuator.registerTimer(this, AvailabilityTimerTimeoutEvent.class,
                                       CHECK_NR_OF_COMPONENTS_AFTER_THIS_TIME);
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // MovableInterface method, called when we are about to be moved or copied.
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public Serializable[] getAttributes() {
        return new Serializable[] { componentGroup, currentComponents };
    }

    // ////////////////////////////////////////////////////////////
    // //////////////////////// FRACTAL STUFF /////////////////////
    // ////////////////////////////////////////////////////////////

    public String[] listFc() {
        return new String[] { FractalInterfaceNames.COMPONENT,
                FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE };
    }

    public Object lookupFc(String interfaceName) throws NoSuchInterfaceException {
        if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
            return mySelf;
        } else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE)) {
            return eventTrigger;
        } else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    public void bindFc(String interfaceName, Object intfValue) throws NoSuchInterfaceException,
        IllegalBindingException, IllegalLifeCycleException {
        if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
            mySelf = (Component) intfValue;
        } else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE)) {
            eventTrigger = (TriggerInterface) intfValue;
        } else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    public void unbindFc(String interfaceName) throws NoSuchInterfaceException,
        IllegalBindingException, IllegalLifeCycleException {
        if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
            mySelf = null;
        } else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE)) {
            eventTrigger = null;
        } else {
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