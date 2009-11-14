package se.kth.ict.npj.hw2.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.security.auth.login.AccountNotFoundException;

import bankrmi.Rejected;

import se.kth.ict.npj.hw2.Item;
import se.kth.ict.npj.hw2.Rejected;
import se.kth.ict.npj.hw2.exception.ClientAlreadyExistsException;
import se.kth.ict.npj.hw2.exception.IllegalItemException;
import se.kth.ict.npj.hw2.exception.ItemAlreadyExistsException;
import se.kth.ict.npj.hw2.exception.UnknownClientException;
import se.kth.ict.npj.hw2.exception.UnknownItemException;

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
	bankrmi.Bank bank = null;
	
	//TODO syncronised methods
	//TODO logging
	protected MarketplaceServerImp(String bankUrl) throws RemoteException {
		super();
		
		try {
			bank = (bankrmi.Bank) Naming.lookup(bankUrl);
		} 
		catch (MalformedURLException e) {
			System.out.println("[LOG] The bank url was not correct: " + e.getMessage());
			System.exit(0);
		} 
		catch (NotBoundException e) {
			System.out.println("[LOG] The bank object was not found: " + e.getMessage());
			System.exit(0);
		}
		catch (RemoteException e) {
			System.out.println("[LOG] The bank object could not be retrieved: " + e.getMessage());
			System.exit(0);
		}

		clientList = new ArrayList<String>();
		itemList = new ArrayList<Item>();
		wishList = new ArrayList<Item>();
		
		System.out.println("[LOG] Server started");
	}

	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServerInterface#buyItem(se.kth.ict.npj.hw2.Item)
	 */
	public synchronized void buyItem(String userId, Item item) throws IllegalItemException, UnknownItemException,
			RemoteException, AccountNotFoundException {
		
		System.out.println("[LOG] Client " + userId + "trying to buy item: " + item.toString());
		if (item.getName() == null || item.getOwner() == null || item.getPrice() == 0) {
			throw new IllegalItemException();
		}
		
		Iterator<Item> iIterator = itemList.iterator();
		while (iIterator.hasNext()) {
			Item item2 = iIterator.next();
			
			if (areEqualItems(item, item2)) {
				try {
					bankrmi.Account sellerAccount = bank.getAccount(item2.getOwner());
					if (sellerAccount == null) {
						throw new AccountNotFoundException("Could not get the seller's account.");
					}
					bankrmi.Account buyerAccount = bank.getAccount(userId);
					if (buyerAccount == null) {
						throw new AccountNotFoundException("Could not get the buyer's account.");
					}
					
					buyerAccount.withdraw(item2.getPrice());
					try { 
						sellerAccount.deposit(item2.getPrice());
						itemList.remove(item2);
					} catch (Rejected re) {
						buyerAccount.deposit(item2.getPrice());
						throw re;
					}
				}
				catch (Rejected e) {
					throw e;
				}
				catch (AccountNotFoundException e) {
					throw e;
				}
				catch (RemoteException e) {
					throw new AccountNotFoundException("Could not update the clients' accounts.");
				}
				
				
				
				
				//TODO callback to seller
				return;
			}
		}
		throw new UnknownItemException();
	}

	public ArrayList<Item> inspectItems() throws RemoteException {
		System.out.println("[LOG] inspectItems()");
		return itemList;
	}
	
	
	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServerInterface#registerClient(java.lang.String)
	 */
	public void registerClient(String id) throws ClientAlreadyExistsException, RemoteException {
		System.out.println("[LOG] Client registering: " + id);
		if (clientList.contains(id)) {
			throw new ClientAlreadyExistsException();
		}
		else {
			clientList.add(id);
		}
	}


	public ArrayList<Item> wishItem(Item item) throws ItemAlreadyExistsException, RemoteException {
		System.out.println("[LOG] Wish item: " + item.toString());
		if (item.getName() == null || item.getOwner() == null || item.getPrice() == 0) {
			throw new IllegalItemException();
		}
		
		Iterator<Item> iIterator = wishList.iterator();
		while (iIterator.hasNext()) {
			Item item2 = (Item) iIterator.next();
			if (areEqualItems(item, item2)) {
				throw new ItemAlreadyExistsException();
			}
		}
		
		ArrayList<Item> satisfyingWishList = satisfyWish(item);
		
		StringTokenizer st = new StringTokenizer(item.getOwner(), "/");
		String prettyName = null;
		while (st.hasMoreTokens()) {
			prettyName = st.nextToken();
		}
		item.setOwnerPretty(prettyName);
		
		if (satisfyingWishList.size() == 0) {
			wishList.add(item);
			return null;
		}
		
		return satisfyingWishList;
	}

	
	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServerInterface#sellItem(java.lang.String, se.kth.ict.npj.hw2.Item)
	 */
	public synchronized void sellItem(Item item) throws RemoteException,
			IllegalItemException, ItemAlreadyExistsException, UnknownClientException {
		
		System.out.println("[LOG] Selling item: " + item.toString());
		if (item.getName() == null || item.getOwner() == null || item.getPrice() == 0) {
			throw new IllegalItemException();
		}
		if (!clientList.contains(item.getOwner())) {
			throw new UnknownClientException();
		}

		Iterator<Item> iIterator = itemList.iterator();
		while (iIterator.hasNext()) {
			Item i = iIterator.next();	
			if (areEqualItems(i, item)) {
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
		
		StringTokenizer st = new StringTokenizer(item.getOwner(), "/");
		String prettyName = null;
		while (st.hasMoreTokens()) {
			prettyName = st.nextToken();
		}
		item.setOwnerPretty(prettyName);
		
		itemList.add(item);
	}

	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServerInterface#unregisterClient(java.lang.String)
	 */
	public synchronized void unregisterClient(String id) throws RemoteException,
			UnknownClientException {
		
		System.out.println("[LOG] Unregister user: " + id);
		if (!clientList.contains(id)) {
			throw new UnknownClientException();
		}
		
//		Iterator<Item> iIterator = itemList.iterator();
//		while (iIterator.hasNext()) {
//			Item item = iIterator.next();
//			if (item.getOwner().equals(id)) {
//				itemList.remove(item);
//			}
//		}
		
		for(int i=0; i<itemList.size(); i++){
			Item item = itemList.get(i);
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
	private synchronized ArrayList<Item> satisfyWish(Item wish) {
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
	
	private boolean areEqualItems(Item i1, Item i2) {
		if (i1.toString().equals(i2.toString()))
			return true;
		else
			return false;
	}

}
