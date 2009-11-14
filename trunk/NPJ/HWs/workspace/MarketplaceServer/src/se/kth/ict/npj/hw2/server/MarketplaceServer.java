package se.kth.ict.npj.hw2.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import se.kth.ict.npj.hw2.Item;
import se.kth.ict.npj.hw2.exception.ClientAlreadyExists;
import se.kth.ict.npj.hw2.exception.IllegalItem;
import se.kth.ict.npj.hw2.exception.ItemAlreadyExists;
import se.kth.ict.npj.hw2.exception.UknownClient;

/**
 * The remote interface of the MarketplaceServer
 * 
 */
public interface MarketplaceServer extends Remote {
	/**
	 * This method registers a client to the Marketplace server by his id.
	 * If the client's id already exists it throws a {@link ClientAlreadyExists}.
	 * 
	 * @param id the id of the Client that is registering into the server
	 * @throws RemoteException
	 * @throws ClientAlreadyExists
	 */
	public void registerClient(String id) throws RemoteException, ClientAlreadyExists;
	public boolean unregisterClient(String id) throws RemoteException, UknownClient;
	/**
	 * This method add a new item to the list with items that are for selling.
	 * If the client is not registered then it throws {@link UknownClient},
	 * if an item with the same price, value and user exists then it throws
	 * {@link ItemAlreadyExists} and if an item has a null field it throws
	 * {@link IllegalItem}.
	 * 
	 * @param item
	 * @throws RemoteException
	 * @throws IllegalItem
	 * @throws ItemAlreadyExists
	 * @throws UknownClient
	 */
	public void sellItem(Item item) throws RemoteException, IllegalItem, ItemAlreadyExists, UknownClient;
	public boolean buyItem(Item item) throws RemoteException;
	public ArrayList<Item> inspectItems() throws RemoteException;
	public void wishItem(Item item) throws RemoteException;
}
