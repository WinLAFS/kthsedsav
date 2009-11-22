package se.kth.ict.npj.hw2.exception;

import java.rmi.RemoteException;

public class UknownItemException extends Exception {

	public UknownItemException() {
		super();
	}

	public UknownItemException(String s) {
		super(s);
	}

}
