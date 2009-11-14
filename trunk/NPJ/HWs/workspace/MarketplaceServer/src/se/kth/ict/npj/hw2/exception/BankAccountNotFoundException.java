package se.kth.ict.npj.hw2.exception;

import java.rmi.RemoteException;

public class BankAccountNotFoundException extends RemoteException {

	public BankAccountNotFoundException() {
		super();
	}

	public BankAccountNotFoundException(String s) {
		super(s);
	}
	
}
