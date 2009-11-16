/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package yass.managers;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.type.ComponentType;

import yass.events.ReplicaChangeEvent;
import yass.interfaces.YASSNames;
import dks.niche.events.CreateGroupEvent;
import dks.niche.events.ComponentFailEvent;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.ids.GroupId;
import dks.niche.ids.ManagementElementId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.wrappers.ManagementDeployParameters;
import dks.niche.wrappers.Subscription;

/**
 * The <code>CreateFileGroupManager</code> class
 * 
 * @author Joel H
 * @version $Id: CreateFileGroupManager.java 294 2006-05-05 17:14:14Z joel $
 */
public class CreateFileGroupManager implements EventHandlerInterface,
		MovableInterface, InitInterface, BindingController, LifeCycleController {

	private NicheActuatorInterface myManagementInterface;

	private NicheAsynchronousInterface myPrivateManagementInterface;

	private int replicationFactor;
	// ///////////////////
	Component mySelf;
	ComponentType aggregatorComponentType = null;
	ComponentType managerComponentType = null;
	
	static final boolean staticADL = System
	.getProperty("yass.test.staticADL") instanceof String ?
			0 < Integer.parseInt(System.getProperty("yass.test.staticADL")) ?
					true : false :
			false;

	
	private boolean status;

	// REMEMBER THE EMPTY CONSTRUCTOR
	public CreateFileGroupManager() {
		// System.err.println("CreateFileGroupManager created");
		status = false;

	}

	public synchronized void eventHandler(Object e, int flag) {

		CreateGroupEvent cge = (CreateGroupEvent) e;

		GroupId newFileGroup = cge.getGroupId();

		// System.out.println("CreateFileGroupManager says: handling new group
		// event "+ newFileGroup.getId());

		// when one wants the watchers collocated, add another rep. of
		// newFileGroup at the end!!

		// WatcherId resourceLeaveWatcher = new ResourceWatcherId(newFileGroup ,
		// myPrivateManagementInterface, new
		// Object[]{WatcherId.RESOURCE_LEAVING}, newFileGroup);
		// WatcherId resourceFailWatcher = new ResourceWatcherId(newFileGroup ,
		// myPrivateManagementInterface, new
		// Object[]{WatcherId.RESOURCE_FAILING}, newFileGroup);

		// currentSubscriptions.add(new
		// WatcherSubscription(resourceLeaveWatcher,
		// "dks.niche.events.ResourceLeaveEvent", true));
		// currentSubscriptions.add(new Subscription(resourceFailWatcher,
		// "dks.niche.events.ComponentFailEvent", false));

		ManagementDeployParameters params = new ManagementDeployParameters();
		params.describeAggregator(
				YASSNames.FILE_REPLICA_AGGREGATOR_CLASS_NAME,
				YASSNames.FILE_REPLICA_AGGREGATOR_ADL_NAME,
				aggregatorComponentType,
				new Object[] {	newFileGroup }
		);
		params.setReliable(1 < replicationFactor);
		NicheId fileReplicaAggregator = myManagementInterface
				.deploy(params, newFileGroup);

		myManagementInterface.subscribe(
				newFileGroup,
				fileReplicaAggregator,
				ComponentFailEvent.class.getName()
		);

		//TODO check if this works well with replication...
		Subscription newSubscription = myManagementInterface.subscribe(
											fileReplicaAggregator,
											YASSNames.FILE_REPLICA_MANAGER_ADL_NAME,
											ReplicaChangeEvent.class.getName(),
											newFileGroup
										);
		
		if (newSubscription == null) {
			// Then we need to deploy a new FileReplicaManager at that place
			
			params = new ManagementDeployParameters();
			
			params.describeManager(
					YASSNames.FILE_REPLICA_MANAGER_CLASS_NAME,
					YASSNames.FILE_REPLICA_MANAGER_ADL_NAME,
					managerComponentType,
					new Object[] {/* no parameters */}
				);
			params.setReliable(1 < replicationFactor);
			NicheId fileReplicaManager = myManagementInterface
					.deploy(params, newFileGroup);
			// System.out.println("CFGM says: time to subscribe to the FRM at
			// "+newFileGroup.getId().getLocation()+" which now should exist!");
			myManagementInterface.subscribe(fileReplicaAggregator,
					fileReplicaManager, ReplicaChangeEvent.class.getName());
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.ManagementElementInterface#init(java.lang.Object[])
	 */
	public void init(Object[] applicationParameters) {
		System.out.println("CreateFileGroupManager initialized");
	}
	
	public void reinit(Object[] applicationParameters) {
		System.out.println("CreateFileGroupManager re-initialized");
	}

	public void init(NicheActuatorInterface actuator) {
		myManagementInterface = actuator;
		myPrivateManagementInterface = myManagementInterface.testingOnly();
		
		replicationFactor = myPrivateManagementInterface.getResourceManager().getReplicationFactor();
		
		if(!staticADL) {
			aggregatorComponentType = actuator.getComponentType(YASSNames.FILE_REPLICA_AGGREGATOR_CLASS_NAME);
			managerComponentType = actuator.getComponentType(YASSNames.FILE_REPLICA_MANAGER_CLASS_NAME);
		}
		// System.out.println("Manager says: myRingId is:
		// "+myManagementInterface.getId());
	}

	public void initId(Object id) {
		// Not used
	}

	public Object[] getAttributes() {
		return null; //stateless, no attributes to care about!
	}
	// FRACTAL STUFF

	public String[] listFc() {
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
				FractalInterfaceNames.EVENT_HANDLER_CLIENT_INTERFACE };
	}

	public Object lookupFc(String interfaceName)
			throws NoSuchInterfaceException {
		if (interfaceName
				.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return myManagementInterface;
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			return mySelf;
		else
			throw new NoSuchInterfaceException(interfaceName);

	}

	public void bindFc(String interfaceName, Object stub)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if (interfaceName
				.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			System.err
					.println("Managers says ERROR, use initinterface instead");
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
			mySelf = (Component) stub;
			// System.err.println("setting the component interface");
		} else
			throw new NoSuchInterfaceException(interfaceName);

	}

	public void unbindFc(String interfaceName) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if (interfaceName
				.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			myManagementInterface = null;
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
			mySelf = null;
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
