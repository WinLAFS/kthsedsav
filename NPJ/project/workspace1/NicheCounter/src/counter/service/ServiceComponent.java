package counter.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import counter.interfaces.CounterInterface;
import counter.interfaces.CounterResyncInterface;
import counter.interfaces.CounterStatusInterface;
import counter.interfaces.HelloAllInterface;
import counter.interfaces.HelloAnyInterface;
import counter.interfaces.SynchronizeInterface;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;

import dks.niche.ids.ComponentId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;

public class ServiceComponent implements SynchronizeInterface, CounterInterface, 
	HelloAnyInterface, HelloAllInterface, BindingController, CounterResyncInterface,
    LifeCycleController {

    private Component myself;
    private boolean status;
    private int counterNumber = 0;
    private int round = 0;
    private CounterStatusInterface counterStatus;
    private ComponentId myGlobalId;
    private int lamport = 0;
    private int resync = 1;
    private boolean previusActionSync = false;
    private SyncMessage syncMessage = new SyncMessage(0, 0);
    private ArrayList<SyncMessage> syncMessageList = new ArrayList<SyncMessage>();
    
    private final static int syncMessagesStackSize = 50;

    public ServiceComponent() {
        System.err.println("CounterService created");
    }
     

    // /////////////////////////////////////////////////////////////////////
    // //////////////////////// Server interfaces //////////////////////////
    // /////////////////////////////////////////////////////////////////////

    public void helloAny(String s) {
        System.out.println(s);
    }

    public void helloAll(String s) {
        System.out.println(s);
    }
    
	public synchronized void inreaseCounter(int roundId) {//TODO
		double r = Math.random();
		round++;
		if(r<0.95) {
			previusActionSync = false;
			
			System.out.println("[service|"+ round + "\t]>\t\t\t\t\t :inc: " + syncMessageList.size());
			
			SyncMessage remove = removeCurrentSyncId(roundId);
			//if (shouldSkipIncrease(roundId)) {
			if (remove != null) {
				System.out.println("[service|"+ round + "\t]> increaseCounter SKIPPED. Synchronized to ID: " + syncMessage.getSyncRoundId());
				syncMessageList.remove(remove);
				
				return;
			}
			
			
			int newVal =  increaseCounter();
			System.out.println("[service|"+ round + "\t]> increaseCounter called. New value: " + newVal + " | " + roundId);
			counterStatus.informCounterValue(myGlobalId, newVal, roundId);
		}
		else {
			System.out.println("[service|"+ round + "\t]> increaseCounter OMIT. Value: " + getCounterNumber());
		}
		
	}
	
	


	public void synchronize(int value) {//TODO REMOVE
//		System.out.println("[service]> synchronize called. Current value: " + getCounterNumber() +
//				", New value: " + value);
//		if (getCounterNumber() < value) {
//			setCounterNumber(value);
//		}
		
	}
	
	public synchronized void reSynchronize(int value, int syncRoundId) {
//		if (resync == 1) {
		System.out.println("[service]> RESYNC.Current: " + getCounterNumber()
				+ ". New: " + value + ". Sync current: " + syncMessage.getSyncRoundId() + ". New: " + syncRoundId);
		
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
//			syncMessage = (syncMessage.getValue() > value) ? syncMessage : (new SyncMessage(syncRoundId, value));
			if (shouldKeepSyncMsg(value)) {
				syncMessageList.add(new SyncMessage(syncRoundId, value));
				System.out.println("[service|"+ round + "\t]>\t\t\t\t\t :ofs: " + syncMessageList.size() + "\t\t| " + syncRoundId);
			}
		}
		else {
			syncMessageList.add(new SyncMessage(syncRoundId, value));//TODO is it ok????
			System.out.println("[service|"+ round + "\t]>\t\t\t\t\t :ofs: " + syncMessageList.size() + "\t\t| " + syncRoundId);
		}
		
		if (getCounterNumber() < value) {
			setCounterNumber(value);
		}
	}
	
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
	
	private boolean shouldSkipIncrease(int roundId) {
		return syncRoundIdExists(roundId);
	}
	
	
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
	
	public int getCounterNumber() {
		return counterNumber;
	}

	public void setCounterNumber(int counterNumber) {
		this.counterNumber = counterNumber;
	}
	
	public int increaseCounter() {
			counterNumber += 1;
			return counterNumber;
	}

    // /////////////////////////////////////////////////////////////////////
    // //////////////////////// Fractal Stuff //////////////////////////////
    // /////////////////////////////////////////////////////////////////////

	public String[] listFc() {
        return new String[] { "component", "counterStatus" };
    }

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

    public void bindFc(final String itfName, final Object itfValue) throws NoSuchInterfaceException {
        if (itfName.equals("component")) {
            myself = (Component) itfValue;
        } else if (itfName.equals("counterStatus")) {
        	counterStatus = (CounterStatusInterface) itfValue;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    public void unbindFc(final String itfName) throws NoSuchInterfaceException {
        if (itfName.equals("component")) {
            myself = null;
        } else if (itfName.equals("counterStatus")) {
        	counterStatus = null;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    public String getFcState() {
        return status ? "STARTED" : "STOPPED";
    }

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

    public void stopFc() throws IllegalLifeCycleException {
        status = false;
    }

	private void init(){
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

	private class SyncMessage {
		private int syncRoundId;
		private int value;
		
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
