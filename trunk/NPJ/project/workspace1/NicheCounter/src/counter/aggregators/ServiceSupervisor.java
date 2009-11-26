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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import counter.events.AvailabilityTimerTimeoutEvent;
import counter.events.ComponentOutOfSyncEvent;
import counter.events.CounterChangedEvent;
import counter.events.ServiceAvailabilityChangeEvent;
import counter.managers.ConfigurationManager;
import counter.service.ServiceComponent;
import counter.watchers.CounterChangedWatcher;
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
 * The {@link ServiceSupervisor} (aggregator) aggregates all the events about counter increases sent to
 * it by {@link CounterChangedWatcher}s and ensures that the {@link ServiceComponent}s are synchronized to the
 * correct value. If one or more {@link ServiceComponent}s are out of sync then it triggers a {@link ComponentOutOfSyncEvent}
 * targeting the {@link ConfigurationManager} that will ensure this message to be delivered to the {@link ServiceComponent}s
 * eventually so that they resynchronize to the correct state.
 * 
 * <p> It can be set to two different check strictness levels:
 * 	-Strict : {@link ServiceSupervisor#isCalculationStrict} = true : ensures the syncronization of values.
 * 	-Not Strict : {@link ServiceSupervisor#isCalculationStrict} = false : uses a more loose checking algorithm
 * 
 */
public class ServiceSupervisor implements EventHandlerInterface, InitInterface, MovableInterface,
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
    private int roundId = 0;
    
    private static final boolean isCalculationStrict = true;
    
    /**
     * Default constructor.
     */
    public ServiceSupervisor() {
    }
    
    //the max counter value received
    private int maxedReceivedvalue = 0;

	/**
	 * Getter.
	 * 
	 * @return int
	 */
	public int getMaxedReceivedvalue() {
		return maxedReceivedvalue;
	}
	
	/**
	 * Setter
	 *  
	 * @param maxedReceivedvalue int
	 */
	public void setMaxedReceivedvalue(int maxedReceivedvalue) {
		this.maxedReceivedvalue = maxedReceivedvalue;
	}
    

    // //////////////////////////////////////////////////////////////////
    // //////// InitInterface methods, gives us init attributes. ////////
    // //////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see dks.niche.fractal.interfaces.InitInterface#init(java.io.Serializable[])
     */
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

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.InitInterface#reinit(java.io.Serializable[])
	 */
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

    /* (non-Javadoc)
     * @see dks.niche.fractal.interfaces.InitInterface#init(dks.niche.interfaces.NicheActuatorInterface)
     */
    public void init(NicheActuatorInterface actuator) {
        this.actuator = actuator;
        gotNicheAPI = true;

        // We don't know the order the init-methods are invoked. Do not run init
        // before all parameters are set.
        if (gotCurrentComponents && gotNicheAPI) {
            init();
        }
    }

    /* (non-Javadoc)
     * @see dks.niche.fractal.interfaces.InitInterface#initId(dks.niche.ids.NicheId)
     */
    public void initId(NicheId id) {
        myId = id;
    }

    /**
     * Initialization method.
     */
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

    /* (non-Javadoc)
     * @see dks.niche.fractal.interfaces.EventHandlerInterface#eventHandler(java.io.Serializable, int)
     */
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
    
    /**
     * The method that implements the functionality of keeping the {@link ServiceComponent}s
     * synchronized.
     * 
     * @param e the {@link CounterChangedEvent} received from {@link CounterChangedWatcher}
     */
    private synchronized void handlerCounterChanged(CounterChangedEvent e){
    	int resNumber = e.getCounterNumber();
    	int roundIdNew = e.getLamport();
    	String id = e.getCid().getId().toString();
    	
    	System.out.println("[aggregator]>>>>> MAX: " + getMaxedReceivedvalue() + " | LAMPORT: " + roundIdNew);
    	
    	//if received a higher value from the component than one known
    	int exValue = currentComponents.get(id);
    	if (resNumber > exValue) {
    		//refresh
    		currentComponents.put(id, resNumber);
    		
    		//if received a new max counter value
    		if(resNumber>getMaxedReceivedvalue()){
    			setMaxedReceivedvalue(resNumber);
    			roundId = roundIdNew;
    		}
    		
    		//set the algorithm to strict or loose
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
    		
    		//checking if a component is out of sync
    		System.out.print("[aggregator]> ====" + margin + "=== CHECK: ");
    		boolean outOfSync = false;
    		while (iterator.hasNext()) {  
    			String key = iterator.next().toString();  
    			Integer value = (Integer) currentComponents.get(key);
    			System.out.print(value.intValue() + " | "); 
    			int maxValue = getMaxedReceivedvalue();
    			if(value.intValue()<=(maxValue-margin)){
    				outOfSync = true;
    			}
    		}
    		System.out.println();
    		//and if there is one we inform the Configuration manager
    		if (outOfSync) {
    			iterator = currentComponents.keySet().iterator();
    			while (iterator.hasNext()) {  
        			String key = iterator.next().toString();
        			currentComponents.put(key, getMaxedReceivedvalue());
    			}
    			System.out.println("[aggregator]> triggering ComponentOutOfSyncEvent. Value: " + getMaxedReceivedvalue() + " | " + roundId);
    			eventTrigger.trigger(new ComponentOutOfSyncEvent(getMaxedReceivedvalue(), roundId));
    		}
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

    /* (non-Javadoc)
     * @see dks.niche.fractal.interfaces.MovableInterface#getAttributes()
     */
    public Serializable[] getAttributes() {
        return new Serializable[] { componentGroup, currentComponents };
    }

    // ////////////////////////////////////////////////////////////
    // //////////////////////// FRACTAL STUFF /////////////////////
    // ////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        return new String[] { FractalInterfaceNames.COMPONENT,
                FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE };
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String interfaceName) throws NoSuchInterfaceException {
        if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
            return mySelf;
        } else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE)) {
            return eventTrigger;
        } else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String, java.lang.Object)
     */
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

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
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

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.LifeCycleController#getFcState()
     */
    public String getFcState() {
        return status ? "STARTED" : "STOPPED";
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.LifeCycleController#startFc()
     */
    public void startFc() throws IllegalLifeCycleException {
        status = true;
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
     */
    public void stopFc() throws IllegalLifeCycleException {
        status = false;
    }
}
