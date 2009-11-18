package se.kth.ict.npj.hw2.exception;

import java.rmi.RemoteException;

public class ItemAlreadyExistsException extends Exception {

	public ItemAlreadyExistsException() {
	}

	public ItemAlreadyExistsException(String s) {
		super(s);
	}
}
