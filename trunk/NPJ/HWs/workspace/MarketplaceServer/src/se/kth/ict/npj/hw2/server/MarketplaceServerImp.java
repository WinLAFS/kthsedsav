package se.kth.ict.npj.hw2.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;

import se.kth.ict.npj.hw2.Item;
import se.kth.ict.npj.hw2.exception.ClientAlreadyExistsException;
import se.kth.ict.npj.hw2.exception.IllegalItemException;
import se.kth.ict.npj.hw2.exception.ItemAlreadyExistsException;
import se.kth.ict.npj.hw2.exception.UknownClientException;
import se.kth.ict.npj.hw2.exception.UknownItemException;

/**
 * @author saibbot
 *
 */
public class MarketplaceServerImp extends UnicastRemoteObject implements MarketplaceServer {
	ArrayList<String> clientList = null;
	ArrayList<Item> itemList = null;
	ArrayList<Item> wishList = null;
	
	//TODO syncronised methods
	protected MarketplaceServerImp() throws RemoteException {
		super();
		clientList = new ArrayList<String>();
		itemList = new ArrayList<Item>();
		wishList = new ArrayList<Item>();
	}

	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServer#buyItem(se.kth.ict.npj.hw2.Item)
	 */
	public void buyItem(Item item) throws UknownItemException, RemoteException {
		Iterator<Item> iIterator = itemList.iterator();
		while (iIterator.hasNext()) {
			Item item2 = iIterator.next();
			
			if (item.hashCode() == item2.hashCode()) {
				itemList.remove(item2);
				//TODO money
				//TODO callback to seller
				return;
			}
		}
		throw new UknownItemException();
	}

	public ArrayList<Item> inspectItems() throws RemoteException {
		return itemList;
	}
	
	
	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServer#registerClient(java.lang.String)
	 */
	public void registerClient(String id) throws ClientAlreadyExistsException, RemoteException {
		if (clientList.contains(id)) {
			throw new ClientAlreadyExistsException();
		}
		else {
			clientList.add(id);
		}
	}


	public void wishItem(Item item) throws RemoteException {
		// TODO Auto-generated method stub

	}

	
	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServer#sellItem(java.lang.String, se.kth.ict.npj.hw2.Item)
	 */
	public void sellItem(Item item) throws RemoteException,
			IllegalItemException, ItemAlreadyExistsException, UknownClientException {
		
		if (item.getName() == null || item.getOwner() == null || item.getPrice() == 0) {
			throw new IllegalItemException();
		}
		if (!clientList.contains(item.getOwner())) {
			throw new UknownClientException();
		}
		Iterator<Item> iIterator = itemList.iterator();
		while (iIterator.hasNext()) {
			Item i = iIterator.next();	
			if (i.hashCode() == item.hashCode()) {
				throw new ItemAlreadyExistsException();
			}
		}
		// TODO make the wish list crap
		itemList.add(item);
	}

	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServer#unregisterClient(java.lang.String)
	 */
	public void unregisterClient(String id) throws RemoteException,
			UknownClientException {
		
		if (!clientList.contains(id)) {
			throw new UknownClientException();
		}
		
		Iterator<Item> iIterator = itemList.iterator();
		while (iIterator.hasNext()) {
			Item item = iIterator.next();
			if (item.getOwner().equals(id)) {
				itemList.remove(item);
			}
		}
		
		//TODO wish list removal 
		
		clientList.remove(id);
	}

}
