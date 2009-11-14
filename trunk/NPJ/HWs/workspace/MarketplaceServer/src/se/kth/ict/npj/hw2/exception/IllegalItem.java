package se.kth.ict.npj.hw2.exception;

import java.rmi.RemoteException;

public class IllegalItem extends RemoteException {

	public IllegalItem() {
		super();
	}

	public IllegalItem(String s) {
		super(s);
	}

}
