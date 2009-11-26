package counter.service;

import java.util.ArrayList;
import java.util.Iterator;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;

import counter.actuators.CounterStatusActuator;
import counter.interfaces.CounterInterface;
import counter.interfaces.CounterResyncInterface;
import counter.interfaces.CounterStatusInterface;
import dks.niche.ids.ComponentId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;
/**
 * The ServiceComponent is the class that describes the main functionality of the system.
 * This is that they are the components that keep the state of the system (a counter here),
 * the inform the managerial components for state changes and they receive corrections
 * by the managerial elements.
 * 
 */
public class ServiceComponent implements CounterInterface, BindingController, 
	CounterResyncInterface, LifeCycleController {

    private Component myself;
    private boolean status;
    private int counterNumber = 0; //counter
    private int round = 0; //for printing purposes
    private CounterStatusInterface counterStatus; //the interface that we keep in order to inform the Sensors about the
    //increase of counter
    private ComponentId myGlobalId;
    private boolean previusActionSync = false;
    private SyncMessage syncMessage = new SyncMessage(0, 0);
    private ArrayList<SyncMessage> syncMessageList = new ArrayList<SyncMessage>(); //history of sync msgs
    
    private final static int syncMessagesStackSize = 50;

    /**
     * Default constructor.
     */
    public ServiceComponent() {
        System.err.println("CounterService created");
    }

    // //////////////////////// Server interfaces //////////////////////////
    
	/**
	 * @see counter.interfaces.CounterInterface#inreaseCounter(int)
	 */
	public synchronized void inreaseCounter(int roundId) {
		double r = Math.random();
		round++;
		if(r<0.95) { //chance NOT to "loose" an increase. Error simulation
			previusActionSync = false;
			
			System.out.println("[service|"+ round + "\t]>\t\t\t\t\t :inc: " + syncMessageList.size());
			
			SyncMessage remove = removeCurrentSyncId(roundId); //search in history, if a sync msg with
			//the same round id exists, ommit increasing
			if (remove != null) {
				System.out.println("[service|"+ round + "\t]> increaseCounter SKIPPED. Synchronized to ID: " + syncMessage.getSyncRoundId());
				syncMessageList.remove(remove);
				
				return;
			}
			
			
			int newVal =  increaseCounter(); //counter++
			System.out.println("[service|"+ round + "\t]> increaseCounter called. New value: " + newVal + " | " + roundId);
			counterStatus.informCounterValue(myGlobalId, newVal, roundId); //inform sensors
		}
		else {
			System.out.println("[service|"+ round + "\t]> increaseCounter OMIT. Value: " + getCounterNumber());
		}
		
	}
	
	
	/**
	 * @see counter.interfaces.CounterResyncInterface#reSynchronize(int, int)
	 */
	public synchronized void reSynchronize(int value, int syncRoundId) {
		System.out.println("[service]> RESYNC.Current: " + getCounterNumber()
				+ ". New: " + value + ". Sync current: " + syncMessage.getSyncRoundId() + ". New: " + syncRoundId);
		
		//keeping sync msgs in history
		if (syncRoundIdExists(syncRoundId)) {
			return;
		}
		
		if(syncMessageList.size()>=syncMessagesStackSize){
			System.out.println("[service|"+ round + "\t]>\t\t\t\t\t Clearing messages list");
			for(int i=0; i<syncMessageList.size()/2; i++){
				syncMessageList.remove(i);
			}
		}
		
		if (previusActionSync) {
			if (shouldKeepSyncMsg(value)) {
				syncMessageList.add(new SyncMessage(syncRoundId, value));
				System.out.println("[service|"+ round + "\t]>\t\t\t\t\t :ofs: " + syncMessageList.size() + "\t\t| " + syncRoundId);
			}
		}
		else {
			syncMessageList.add(new SyncMessage(syncRoundId, value));
			System.out.println("[service|"+ round + "\t]>\t\t\t\t\t :ofs: " + syncMessageList.size() + "\t\t| " + syncRoundId);
		}
		
		if (getCounterNumber() < value) { //updating counter value if needed
			setCounterNumber(value);
		}
	}
	
	
	/**
	 * Find the message in the list of history of {@link SyncMessage} that we have already
	 * received that has the same roundId ad the one provided or returns null if no any.
	 * 
	 * @param roundId the target synchronization id
	 * @return {@link SyncMessage}
	 */
	private SyncMessage removeCurrentSyncId(int roundId) {
		Iterator<SyncMessage> sIterator = syncMessageList.iterator();
		while (sIterator.hasNext()) {
			ServiceComponent.SyncMessage syncMessage = (ServiceComponent.SyncMessage) sIterator.next();
			if (syncMessage.getSyncRoundId() == roundId) {
				return syncMessage;
			}
		}
		return null;
	}
	
	/**
	 * Check if a message in the list of history of {@link SyncMessage} that we have already
	 * received has the same roundId ad the one provided or returns false if no any.
	 * 
	 * @param syncRoundId the target sync id
	 * @return {@link Boolean}
	 */
	boolean syncRoundIdExists(int syncRoundId) {
		Iterator<SyncMessage> sIterator = syncMessageList.iterator();
		while (sIterator.hasNext()) {
			ServiceComponent.SyncMessage syncMessage = (ServiceComponent.SyncMessage) sIterator.next();
			if (syncMessage.getSyncRoundId() == syncRoundId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if a synchronization message just received should be placed in the
	 * history list of {@link SyncMessage}.
	 * 
	 * @param value the counter value that the {@link SyncMessage} contains
	 * @return {@link Boolean}
	 */
	boolean shouldKeepSyncMsg(int value) {
		if (syncMessageList.isEmpty()) {
			return true;
		}
		
		Iterator<SyncMessage> sIterator = syncMessageList.iterator();
		while (sIterator.hasNext()) {
			ServiceComponent.SyncMessage syncMessage = (ServiceComponent.SyncMessage) sIterator.next();
			if (syncMessage.getValue() < value) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Default getter.
	 * 
	 * @return int
	 */
	public int getCounterNumber() {
		return counterNumber;
	}

	/**
	 * Default setter.
	 * 
	 * @param counterNumber
	 */
	public void setCounterNumber(int counterNumber) {
		this.counterNumber = counterNumber;
	}
	
	/**
	 * Increases our counter by one and returns the increased value.
	 * 
	 * @return {@link Integer}
	 */
	public int increaseCounter() {
			counterNumber += 1;
			return counterNumber;
	}

    // /////////////////////////////////////////////////////////////////////
    // //////////////////////// Fractal Stuff //////////////////////////////
    // /////////////////////////////////////////////////////////////////////

	/**
	 * @see org.objectweb.fractal.api.control.BindingController#listFc()
	 */
	public String[] listFc() {
        return new String[] { "component", "counterStatus" };
    }

    /**
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
        if (itfName.equals("component")) {
            return myself;
        } else if (itfName.equals("counterStatus")) {
        	return counterStatus;
        }
        else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    /**
     * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String, java.lang.Object)
     */
    public void bindFc(final String itfName, final Object itfValue) throws NoSuchInterfaceException {
        if (itfName.equals("component")) {
            myself = (Component) itfValue;
        } else if (itfName.equals("counterStatus")) {
        	counterStatus = (CounterStatusInterface) itfValue;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    /**
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(final String itfName) throws NoSuchInterfaceException {
        if (itfName.equals("component")) {
            myself = null;
        } else if (itfName.equals("counterStatus")) {
        	counterStatus = null;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    /**
     * @see org.objectweb.fractal.api.control.LifeCycleController#getFcState()
     */
    public String getFcState() {
        return status ? "STARTED" : "STOPPED";
    }

    /**
     * @see org.objectweb.fractal.api.control.LifeCycleController#startFc()
     */
    public void startFc() throws IllegalLifeCycleException {
    	init();
    	Component jadeNode = null;
		Component niche = null;
		OverlayAccess overlayAccess = null;

		Component comps[] = null;
		try {
			comps = Fractal.getSuperController(myself).getFcSuperComponents();
		}
		catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < comps.length; i++) {
			try {
				if (Fractal.getNameController(comps[i]).getFcName().equals("managed_resources")) {
					jadeNode = comps[i];
					break;
				}
			}
			catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}
		}

		try {
			niche = FractalUtil.getFirstFoundSubComponentByName(jadeNode,"nicheOS");
		}
		catch (NoSuchComponentException e1) {
			e1.printStackTrace();
		}

		try {
			overlayAccess = (OverlayAccess) niche.getFcInterface("overlayAccess");
		}
		catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		NicheActuatorInterface nicheOSSupport = overlayAccess.getOverlay().getComponentSupport(myself);
		
		myGlobalId = nicheOSSupport.getResourceManager().getComponentId(myself);
    	
    	status = true;
        System.err.println("Service component started.");
    }

    /**
     * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
     */
    public void stopFc() throws IllegalLifeCycleException {
        status = false;
    }

	/**
	 * Initializes the {@link ServiceComponent}.
	 * 
	 */
	private void init() {
		Component jadeNode = null;
		Component niche = null;
		OverlayAccess overlayAccess = null;

		Component comps[] = null;
		try {
			comps = Fractal.getSuperController(myself).getFcSuperComponents();
		}
		catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < comps.length; i++) {
			try {
				if (Fractal.getNameController(comps[i]).getFcName().equals("managed_resources")) {
					jadeNode = comps[i];
					break;
				}
			}
			catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}
		}

		try {
			niche = FractalUtil.getFirstFoundSubComponentByName(jadeNode,"nicheOS");
		}
		catch (NoSuchComponentException e1) {
			e1.printStackTrace();
		}

		try {
			overlayAccess = (OverlayAccess) niche.getFcInterface("overlayAccess");
		}
		catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		NicheComponentSupportInterface nicheOSSupport = overlayAccess.getOverlay().getComponentSupport(myself);
		myGlobalId = nicheOSSupport.getResourceManager().getComponentId(myself);
		
	}

	/**
	 * Inner class of {@link ServiceComponent} that is used to represent
	 * the syncronization messages received by {@link CounterStatusActuator}.
	 *
	 */
	private class SyncMessage {
		private int syncRoundId;
		private int value;
		
		/**
		 * Constructor
		 * 
		 * @param syncRoundId
		 * @param value
		 */
		public SyncMessage(int syncRoundId, int value) {
			super();
			this.syncRoundId = syncRoundId;
			this.value = value;
		}

		public int getSyncRoundId() {
			return syncRoundId;
		}

		public int getValue() {
			return value;
		}
	}
}
