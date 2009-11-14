package se.kth.ict.npj.hw2.exception;

import java.rmi.RemoteException;

public class ItemAlreadyExistsException extends RemoteException {

	public ItemAlreadyExistsException() {
	}

	public ItemAlreadyExistsException(String s) {
		super(s);
	}
}
