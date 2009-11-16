package yass.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import yass.events.ReplicaChangeEvent;
import yass.interfaces.ReplicaRestoreRequest;
import yass.interfaces.YASSNames;
import dks.niche.exceptions.OperationTimedOutException;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;

public class FileReplicaManager implements EventHandlerInterface, MovableInterface, 
		InitInterface, BindingController, LifeCycleController {

	// Client Interfaces
	private ReplicaRestoreRequest replicaRestoreRequest;

	private Component myself;

	NicheActuatorInterface actuator;

	NicheAsynchronousInterface logger;

	private boolean status;

	private boolean busy;

	// Local Variables

	// NicheComponentSupport nicheOSSupport;
	NicheId myGlobalId;

	HashMap<String, RestoreJob> pendingRestoreJobs;
	Vector<String> queue;

	public FileReplicaManager() {
		// System.err.println("FileReplicaManager created");
		
		status = false;
		busy = false;
	}

	private void nextOperation() {
		
		if (!busy) {
			
			busy = true;
			RestoreJob restoreJob;
			
			if (queue.size() > 0) {
				
				restoreJob = pendingRestoreJobs.get(queue.remove(0));
				
				// System.out.println("FileReplicaManager says: processing event
				// about group "+currentGroup.getId());
				processReplicaChangeEvent(restoreJob.groupToRestore);
				
			} else {
				busy = false;
			}
		} else {
			// System.out.println("FileReplicaManager says: busy.
			// "+pendingReplicaGroups.size() + " items waiting");
		}
	}

	public void eventHandler(Object e, int flag) {
		
		if(myGlobalId.getReplicaNumber() < 1) {
			
			ReplicaChangeEvent event = (ReplicaChangeEvent) e;
			RestoreJob restoreJob = new RestoreJob(event.getFailedNodeId(), event.getGroupId());
			synchronized (pendingRestoreJobs) {
				if(pendingRestoreJobs.containsKey(restoreJob.getKey())) {
					//duplicate
					/*#%*/ logger.log(myGlobalId.toString() + " says: prime replica got a duplicate");
				} else {
					queue.add(restoreJob.getKey());
					pendingRestoreJobs.put(restoreJob.getKey(), restoreJob);
					/*#%*/ logger.log(myGlobalId.toString() + " says: prime replica taking action");
				}
			}
			// System.out.println("FileReplicaManager says: received event about
			// group "+groupId.getId());
			nextOperation();
		} /*#%*/ else {
		/*#%*/ logger.log(myGlobalId.toString() + " says: replica is ignoring");
		/*#%*/ }
	}

	private synchronized void processReplicaChangeEvent(GroupId groupId) {
		// System.out.println("The FileReplicaManager at "+myGlobalId+" says:
		// trying to restore the group members of "+groupId.getId() + " -
		// initiate bindId");
		/*#%*/ logger.log("FileReplicaManager " + myGlobalId + " says: trying to restore the group members of "
		/*#%*/ 				+ groupId.getId() + " - initiate bindId");
		replicaRestoreRequest = null;
		
		while(null == replicaRestoreRequest) {
		try {
		actuator.bind(myGlobalId,
				YASSNames.CLIENT_INTERFACE_RESTORE_REPLICA_REQUEST, groupId,
				YASSNames.SERVER_INTERFACE_RESTORE_REPLICA_REQUEST,
				JadeBindInterface.ONE_TO_ANY);
		} catch(OperationTimedOutException e) {
			/*#%*/ logger.log("FileReplicaManager " + myGlobalId + " says: System is unstable. Sleep & retry binding");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		} //end while
		/*#%*/ logger.log("FileReplicaManager " + myGlobalId + " says: Binding completed, now send msg to remaining member of "
		/*#%*/ 				+ groupId.getId());
		try {
			replicaRestoreRequest.replicaRestoreRequest(groupId);
		} catch (OperationTimedOutException e) {
			/*#%*/ logger.log(
			/*#%*/		"FileReplicaManager "
			/*#%*/	+ myGlobalId
			/*#%*/	+ " says: the request to restore "
			/*#%*/	+ groupId.getId()
			/*#%*/	+ " timed out. It might have failed!"
			/*#%*/	);
			//TODO: what should be the strategy?
		}
		
		busy = false;
		nextOperation();

	}

	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// Attributes
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public void init(Object[] parameters) {
		// No params to set
		pendingRestoreJobs = new HashMap<String, RestoreJob>();
		queue = new Vector<String>();
	}
	@SuppressWarnings("unchecked")
	public void reinit(Object[] parameters) {
		pendingRestoreJobs = (HashMap<String, RestoreJob>) parameters[0];
		queue = (Vector<String>)parameters[1];

	}

	public void init(NicheActuatorInterface actuator) {
		this.actuator = actuator;
		if (myGlobalId != null) {
			actuator.setOwner((IdentifierInterface) myGlobalId);
		}
		this.logger = actuator.testingOnly();

	}

	public void initId(Object id) {
		myGlobalId = (NicheId) id;
		if (actuator != null) {
			actuator.setOwner((IdentifierInterface) myGlobalId);
		}
	}

	public Object[]getAttributes() {
		return new Object[] {
				//does not work as it is now
			pendingRestoreJobs,
			queue
		};
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	public String[] listFc() {

		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				YASSNames.CLIENT_INTERFACE_RESTORE_REPLICA_REQUEST };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {

		if (itfName.equals(FractalInterfaceNames.COMPONENT))
			return myself;

		if (itfName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return actuator;

		if (itfName.equals(YASSNames.CLIENT_INTERFACE_RESTORE_REPLICA_REQUEST))
			return replicaRestoreRequest;

		throw new NoSuchInterfaceException(itfName);
	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		if (itfName.equals("component"))
			myself = (Component) itfValue;

		else if (itfName
				.equals(YASSNames.CLIENT_INTERFACE_RESTORE_REPLICA_REQUEST)) {
			replicaRestoreRequest = (ReplicaRestoreRequest) itfValue;
		} else if (itfName
				.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			actuator = (NicheActuatorInterface) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals(FractalInterfaceNames.COMPONENT))
			myself = null;
		else if (itfName
				.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			actuator = null;
		else if (itfName
				.equals(YASSNames.CLIENT_INTERFACE_RESTORE_REPLICA_REQUEST))
			replicaRestoreRequest = null;

		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {

		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {

		status = true;

		// System.err.println("FileReplicaManager started");

	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;

	}
	
	

}