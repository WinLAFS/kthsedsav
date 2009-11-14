package se.kth.ict.npj.hw2.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import se.kth.ict.npj.hw2.Item;
import se.kth.ict.npj.hw2.exception.ClientAlreadyExistsException;
import se.kth.ict.npj.hw2.exception.IllegalItemException;
import se.kth.ict.npj.hw2.exception.ItemAlreadyExistsException;
import se.kth.ict.npj.hw2.exception.UknownClientException;
import se.kth.ict.npj.hw2.exception.UknownItemException;

/**
 * The remote interface of the MarketplaceServerInterface.
 * 
 */
public interface MarketplaceServerInterface extends Remote {
	
	/**
	 * This method registers a client to the Marketplace server by his id.
	 * If the client's id already exists it throws a {@link ClientAlreadyExistsException}.
	 * 
	 * @param id the id of the Client that is registering into the server
	 * @throws RemoteException
	 * @throws ClientAlreadyExistsException
	 */
	public void registerClient(String id) throws RemoteException, ClientAlreadyExistsException;
	
	
	/**
	 * Unregister a client from the Marketplace server. If the clients does not exist
	 * it throws {@link UknownClientException}. Also, it removes all items and wish list items
	 * that belonged to this client.
	 * 
	 * @param id the id of the client to be removed
	 * @throws RemoteException
	 * @throws UknownClientException
	 */
	public void unregisterClient(String id) throws RemoteException, UknownClientException;
	
	/**
	 * This method add a new item to the list with items that are for selling.
	 * If the client is not registered then it throws {@link UknownClientException},
	 * if an item with the same price, value and user exists then it throws
	 * {@link ItemAlreadyExistsException} and if an item has a null field it throws
	 * {@link IllegalItemException}.
	 * 
	 * @param item the item that is going to be added to the server for selling
	 * @throws RemoteException
	 * @throws IllegalItemException
	 * @throws ItemAlreadyExistsException
	 * @throws UknownClientException
	 */
	public void sellItem(Item item) throws RemoteException, IllegalItemException, ItemAlreadyExistsException, UknownClientException;
	
	/**
	 * 
	 * 
	 * @param item
	 * @throws RemoteException
	 * @throws UknownItemException
	 */
	public void buyItem(Item item) throws RemoteException, UknownItemException;
	
	/**
	 * This methods returns all the available items if the Marketplace server.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public ArrayList<Item> inspectItems() throws RemoteException;
	public void wishItem(Item item) throws RemoteException;
}
