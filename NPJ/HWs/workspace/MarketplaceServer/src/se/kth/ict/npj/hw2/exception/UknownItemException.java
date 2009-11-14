package se.kth.ict.npj.hw2.exception;

import java.rmi.RemoteException;

public class UknownItemException extends RemoteException {

	public UknownItemException() {
		super();
	}

	public UknownItemException(String s) {
		super(s);
	}

}
