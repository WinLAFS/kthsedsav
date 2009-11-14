package se.kth.ict.npj.hw2.client.objects;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import se.kth.ict.npj.hw2.Item;
import se.kth.ict.npj.hw2.client.MPClientGUI;

public class MPClientImpl extends UnicastRemoteObject implements MPClientInterface {

	private String clientId;
	private MPClientGUI gui;
	
	/**
	 * Constuctor accepts id of the client and link to client gui.
	 * 
	 * @param clientId Id of the client
	 * @param gui Link to client gui
	 * @throws RemoteException
	 */
	protected MPClientImpl(String clientId, MPClientGUI gui) throws RemoteException {
		super();
		this.clientId = clientId;
		this.gui = gui;	
	}

	private static final long serialVersionUID = 5555500677813683130L;

	@Override
	public String getId() throws RemoteException {
		return this.clientId;
	}

	@Override
	public void receiveItemSoldNotification(Item item) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveWishedItemNotification(Item item) throws RemoteException {
		// TODO Auto-generated method stub

	}

}
