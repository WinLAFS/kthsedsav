package yass.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;

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
import yass.tests.HealingTestServlet;
import dks.niche.exceptions.OperationTimedOutException;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;

public class StorageComponent implements FileWriteRequest, FindReplicas, ReplicaRestoreRequest, FindReplicasAck, FileWrite, FileRead,
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
 
	static final int TEST_MODE = System.getProperty("yass.test.mode") instanceof String ? Integer.parseInt(System.getProperty("yass.test.mode")) : -1;

	ComponentId myGlobalId;

	String myLocation;

	int totalSpace;

	int freeSpace;

	GroupId currentFileGroup;

	Random myRandom = new Random();

	Hashtable<String, File> storage;

	Hashtable<Integer, Boolean> pendingWriteRequests;

	Hashtable<String, Long> pendingWriteRequestSizes;
	
	Hashtable<String, Integer> pendingWriteRequestHops;
	
	Hashtable<String, Long> pendingWriteRequestTimes;

	Hashtable<Integer, Boolean> activeRestoreRequests;

	Hashtable<String, FileGroup> pendingFileGroups;

	Hashtable<String, String> storedFileGroupsId2Name;

	Hashtable<String, GroupId> storedFileGroupsName2Id;
	
	//For healing-time-measuring:
	static Properties healingTimes;

	NicheComponentSupportInterface nicheOSSupport;
	NicheAsynchronousInterface logger;

	ArrayList<StoreRequest> pendingStoreRequests;

	ArrayList<GroupId> pendingRestoreRequests; // Should be HIGH-PRIO!

	ArrayList<GroupId> waitingRestoreRequests; // Unsuccesful ones has to wait

	public StorageComponent() {
		System.err.println("STORAGE created");
		storage = new Hashtable<String, File>();
		pendingFileGroups = new Hashtable<String, FileGroup>();

		pendingWriteRequests = new Hashtable<Integer, Boolean>();
		pendingWriteRequestSizes = new Hashtable<String, Long>();
		pendingWriteRequestHops = new Hashtable<String, Integer>();
		pendingWriteRequestTimes = new Hashtable<String, Long>();
		
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
		synchronized (this) {

			if (!busy) {
				// System.out.println(">>>>>> STORAGE at " + myLocation + ":
				// processing nextOperation(): was NOT busy");
				busy = true;
				if (pendingRestoreRequests.size() > 0) { // HIGH-prio!
					processReplicaRestoreRequest(pendingRestoreRequests
							.remove(0));
				} else if (pendingStoreRequests.size() > 0) {
					StoreRequest nextRequest = pendingStoreRequests.remove(0);
					processFileWriteRequest(nextRequest.uniqueFileName,
							nextRequest.initiator, nextRequest.size,
							nextRequest.replicationDegree, nextRequest.userRef);
				} else if (waitingRestoreRequests.size() > 0) {
//					System.out
//							.println(">>>>>> STORAGE at "
//									+ myLocation
//									+ ": SLEEP before processing unsuccessful replica-restoration");
					
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

		}
	}

	// front end one-to-any -
	// - can come from any front-end, needs to be queued!
	public YassResult fileWriteRequest(String uniqueFileName, ComponentId initiator,
			long size, int replicationDegree, Object userRef) {
//		System.out.println(">>>>>> STORAGE at " + myLocation
//				+ ": received fileWriteRequest() : " + uniqueFileName);
		
		/*#%*/ logger.log(">>>>>> STORAGE received fileWriteRequest() : " + uniqueFileName);
		
		pendingStoreRequests.add(new StoreRequest(uniqueFileName, initiator,
				size, replicationDegree, userRef));
		
		pendingWriteRequestTimes.put(uniqueFileName, System.currentTimeMillis());
		
		nextOperation();
		
		return null;
	}

	// internal
	private void processFileWriteRequest(String uniqueFileName,
			ComponentId initiator, long size, int replicationDegree,
			Object userRef) {
//		System.out.println(">>>>>> STORAGE at " + myLocation
//				+ ": processing fileWriteRequest() : " + uniqueFileName);
		
		/*#%*/ logger.log(">>>>>> STORAGE processing fileWriteRequest() : " + uniqueFileName);
		
		
		pendingFileGroups.put(uniqueFileName, new FileGroup(replicationDegree,
				initiator, userRef));
		int uid = myRandom.nextInt();
		findReplicas.findReplicas(uniqueFileName, size, uid, replicationDegree,
				userRef, myGlobalId, TTL);

	}

	// storage component one-to-any
	public YassResult findReplicas(String uniqueFileName, long size, int uid,
			int replicationDegree, Object userRef, Object leaderId, int ttl) {

		ttl--;
		
		boolean accept = size <= freeSpace && !pendingWriteRequests.containsKey(uid); 

		if (accept) {
			replicationDegree--;
			pendingWriteRequests.put(uid, true);
		}

		// check if we need more replicas - this is on critical path, do this first!
		if (replicationDegree > 0) {
			if (ttl > 0) {
				
				/*#%*/ logger.log(">>>>>> STORAGE-findReplicas() - need more replicas for " + uniqueFileName);

				findReplicas.findReplicas(uniqueFileName, size, uid,
						replicationDegree, userRef, leaderId, ttl);
			} else {
				findReplicasAck.replicaStoregeDenied(myGlobalId,
						uniqueFileName, ttl, leaderId);
			}

		}

		if(accept) {
//			System.out.println(">>>>>> STORAGE at " + myLocation
//					+ ": findReplicas() for " + uniqueFileName);

			/*#%*/ logger.log(">>>>>> STORAGE-findReplicas() for " + uniqueFileName);
			
			pendingWriteRequestSizes.put(uniqueFileName, size);

			// inform the owner
			// locally
			if (leaderId.equals(myGlobalId)) {
				replicaStorageAccepted(myGlobalId, uniqueFileName, ttl, leaderId);
			} else {// remotely
				findReplicasAck.replicaStorageAccepted(myGlobalId,
						uniqueFileName, ttl, leaderId);
			}
		} /*#%*/ else {
			/*#%*/ logger.log(">>>>>> STORAGE-findReplicas() for " + uniqueFileName + ": Denied!");
			/*#%*/ if (!pendingWriteRequests.containsKey(uid)) {
				/*#%*/ logger.log(" Not enough free space! Had " + freeSpace
				/*#%*/ 		+ " but needed " + size);
			/*#%*/ } else {
				/*#%*/ logger.log(" File already accepted!");
			/*#%*/ }

			/*#%*/ }

		return null;
	}

	// storage component one-to-one
	public synchronized void replicaStorageAccepted(Object replicaGlobalId,
			String uniqueFileName, int ttl, Object leaderId) {
		// System.out.print(">>>>>> STORAGE: replicaStorageAccepted(): :
		// "+uniqueFileName);
		FileGroup fg = pendingFileGroups.get(uniqueFileName);
		fg.members.add((ComponentId) replicaGlobalId);
		
		Integer ttlSeen = pendingWriteRequestHops.get(uniqueFileName);
		if(ttlSeen != null) {
			if(ttl < ttlSeen) {
				pendingWriteRequestHops.put(uniqueFileName, ttl);
			}
		} else {
			pendingWriteRequestHops.put(uniqueFileName, ttl);
		}
		if (fg.isReady()) {

			// //////////////////////////////////////////////////////////////////
			// //////////////////////////////////////////////////////////////////
			// //////////////////////////////////////////////////////////////////
			// createFileGroup.createFileGroup(uniqueFileName,
			// fg.members.toArray());
			// //////////////////////////////////////////////////////////////////
			// //////////////////////////////////////////////////////////////////
			// //////////////////////////////////////////////////////////////////

			if (pendingFileGroups.containsKey(uniqueFileName)) {

				/*#%*/ logger.log(">>>>>> STORAGE-replicaStorageAccepted() for " + uniqueFileName
				/*#%*/ 		+ " for FE " + fg.initiator);

				GroupId globalFileGroupId = nicheOSSupport
						.createGroup(YASSNames.TYPE_REPLICA_GROUP, fg.members);
				// pendingFileGroups.

				// FIXME: disabled the following only for testing of management
				// deployment!

				int hopsUsed = TTL - pendingWriteRequestHops.get(uniqueFileName);
				long timeUsed = System.currentTimeMillis() - pendingWriteRequestTimes.get(uniqueFileName);

				fileWriteRequestAck.fileWriteRequestAccepted(
						uniqueFileName,
						globalFileGroupId,
						timeUsed,
						hopsUsed,
						fg.initiator
				);
				synchronized (this) {
					busy = false;
					nextOperation();
				}

			} else {
				System.err.println("ERROR, request not properly stored");
			}

		}/*#%*/  else {
		/*#%*/ logger.log(">>>>>> STORAGE-replicaStorageAccepted() for " + uniqueFileName
		/*#%*/ 			+ " but need more replicas");
		/*#%*/ }

	}

	// storage component one-to-one
	public void replicaStoregeDenied(Object replicaGlobalId,
			String uniqueFileName, int ttl, Object leaderID) {
		/*#%*/ logger.log(">>>>>> STORAGE-replicaStoregeDenied() : " + uniqueFileName);
		// FIXME I should tell "me" and other members that we are aborting
		// FIXME I should tell "me" and other members that we are aborting
		// FIXME I should tell "me" and other members that we are aborting
		if (pendingFileGroups.containsKey(uniqueFileName)) {
			FileGroup fg = pendingFileGroups.remove(uniqueFileName);
			// sending to fg.userRef
			fileWriteRequestAck.fileWriteRequestDenied(uniqueFileName, TTL,
					fg.initiator);
			synchronized (this) {
				busy = false;
				nextOperation();
			}
		}

	}

	public void fileWrite(String uniqueFileName, ComponentId initiator,
			File theFile, GroupId group) {
		fileWrite(uniqueFileName, initiator, theFile, group, false);
	}

	public void replicaFileWrite(String uniqueFileName, File theFile,
			GroupId group, ComponentId myself) {
		fileWrite(uniqueFileName, null, theFile, group, true);
	}

	private synchronized void fileWrite(String uniqueFileName,
			ComponentId initiator, File theFile, GroupId theGroup,
			boolean restoreRequest) {

		// What happens if two ppl try to store two large files at once, CHECK
//		System.out.println(">>>>>> STORAGE at " + myLocation
//				+ ": fileWrite() : " + uniqueFileName + " represented as: "
//				+ theGroup.getId());
		
		/*#%*/ logger.log(">>>>>> STORAGE-fileWrite() : " + uniqueFileName + " represented as: "
		/*#%*/ 				+ theGroup.getId());

		Long s = pendingWriteRequestSizes.get(uniqueFileName);
		freeSpace -= s;

		if (s != null) {

			storage.put(uniqueFileName, theFile);
			storedFileGroupsId2Name.put(theGroup.getId().toString(),
					uniqueFileName);
			storedFileGroupsName2Id.put(uniqueFileName, theGroup);

			if (!restoreRequest) {

				fileWriteAck.fileWriteSucceeded(uniqueFileName, initiator);
				if(TEST_MODE == FrontendImpl.DEMO_MODE) {
					System.out.println("File " + uniqueFileName + " successfully stored in component at node " + myGlobalId.getResourceRef().getDKSRef().getId());
				}

			} else {
				
				if(TEST_MODE == FrontendImpl.DEMO_MODE) {
					System.out.println("File " + uniqueFileName + " successfully restored in component at node " + myGlobalId.getResourceRef().getDKSRef().getId());
				}
	
			}
			if (pushLoadChange != null) {
				pushLoadChange.newLoad(totalSpace - freeSpace);
			} else {
				//System.err.println("no loadsensor!");
			}
			
			if(TEST_MODE == FrontendImpl.FAIL_TEST) {
				String latestTime = ""+System.currentTimeMillis();
				healingTimes.put(uniqueFileName, latestTime);
				healingTimes.put(HealingTest.LATEST_LOCAL_STORE_TIME, latestTime);
				//System.out.println("Putting: " + uniqueFileName + " @ " + latestTime);
			}

		} else {
			
			/*#%*/ String msg = ">>>>>> STORAGE-fileWrite() : " + uniqueFileName + " represented as: "
			/*#%*/ 	+ theGroup.getId() + " FAILED";
			
			/*#%*/ System.err.println(msg);			
			/*#%*/ logger.log(msg);

			if (!restoreRequest) {
				fileWriteAck.fileWriteFailed(uniqueFileName, initiator);
			}
		}
		return; // null;
	}

	public YassResult fileRead(String uniqueFileName, ComponentId initiator,
			boolean flag) {
		/*#%*/ logger.log(">>>>>> STORAGE-fileRead(): File = \"" + uniqueFileName + "\" ");
		File file = storage.get(uniqueFileName);
		if (file == null) {
			/*#%*/ logger.log("Failed");
			fileReadAck.fileReadFailed(uniqueFileName, "File Not Found!!",
					initiator);
		} else {
			/*#%*/ logger.log("Successful");
			if(TEST_MODE == FrontendImpl.DEMO_MODE) {
				System.out.println("File " + uniqueFileName + " retrieved from component at node " + myGlobalId.getResourceRef().getDKSRef().getId());
			}
			fileReadAck.fileReadSuccessful(uniqueFileName, file, initiator);
		}
		return null;
	}

	public void fileRemove(String uniqueFileName) {
		/*#%*/ logger.log(">>>>>> STORAGE-fileRemove(): "
		/*#%*/ 		+ uniqueFileName);
		File file = storage.remove(uniqueFileName);

		if (file != null) {
		
			GroupId gid = storedFileGroupsName2Id.remove(uniqueFileName);
			if (gid != null) {
				storedFileGroupsId2Name.remove(gid.getId().toString());

				freeSpace += file.length();
				/*#%*/ logger.log(" Successful. Now " + freeSpace
				/*#%*/ 		+ " available");
				
				if(TEST_MODE == FrontendImpl.DEMO_MODE) {
					System.out.println("File " + uniqueFileName + " removed from component at node " + myGlobalId.getResourceRef().getDKSRef().getId());
				}

				// fileRemoveAck.fileRemoveSuccessful(uniqueFileName);
				nicheOSSupport.removeGroup(gid);
				if (pushLoadChange != null) {
					/*#%*/ logger.log("$$$$$$$$$$$$ StorageComponent" 
					/*#%*/ 		+ " is pushing the new load to the LoadSensor: "
					/*#%*/ 		+ (totalSpace - freeSpace));
					pushLoadChange.newLoad(totalSpace - freeSpace);
				}
			}
			/*#%*/ else {
			/*#%*/ 	logger.log("fileRemove Failed - no group");
			/*#%*/ }
		} /*#%*/ else {
		/*#%*/ 	logger.log("fileRemove Failed - no file");
		/*#%*/ }

		// TODO Remove file group
	}

	// manager one-to-any
	public YassResult replicaRestoreRequest(GroupId groupId) {
		/*#%*/ logger.log(">>>>>> STORAGE says: received request to restore " + groupId.getId());
		pendingRestoreRequests.add(groupId);
		nextOperation();
		return null;
	}

	public void processReplicaRestoreRequest(GroupId groupId) { // throws
		// FileNotFoundException
		// {

		currentFileGroup = groupId;
		String uniqueFileName = storedFileGroupsId2Name.get(((GroupId) groupId)
				.getId().toString());
		if (uniqueFileName != null) {

			File f = storage.get(uniqueFileName);
			/*#%*/ logger.log(">>>>>> STORAGE-replicaRestoreRequest(): I'm responsible to find a new replica host for "
			/*#%*/ 				+ uniqueFileName + " file: " + f);
			int uid = 2 * myRandom.nextInt();
			int replicationDegree = 1; // we only want to re-create one more replica
			try {
				findReplicas.restoreReplicas(uniqueFileName, f.length(), uid,
					replicationDegree, groupId, myGlobalId, TTL);
			} catch(OperationTimedOutException e) {
				/*#%*/ logger.log(">>>>>> STORAGE-replicaRestoreRequest(): restoring "
					/*#%*/ 				+ ((GroupId) groupId).getId().toString()
					/*#%*/ 				+ " - "
					/*#%*/ 				+ uniqueFileName
					/*#%*/ 				+ " timed out, retry later "
					/*#%*/ 	);						
				
				waitingRestoreRequests.add((GroupId) groupId); // this will of
				// course loop
				synchronized (this) {
					busy = false;
					nextOperation();
				}

			}

		} else {
			// throw new FileNotFoundException();
			/*#%*/ logger.log(">>>>>> STORAGE-replicaRestoreRequest(): I cannot find a new replica host for "
			/*#%*/ 				+ groupId.getId()
			/*#%*/ 				+ " since I dont have the file myself!!!");

			synchronized (this) {
				busy = false;
				nextOperation();
			}

		}

	}

	// storage component one-to-any
	public YassResult restoreReplicas(String uniqueFileName, long size, int uid,
			int replicationDegree, Object groupId, Object leaderId, int ttl) {

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
			findReplicasAck.replicaRestoreAccepted(myGlobalId, uniqueFileName, ttl,
					groupId, leaderId);

		} else {

			/*#%*/ logger.log(
			/*#%*/ 		">>>>>> STORAGE-restoreReplicas(): "
			/*#%*/ 		+ uniqueFileName
			/*#%*/ 		+ " Denied by "
			/*#%*/ 		+ ((ComponentId) myGlobalId).getId()
			/*#%*/ 		+ " because: "
			/*#%*/ 		+ ( 
			/*#%*/ 				(activeRestoreRequests.containsKey(uid)) ?
			/*#%*/ 						"I already agreed to restore it "
			/*#%*/ 						:
			/*#%*/ 							(storage.containsKey(uniqueFileName)) ?
			/*#%*/ 									"I already have the file "
			/*#%*/ 									:
			/*#%*/ 									(size <= freeSpace) ?
			/*#%*/ 											"No space left"
			/*#%*/ 											:
			/*#%*/ 											"Unknown reasons! Error!"
			/*#%*/ 			)
			/*#%*/ );
			
			// Right now just restoring _one_ replica!
			if (ttl > 0) {
				findReplicas.restoreReplicas(uniqueFileName, size, uid,
						replicationDegree, groupId, leaderId, ttl);
			} else {
				findReplicasAck.replicaRestoreDenied(myGlobalId,
						uniqueFileName, ttl, groupId, leaderId);
			}

		}
		return null;
	}

	public void replicaRestoreAccepted(Object replicaGlobalId,
			String uniqueFileName, int ttl, Object groupId, Object leaderID) {

		// Now just assume they crash one by one, ok?
		/*#%*/ logger.log(">>>>>> STORAGE at " + myLocation
		/*#%*/ 		+ " says: replicaRestoreAccepted(): : " + uniqueFileName
		/*#%*/ 		+ " by " + ((ComponentId) replicaGlobalId).getId()
		/*#%*/ 		+ " - add to group and send file to it");
		// //////////////////////////////////////////////////////////////////
		// //////////////////////////////////////////////////////////////////
		// //////////////////////////////////////////////////////////////////
		// createFileGroup.createFileGroup(uniqueFileName,
		// fg.members.toArray());
		// //////////////////////////////////////////////////////////////////
		// //////////////////////////////////////////////////////////////////
		// //////////////////////////////////////////////////////////////////

		nicheOSSupport.addToGroup(replicaGlobalId, currentFileGroup);
		File f = storage.get(uniqueFileName);
		fileWrite.replicaFileWrite(uniqueFileName, f, currentFileGroup,
				(ComponentId) replicaGlobalId);
		/*#%*/ logger.log(">>>>>> STORAGE at " + myLocation
		/*#%*/ 		+ " says: replicaRestore - DONE: for group "
		/*#%*/ 		+ ((GroupId) groupId).getId() + "& file " + uniqueFileName);
		/*
		 * Assuming the write was successful (no acks as of now...), process
		 * remaining items, if any
		 * 
		 */
		synchronized (this) {
			busy = false;
			nextOperation();
		}

	}

	public void replicaRestoreDenied(Object replicaGlobalID,
			String uniqueFileName, int ttl, Object groupId, Object leaderID) {
		/*#%*/ logger.log(">>>>>> STORAGE at "
		/*#%*/ 				+ myLocation
		/*#%*/ 				+ ": replicaRestoreDENIED(): Warning! Replication degree could not be restored for: "
		/*#%*/ 				+ uniqueFileName);
		/*
		 * In this case, put the group back in the queue & try again
		 */
		waitingRestoreRequests.add((GroupId) groupId); // this will of
		// course loop
		synchronized (this) {
			busy = false;
			nextOperation();
		}

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

		long startTime;
		
		StoreRequest(String uniqueFileName, ComponentId initiator, long size,
				int replicationDegree, Object userRef) {
			this.uniqueFileName = uniqueFileName;
			this.initiator = initiator;
			this.size = size;
			this.replicationDegree = replicationDegree;
			this.userRef = userRef;
			//this.startTime = startTime;
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
		
		if(TEST_MODE == FrontendImpl.FAIL_TEST) {
			healingTimes = logger.getResourceManager().getTestProperties();
		}

	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;

	}

}
