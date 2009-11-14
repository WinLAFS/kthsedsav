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
 * The MarketplaceServerInterface's implementation that implements all
 * the methods needed for the Marketplace server and keeps the data
 * about the items that are being sold, the clients registered and
 * the wish list of the clients.
 * 
 */
public class MarketplaceServerImp extends UnicastRemoteObject implements MarketplaceServerInterface {
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
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServerInterface#buyItem(se.kth.ict.npj.hw2.Item)
	 */
	public void buyItem(String userId ,Item item) throws IllegalItemException, UknownItemException, RemoteException {
		if (item.getName() == null || item.getOwner() == null || item.getPrice() == 0) {
			throw new IllegalItemException();
		}
		
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
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServerInterface#registerClient(java.lang.String)
	 */
	public void registerClient(String id) throws ClientAlreadyExistsException, RemoteException {
		if (clientList.contains(id)) {
			throw new ClientAlreadyExistsException();
		}
		else {
			clientList.add(id);
		}
	}


	public ArrayList<Item> wishItem(Item item) throws ItemAlreadyExistsException, RemoteException {
		if (item.getName() == null || item.getOwner() == null || item.getPrice() == 0) {
			throw new IllegalItemException();
		}
		
		Iterator<Item> iIterator = wishList.iterator();
		while (iIterator.hasNext()) {
			Item item2 = (Item) iIterator.next();
			if (item.hashCode() == item2.hashCode()) {
				throw new ItemAlreadyExistsException();
			}
		}
		
		ArrayList<Item> satisfyingWishList = satisfyWish(item);
		
		if (satisfyingWishList.size() == 0) {
			wishList.add(item);
			return null;
		}
		
		return satisfyingWishList;
	}

	
	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServerInterface#sellItem(java.lang.String, se.kth.ict.npj.hw2.Item)
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

		Iterator<Item> wlIterator = wishList.iterator();
		while (wlIterator.hasNext()) {
			Item item2 = (Item) wlIterator.next();
			if (item.getName().equalsIgnoreCase(item2.getName()) && item.getPrice() <= item2.getPrice()
					&& (!item.getOwner().equals(item2.getOwner()))) {
				
				//TODO callback
				wishList.remove(item2);
				break;
			}
		}
		
		itemList.add(item);
	}

	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServerInterface#unregisterClient(java.lang.String)
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
		
		Iterator<Item> wlIterator = wishList.iterator();
		while (wlIterator.hasNext()) {
			Item item2 = (Item) wlIterator.next();
			if (item2.getOwner().equals(id)) {
				wishList.remove(item2);
			}
		}
		
		clientList.remove(id);
	}
	
	/**
	 * This method is responsible for finding the items that satisfy a clients wish.
	 * 
	 * @param wish the clients wanted item
	 * @return
	 */
	private ArrayList<Item> satisfyWish(Item wish) {
		ArrayList<Item> satisfyingItemList = new ArrayList<Item>();
		
		Iterator<Item> iIterator = itemList.iterator();
		while (iIterator.hasNext()) {
			Item item = iIterator.next();
			
			if (item.getName().equalsIgnoreCase(wish.getName()) && item.getPrice() <= wish.getPrice()
					&& (!item.getOwner().equals(wish.getOwner()))) {
				satisfyingItemList.add(item);
			}
		}
		
		return satisfyingItemList;
	}

}
