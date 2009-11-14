package se.kth.ict.npj.hw2.exception;

import java.rmi.RemoteException;

public class IllegalItemException extends RemoteException {

	public IllegalItemException() {
		super();
	}

	public IllegalItemException(String s) {
		super(s);
	}

}
