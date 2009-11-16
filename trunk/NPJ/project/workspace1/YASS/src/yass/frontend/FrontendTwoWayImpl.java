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
import yass.storage.FileRemove;
import yass.storage.FileWrite;
import yass.storage.FileWriteRequest;
import yass.storage.YassResult;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;

public class FrontendTwoWayImpl implements Frontend, BindingController,
		LifeCycleController {

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

	String pendingReadFileName;

	String pendingRemoveFileName;

	ArrayList<String> pendingWriteFileNames;

	static final int TEST_MODE = System.getProperty("yass.test.mode") instanceof String ? Integer
			.parseInt(System.getProperty("yass.test.mode"))
			: -1;

	public static final int CACHE_TEST = 1;

	public static final int FAIL_TEST = 2;

	public static final int DEMO_MODE = 3;

	static final int DEFAULT_REPLICATION_DEGREE = System
			.getProperty("yass.test.defaultReplicationDegree") instanceof String ? Integer
			.parseInt(System.getProperty("yass.test.defaultReplicationDegree"))
			: 2;

	public FrontendTwoWayImpl() {

		// if (TEST_MODE == null || TEST_MODE.equals("0")) {
		UserInterfaceThread uit = new UserInterfaceThread(this);
		new Thread(uit).start();
		// }

		storedFiles = new Hashtable<String, Boolean>();
		storeRequestFiles = new Hashtable<String, File>();
		acknowledgedFileWriteRequests = new Hashtable<String, Object>();
		hopsUsed = new Hashtable<String, Integer>();
		
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
			GroupId globalFileGroupID, long time, int hopsUsed, ComponentId ignore) {
		// Object is GroupId!

		System.err.println("NOT TO BE USED!");

		// }

		// //////////////////////
		// ////////// DO NOT tell the UI
		// //////////////////////

	}

	public synchronized void fileWriteRequestDenied(String uniqueFileName, int hopsUsed,
			ComponentId ignore) {
		// System.out.println(">>>>>> FE: fileWriteRequestDenied(): file "
		// + uniqueFileName + " could not be stored");
		/*#%*/ logger.log(">>>>>> FE: fileWriteRequestDenied(): file "
		/*#%*/ 		+ uniqueFileName + " could not be stored");

		storeRequestFiles.remove(uniqueFileName);
		// //////////////////////
		// //////////tell the UI
		// //////////////////////
		myUI.storeAck(uniqueFileName, 0, 0, 0, 0, -1, false);
	}

	public synchronized void fileWriteSucceeded(String uniqueFileName,
			ComponentId ignore) {

		if (storedFiles.containsKey(uniqueFileName)) {
			// System.out.println(">>>>>> FE: new fileWriteSucceeded()-replica
			// for file = "
			// + uniqueFileName);
			//			
			/*#%*/ logger.log(">>>>>> FE: new fileWriteSucceeded()-replica for file = "
			/*#%*/ 				+ uniqueFileName);

		} else {
			// System.out.println(">>>>>> FE: fileWriteSucceeded() for file = "
			// + uniqueFileName);

			/*#%*/ logger.log(">>>>>> FE: fileWriteSucceeded() for file = "
			/*#%*/ 		+ uniqueFileName);

			storedFiles.put(uniqueFileName, true);
			Integer hops = hopsUsed.get(uniqueFileName);
			myUI.storeAck(uniqueFileName, 0, 0, 0, 0, hops != null ? hops : -1, true);
		}

	}

	public synchronized void fileWriteFailed(String uniqueFileName,
			ComponentId ignore) {

		// System.out.println(">>>>>> FE: fileWriteFailed(): file = "
		// + uniqueFileName);
		//		
		/*#%*/ logger.log(">>>>>> FE: fileWriteFailed(): file = " + uniqueFileName);

		acknowledgedFileWriteRequests.remove(uniqueFileName);
		// //////////////////////
		// //////////tell the UI
		// //////////////////////
		myUI.storeAck(uniqueFileName, 0, 0, 0, 0, -1, false);
	}

	public synchronized void fileReadSuccessful(String uniqueFileName,
			File file, ComponentId ignore) {

		System.err.println("NOT USED");

	}

	public synchronized void fileReadFailed(String uniqueFileName,
			String errorMessage, ComponentId ignore) {
		System.err.println("NOT USED");

	}

	// //////////////////////////////////////////////////////////////////////////
	// ///////////////////////////// UI Stuff //////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public synchronized void store(File f) {
		String fileName = f.getName();
		// System.out.println(">>>>>> FE: store request(): " + fileName);
		/*#%*/ logger.log(">>>>>> FE: store request(): " + fileName);

		pendingWriteFileNames.add(fileName);
		storeRequestFiles.put(fileName, f);

		YassResult result = fileWriteRequest.fileWriteRequest(fileName,
				myGlobalId, f.length(), DEFAULT_REPLICATION_DEGREE, "userRef");

		if (result.isSucceeded()) {

			/*#%*/ logger.log(">>>>>> FE: fileWriteRequestAccepted(): file = "
			/*#%*/ 		+ fileName + ", GlobalFileGroupID = "
			/*#%*/ 		+ result.getFileGroup().getId());

			acknowledgedFileWriteRequests.put(fileName, result.getFileGroup());
			hopsUsed.put(fileName, result.getNumberOfHops());

			try {
				unbindFc(YASSNames.CLIENT_INTERFACE_FILE_WRITE);
			} catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}

			// pendingWriteFileName = uniqueFileName; //this is the only order -
			// if assignment is below dynamic bind, the bind can finish before
			// the assign operation
			nicheOSSupport.bind(myGlobalId,
					YASSNames.CLIENT_INTERFACE_FILE_WRITE, result
							.getFileGroup(),
					YASSNames.SERVER_INTERFACE_FILE_WRITE,
					JadeBindInterface.ONE_TO_MANY);

			// one way is to stop here, the other is to continue...
			/*#%*/ logger.log(">>>>>> FE: fileWriteRequestAccepted(): file = "
			/*#%*/ 		+ fileName + " Bind completed, now writing!");

			fileWrite.fileWrite(
					fileName,
					myGlobalId,
					f,
					result.getFileGroup()
			);

		} else {
			System.out.println("No available storage");
			/*#%*/ logger.log(">>>>>> FE: Allocation failed");
		}

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
						JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE);
				/*#%*/ logger.log(">>>>>> FE: processPendingReadFile() "
				/*#%*/ 		+ pendingReadFileName);
				YassResult result = fileRead.fileRead(pendingReadFileName,
						myGlobalId, true);
				if (result.isSucceeded()) {
					/*#%*/ logger.log(">>>>>> FE: fileReadSuccessful(): "
					/*#%*/ 		+ uniqueFileName);
					myUI.retrieveAck(uniqueFileName, result.getFile());
				} else {
					/*#%*/ logger.log(">>>>>> FE: fileReadFailed(): "
					/*#%*/ 		+ result.getMessage());
					myUI.retrieveAck(uniqueFileName, null);
				}

			} else {
				/*#%*/ logger.log(">>>>>> FE: retrieve-request for " + uniqueFileName
				/*#%*/ 		+ " denied, file was never properly stored!");
			}
			pendingReadFileName = null;
		}/*#%*/  else {
		/*#%*/ 	logger.log(">>>>>> FE: retrieve-request for " + uniqueFileName
		/*#%*/ 			+ " denied, pending operations!");
		/*#%*/ }

	}

	public synchronized void remove(String uniqueFileName) {
		if (pendingRemoveFileName == null) {
			/*#%*/ logger.log(">>>>>> FE: issue request to remove " + uniqueFileName);

			try {
				unbindFc(YASSNames.CLIENT_INTERFACE_FILE_REMOVE);
			} catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}

			pendingRemoveFileName = uniqueFileName;
			nicheOSSupport.bind(myGlobalId,
					YASSNames.CLIENT_INTERFACE_FILE_REMOVE,
					acknowledgedFileWriteRequests.get(uniqueFileName),
					YASSNames.SERVER_INTERFACE_FILE_REMOVE,
					JadeBindInterface.ONE_TO_MANY);
			/*#%*/ logger.log(">>>>>> FE: processPendingRemoveFile() "
			/*#%*/ 		+ pendingRemoveFileName);
			fileRemove.fileRemove(pendingRemoveFileName);
			storedFiles.remove(pendingRemoveFileName);
			myUI.removeAck(pendingRemoveFileName, true);
			pendingRemoveFileName = null;

		} /*#%*/ else {
		/*#%*/ logger.log(">>>>>> FE: remove-request for " + uniqueFileName
		/*#%*/ 			+ " denied, pending operations!");
		/*#%*/ }

	}

	public ArrayList<String> list() {
		return new ArrayList<String>(storedFiles.keySet());
	}

	// public void bindCompleted() {
	// System.out.println(">>>>>> FE: New inding established");
	// if(pendingReadFileName != null) {
	// System.out.println(">>>>>> FE: processPendingReadFile()");
	// processPendingReadFile();
	// } else if (pendingWriteFileName != null) {
	// System.out.println(">>>>>> FE: processPendingWriteFile()");
	// processPendingWrite();
	// } else if(pendingRemoveFileName != null) {
	// System.out.println(">>>>>> FE: processPendingRemoveFile()");
	// processPendingRemoveFile();
	// } else {
	// System.out.println(">>>>>> FE: bindCompleted() IGNORED");
	//			
	// }
	//		
	// }

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

		// if (TEST_MODE != null && TEST_MODE.equals("1")) {
		// UserInterfaceThread uit = new UserInterfaceThread(this);
		// new Thread(uit).start();
		// }

		System.err.println("FE Started. GlobalId = " + myGlobalId + " at "
				+ myGlobalId.getResourceRef().getDKSRef().getId());

		// nicheOSSupport.registerBindNotifyHandler(myGlobalId, this);

	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;

	}

	class UserInterfaceThread implements Runnable {

		Frontend connection;

		UserInterfaceThread(Frontend connection) {
			this.connection = connection;

			System.err.println("Test Mode is = " + TEST_MODE);

			if (TEST_MODE == CACHE_TEST) {
				myUI = new CacheTest();
			} else if (TEST_MODE == FAIL_TEST) {
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
