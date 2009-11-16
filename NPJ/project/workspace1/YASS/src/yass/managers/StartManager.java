package yass.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.jasmine.jade.service.componentdeployment.DeploymentParams;
import org.objectweb.jasmine.jade.service.componentdeployment.NicheIdRegistry;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.Serialization;

import yass.interfaces.YASSNames;
import dks.niche.events.ComponentFailEvent;
import dks.niche.events.CreateGroupEvent;
import dks.niche.events.MemberAddedEvent;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.wrappers.ManagementDeployParameters;
import dks.niche.wrappers.ScriptInfo;
import dks.niche.wrappers.SimpleResourceManager;

public class StartManager implements BindingController, LifeCycleController {

	// Client Interfaces
	private OverlayAccess dcmService;

	// private NicheAsynchronousInterface logger;

	// private DCMRegistryInterface DCMRegistry;
	private NicheIdRegistry idRegistry;

	// ///////////////
	Component mySelf;

	private boolean status;

	// public static final int NUMBER_OF_FES = 2;

	// Local variables
	// PROGRAM STANDARD SETTINGS, if none can be loaded

	int minimumComponentSize = 600000;

	// double loadHigh = 0.8;

	double loadLow = 0.1;

	private final String YASS_SETTINGS = "yass.settings";

	static final double loadHigh = System.getProperty("yass.test.loadHigh") instanceof String ? Double
			.parseDouble(System.getProperty("yass.test.loadHigh"))
			: .8;
	
	static final int averageStorage = System.getProperty("yass.test.averageStorage") instanceof String ?
			Integer.parseInt(System.getProperty("yass.test.averageStorage"))
		: 
			3*600000;

	static final int twoWayBindings = System
			.getProperty("yass.test.twoWayBindings") instanceof String ?
					0 < Integer.parseInt(System.getProperty("yass.test.twoWayBindings")) ?
							JadeBindInterface.WITH_RETURN_VALUE : 0 
			: 0;

	static final int selfExcludingOneToAnyBindings = System
	.getProperty("yass.test.smartOneToAnyBindings") instanceof String ?
			0 < Integer.parseInt(System.getProperty("yass.test.selfExcludingOneToAnyBindings")) ?
					JadeBindInterface.NO_SEND_TO_SENDER : 0
			: 0;

	
	static final int sizeManagement = System
			.getProperty("yass.test.sizeManagement") instanceof String ? Integer
			.parseInt(System.getProperty("yass.test.sizeManagement"))
			: 1;

	static final int replicaManagement = System
			.getProperty("yass.test.replicaManagement") instanceof String ? Integer
			.parseInt(System.getProperty("yass.test.replicaManagement"))
			: 1;
			
	static final int loadManagement = System
			.getProperty("yass.test.loadManagement") instanceof String ? Integer
			.parseInt(System.getProperty("yass.test.loadManagement"))
			: 1;
			
	static final int metaManagement = System
			.getProperty("yass.test.metaManagement") instanceof String ? Integer
			.parseInt(System.getProperty("yass.test.metaManagement"))
			: 0; //0 by default
		
	static final int loadChangeThreshold =
			System.getProperty("yass.test.loadChangeThreshold") instanceof String ?
					Integer.parseInt(System.getProperty("yass.test.loadChangeThreshold"))
				:
					10000; //10000 by default
			
		
	static final boolean staticADL = System
			.getProperty("yass.test.staticADL") instanceof String ?
					0 < Integer.parseInt(System.getProperty("yass.test.staticADL")) ?
							true : false :
					false;

			
	// int

	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// startScript
	// ///////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	NicheActuatorInterface myActuatorInterface;

	NicheAsynchronousInterface myAsynchronousInterface;

	NicheManagementInterface myNiche;

	SimpleResourceManager myRM;

	// static int INITIAL_MAX_NO_OF_COMPONENTS = 5;

	// ArrayList<Object> storageComponentRefs;
	// ArrayList<ComponentId> storageComponents;

