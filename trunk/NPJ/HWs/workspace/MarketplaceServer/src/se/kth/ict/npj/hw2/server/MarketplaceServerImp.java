package se.kth.ict.npj.hw2.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;

import se.kth.ict.npj.hw2.Item;
import se.kth.ict.npj.hw2.exception.ClientAlreadyExists;
import se.kth.ict.npj.hw2.exception.IllegalItem;
import se.kth.ict.npj.hw2.exception.ItemAlreadyExists;
import se.kth.ict.npj.hw2.exception.UknownClient;

/**
 * @author saibbot
 *
 */
public class MarketplaceServerImp extends UnicastRemoteObject implements MarketplaceServer {
	ArrayList<String> clientList = null;
	ArrayList<Item> itemList = null;
	
	
	protected MarketplaceServerImp() throws RemoteException {
		super();
		clientList = new ArrayList<String>();
		itemList = new ArrayList<Item>();
	}

	public boolean buyItem(Item item) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public ArrayList<Item> inspectItems() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServer#registerClient(java.lang.String)
	 */
	public void registerClient(String id) throws ClientAlreadyExists, RemoteException {
		if (clientList.contains(id)) {
			throw new ClientAlreadyExists();
		}
		else {
			clientList.add(id);
		}
	}

	public boolean unregisterClient(String id) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public void wishItem(Item item) throws RemoteException {
		// TODO Auto-generated method stub

	}

	
	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServer#sellItem(java.lang.String, se.kth.ict.npj.hw2.Item)
	 */
	public void sellItem(Item item) throws RemoteException,
			IllegalItem, ItemAlreadyExists, UknownClient {
		
		if (item.getName() == null || item.getOwner() == null || item.getPrice() == 0) {
			throw new IllegalItem();
		}
		if (!clientList.contains(item.getOwner())) {
			throw new UknownClient();
		}
		Iterator<Item> iIterator = itemList.iterator();
		while (iIterator.hasNext()) {
			Item i = iIterator.next();	
			if (i.hashCode() == item.hashCode()) {
				throw new ItemAlreadyExists();
			}
		}
		
		itemList.add(item);
	}

}
