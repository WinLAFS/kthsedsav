package se.kth.ict.npj.hw2.exception;

import java.rmi.RemoteException;

/**
 * An exception that will be thrown if a client tries to
 * unregister from the Marketplace server while it is not
 * registered to the server.
 *
 */
public class UknownClientException extends Exception {

	public UknownClientException() {
		super();
	}

	public UknownClientException(String s) {
		super(s);
	}
}