	// Object frontEndRef;
	// ComponentId frontEnd;

	// Object compositeRef;
	// ComponentId composite;

	// static int MODE_LEAVING = 1;
	// static int MODE_FAILURE = 2;

	public StartManager() {

	}

	public StartManager(NicheManagementInterface nicheInstance, ScriptInfo si)
	// we do make things simple now, ok?
	{
	}

	/*
	 * OBS: nor is the resourceId used...
	 * 
	 * object[0] = result of deploy operation: null, failure description et
	 * cetera
	 * 
	 * object[1] = localComponentId
	 * 
	 */

	// private ArrayList<ComponentId> getComponentIds(ArrayList<Object[]>
	// indata) {
	//		
	// ArrayList<ComponentId> result = new ArrayList(indata.size());
	// int i = 0;
	// for (Object[] partialResult : indata) {
	// System.out.println("i: "+i+" obj: "+partialResult);
	// ComponentId tempId = (ComponentId)partialResult[1];
	// //System.out.println("EventHandlerIllustrator-getComponentIds says: "+i+"
	// from "+tempId.getDKSRef().getId()); //Test
	// result.add(tempId);
	// i++;
	// }
	// return result;
	// }
	private void startScript() {

		System.err
				.println("$$$$$$$$$$$$$$$$$$$$$$$$$ Starting YASS StartManager $$$$$$$$$$$$$$$$$$$$$$$$$$$");

		// System.out.println("Start continuation of initial script");

		// .getNicheActuator(null);
		this.myAsynchronousInterface = dcmService.getOverlay()
				.getNicheAsynchronousSupport(); // nicheInstance.getNicheAsynchronousSupport();

		this.myRM = myAsynchronousInterface.getResourceManager();

		this.myActuatorInterface =
			dcmService.getOverlay().getJadeSupport();

		int replicationFactor = myRM.getReplicationFactor();
		
		ArrayList<ComponentId> storageComponents = new ArrayList<ComponentId>();
		ArrayList<ComponentId> frontEnds = new ArrayList<ComponentId>();
		ComponentId frontEnd;
		GroupId feGid = null;

		// ComponentId frontEnds[] = new ComponentId[NUMBER_OF_FES];

		frontEnd = (ComponentId) idRegistry.lookup(YASSNames.APPLICATION_PREFIX
				+ YASSNames.FRONTEND_COMPONENT + 1);

		if (frontEnd == null) {
			frontEnd = (ComponentId) idRegistry
					.lookup(YASSNames.APPLICATION_PREFIX
							+ YASSNames.FRONTEND_COMPONENT);
			if (frontEnd == null) {
				System.err.println("ERROR, no front end");
			}
		}

		ComponentId f = (ComponentId) idRegistry
				.lookup(YASSNames.APPLICATION_PREFIX
						+ YASSNames.FRONTEND_COMPONENT + 2);

		int numberOfFEs = 42;

		if (f == null) {
			numberOfFEs = 1;
			/*#%*/ myAsynchronousInterface.log("StartManager found one FrontEnd");
			// frontEnd =
			// (ComponentId)idRegistry.lookup(YASSNames.APPLICATION_PREFIX+
			// YASSNames.FRONTEND_COMPONENT + 1);
		} else {

			frontEnds.add(frontEnd);
			int fr_index = 3;
			while (f != null) {
				frontEnds.add(f);

				f = (ComponentId) idRegistry
						.lookup(YASSNames.APPLICATION_PREFIX
								+ YASSNames.FRONTEND_COMPONENT + fr_index);

			}

			/*#%*/ myAsynchronousInterface.log("StartManager found "
			/*#%*/ 		+ frontEnds.size() + " FrontEnds");
			feGid = myActuatorInterface.createGroup(YASSNames.TYPE_FRONTEND_GROUP, frontEnds);

		}

		ComponentId s = (ComponentId) idRegistry
				.lookup(YASSNames.APPLICATION_PREFIX
						+ YASSNames.STORAGE_COMPONENT + "1");

		ComponentId s1 = (ComponentId) idRegistry
				.lookup(YASSNames.APPLICATION_PREFIX
						+ YASSNames.STORAGE_COMPONENT + "1");

		// ComponentId s2 = (ComponentId) idRegistry
		// .lookup(YASSNames.APPLICATION_PREFIX
		// + YASSNames.STORAGE_COMPONENT + "2");
		// ComponentId s3 = (ComponentId) idRegistry
		// .lookup(YASSNames.APPLICATION_PREFIX
		// + YASSNames.STORAGE_COMPONENT + "3");

		int sc_index = 1;

		while (s != null) {
			storageComponents.add(s);
			sc_index++;
			s = (ComponentId) idRegistry.lookup(YASSNames.APPLICATION_PREFIX
					+ YASSNames.STORAGE_COMPONENT + sc_index);
		}
		// storageComponents.add(s2);
		// storageComponents.add(s3);
		// crunchTheDeployedComponentInfo(si.getComponents());

		ArrayList<String> settings = myRM.loadSettings(YASS_SETTINGS);

		int maximumAllocatedStorage;
		int minimumAllocatedStorage;
		double upperLoadThreshold;
		double lowerLoadThreshold;
		int retryDelay;

		if (settings != null) {
			maximumAllocatedStorage = Integer.parseInt(settings.get(0));
			minimumAllocatedStorage = Integer.parseInt(settings.get(1));
			upperLoadThreshold = Double.parseDouble(settings.get(2));
			lowerLoadThreshold = Double.parseDouble(settings.get(3));
			retryDelay = Integer.parseInt(settings.get(4));
		} else {
			maximumAllocatedStorage = (int) (1.2 * averageStorage);
			minimumAllocatedStorage = (int) (0.95 * averageStorage);
			upperLoadThreshold = loadHigh;
			lowerLoadThreshold = loadLow;
			retryDelay = 15000;
		}

		int i = 0;

		/*
			Create group template
		*/
		
		GroupId template = myActuatorInterface.getGroupTemplate();
		
		template.addServerBinding(YASSNames.SERVER_INTERFACE_FILE_WRITE_REQUEST, JadeBindInterface.ONE_TO_ANY | twoWayBindings);
		template.addServerBinding(YASSNames.SERVER_INTERFACE_FILE_WRITE, JadeBindInterface.ONE_TO_MANY | twoWayBindings);
		
		template.addServerBinding(YASSNames.SERVER_INTERFACE_FIND_REPLICA, JadeBindInterface.ONE_TO_ANY | twoWayBindings);
		
		//template.addServerBinding(YASSNames.SERVER_INTERFACE_FIND_REPLICA_ACK, JadeBindInterface.ONE_TO_ANY | twoWayBindings);
		
		template.addServerBinding(YASSNames.SERVER_INTERFACE_FILE_READ, JadeBindInterface.ONE_TO_ANY | twoWayBindings);
		template.addServerBinding(YASSNames.SERVER_INTERFACE_FILE_REMOVE, JadeBindInterface.ONE_TO_ANY | twoWayBindings);
		
		template.addServerBinding(YASSNames.SERVER_INTERFACE_RESTORE_REPLICA_REQUEST, JadeBindInterface.ONE_TO_ANY | twoWayBindings);
		
		if(twoWayBindings == 0) {
			template.addServerBinding(YASSNames.SERVER_INTERFACE_FIND_REPLICA_ACK, JadeBindInterface.ONE_TO_ANY | twoWayBindings);
		}
		
		GroupId gid = myActuatorInterface.createGroup(template, storageComponents); // .createGroup(cid);

		
		GroupId senders, receivers;

		ComponentId singleSender, singleReceiver;
		String clientIfName, serverIfName;
		int type;

		if (numberOfFEs == 1) {

			clientIfName = YASSNames.CLIENT_INTERFACE_FILE_WRITE_REQUEST;
			serverIfName = YASSNames.SERVER_INTERFACE_FILE_WRITE_REQUEST;

			singleSender = frontEnd;
			receivers = gid;

			type = JadeBindInterface.ONE_TO_ANY | twoWayBindings;
			// System.out.println("fileWriteRequest: We believe the
			// client/sender to be "+singleSender.getId()+ " and we believe the
			// server/receiver to be "+receivers.getId());

			myActuatorInterface.bind(singleSender, clientIfName, receivers,
					serverIfName, type);

			/** **** */
			clientIfName = YASSNames.CLIENT_INTERFACE_FILE_WRITE_ACK;
			serverIfName = YASSNames.SERVER_INTERFACE_FILE_WRITE_ACK;

			senders = gid;
			singleReceiver = frontEnd;

			// System.out.println("fileWriteAck: We believe the
			// client/sender to
			// be "+senders.getId()+ " and we believe the server/receiver to
			// be
			// "+singleReceiver.getId());

			myActuatorInterface.bind(senders, clientIfName, singleReceiver,
					serverIfName, JadeBindInterface.ONE_TO_ONE);

			/** **** */

			if (0 == twoWayBindings) {

				clientIfName = YASSNames.CLIENT_INTERFACE_FILE_WRITE_REQUEST_ACK;
				serverIfName = YASSNames.SERVER_INTERFACE_FILE_WRITE_REQUEST_ACK;

				senders = gid; // storageComponents.get(0);
				singleReceiver = frontEnd;

				// System.out.println("fileWriteRequestAck: We believe the
				// client/sender to be "+senders.getId()+ " and we believe the
				// server/receiver to be "+singleReceiver.getId());

				myActuatorInterface.bind(senders, clientIfName, singleReceiver,
						serverIfName, JadeBindInterface.ONE_TO_ONE);

				/** **** */

				clientIfName = "fileReadAck";
				serverIfName = "fileReadAck";

				senders = gid; // storageComponents.get(0);
				singleReceiver = frontEnd;

				// System.out
				// .println("fileReadAck: We believe the client/sender to be "
				// + senders.getId()
				// + " and we believe the server/receiver to be "
				// + singleReceiver.getId());

				myActuatorInterface.bind(senders, clientIfName, singleReceiver,
						serverIfName, JadeBindInterface.ONE_TO_ONE);

			}

		} else if (numberOfFEs > 1) {

			clientIfName = YASSNames.CLIENT_INTERFACE_FILE_WRITE_REQUEST;
			serverIfName = YASSNames.SERVER_INTERFACE_FILE_WRITE_REQUEST;

			senders = feGid;
			receivers = gid;

			// System.out.println("fileWriteRequest: We believe the
			// client/sender to be "+singleSender.getId()+ " and we believe the
			// server/receiver to be "+receivers.getId());

			type = JadeBindInterface.ONE_TO_ANY | twoWayBindings;

			myActuatorInterface.bind(feGid, clientIfName, receivers,
					serverIfName, type);

			/** **** */
			clientIfName = YASSNames.CLIENT_INTERFACE_FILE_WRITE_ACK;
			serverIfName = YASSNames.SERVER_INTERFACE_FILE_WRITE_ACK;

			senders = gid;
			receivers = feGid;

			// System.out.println("fileWriteAck: We believe the client/sender to
			// be "+senders.getId()+ " and we believe the server/receiver to be
			// "+singleReceiver.getId());

			myActuatorInterface.bind(senders, clientIfName, receivers,
					serverIfName, JadeBindInterface.ONE_TO_ONE);

			/** **** */

			if (0 == twoWayBindings) {
				
				clientIfName = YASSNames.CLIENT_INTERFACE_FILE_WRITE_REQUEST_ACK;
				serverIfName = YASSNames.SERVER_INTERFACE_FILE_WRITE_REQUEST_ACK;

				senders = gid; // storageComponents.get(0);
				receivers = feGid;

				// System.out.println("fileWriteRequestAck: We believe the
				// client/sender to be "+senders.getId()+ " and we believe the
				// server/receiver to be "+singleReceiver.getId());

				myActuatorInterface.bind(senders, clientIfName, receivers,
						serverIfName, JadeBindInterface.ONE_TO_ONE);

				/** **** */

				clientIfName = YASSNames.CLIENT_INTERFACE_FILE_READ_ACK;
				serverIfName = YASSNames.SERVER_INTERFACE_FILE_READ_ACK;

				senders = gid; // storageComponents.get(0);
				receivers = feGid;

				// System.out.println("fileReadAck: We believe the client/sender
				// to
				// be "+senders.getId()+ " and we believe the server/receiver to
				// be
				// "+singleReceiver.getId());

				myActuatorInterface.bind(senders, clientIfName, receivers,
						serverIfName, JadeBindInterface.ONE_TO_ONE);

			}

		} // end if noFEs < || > 1

		/** **** */

		clientIfName = YASSNames.CLIENT_INTERFACE_FIND_REPLICA;
		serverIfName = YASSNames.SERVER_INTERFACE_FIND_REPLICA;

		senders = gid; // storageComponents.get(0);
		receivers = gid; // storageComponents.get(0);

		type = JadeBindInterface.ONE_TO_ANY | twoWayBindings | selfExcludingOneToAnyBindings;

//		BindId findReplicas_anyToAny = 
			myActuatorInterface.bind(senders,
				clientIfName, receivers, serverIfName, type);

		/** **** */
		if (0 == twoWayBindings) {

			clientIfName = YASSNames.CLIENT_INTERFACE_FIND_REPLICA_ACK;
			serverIfName = YASSNames.SERVER_INTERFACE_FIND_REPLICA_ACK;

			senders = gid;
			receivers = gid;

			// System.out.println("findReplicasAck: We believe the client/sender
			// to
			// be "+senders.getId()+ " and we believe the server/receiver to be
			// "+receivers.getId());

			//BindId findReplicasAck_anyToAny = 
				
				myActuatorInterface.bind(senders,
					clientIfName, receivers, serverIfName,
					JadeBindInterface.ONE_TO_ANY);
			// FIXME - special type needed
		}

		/** **** */

		clientIfName = "restoreReplica";
		serverIfName = "fileWriteS";

		senders = gid;
		receivers = gid;

		type = JadeBindInterface.ONE_TO_ANY | selfExcludingOneToAnyBindings;
		
		myActuatorInterface.bind(
					senders,
					clientIfName,
					receivers,
					serverIfName,
					type
		);

		/** END OF BIND SECTION **** */
		
		ManagementDeployParameters params;
		HashMap<String, String> reDeployNames = new HashMap<String, String>();
		HashMap<String, ManagementDeployParameters> reDeployParameters = new HashMap<String, ManagementDeployParameters>();
		ComponentType currentComponentType;
		
		NicheId newComponentLoadWatcher = null;

		if (loadManagement == 1) {

			currentComponentType = staticADL ? null : myActuatorInterface.getComponentType(YASSNames.LOAD_WATCHER_CLASS_NAME);
			params = new ManagementDeployParameters();
			params.describeWatcher(
					YASSNames.LOAD_WATCHER_CLASS_NAME,
					YASSNames.LOAD_WATCHER_ADL_NAME,
					currentComponentType,
					new Object[] {
							"MyWatcherParams1", "MyWatcherParams2",
							loadChangeThreshold, "NotUsedSensorParameters"
						},
					gid.getId()
				);
			
			params.setReliable(1 < replicationFactor);
			newComponentLoadWatcher = myActuatorInterface.deploy(params, gid);
			
			reDeployNames.put(newComponentLoadWatcher.toString(), YASSNames.LOAD_WATCHER_CLASS_NAME);
			reDeployParameters.put(newComponentLoadWatcher.toString(), params);

		}

		// Below is code only for tests to verify that the same functionality as
		// with groups can be achieved using single SNRs
		// params = new ManagementDeployParameters();
		// params.describeWatcher("yass.watchers.LoadWatcher", "LoadWatcher",
		// new Object[] { "MyWatcherParams1", "MyWatcherParams2", 50000,
		// "NotUsedSensorParameters" }, gid.getId());
		// ManagementElementId newComponentLoadWatcher = myActuatorInterface
		// .deploy(params, storageComponents.get(0));
		//
		// params = new ManagementDeployParameters();
		// params.describeWatcher("yass.watchers.LoadWatcher", "LoadWatcher",
		// new Object[] { "MyWatcherParams1", "MyWatcherParams2", 50000,
		// "NotUsedSensorParameters" }, gid.getId());
		// ManagementElementId newComponentLoadWatcher2 = myActuatorInterface
		// .deploy(params, storageComponents.get(1));
		//
		// params = new ManagementDeployParameters();
		// params.describeWatcher("yass.watchers.LoadWatcher", "LoadWatcher",
		// new Object[] { "MyWatcherParams1", "MyWatcherParams2", 50000,
		// "NotUsedSensorParameters" }, gid.getId());
		// ManagementElementId newComponentLoadWatcher3 = myActuatorInterface
		// .deploy(params, storageComponents.get(2));

		NicheId storageAggregator = null;
		NicheId configurationManager = null;
		
		if (sizeManagement == 1) {
			
			currentComponentType = staticADL ? null : myActuatorInterface.getComponentType(YASSNames.STORAGE_AGGREGATOR_CLASS_NAME);
			
			params = new ManagementDeployParameters();
			params
					.describeAggregator(
							YASSNames.STORAGE_AGGREGATOR_CLASS_NAME,
							YASSNames.STORAGE_AGGREGATOR_ADL_NAME,
							currentComponentType,
							new Object[] {
								gid.getId(),
								maximumAllocatedStorage,
								minimumAllocatedStorage,
								upperLoadThreshold,
								lowerLoadThreshold,
								retryDelay,
								storageComponents
								}
							);

			params.setReliable(1 < replicationFactor);
			storageAggregator =
				myActuatorInterface.deploy(
								params,
								gid
				);

			reDeployNames.put(storageAggregator.toString(), YASSNames.STORAGE_AGGREGATOR_CLASS_NAME);
			reDeployParameters.put(storageAggregator.toString(), params);

			myActuatorInterface.subscribe(
					gid,
					storageAggregator,
					ComponentFailEvent.class.getName()
			);

			myActuatorInterface.subscribe(gid, storageAggregator,
					MemberAddedEvent.class.getName());

			if (newComponentLoadWatcher != null) {
				myActuatorInterface.subscribe(newComponentLoadWatcher,
						storageAggregator,
						"yass.events.ComponentStateChangeEvent");
			}

			DeploymentParams fp = null;
			try {
				fp = (DeploymentParams) Serialization.deserialize(s1
						.getSerializedDeployParameters());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			fp.name = fp.name + "4"; // fp.name.charAt(fp.na)
			
			currentComponentType = staticADL ? null : myActuatorInterface.getComponentType(YASSNames.CONFIGURATION_MANAGER_CLASS_NAME);
			params = new ManagementDeployParameters();
			params.describeManager(
					YASSNames.CONFIGURATION_MANAGER_CLASS_NAME,
					YASSNames.CONFIGURATION_MANAGER_ADL_NAME,
					currentComponentType,
					new Object[] {
							gid,
							// si.getDeploymentParams(),
							fp, minimumComponentSize, minimumAllocatedStorage,
							upperLoadThreshold, lowerLoadThreshold
						});
			params.setReliable(1 < replicationFactor);
			configurationManager =
				myActuatorInterface
					.deploy(params, gid);
			
			
			myActuatorInterface.subscribe(storageAggregator,
					configurationManager,
					"yass.events.StorageAvailabilityChangeEvent");

			reDeployNames.put(configurationManager.toString(), YASSNames.CONFIGURATION_MANAGER_CLASS_NAME);
			reDeployParameters.put(configurationManager.toString(), params);

		}

		NicheId createFileGroupManager = null;
		if (replicaManagement == 1) {

			currentComponentType = staticADL ? null : myActuatorInterface.getComponentType(YASSNames.CREATE_FILE_GROUP_MANAGER_CLASS_NAME);
			params = new ManagementDeployParameters();
			params.describeManager(
					YASSNames.CREATE_FILE_GROUP_MANAGER_CLASS_NAME,
					YASSNames.CREATE_FILE_GROUP_MANAGER_ADL_NAME,
					currentComponentType,
					new Object[] {/* no params */}
				);
			params.setReliable(1 < replicationFactor);
			createFileGroupManager = myActuatorInterface
					.deploy(params, gid); // new
			
			reDeployNames.put(createFileGroupManager.toString(), YASSNames.CREATE_FILE_GROUP_MANAGER_CLASS_NAME);
			reDeployParameters.put(createFileGroupManager.toString(), params);
			
			myActuatorInterface.subscribe(gid, createFileGroupManager,
					CreateGroupEvent.class.getName());

		}

		if(0 < metaManagement) {
			
			boolean workToDo = false;
			//subscribe for errors / moves of those MEs != null
			
			currentComponentType = staticADL ? null : myActuatorInterface.getComponentType(YASSNames.META_MANAGER_CLASS_NAME);
			params = new ManagementDeployParameters();
			params.describeManager(
					YASSNames.META_MANAGER_CLASS_NAME,
					YASSNames.META_MANAGER_ADL_NAME,
					currentComponentType,
					new Object[] {
							reDeployParameters,
							reDeployNames	
					}
				);
			
			params.setReliable(1 < replicationFactor);
			
			NicheId metaManager =
				myActuatorInterface
					.deploy(params, null);
			
			if(newComponentLoadWatcher != null) {
				workToDo = true;
				myActuatorInterface.subscribe(newComponentLoadWatcher, metaManager, ComponentFailEvent.class.getName());
			}
			if(storageAggregator != null) {
				workToDo = true;
				myActuatorInterface.subscribe(storageAggregator, metaManager, ComponentFailEvent.class.getName());
			}
			if(configurationManager != null) {
				workToDo = true;
				myActuatorInterface.subscribe(configurationManager, metaManager, ComponentFailEvent.class.getName());
			}
			if(createFileGroupManager != null) {
				workToDo = true;
				myActuatorInterface.subscribe(createFileGroupManager, metaManager, ComponentFailEvent.class.getName());
				//myActuatorInterface.subscribe(createFileGroupManager, metaManager, ManagementElementDeployedEvent.class.getName());
				
			}			
			if(!workToDo) {
				System.out.println("Warning, system was told to have a meta-manager, but it has nothing to monitor!");
			}
		}
		if (loadManagement + sizeManagement + replicaManagement < 1) {
			System.out.println("Warning, system is running without ME:s");
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	public String[] listFc() {

		// Client interfaces list
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.ID_REGISTRY };

	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {

		if (itfName.equals(FractalInterfaceNames.OVERLAY_ACCESS))
			return dcmService;
		else if (itfName.equals(FractalInterfaceNames.ID_REGISTRY))
			return idRegistry;
		else if (itfName.equals("component"))
			return mySelf;
		else
			throw new NoSuchInterfaceException(itfName);

	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		if (itfName.equals(FractalInterfaceNames.OVERLAY_ACCESS))
			dcmService = (OverlayAccess) itfValue;
		else if (itfName.equals(FractalInterfaceNames.ID_REGISTRY))
			idRegistry = (NicheIdRegistry) itfValue;
		else if (itfName.equals("component"))
			mySelf = (Component) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		if (itfName.equals(FractalInterfaceNames.OVERLAY_ACCESS))
			dcmService = null;
		else if (itfName.equals(FractalInterfaceNames.ID_REGISTRY))
			idRegistry = null;
		else if (itfName.equals("component"))
			mySelf = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {

		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		status = true;

		startScript();
	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;

	}

}
