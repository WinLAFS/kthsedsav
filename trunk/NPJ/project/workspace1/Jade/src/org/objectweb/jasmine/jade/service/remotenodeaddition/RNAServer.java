package org.objectweb.jasmine.jade.service.remotenodeaddition;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import javax.rmi.PortableRemoteObject;

import dks.niche.wrappers.NodeRef;


public class RNAServer implements RemoteNodeAddition {

	//singleton
	private static RNAServer instance;

	public synchronized static RNAServer createInstance(int port, String name) {

		if (instance!=null)
			throw new IllegalStateException("RNAServer already created");
		instance=new RNAServer(port, name);
		return instance;
	}

	public synchronized static RNAServer getInstance() {

		if (instance==null)
			throw new IllegalStateException("RNAServer not created");
		return instance;
	}
	
	
	private int registryPort;
	private String nameRNA;

	private boolean invoked = false;
	private boolean replyAvailable = false;
	private NodeRef nodeRef = null;

	private RNADetails details = null;

	public RNAServer(int port, String name) {
		this.registryPort = port;
		this.nameRNA = name;
	}

	public NodeRef addNode(String VORegistryHost, int VORegistryPort,
			String memberId, HashMap<String, String> configProperties) {

		if (!(requestAdd(VORegistryHost, VORegistryPort, memberId,
				configProperties)))
			return null;

		NodeRef result = null;
		try {
			result = replyAdd();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		return result;
	}

	private synchronized NodeRef replyAdd() throws InterruptedException {

		while (replyAvailable == false)
			wait();
		System.out.println("[RemoteNodeAddition] Replying");
		return this.nodeRef;

	}

	private synchronized boolean requestAdd(String VORegistryHost,
			int VORegistryPort, String memberId,
			HashMap<String, String> configProperties) {

		if (invoked == true) {
			System.err
					.println("[RemoteNodeAddition] Already invoked. Call ignored");
			return false;
		}
		System.out.println("[RemoteNodeAddition] Incoming Call");
		
		details = new RNADetails(VORegistryHost, VORegistryPort, memberId,
				configProperties);
		invoked = true;
		notifyAll();
		return true;
	}

	public synchronized RNADetails getRNADetails() throws InterruptedException {
		while (invoked == false)
			wait();
		return details;
	}

	public synchronized void start() {

		try {
			PortableRemoteObject.exportObject(this);
//			RemoteNodeAddition stub = (RemoteNodeAddition) UnicastRemoteObject
//					.exportObject(this, 0);

			// Bind the RNA server to the registry, creating one if necessary
			try {
				LocateRegistry.createRegistry(registryPort);
				System.out
						.println("[RemoteNodeAddition] Created RMI registry on port "
								+ registryPort);
			} catch (Exception ex) {
				System.out
						.println("[RemoteNodeAddition] Could not create RMI registry on port "
								+ registryPort
								+ ". Probably it already exists.)");
				/* ignore */
			}

			Registry registry = LocateRegistry.getRegistry(registryPort);
			registry.rebind(nameRNA, this);

			System.out.println("[RemoteNodeAddition] Exported service at port "+registryPort+" with name: "+nameRNA);
		} catch (Exception e) {
			System.err.println("[RemoteNodeAddition] Server exception: "
					+ e.toString());
			e.printStackTrace();
		}
	}

	
	// Just for testing
	public static void main(String args[]) {

		try {
			RNAServer server = RNAServer.createInstance(1099, "RNAService");
			server.start();
			NodeRef reply = new NodeRef();
			reply.setJadeNode("This is a test");
			server.setNodeRef(reply);
			System.out.println("RNA Details: " + server.getRNADetails());
			while (true)
				;
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	public synchronized void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
		replyAvailable = true;
		notifyAll();
	}

}
