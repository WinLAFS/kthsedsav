package se.kth.ict.npj.hw2.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.security.auth.login.AccountNotFoundException;

import se.kth.ict.npj.hw2.Item;
import se.kth.ict.npj.hw2.exception.ClientAlreadyExistsException;
import se.kth.ict.npj.hw2.exception.IllegalItemException;
import se.kth.ict.npj.hw2.exception.ItemAlreadyExistsException;
import se.kth.ict.npj.hw2.exception.UnknownClientException;
import se.kth.ict.npj.hw2.exception.UnknownItemException;

/**
 * The remote interface of the MarketplaceServerInterface.
 * 
 */
public interface MarketplaceServerInterface extends Remote {
	
	/**
	 * This method registers a client to the Marketplace server and
	 * store his data to the database so the user will be able to log
	 * in to the system for a future use. After a successful registration
	 * the user is considered logged in.
	 * If the client's id already exists it throws a {@link ClientAlreadyExistsException}.
	 * 
	 * @param id the id of the Client that is registering into the server
	 * @param password the password of the new user
	 * @throws RemoteException
	 * @throws ClientAlreadyExistsException
	 */
	public void registerClient(String id, String password) throws RemoteException, ClientAlreadyExistsException;
	
	
	/**
	 * Unregister a client from the Marketplace server. If the clients does not exist
	 * it throws {@link UnknownClientException}. Also, it removes all items and wish list items
	 * that belonged to this client.
	 * 
	 * @param id the id of the client to be removed
	 * @throws RemoteException
	 * @throws UnknownClientException
	 */
	public void unregisterClient(String id) throws RemoteException, UnknownClientException;
	
	/**
	 * This method add a new item to the list with items that are for selling.
	 * If the client is not registered then it throws {@link UnknownClientException},
	 * if an item with the same price, value and user exists then it throws
	 * {@link ItemAlreadyExistsException} and if an item has a null field it throws
	 * {@link IllegalItemException}.
	 * 
	 * @param item the item that is going to be added to the server for selling
	 * @throws RemoteException
	 * @throws IllegalItemException
	 * @throws ItemAlreadyExistsException
	 * @throws UnknownClientException
	 */
	public void sellItem(Item item) throws RemoteException, IllegalItemException, ItemAlreadyExistsException, UnknownClientException;
	
	/**
	 * This method is responsible for handling the buy of an item. If the item 
	 * does not exist in the item list it throws {@link UnknownItemException} and if
	 * the item has some null attributes it throws {@link IllegalItemException}.
	 * It also handles the update of the balances of the seller and the buyer and
	 * the notification that the item was sold to the seller. 
	 * 
	 * @param userId the buyer's id
	 * @param item the item that the client wants to by
	 * @throws RemoteException
	 * @throws UnknownItemException
	 */
	public void buyItem(String userId, Item item) throws RemoteException, UnknownItemException, IllegalItemException, AccountNotFoundException;
	
	/**
	 * This methods returns all the available items if the Marketplace server.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public ArrayList<Item> inspectItems() throws RemoteException;
	
	/**
	 * This method adds an item to the wish list or returns immediately some results
	 * that satisfy the users criteria. If the item is malformed then an {@link IllegalItemException}
	 * is thrown and if the item exists in the wish list then a {@link ItemAlreadyExistsException}.
	 *  
	 * 
	 * @param item the item that the user wants to add into the wish list
	 * @return
	 * @throws RemoteException
	 * @throws ItemAlreadyExistsException
	 * @throws IllegalItemException
	 */
	public ArrayList<Item> wishItem(Item item) throws RemoteException, ItemAlreadyExistsException, IllegalItemException;
}
