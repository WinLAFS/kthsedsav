package org.objectweb.jasmine.jade.service.remotenodeaddition;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import dks.niche.wrappers.NodeRef;

public class RNAClient {

	private RNAClient() {
	}

	public static NodeRef addToVO(String nodeURL, String VORegistryHost,
			int VORegistryPort) {
		try {
			RemoteNodeAddition stub = (RemoteNodeAddition) Naming
					.lookup(nodeURL);
			String memberId = null;
			HashMap<String, String> configProperties = null;
			NodeRef response = stub.addNode(VORegistryHost, VORegistryPort,
					memberId, configProperties);
			return response;
		} catch (Exception e) {
			System.err.println("RNAClient exception: " + e.toString());
			e.printStackTrace();
		}
		return null;}

	public static void main(String[] args) {

		if (args.length != 5) {
			System.err
					.println("Usage: RNAClient <RNAhost> <RNAport> <RNAname> <VOHost> <VOport>");
			System.exit(1);
		}
		String registryHost = args[0];
		String registryPort = args[1];
		String nameRNA = args[2];

		try {
			String URL = "rmi://" + registryHost + ":" + registryPort + "/"
					+ nameRNA;
			System.out.println("Listing registered names");
			for (String s : Naming.list("//" + registryHost + ":"
					+ registryPort + "/")) {
				System.out.println(s);
			}
			System.out.println("Looking up: " + URL);

			RemoteNodeAddition stub = (RemoteNodeAddition) Naming.lookup(URL);
			String memberId = null;
			String VORegistryHost = args[3];
			int VORegistryPort = Integer.parseInt(args[4]);
			HashMap<String, String> configProperties = null;
			NodeRef response = stub.addNode(VORegistryHost, VORegistryPort,
					memberId, configProperties);
			if (response != null)
				System.out.println("Response: " + response.getJadeNode());
			else
				System.out.println("Already invoked: ");

		} catch (Exception e) {
			System.err.println("RNAClient exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
