package se.kth.ict.npj.hw2.exception;

import java.rmi.RemoteException;

public class UnknownItemException extends Exception {

	public UnknownItemException() {
		super();
	}

	public UnknownItemException(String s) {
		super(s);
	}

}
