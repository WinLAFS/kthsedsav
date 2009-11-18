package se.kth.ict.npj.hw2.exception;

import java.rmi.RemoteException;

/**
 * An exception that will be thrown if a client tries to
 * unregister from the Marketplace server while it is not
 * registered to the server.
 *
 */
public class UnknownClientException extends Exception {

	public UnknownClientException() {
		super();
	}

	public UnknownClientException(String s) {
		super(s);
	}
}
