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

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import yass.events.AvailabilityTimerTimeoutEvent;
import yass.events.ComponentStateChangeEvent;
import yass.events.StorageAvailabilityChangeEvent;
import yass.frontend.FrontendImpl;
import dks.niche.events.ComponentFailEvent;
import dks.niche.events.ResourceLeaveEvent;
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
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;

/**
 * The <code>StorageAggregator</code> class
 * 
 * @author Joel
 * @version $Id: StorageAggregator.java 294 2006-05-05 17:14:14Z joel $
 */
public class StorageAggregator implements EventHandlerInterface, MovableInterface, InitInterface,
		BindingController, LifeCycleController {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = -9008437658170151593L;

	static String TEST_FLAG = System.getProperty("yass.test.mode");

	static final int TEST_MODE = TEST_FLAG instanceof String ? Integer
			.parseInt(TEST_FLAG) : -1;

	// ///////////////////
	Component mySelf;

	TriggerInterface triggerInterface;

	NicheId myId;

	/*
	 * Keeping the state of the system...
	 */

	// 0
	NicheId groupId; // maybe not needed??

	// 1 (the totalSize is for now extracted from the ResourceRef)
	//HashMap<String, Integer> usedStorage;

	HashMap<String, Integer> totalStorage;
	HashMap<String, Integer> usedStorage;

	// 2, 3
	int maximumAllocatedStorage;

	int minimumAllocatedStorage;

	// 4, 5
	double upperTotalUtilizationThreshold;

	double lowerTotalUtilizationThreshold;
	
	double averageUtilization;

	// 6
	int availabilityTimerPeriod;

	// not sent
	int currentAllocatedStorage = 0;

	double upperIndividualUtilizationThreshold;
	
	transient int storageThreshold;

	transient int currentUsedStorage = 0;

	transient long availabilityTimerId;

	private NicheActuatorInterface actuator;

	private NicheAsynchronousInterface logger;

	private boolean status;

	private boolean hasTriggeredLoadIsHighEvent = false;
	
	private HashMap<String, Boolean> hasFiredIndividualLoadHigh;

	/*#%*/ transient String latestLogMessage;

	private boolean timeToDoReinit;
	// empty constructor always needed!

	public StorageAggregator() {

	}

	@SuppressWarnings("unchecked")
	public void init(Object[] parameters) {


		hasFiredIndividualLoadHigh = new HashMap<String, Boolean>();
		
		this.groupId = (NicheId) parameters[0];

		this.maximumAllocatedStorage = (Integer) parameters[1];
		this.minimumAllocatedStorage = (Integer) parameters[2];

		this.upperTotalUtilizationThreshold = (Double) parameters[3];
		this.lowerTotalUtilizationThreshold = (Double) parameters[4];

		this.upperIndividualUtilizationThreshold = 1.2 * upperTotalUtilizationThreshold;
		
		this.availabilityTimerPeriod = (Integer) parameters[5];
		
		/*#%*/ latestLogMessage = "StorageAggregator " + myId + " says: ";
		
		ArrayList<ComponentId> tempCId = (ArrayList<ComponentId>) parameters[6];
			
		totalStorage = new HashMap<String, Integer>();
		usedStorage = new HashMap<String, Integer>();
		averageUtilization = 0;
		
		int size;
			// int i = 0;
		for (ComponentId cid : tempCId) {
			size = cid.getResourceRef().getTotalStorage(); // TODO: should
			// eventually be:
			// cid.getResourceRef().getAllocatedStorage();
			totalStorage.put(cid.getId().toString(), size);
			usedStorage.put(cid.getId().toString(), 0);
			currentAllocatedStorage += size;
		}
	
		/*#%*/ latestLogMessage += " I'm initialized with currently allocated storage = "
		/*#%*/ 					+ currentAllocatedStorage
		/*#%*/ 					+ ". Desired amount is at least "
		/*#%*/ 					+ minimumAllocatedStorage
		/*#%*/ 					+ (minimumAllocatedStorage < currentAllocatedStorage ?
		/*#%*/ 							" so there is enough"
		/*#%*/ 						:
		/*#%*/ 							" so there is NOT enough storage"
		/*#%*/ 					)
		/*#%*/ 					;
		
		/*#%*/ if (TEST_MODE == FrontendImpl.DEMO_MODE
		/*#%*/ 		|| TEST_MODE == FrontendImpl.FAIL_TEST) {
		/*#%*/ 	System.out
		/*#%*/ 			.println(latestLogMessage);
		/*#%*/ 
		/*#%*/ }

		availabilityTimerId = -1; // 0; //actuator.registerTimer(this,
		// AvailabilityTimerTimeoutEvent.class,
		// availabilityTimerPeriod);
		// System.out.println("StorageAggregator created!");

	}
	
	public void reinit(Object[] parameters) {


		hasFiredIndividualLoadHigh = new HashMap<String, Boolean>();
		
		this.groupId = (NicheId) parameters[0];

		this.maximumAllocatedStorage = (Integer) parameters[1];
		this.minimumAllocatedStorage = (Integer) parameters[2];

		this.upperTotalUtilizationThreshold = (Double) parameters[3];
		this.lowerTotalUtilizationThreshold = (Double) parameters[4];

		this.upperIndividualUtilizationThreshold = 1.2 * upperTotalUtilizationThreshold;
		
		this.availabilityTimerPeriod = (Integer) parameters[5];	
		
		this.averageUtilization = (Double)parameters[6];
				
		availabilityTimerId = -1; 
		
		if(timeToDoReinit) {
			reinit();
		} else {
			timeToDoReinit = true;
		}

	}

	public void init(NicheActuatorInterface actuator) {
		this.actuator = actuator;
		this.logger = actuator.testingOnly();
		if(timeToDoReinit) {
			reinit();
		}  else {
			timeToDoReinit = true;
		/*#%*/ logger.log(latestLogMessage);
		 }
	}

	public void initId(Object id) {
		myId = (NicheId) id;
	}

	private void reinit() {
		
		/*#%*/ latestLogMessage = "StorageAggregator " + myId + " says: ";
		
		totalStorage = new HashMap<String, Integer>();
		usedStorage  = new HashMap<String, Integer>();
		currentAllocatedStorage = 0;
		
		int size;
		//Really ComponentIds, but they must be cast individually
		Object[] currentMembers = null;
		while(null == currentMembers) {
			try{
				currentMembers = (Object[])actuator.query(groupId, NicheComponentSupportInterface.GET_CURRENT_MEMBERS);
			} catch (OperationTimedOutException e) {
				//do nuffin, just loop
			}
		}
		 
		ComponentId tempComponentId;
		
		for (Object cidObject : currentMembers) {
			tempComponentId = (ComponentId)cidObject;
			size = tempComponentId.getResourceRef().getTotalStorage();
			// TODO: should
			// eventually be:
			// cid.getResourceRef().getAllocatedStorage();
			totalStorage.put(tempComponentId.getId().toString(), size);
			//this is just a hint/guess until we get updated stats: this can be very out of sync
			usedStorage.put(tempComponentId.getId().toString(), (int)(averageUtilization*size));
			currentAllocatedStorage += size;
		}
		
		/*#%*/ latestLogMessage += " I'm re-initialized with currently allocated storage = "
			/*#%*/ 	+ currentAllocatedStorage
			/*#%*/ 	+ ". Desired amount is at least "
			/*#%*/ 	+ minimumAllocatedStorage
			/*#%*/ 	+ (minimumAllocatedStorage < currentAllocatedStorage ?
			/*#%*/ 			" so there is enough"
			/*#%*/ 		:
			/*#%*/ 			" so there is NOT enough storage"
			/*#%*/ 	)
			/*#%*/ 	;
			
			/*#%*/ if (TEST_MODE == FrontendImpl.DEMO_MODE
			/*#%*/ 		|| TEST_MODE == FrontendImpl.FAIL_TEST) {
			/*#%*/ 	System.out
			/*#%*/ 			.println(latestLogMessage);
			/*#%*/ }
			
			if(minimumAllocatedStorage < currentAllocatedStorage) {
				
				//Don't trigger now, just set the timer &
				//give the syst. some more time to stabilize
				availabilityTimerId = actuator.registerTimer(this,
						AvailabilityTimerTimeoutEvent.class,
						availabilityTimerPeriod);
				hasTriggeredLoadIsHighEvent = true;

				
			}

	}
	protected void updateParameters(Object[] parameters) {

		this.maximumAllocatedStorage = (Integer) parameters[0];
		this.minimumAllocatedStorage = (Integer) parameters[1];

		this.upperTotalUtilizationThreshold = (Double) parameters[2];
		this.lowerTotalUtilizationThreshold = (Double) parameters[3];

		this.availabilityTimerPeriod = (Integer) parameters[4];

	}

	public void handleResourceLeaveEvent(ResourceLeaveEvent e) {

		// remove the amount of storage that was allocated on the leaving
		// resource, and remove the resource itself
		
		/*#%*/ String logMessage =
		/*#%*/ 	"StorageAggregator has received a ResourceLeaveEvent & is removing: "
		/*#%*/ 	+ e.getNicheId().toString();
		
		/*#%*/ if (TEST_MODE == FrontendImpl.DEMO_MODE) {
		/*#%*/ 	System.out
		/*#%*/ 			.println(logMessage);
		/*#%*/ }
		/*#%*/ logger.log(logMessage);
		
		// ResourceDescription leavingResource =
		// resourceUsage.remove(e.getComponentId().getId().toString());
		int size = totalStorage.remove(e.getNicheId().getId().toString());

		currentAllocatedStorage = currentAllocatedStorage - size;

		// currentUtilization = currentUtilization -
		// leavingResource.getCurrentUtilization();

		if (currentAllocatedStorage < minimumAllocatedStorage) {

			// System.out.println("SA says: Therefore I'm informing the
			// manager!");

			if (availabilityTimerId > 0) {
				actuator.cancelTimer(availabilityTimerId);
			}
			triggerInterface.trigger(new StorageAvailabilityChangeEvent(
					currentAllocatedStorage, currentUsedStorage));
			availabilityTimerId = actuator.registerTimer(this,
					AvailabilityTimerTimeoutEvent.class,
					availabilityTimerPeriod);
			// Give management time to react and fix the problem
		}
	}

	public void handleComponentFailEvent(ComponentFailEvent e) {
		// remove the amount of storage that was allocated on the failed
		// resource, and remove the resource itself
		
		if (totalStorage.containsKey(e.getNicheId().toString())) {

			/*#%*/ logger.log("ComponentFailEvent with id " + e.getNicheId() + " received");
			
			int failedResource = totalStorage.remove(e.getNicheId().toString());
			currentAllocatedStorage = currentAllocatedStorage - failedResource;
			currentUsedStorage = currentUsedStorage
					- usedStorage.get(e.getNicheId().toString());

			double currentUsage = (double) currentUsedStorage
					/ (double) currentAllocatedStorage;

			if (TEST_MODE == FrontendImpl.DEMO_MODE
					||
				TEST_MODE == FrontendImpl.FAIL_TEST) {
				
				System.out
						.println("StorageAggregator has received a ComponentFailEvent and is removing: "
								+ e.getNicheId().getId().toString() + " ");
				System.out
						.println("The result is that currentAllocatedStorage = "
								+ currentAllocatedStorage
								+ " vs minimumAllocatedStorage = "
								+ minimumAllocatedStorage);
			} 
			
			if (currentAllocatedStorage < minimumAllocatedStorage
					|| upperTotalUtilizationThreshold < currentUsage) {
				actuator.cancelTimer(availabilityTimerId);
				triggerInterface.trigger(new StorageAvailabilityChangeEvent(
						currentAllocatedStorage, currentUsedStorage));
				availabilityTimerId = actuator.registerTimer(this,
						AvailabilityTimerTimeoutEvent.class,
						availabilityTimerPeriod);
				hasTriggeredLoadIsHighEvent = false;
			}
		} /*#%*/ else {
		/*#%*/ logger.log("ComponentFailEvent with id " + e.getNicheId()
		/*#%*/ 			+ " DUPLICATED");
		/*#%*/ }

	}

	public void handleMemberAddedEvent(MemberAddedEvent e) {

		// ResourceDescription rd;
		ComponentId cid = (ComponentId) e.getSNR();
		String newId = cid.getId().toString();
		if (totalStorage.containsKey(newId)) {
			// System.out.println("SA says: "+cid.getId().toString() +" was
			// already registered");
			/*#%*/ logger.log("MemberAddedEvent DUPLICATED"); 
		} else {
			// rd = cid.getResourceRef().getResourceDescription();
			int size = cid.getResourceRef().getTotalStorage(); // TODO: should
			// be:
			// cid.getResourceRef().getAllocatedStorage();
			totalStorage.put(newId, size);
			usedStorage.put(newId, 0); //init to 0
			currentAllocatedStorage += size;
			// if (TEST_MODE == FrontendImpl.DEMO_MODE) {
			// System.out.println("StorageAggregator is adding component "
			// + cid.getId().toString() + " with size " + size
			// + " to a new total of " + currentAllocatedStorage);
			// }
			hasTriggeredLoadIsHighEvent = false;
			/*#%*/ logger.log(
			/*#%*/ 	"MemberAddedEvent processed: "
			/*#%*/ 	+ newId
			/*#%*/ 	+ " was added"
			/*#%*/ );
		}

	}

	public synchronized void handleComponentStateChangeEvent(
			ComponentStateChangeEvent e) {
		
		// trigger

		// int changedResource =
		// totalStorage.get(e.getAffectedComponent().toString());
		int delta = e.getNewState() - e.getOldState();
		currentUsedStorage = currentUsedStorage + delta;
		int totalSizeOfComponent = totalStorage.get(
				e.getAffectedComponent().getId().toString()
		);
		
		usedStorage.put(e.getAffectedComponent().getId().toString(), e
				.getNewState());

		averageUtilization = (double) currentUsedStorage
				/ (double) currentAllocatedStorage;

		double currentComponentUsage = 0;
		
		//This is a rather primitive fix to avoid repeated reports from the same component
		String id = e.getAffectedComponent().getId().toString();
		if(!hasFiredIndividualLoadHigh.containsKey(id)) {
			currentComponentUsage = (double) e.getNewState()
			/ (double) totalSizeOfComponent;
			if(upperIndividualUtilizationThreshold < currentComponentUsage) {
				hasFiredIndividualLoadHigh.put(id, true);
			}
		}		
		
		if ( (upperTotalUtilizationThreshold < averageUtilization || upperIndividualUtilizationThreshold < currentComponentUsage) 
				&& !hasTriggeredLoadIsHighEvent) {

			/*#%*/ String message = "StorageAggregator is triggering a LoadIsHigh-event. The total current usage is now "
			/*#%*/ 	+ (int)(100 * averageUtilization) + " and the most loaded component is " + (int)(100 * currentComponentUsage) + " full";
			
			/*#%*/ logger.log(message);
			/*#%*/ if (TEST_MODE == FrontendImpl.DEMO_MODE) {
			/*#%*/ 	System.err
			/*#%*/ 			.println(message); //
			/*#%*/ }

			availabilityTimerId = actuator.registerTimer(this,
					AvailabilityTimerTimeoutEvent.class,
					availabilityTimerPeriod); // Instead of
			// resource-join-functionality,
			// we set a timer and poll the
			// system...
			triggerInterface.trigger(new StorageAvailabilityChangeEvent(
					currentAllocatedStorage, currentUsedStorage));
			hasTriggeredLoadIsHighEvent = true;
		
		}
		else {
//			 System.err.println("$$$$$$$$$$$$ The StorageAggregator is NOT " +
//			 "triggering a loadishigh-event, since delta "+ delta + " made " +
//			" currentUsage become "+ currentUsage + ": used " +
//			 currentUsedStorage + "/ allocated " + currentAllocatedStorage +
//			 " OR "+hasTriggeredLoadIsHighEvent);
			/*#%*/ logger.log("The StorageAggregator is NOT triggering any LoadIsHighEvent");
		}

	}

	private void handlerAvailabilityTimerTimeout() {

		// Check current status
		// - hasTriggeredLoadIsHighEvent is reset when a new resource has joined,
		// so it's enough to check thatone
		if(!hasTriggeredLoadIsHighEvent) {

//			double currentUsage = (double) currentUsedStorage
//				/ (double) currentAllocatedStorage;
//
//			if (currentAllocatedStorage < minimumAllocatedStorage
//				|| upperUtilizationThreshold < currentUsage) {
			triggerInterface.trigger(
					new StorageAvailabilityChangeEvent(
							currentAllocatedStorage,
							currentUsedStorage)
					);
			
			availabilityTimerId =
				actuator.registerTimer(
						this,
						AvailabilityTimerTimeoutEvent.class,
						availabilityTimerPeriod
				);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.NicheEventHandlerInterface#getHandlerMethodName(java.lang.Class)
	 */
	public void eventHandler(Object e, int flag) {
		// System.out.println("SA says: I got event!");
		/*#%*/ 	logger.log("StorageAggregator got event of type "+e.getClass().getSimpleName());
		if (e instanceof ResourceLeaveEvent) {
			handleResourceLeaveEvent((ResourceLeaveEvent) e);
		} else if (e instanceof ComponentFailEvent) {
			handleComponentFailEvent((ComponentFailEvent) e);
		} else if (e instanceof MemberAddedEvent) {
			handleMemberAddedEvent((MemberAddedEvent) e);
		} else if (e instanceof ComponentStateChangeEvent) {
			handleComponentStateChangeEvent((ComponentStateChangeEvent) e);
		} else if (e instanceof AvailabilityTimerTimeoutEvent) {
			handlerAvailabilityTimerTimeout();
		} else {
			// System.out.println("Sa says: unknown event type, error");
		}

	}

	
	//Re-init material
	public Object[] getAttributes() {

		Object [] parameters = new Object[] {
					
			this.groupId, //0
			this.maximumAllocatedStorage, //1
			this.minimumAllocatedStorage, //2
			this.upperTotalUtilizationThreshold, //3
			this.lowerTotalUtilizationThreshold, //4
			this.availabilityTimerPeriod, //5
			this.averageUtilization //6			
		};
		//Don't send, re-compute instead: this.currentAllocatedStorage;
		return parameters;
		
	}
	// FRACTAL STUFF

	public String[] listFc() {
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE };
	}

	public Object lookupFc(String interfaceName)
			throws NoSuchInterfaceException {
		if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			return mySelf;
		else if (interfaceName
				.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return null;
		else if (interfaceName
				.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			return triggerInterface;
		else
			throw new NoSuchInterfaceException(interfaceName);
	}

	public void bindFc(String interfaceName, Object stub)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
			mySelf = (Component) stub;
		} else if (interfaceName
				.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE)) {
			// nuffin
		} else if (interfaceName
				.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE)) {
			triggerInterface = (TriggerInterface) stub;
		} else
			throw new NoSuchInterfaceException(interfaceName);
	}

	public void unbindFc(String interfaceName) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
			mySelf = null;
		} else if (interfaceName
				.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE)) {
			// nuffin
		} else if (interfaceName
				.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE)) {
			triggerInterface = null;
		} else
			throw new NoSuchInterfaceException(interfaceName);

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
