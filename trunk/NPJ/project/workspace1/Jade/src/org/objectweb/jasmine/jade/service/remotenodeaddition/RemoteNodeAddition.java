package org.objectweb.jasmine.jade.service.remotenodeaddition;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import dks.niche.wrappers.NodeRef;

public interface RemoteNodeAddition extends Remote {
	
	public NodeRef addNode(String VORegistryHost, int VORegistryPort, String memberId, HashMap<String,String> configProperties) throws RemoteException;

}
