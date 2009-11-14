package se.kth.ict.npj.hw2.exception;

import java.rmi.RemoteException;

/**
 * An exception that will be thrown if an existing client
 * is trying to reregister to the Marketplace server.
 *
 */
public class ClientAlreadyExists extends RemoteException {
	
	public ClientAlreadyExists() {
		super();
	}
	
	public ClientAlreadyExists(String s) {
		super(s);
	}
}
