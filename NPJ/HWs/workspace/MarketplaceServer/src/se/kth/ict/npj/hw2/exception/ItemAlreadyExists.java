package se.kth.ict.npj.hw2.exception;

import java.rmi.RemoteException;

public class ItemAlreadyExists extends RemoteException {

	public ItemAlreadyExists() {
	}

	public ItemAlreadyExists(String s) {
		super(s);
	}
}
