package yass.frontend;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;

import yass.interfaces.Frontend;
import yass.interfaces.YASSNames;
import yass.storage.FileRead;
import yass.storage.FileReadAck;
import yass.storage.FileRemove;
import yass.storage.FileWrite;
import yass.storage.FileWriteAck;
import yass.storage.FileWriteRequest;
import yass.storage.FileWriteRequestAck;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;

public class FrontendImpl implements Frontend, BindingController, LifeCycleController {

	// Client Interfaces
	FileWriteRequest fileWriteRequest;

	FileWrite fileWrite;

	FileRead fileRead;

	FileRemove fileRemove;

	private Component myself;

	private boolean status;

	ComponentId myGlobalId;
	
	NicheComponentSupportInterface nicheOSSupport;
	NicheAsynchronousInterface logger;
	
	UserInterfaceInterface myUI;

	Hashtable<String, Boolean> storedFiles;

	Hashtable<String, File> storeRequestFiles; // Reqested to be _STORED_

	Hashtable<String, Object> acknowledgedFileWriteRequests;

	Hashtable<String, Integer> hopsUsed; //only for statistics
	Hashtable<String, Long> myPendingRequestTimes; //only for statistics
	Hashtable<String, Long> storagePendingRequestTimes; //only for statistics
	Hashtable<String, Long> pendingBindTimes; //only for statistics
	
	String pendingReadFileName;

	String pendingRemoveFileName;

	ArrayList<String> pendingWriteFileNames;

	static final int TEST_MODE = System.getProperty("yass.test.mode") instanceof String ? Integer.parseInt(System.getProperty("yass.test.mode")) : -1;
	
	static final int DEFAULT_REPLICATION_DEGREE = System.getProperty("yass.test.defaultReplicationDegree") instanceof String ? Integer.parseInt(System.getProperty("yass.test.defaultReplicationDegree")) : 2;
	
	public FrontendImpl() {
		
		//if (TEST_MODE == null || TEST_MODE.equals("0")) {
			UserInterfaceThread uit = new UserInterfaceThread(this);
			new Thread(uit).start();
		//} 
		
		storedFiles = new Hashtable<String, Boolean>();
		storeRequestFiles = new Hashtable<String, File>();
		acknowledgedFileWriteRequests = new Hashtable<String, Object>();
		hopsUsed  = new Hashtable<String, Integer>();
		myPendingRequestTimes = new Hashtable<String, Long>();
		storagePendingRequestTimes = new Hashtable<String, Long>();
		pendingBindTimes = new Hashtable<String, Long>();
		
		pendingReadFileName = null;
		pendingRemoveFileName = null;
		pendingWriteFileNames = new ArrayList<String>();
		System.err.println("FE created");
	}

