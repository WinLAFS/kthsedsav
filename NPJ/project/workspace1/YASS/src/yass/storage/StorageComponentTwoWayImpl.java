package yass.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;

import yass.frontend.FrontendImpl;
import yass.interfaces.ReplicaRestoreRequest;
import yass.interfaces.YASSNames;
import yass.sensors.LoadChangeInterface;
import yass.tests.HealingTest;
import dks.niche.exceptions.OperationTimedOutException;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;

public class StorageComponentTwoWayImpl implements FileWriteRequest, FindReplicas, ReplicaRestoreRequest, FileWrite, FileRead,
FileRemove,	ServiceAttributes, BindingController, LifeCycleController {

	// Client Interfaces
	FindReplicas findReplicas;

	FindReplicasAck findReplicasAck;

	FileWriteRequestAck fileWriteRequestAck;

	FileWriteAck fileWriteAck;

	FileReadAck fileReadAck;

	LoadChangeInterface pushLoadChange;

	// for restoration:
	FileWrite fileWrite;

	private Component myself;

	private boolean status;

	private boolean busy;

	// Local Variables
	static final int TTL = 20;

	static final int TEST_MODE = System.getProperty("yass.test.mode") instanceof String ? Integer
			.parseInt(System.getProperty("yass.test.mode"))
			: -1;

	ComponentId myGlobalId;

	String myLocation;

	int totalSpace;

	int freeSpace;

	//GroupId currentFileGroup;

	Random myRandom = new Random();

	Hashtable<String, File> storage;

	Hashtable<Integer, Boolean> pendingWriteRequests;

	Hashtable<String, Long> pendingWriteRequestSizes;

	//Hashtable<String, Integer> pendingWriteRequestHops;
	
	Hashtable<Integer, Boolean> activeRestoreRequests;

	Hashtable<String, FileGroup> pendingFileGroups;

	Hashtable<String, String> storedFileGroupsId2Name;

	Hashtable<String, GroupId> storedFileGroupsName2Id;

	// For healing-time-measuring:
	static Properties healingTimes;

	NicheComponentSupportInterface nicheOSSupport;

	NicheAsynchronousInterface logger;

	ArrayList<StoreRequest> pendingStoreRequests;

	ArrayList<GroupId> pendingRestoreRequests; // Should be HIGH-PRIO!

	ArrayList<GroupId> waitingRestoreRequests; // Unsuccesful ones has to wait

	public StorageComponentTwoWayImpl() {
		System.err.println("STORAGE with two-ways bindings created");
		storage = new Hashtable<String, File>();
		pendingFileGroups = new Hashtable<String, FileGroup>();

		pendingWriteRequests = new Hashtable<Integer, Boolean>();
		pendingWriteRequestSizes = new Hashtable<String, Long>();
		
		activeRestoreRequests = new Hashtable<Integer, Boolean>();
		storedFileGroupsName2Id = new Hashtable<String, GroupId>();
		storedFileGroupsId2Name = new Hashtable<String, String>();

		pendingStoreRequests = new ArrayList();
		pendingRestoreRequests = new ArrayList();
		waitingRestoreRequests = new ArrayList();

		status = false;
		totalSpace = freeSpace = 0; // DEFAUT_STORAGE;
	}

	private void nextOperation() {

		// System.out.println(">>>>>> STORAGE at " + myLocation + ": processing
		// nextOperation()");
		//synchronized (this) {

			if (!busy) {
				// System.out.println(">>>>>> STORAGE at " + myLocation + ":
				// processing nextOperation(): was NOT busy");
				busy = true;
				if (pendingRestoreRequests.size() > 0) { // HIGH-prio!
					processReplicaRestoreRequest(pendingRestoreRequests
							.remove(0));
				} else if (waitingRestoreRequests.size() > 0) {

					/*#%*/ logger.log(">>>>>> STORAGE: SLEEP before processing unsuccessful replica-restoration");
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					/*#%*/ logger.log(">>>>>> STORAGE: Now processing unsuccessful replica-restoration");

					processReplicaRestoreRequest(waitingRestoreRequests
							.remove(0));
				} else {
					busy = false;
				}
			} else {
				// System.out.println(">>>>>> STORAGE at " + myLocation + ":
				// processing nextOperation(): was busy");
			}

		//}
	}

	
	// front end one-to-any -
	// - can come from any front-end, needs to be queued!
	public YassResult fileWriteRequest(String uniqueFileName,
			ComponentId initiator, long size, int replicationDegree,
			Object userRef) {
		// System.out.println(">>>>>> STORAGE at " + myLocation
		// + ": received fileWriteRequest() : " + uniqueFileName);

		/*#%*/ logger.log(">>>>>> STORAGE received fileWriteRequest() : "
		/*#%*/ 		+ uniqueFileName);

		// pendingStoreRequests.add(new StoreRequest(uniqueFileName, initiator,
		// size, replicationDegree, userRef));
		// nextOperation();

		return processFileWriteRequest(uniqueFileName, initiator, size,
				replicationDegree, userRef);

	}

	// internal
	private YassResult processFileWriteRequest(String uniqueFileName,
			ComponentId initiator, long size, int replicationDegree,
			Object userRef) {
		// System.out.println(">>>>>> STORAGE at " + myLocation
		// + ": processing fileWriteRequest() : " + uniqueFileName);

		/*#%*/ logger.log(">>>>>> STORAGE processing fileWriteRequest() : "
		/*#%*/ 		+ uniqueFileName);

		pendingFileGroups.put(uniqueFileName, new FileGroup(replicationDegree,
				initiator, userRef));
		int uid = myRandom.nextInt();

		int ttl = TTL;
		YassResult temp;

		FileGroup fg = new FileGroup(replicationDegree, initiator, userRef);

		while (ttl > 0) {

			temp = findReplicas.findReplicas(uniqueFileName, size, uid,
					replicationDegree, userRef, myGlobalId, ttl);
			
			ttl--;

			if (temp.isSucceeded()) {
				fg.members.add(temp.getReplicaHost());
				
				/*#%*/ logger.log(">>>>>> STORAGE: replica found for "+ uniqueFileName + " with ttl " + ttl + " new host is " + temp.getReplicaHost().getId());
				
				if (fg.isReady()) {


					/*#%*/ logger.log(">>>>>> STORAGE-replicaStorageAccepted() for "
					/*#%*/ 		+ uniqueFileName + " for FE " + fg.initiator.getId() + " in " + (TTL-ttl) + " number of hops");

					GroupId globalFileGroupId = nicheOSSupport
							.createGroup(YASSNames.TYPE_REPLICA_GROUP, fg.members);

					return new YassResult(globalFileGroupId, (TTL-ttl));

				}

			} /*#%*/ else {
			/*#%*/ logger.log(">>>>>> STORAGE: replica NOT found for "+ uniqueFileName + " with ttl " + ttl);
			/*#%*/ }
		}
		// ttl == 0, store-request failed, return null
		return new YassResult("The store request could not be fulfilled right now, please try again later");

	}

	// storage component one-to-any
	public YassResult findReplicas(String uniqueFileName, long size, int uid,
			int replicationDegree, Object userRef, Object leaderId, int ttl) {
		
		if (size <= freeSpace && !pendingWriteRequests.containsKey(uid)) {

			// System.out.println(">>>>>> STORAGE at " + myLocation
			// + ": findReplicas() for " + uniqueFileName);

			/*#%*/ logger.log(">>>>>> STORAGE-findReplicas(), server side. Accepting file " + uniqueFileName);

			pendingWriteRequests.put(uid, true);
			pendingWriteRequestSizes.put(uniqueFileName, size);

			// inform the owner
			
			return new YassResult(myGlobalId);

		}

		String reason = ">>>>>> STORAGE-findReplicas() for " + uniqueFileName
		 		+ ": Denied!\n";
		
		if (!pendingWriteRequests.containsKey(uid)) {
		 reason += " Not enough free space! Had " + freeSpace	+ " but needed " + size;
			
		} else {
			 reason += " File already accepted!";
		}
		/*#%*/ logger.log(reason);
		return new YassResult(reason);

	}


	public synchronized void fileWrite(String uniqueFileName, ComponentId initiator,
			File theFile, GroupId group) {
		fileWrite(uniqueFileName, initiator, theFile, group, false);
	}

	public void replicaFileWrite(String uniqueFileName, File theFile,
			GroupId group, ComponentId myself) {
		fileWrite(uniqueFileName, null, theFile, group, true);
	}

	private void fileWrite(String uniqueFileName,
			ComponentId initiator, File theFile, GroupId theGroup,
			boolean restoreRequest) {

		// What happens if two ppl try to store two large files at once, CHECK
		// System.out.println(">>>>>> STORAGE at " + myLocation
		// + ": fileWrite() : " + uniqueFileName + " represented as: "
		// + theGroup.getId());

		/*#%*/ logger.log(">>>>>> STORAGE-fileWrite() : " + uniqueFileName
		/*#%*/ 		+ " represented as: " + theGroup.getId());

		Long s = pendingWriteRequestSizes.get(uniqueFileName);
				
		freeSpace -= s;

		if (s != null) {

			storage.put(uniqueFileName, theFile);
			storedFileGroupsId2Name.put(theGroup.getId().toString(),
					uniqueFileName);
			storedFileGroupsName2Id.put(uniqueFileName, theGroup);

			if (!restoreRequest) {

				fileWriteAck.fileWriteSucceeded(uniqueFileName, initiator);
				if (TEST_MODE == FrontendImpl.DEMO_MODE) {
					System.out.println("File " + uniqueFileName
							+ " successfully stored in component at node "
							+ myGlobalId.getResourceRef().getDKSRef().getId());
				}

			} else {

				if (TEST_MODE == FrontendImpl.DEMO_MODE) {
					System.out.println("File " + uniqueFileName
							+ " successfully restored in component at node "
							+ myGlobalId.getResourceRef().getDKSRef().getId());
				}
				//int hops = pendingWriteRequestHops.get(uniqueFileName);

			}
			
			if (pushLoadChange != null) {
				// System.err.println("$$$$$$$$$$$$ StorageComponent at " +
				// myLocation + " is pushing the new load to the LoadSensor: " +
				// (totalSpace-freeSpace));
				pushLoadChange.newLoad(totalSpace - freeSpace);
			}
			if (TEST_MODE == FrontendImpl.FAIL_TEST) {
				String latestTime = "" + System.currentTimeMillis();
				healingTimes.put(uniqueFileName, latestTime);
				healingTimes.put(HealingTest.LATEST_LOCAL_STORE_TIME,
						latestTime);
				// System.out.println("Putting: " + uniqueFileName + " @ " +
				// latestTime);
			}
			
			return; // new YassResult("not used"); //not used
		}

		System.err.println(">>>>>> STORAGE at " + myLocation
				+ ": fileWrite() : " + uniqueFileName + " represented as: "
				+ theGroup.getId() + " FAILED");

		/*#%*/ logger.log(">>>>>> STORAGE-fileWrite() : " + uniqueFileName
		/*#%*/ 		+ " represented as: " + theGroup.getId() + " FAILED");

		fileWriteAck.fileWriteFailed(uniqueFileName, initiator);
		return; // new YassResult("not used"); //not used;

	}

	public YassResult fileRead(String uniqueFileName, ComponentId initiator,
			boolean flag) {
		/*#%*/ logger.log(">>>>>> STORAGE-fileRead(): File = \"" + uniqueFileName
		/*#%*/ 		+ "\" ");
		File file = storage.get(uniqueFileName);
		if (file == null) {
			/*#%*/ 	logger.log("Failed");
			return new YassResult("File read of file " + uniqueFileName + " failed");
		} 
		
		/*#%*/ logger.log("Successful");
		if (TEST_MODE == FrontendImpl.DEMO_MODE) {
			System.out.println("File " + uniqueFileName
					+ " retrieved from component at node "
					+ myGlobalId.getResourceRef().getDKSRef().getId());
		}
		return new YassResult(file);
		
	}

	public void fileRemove(String uniqueFileName) {
		/*#%*/ logger.log(">>>>>> STORAGE-fileRemove(): " + uniqueFileName);
		File file = storage.remove(uniqueFileName);

		if (file != null) {

			GroupId gid = storedFileGroupsName2Id.remove(uniqueFileName);
			if (gid != null) {
				storedFileGroupsId2Name.remove(gid.getId().toString());

				freeSpace += file.length();
				/*#%*/ logger.log(" Successful. Now " + freeSpace + " available");

				if (TEST_MODE == FrontendImpl.DEMO_MODE) {
					System.out.println("File " + uniqueFileName
							+ " removed from component at node "
							+ myGlobalId.getResourceRef().getDKSRef().getId());
				}

				// fileRemoveAck.fileRemoveSuccessful(uniqueFileName);
				nicheOSSupport.removeGroup(gid);
				if (pushLoadChange != null) {
					/*#%*/ logger.log("$$$$$$$$$$$$ StorageComponent"
					/*#%*/ 		+ " is pushing the new load to the LoadSensor: "
					/*#%*/ 		+ (totalSpace - freeSpace));
					pushLoadChange.newLoad(totalSpace - freeSpace);
				}
			} /*#%*/ else {
			/*#%*/ logger.log("fileRemove Failed - no group");
			/*#%*/ }
		} /*#%*/ else {
		/*#%*/ logger.log("fileRemove Failed - no file");
		/*#%*/ }

		// TODO Remove file group
	}

	// manager one-to-any
	public YassResult replicaRestoreRequest(GroupId groupId) {
		/*#%*/ logger.log(">>>>>> STORAGE says: received request to restore " + groupId.getId());
		pendingRestoreRequests.add(groupId);
		nextOperation();
		return new YassResult("Queued");
	}

	private void processReplicaRestoreRequest(GroupId groupId) { // throws
		// FileNotFoundException
		// {

		GroupId currentFileGroup = groupId; //localize = just rename it!
		String uniqueFileName = storedFileGroupsId2Name.get(((GroupId) groupId)
				.getId().toString());
		if (uniqueFileName != null) {

			File f = storage.get(uniqueFileName);
			/*#%*/ logger.log(">>>>>> STORAGE-replicaRestoreRequest(): I'm responsible to find a new replica host for "
			/*#%*/ 				+ uniqueFileName + " file: " + f);
			int uid = 2 * myRandom.nextInt();
			int replicationDegree = 1; // ??

			YassResult result = null;
			int ttl = TTL;
			while (ttl > 0) {
				ttl--;
				try {
				result = findReplicas.restoreReplicas(uniqueFileName, f
						.length(), uid, replicationDegree, groupId, myGlobalId,
						ttl);
				} catch(OperationTimedOutException e) {
					break; //exit this while loop and put this specific job in queue.
				}
				if (result.isSucceeded()) {
					// Now just assume they crash one by one, ok?
					/*#%*/ logger.log(">>>>>> STORAGE-replicaRestoreAccepted(): : "
					/*#%*/ 		+ uniqueFileName + " by "
					/*#%*/ 		+ result.getReplicaHost().getId()
					/*#%*/ 		+ " - add to group and send file to it");
					nicheOSSupport.addToGroup(result.getReplicaHost(), currentFileGroup);
					fileWrite.replicaFileWrite(uniqueFileName, f,
							currentFileGroup, result.getReplicaHost());
					/*#%*/ logger.log(">>>>>> STORAGE-replicaRestore() - DONE: for group "
					/*#%*/ 		+ ((GroupId) groupId).getId() + "& file "
					/*#%*/ 		+ uniqueFileName);

					synchronized (this) {
						busy = false;
						nextOperation();
					}

					return;
				}
			}
			/*#%*/ logger.log(">>>>>> STORAGE at "
			/*#%*/ 				+ myLocation
			/*#%*/ 				+ ": replicaRestoreDENIED(): Warning! Replication degree could not be restored for: "
			/*#%*/ 				+ uniqueFileName);
			/*
			 * In this case, put the group back in the queue & try again later
			 */
			waitingRestoreRequests.add((GroupId) groupId); //this will loop! 
			
			synchronized (this) {
				busy = false;
				nextOperation();
			}

			return;
			
		}
		/*#%*/ logger.log(">>>>>> STORAGE-replicaRestoreRequest(): I cannot find a new replica host for "
		/*#%*/ 				+ groupId.getId()
		/*#%*/ 				+ " since I dont have the file myself!!!");

		synchronized (this) {
			busy = false;
			nextOperation();
		}

		return;


	}

	// storage component one-to-any
	public YassResult restoreReplicas(String uniqueFileName, long size,
			int uid, int replicationDegree, Object groupId, Object leaderId,
			int ttl) {

		ttl--;
		if (size <= freeSpace && !activeRestoreRequests.containsKey(uid)
				&& !storage.containsKey(uniqueFileName)) {

			/*#%*/ logger.log(">>>>>> STORAGE-restoreReplicas() : " + uniqueFileName
			/*#%*/ 		+ " Accepted by " + ((ComponentId) myGlobalId).getId());
			activeRestoreRequests.put(uid, true);
			pendingWriteRequestSizes.put(uniqueFileName, size);

			replicationDegree--;
			// inform the owner, cannot be local since the leader already has
			// the file---
			return new YassResult(myGlobalId);

		}

		String reason = ">>>>>> STORAGE-restoreReplicas(): " + uniqueFileName
		+ " Denied by " + ((ComponentId) myGlobalId).getId()
		+ " because: I already agreed to restore it: "
		+ (activeRestoreRequests.containsKey(uid))
		+ " or I already have the file "
		+ (storage.containsKey(uniqueFileName));
		/*#%*/ logger.log(reason);

		return new YassResult(reason);

	}

	// ////////////////////////////////////////////////////////////////////
	// ////////////////////// Helper Classes ////////////////////////////
	// ////////////////////////////////////////////////////////////////////

	class FileGroup {
		int replicationDegree;

		Object userRef;

		ComponentId initiator;

		ArrayList<ComponentId> members;

		public FileGroup(int replicationDegree, ComponentId initiator,
				Object userRef) {
			this.userRef = userRef;
			this.initiator = initiator;
			this.replicationDegree = replicationDegree;
			members = new ArrayList<ComponentId>(replicationDegree);
		}

		boolean isReady() {
			if (members.size() == replicationDegree)
				return true;
			return false;
		}
	}

	class StoreRequest {
		String uniqueFileName;

		ComponentId initiator;

		long size;

		int replicationDegree;

		Object userRef;

		StoreRequest(String uniqueFileName, ComponentId initiator, long size,
				int replicationDegree, Object userRef) {
			this.uniqueFileName = uniqueFileName;
			this.initiator = initiator;
			this.size = size;
			this.replicationDegree = replicationDegree;
			this.userRef = userRef;
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// Attributes
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public int getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(int fs) {
		freeSpace = fs;
	}

	public int getTotalSpace() {
		return totalSpace;
	}

	public void setTotalSpace(int ts) {
		totalSpace = ts;
	}

	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public String[] listFc() {

		return new String[] { "component", "findReplicas", "findReplicasAck",
				"fileWriteRequestAck", "fileWriteAck", "fileReadAck",
				"restoreReplica", "pushLoadChange" };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {

		if (itfName.equals("findReplicas"))
			return findReplicas;
		else if (itfName.equals("findReplicasAck"))
			return findReplicasAck;
		else if (itfName.equals("fileWriteRequestAck"))
			return fileWriteRequestAck;
		else if (itfName.equals("fileWriteAck"))
			return fileWriteAck;
		else if (itfName.equals("fileReadAck"))
			return fileReadAck;
		else if (itfName.equals("restoreReplica"))
			return fileWrite;
		else if (itfName.equals("pushLoadChange"))
			return pushLoadChange;
		else if (itfName.equals("component"))
			return myself;
		else
			throw new NoSuchInterfaceException(itfName);

	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		if (itfName.equals("findReplicas"))
			findReplicas = (FindReplicas) itfValue;
		else if (itfName.equals("findReplicasAck"))
			findReplicasAck = (FindReplicasAck) itfValue;
		else if (itfName.equals("fileWriteRequestAck"))
			fileWriteRequestAck = (FileWriteRequestAck) itfValue;
		else if (itfName.equals("fileWriteAck"))
			fileWriteAck = (FileWriteAck) itfValue;
		else if (itfName.equals("fileReadAck"))
			fileReadAck = (FileReadAck) itfValue;
		else if (itfName.equals("restoreReplica"))
			fileWrite = (FileWrite) itfValue;
		else if (itfName.equals("pushLoadChange"))
			pushLoadChange = (LoadChangeInterface) itfValue;

		else if (itfName.equals("component"))
			myself = (Component) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals("findReplicas"))
			findReplicas = null;
		else if (itfName.equals("findReplicasAck"))
			findReplicasAck = null;
		else if (itfName.equals("fileWriteRequestAck"))
			fileWriteRequestAck = null;
		else if (itfName.equals("fileWriteAck"))
			fileWriteAck = null;
		else if (itfName.equals("fileReadAck"))
			fileReadAck = null;
		else if (itfName.equals("restoreReplica"))
			fileWrite = null;
		else if (itfName.equals("pushLoadChange"))
			pushLoadChange = null;
		else if (itfName.equals("component"))
			myself = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {

		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		Component jadeNode = null;
		Component niche = null;
		OverlayAccess overlayAccess = null;

		Component comps[] = null;
		try {
			comps = Fractal.getSuperController(myself).getFcSuperComponents();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < comps.length; i++) {
			try {
				if (Fractal.getNameController(comps[i]).getFcName().equals(
						"managed_resources")) {
					jadeNode = comps[i];
					break;
				}
			} catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}
			;
		}

		try {
			niche = FractalUtil.getFirstFoundSubComponentByName(jadeNode,
					"nicheOS");
		} catch (NoSuchComponentException e1) {
			e1.printStackTrace();
		}

		try {
			overlayAccess = (OverlayAccess) niche
					.getFcInterface("overlayAccess");
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		nicheOSSupport = overlayAccess.getOverlay().getComponentSupport(myself);
		logger = overlayAccess.getOverlay().getNicheAsynchronousSupport();

		status = true;

		myGlobalId = nicheOSSupport.getResourceManager().getComponentId(myself);
		myLocation = "" + myGlobalId.getResourceRef().getDKSRef().getId();

		totalSpace = freeSpace = nicheOSSupport.getResourceManager()
				.getTotalStorage(myself);
		nicheOSSupport.setOwner(myGlobalId);

		System.err.println("STORAGE Started component = " + myGlobalId.getId()
				+ " at " + myLocation + " with totalspace: " + totalSpace);

		if (TEST_MODE == FrontendImpl.FAIL_TEST) {
			healingTimes = logger.getResourceManager().getTestProperties();
		}

	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;

	}

}