	public void run() {
		// NOT USED

	}

	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////////// Interfaces
	// //////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public synchronized void fileWriteRequestAccepted(String uniqueFileName,
			GroupId globalFileGroupId, long timeUsed, int hops, ComponentId ignore) {
		// Object is GroupId!

		boolean pendingWriteFileName = pendingWriteFileNames
				.remove(uniqueFileName);
		// synchronized (uniqueFileName) {

		if (pendingWriteFileName) {
//			System.out.println(">>>>>> FE: fileWriteRequestAccepted(): file = "
//					+ uniqueFileName + ", GlobalFileGroupID = "
//					+ globalFileGroupID.getId());
			
			/*#%*/ logger.log(">>>>>> FE: fileWriteRequestAccepted(): file = "
			/*#%*/ 		+ uniqueFileName + ", GlobalFileGroupID = "
			/*#%*/ 		+ globalFileGroupId.getId());
			
			// storedFiles.put(uniqueFileName, globalFileGroupID);
			acknowledgedFileWriteRequests
					.put(uniqueFileName, globalFileGroupId);
			
			hopsUsed.put(uniqueFileName, hops);
			myPendingRequestTimes.put(uniqueFileName, System.currentTimeMillis());
			storagePendingRequestTimes.put(uniqueFileName, timeUsed);
			
			try {
				unbindFc(YASSNames.CLIENT_INTERFACE_FILE_WRITE);
			} catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}

			// pendingWriteFileName = uniqueFileName; //this is the only order -
			// if assignment is below dynamic bind, the bind can finish before
			// the assign operation
			nicheOSSupport.bind(myGlobalId,
					YASSNames.CLIENT_INTERFACE_FILE_WRITE, globalFileGroupId,
					YASSNames.SERVER_INTERFACE_FILE_WRITE,
					JadeBindInterface.ONE_TO_MANY);

			// one way is to stop here, the other is to continue...

			File f = storeRequestFiles.remove(uniqueFileName);
			if (f != null) {
				
				/*#%*/ logger.log(">>>>>> FE: fileWriteRequestAccepted(): file = "
				/*#%*/ 		+ uniqueFileName 
				/*#%*/ 		+ " Bind completed, now writing!");
				
				pendingBindTimes.put(uniqueFileName, System.currentTimeMillis());
				fileWrite.fileWrite(uniqueFileName, myGlobalId, f,
						(GroupId) globalFileGroupId);
				// Bound to the specific file group for this file only
			}
			// pendingWriteFileName = null;

		} /*#%*/ else {
		/*#%*/ logger.log(">>>>>> FE: fileWriteRequestAccepted() ERROR, duplicated fileWriteRequestAccepted for "+ uniqueFileName +" IGNORE");
		/*#%*/ }
		// }

		// //////////////////////
		// ////////// DO NOT tell the UI
		// //////////////////////

	}

	public synchronized void fileWriteRequestDenied(String uniqueFileName, int hops, ComponentId ignore) {

		/*#%*/ logger.log(">>>>>> FE: fileWriteRequestDenied(): file "
		/*#%*/ 		+ uniqueFileName + " could not be stored");
		
		storeRequestFiles.remove(uniqueFileName);
		// //////////////////////
		// //////////tell the UI
		// //////////////////////
		myUI.storeAck(uniqueFileName, 0, 0, 0, 0, -1, false);
	}

	public synchronized void fileWriteSucceeded(String uniqueFileName, ComponentId ignore) {

		if (storedFiles.containsKey(uniqueFileName)) {

			/*#%*/ logger.log(">>>>>> FE: new fileWriteSucceeded()-replica for file = "
			/*#%*/ 		+ uniqueFileName);
			
		} else {
//			System.out.println(">>>>>> FE: fileWriteSucceeded() for file = "
//					+ uniqueFileName);
			
			/*#%*/ logger.log(">>>>>> FE: fileWriteSucceeded() for file = "
			/*#%*/ 		+ uniqueFileName);
			
			storedFiles.put(uniqueFileName, true);
			Integer hops = hopsUsed.get(uniqueFileName);
			
			myUI.storeAck(uniqueFileName, myPendingRequestTimes.get(uniqueFileName), storagePendingRequestTimes.get(uniqueFileName), pendingBindTimes.get(uniqueFileName), System.currentTimeMillis(), hops != null ? hops : -1, true);
		}

	}

	public synchronized void fileWriteFailed(String uniqueFileName, ComponentId ignore) {

//		System.out.println(">>>>>> FE: fileWriteFailed(): file = "
//				+ uniqueFileName);
//		
		/*#%*/ logger.log(">>>>>> FE: fileWriteFailed(): file = "
		/*#%*/ 		+ uniqueFileName);
		

		acknowledgedFileWriteRequests.remove(uniqueFileName);
		// //////////////////////
		// //////////tell the UI
		// //////////////////////
		myUI.storeAck(uniqueFileName, 0, 0, 0, 0, -1, false);
	}

	public synchronized void fileReadSuccessful(String uniqueFileName, File file, ComponentId ignore) {
//		System.out.println(">>>>>> FE: fileReadSuccessful(): " + uniqueFileName
//				+ " = " + file);
//		
		/*#%*/ logger.log(">>>>>> FE: fileReadSuccessful(): " + uniqueFileName
		/*#%*/ 		+ " = " + file);
		

		// //////////////////////
		// //////////tell the UI and pass file & name
		// //////////////////////
		myUI.retrieveAck(uniqueFileName, file);

	}

	public synchronized void fileReadFailed(String uniqueFileName, String errorMessage, ComponentId ignore) {
		//System.out.println(">>>>>> FE: fileReadFailed(): " + errorMessage);
		/*#%*/ logger.log(">>>>>> FE: fileReadFailed(): " + errorMessage);
		// //////////////////////
		// //////////tell the UI
		// //////////////////////
		myUI.retrieveAck(uniqueFileName, null);

	}

	// //////////////////////////////////////////////////////////////////////////
	// ///////////////////////////// UI Stuff //////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public synchronized void store(File f) {
		String fileName = f.getName();
		//System.out.println(">>>>>> FE: store request(): " + fileName);
		/*#%*/ logger.log(">>>>>> FE: store request(): " + fileName);
		
		pendingWriteFileNames.add(fileName);
		storeRequestFiles.put(fileName, f);
		
		fileWriteRequest.fileWriteRequest(fileName, myGlobalId, f.length(),
				DEFAULT_REPLICATION_DEGREE, "userRef");
	}

	public synchronized void retrieve(String uniqueFileName) {

		if (pendingReadFileName == null) {

			try {
				unbindFc(YASSNames.CLIENT_INTERFACE_FILE_READ);
			} catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}

			pendingReadFileName = uniqueFileName;
			GroupId fileGroup = (GroupId) acknowledgedFileWriteRequests
					.get(uniqueFileName);
			if (fileGroup != null) {
				nicheOSSupport.bind(myGlobalId,
						YASSNames.CLIENT_INTERFACE_FILE_READ, fileGroup,
						YASSNames.SERVER_INTERFACE_FILE_READ,
						JadeBindInterface.ONE_TO_ANY);
				/*#%*/ logger.log(">>>>>> FE: processPendingReadFile() "
				/*#%*/ 		+ pendingReadFileName);
				fileRead.fileRead(pendingReadFileName, myGlobalId, true);

			} /*#%*/ else {
				/*#%*/ logger.log(">>>>>> FE: retrieve-request for "
				/*#%*/ 		+ uniqueFileName
				/*#%*/ 		+ " denied, file was never properly stored!");
			/*#%*/ }
			pendingReadFileName = null;
		} /*#%*/ else {
		/*#%*/ logger.log(">>>>>> FE: retrieve-request for "
		/*#%*/ 			+ uniqueFileName + " denied, pending operations!");
		/*#%*/ }

	}

	public synchronized void remove(String uniqueFileName) {
		if (pendingRemoveFileName == null) {
			/*#%*/ logger.log(">>>>>> FE: issue request to remove "
			/*#%*/ 		+ uniqueFileName);

			GroupId groupId = (GroupId)acknowledgedFileWriteRequests.get(uniqueFileName);
			
			if(groupId != null) {
				Object[] currentMembers = (Object[])nicheOSSupport.query(groupId, NicheComponentSupportInterface.GET_CURRENT_MEMBERS);
				
				/*#%*/ String logMessage = 
				/*#%*/ 
				/*#%*/		">>>>>> FE: before removal of "
				/*#%*/ 		+ uniqueFileName
				/*#%*/ 		+ " the corresponding group "
				/*#%*/ 		+ groupId.getId().toString()
				/*#%*/ 		+ " had the dks-members "
				/*#%*/ 		+ ((ComponentId)currentMembers[0]).getResourceRef().getDKSRef()
				/*#%*/ 		+ " and "
				/*#%*/ 		+ ((ComponentId)currentMembers[1]).getResourceRef().getDKSRef()
				/*#%*/	;
				/*#%*/ logger.log(logMessage);
				/*#%*/ System.out.println(logMessage);
				
				try {
					unbindFc(YASSNames.CLIENT_INTERFACE_FILE_REMOVE);
				} catch (NoSuchInterfaceException e) {
					e.printStackTrace();
				}
	
				pendingRemoveFileName = uniqueFileName;
				nicheOSSupport.bind(myGlobalId,
						YASSNames.CLIENT_INTERFACE_FILE_REMOVE,
						groupId,
						YASSNames.SERVER_INTERFACE_FILE_REMOVE,
						JadeBindInterface.ONE_TO_MANY);
				/*#%*/ logger.log(">>>>>> FE: processPendingRemoveFile() "
				/*#%*/ 		+ pendingRemoveFileName);
				fileRemove.fileRemove(pendingRemoveFileName);
				storedFiles.remove(pendingRemoveFileName);
				myUI.removeAck(pendingRemoveFileName, true);
				pendingRemoveFileName = null;
			}
			/*#%*/ else {
				/*#%*/ logger.log(">>>>>> FE: remove-request for "
				/*#%*/ 		+ uniqueFileName + " denied, group was not really stored!");
				/*#%*/ }

		} /*#%*/ else {
		/*#%*/ logger.log(">>>>>> FE: remove-request for "
		/*#%*/ 		+ uniqueFileName + " denied, pending operations!");
		/*#%*/ }

	}

	public ArrayList<String> list() {
		return new ArrayList<String>(storedFiles.keySet());
	}


	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////// Fractal Stuff ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	public String[] listFc() {

		return new String[] { "component", "fileWriteRequest", "fileWrite",
				"fileRead", "fileRemove" };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {

		if (itfName.equals(YASSNames.CLIENT_INTERFACE_FILE_WRITE_REQUEST))
			return fileWriteRequest;
		else if (itfName.equals(YASSNames.CLIENT_INTERFACE_FILE_WRITE))
			return fileWrite;
		else if (itfName.equals(YASSNames.CLIENT_INTERFACE_FILE_READ))
			return fileRead;
		else if (itfName.equals(YASSNames.CLIENT_INTERFACE_FILE_REMOVE))
			return fileRemove;
		else if (itfName.equals("component"))
			return myself;
		else
			throw new NoSuchInterfaceException(itfName);

	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		if (itfName.equals(YASSNames.CLIENT_INTERFACE_FILE_WRITE_REQUEST))
			fileWriteRequest = (FileWriteRequest) itfValue;
		else if (itfName.equals(YASSNames.CLIENT_INTERFACE_FILE_WRITE)) {
			fileWrite = (FileWrite) itfValue;

		} else if (itfName.equals(YASSNames.CLIENT_INTERFACE_FILE_READ)) {
			fileRead = (FileRead) itfValue;
		} else if (itfName.equals(YASSNames.CLIENT_INTERFACE_FILE_REMOVE)) {
			fileRemove = (FileRemove) itfValue;
		} else if (itfName.equals("component"))
			myself = (Component) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals(YASSNames.CLIENT_INTERFACE_FILE_WRITE_REQUEST))
			fileWriteRequest = null;
		else if (itfName.equals(YASSNames.CLIENT_INTERFACE_FILE_WRITE))
			fileWrite = null;
		else if (itfName.equals(YASSNames.CLIENT_INTERFACE_FILE_READ))
			fileRead = null;
		else if (itfName.equals(YASSNames.CLIENT_INTERFACE_FILE_REMOVE))
			fileRemove = null;
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
		nicheOSSupport.setOwner((IdentifierInterface) myGlobalId);

		try {
			myUI.setName(Fractal.getNameController(myself).getFcName());
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

//		if (TEST_MODE != null && TEST_MODE.equals("1")) {
//			UserInterfaceThread uit = new UserInterfaceThread(this);
//			new Thread(uit).start();
//		}
		
		System.err.println("FE Started. GlobalId = " + myGlobalId + " at " + myGlobalId.getResourceRef().getDKSRef().getId());

		// nicheOSSupport.registerBindNotifyHandler(myGlobalId, this);

	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;

	}

	class UserInterfaceThread implements Runnable {

		FrontendImpl connection;

		UserInterfaceThread(FrontendImpl connection) {
			this.connection = connection;

			System.err.println("Test Mode is = " + TEST_MODE);

			if (TEST_MODE == CACHE_TEST) {
				myUI = new CacheTest();
			}else if (TEST_MODE == FAIL_TEST) {
				myUI = new FailureTest();
			} else {
				myUI = new UserInterface();
			}
			
		}

		public void run() {

			myUI.run(connection, TEST_MODE);

		}
	}

}
