package se.kth.ict.npj.hw2.client;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.security.auth.login.AccountNotFoundException;

import bankrmi.Account;

import se.kth.ict.npj.hw2.Item;
import se.kth.ict.npj.hw2.exception.ClientAlreadyExistsException;
import se.kth.ict.npj.hw2.exception.IllegalItemException;
import se.kth.ict.npj.hw2.exception.ItemAlreadyExistsException;
import se.kth.ict.npj.hw2.exception.UknownClientException;
import se.kth.ict.npj.hw2.exception.UknownItemException;
import se.kth.ict.npj.hw2.server.MarketplaceServerInterface;

public class MPClientLogic {
	private MPClientGUI gui;
	private MarketplaceServerInterface serverInt;
	private String userName;
	private bankrmi.Bank bank;
	Account account;
	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Constructor accepts gui object of the client
	 * 
	 * @param gui GUI object of the client
	 */
	public MPClientLogic(MPClientGUI gui){
		this.gui=gui;
	}
	
	/**
	 * The method performs connection of the client to the servers.
	 * It connects to Marketplace and bank servers and updates client's
	 * gui.
	 * 
	 * @param user Username string
	 * @param server Server host string
	 * @param port Servers port number
	 */
	public void connectToServer(String user, String server, String port){
		try {
			String portStr="";
			if(port!=null && !port.equals("")){
				portStr=":"+port;
			}
			serverInt = (MarketplaceServerInterface)Naming.lookup("rmi://"+server+portStr+"/server");
			try {
				serverInt.registerClient("rmi://"+InetAddress.getLocalHost().getCanonicalHostName()+"/"+user);
				this.userName = "rmi://"+InetAddress.getLocalHost().getCanonicalHostName()+"/"+user;
				
				String bankUrl = "rmi://"+server+portStr+"/NordBanken";
				bank = (bankrmi.Bank) Naming.lookup(bankUrl);
				account = bank.newAccount(userName);
				account.deposit(100);
			} catch (UnknownHostException e) {
				gui.connectionError("Can't conect to server");
				System.err.println("[LOG] UnknownHostException when connecting to server");
				return;
			}
			
			gui.connectionSuccessful();
			updateItems();
		} catch (ClientAlreadyExistsException e){
			gui.connectionError("Choose another name");
			System.err.println("[LOG] ClientAlreadyExistsException when connecting to server");
		} catch (MalformedURLException e) {
			gui.connectionError("Bad server address or port");
			System.err.println("[LOG] MalformedURLException when connecting to server");
		} catch (RemoteException e) {
			gui.connectionError("Can't conect to server");
			System.err.println("[LOG] RemoteException when connecting to server");
		} catch (NotBoundException e) {
			gui.connectionError("Can't connect to server");
			System.err.println("[LOG] NotBoundException when connecting to server");
		}
	}
	
	/**
	 * The method connects to the server, retrieves items list and
	 * updates client's gui.
	 */
	public void updateItems(){
		gui.setNotificationMessage("Communicating with server, please wait");
		try {
			if(serverInt!=null){
				ArrayList<Item> items = serverInt.inspectItems();
				gui.updateItemsList(items);
				gui.setNotificationMessage("Items list updated");
			} else {
				gui.setNotificationMessage("Can't connect to the server");
			}
		} catch (RemoteException e) {
			gui.setNotificationMessage("Error connecting to server");
			System.err.println("[LOG] RemoteException when updating items list");
		}
	}
	
	/**
	 * The method sends request to the server to sell a new item.
	 * 
	 * @param itemName Name of the item.
	 * @param itemPrice Price of the item.
	 */
	public void sellItem(String itemName, String itemPrice){
		gui.setNotificationMessage("Communicating with server, please wait");
		int price = 0;
		try{
			price = Integer.parseInt(itemPrice);
		} catch(NumberFormatException ex){
			gui.setNotificationMessage("Bad price");
			System.err.println("[LOG] NumberFormatException when selling item");
			return;
		}
		
		Item item = new Item();
		item.setName(itemName);
		item.setOwner(userName);
		item.setPrice(price);
		
		try {
			serverInt.sellItem(item);
		} catch (IllegalItemException e) {
			System.err.println("[LOG] IllegalItemException when selling item");
			gui.setNotificationMessage("Can't sell item");
			return;
		} catch (ItemAlreadyExistsException e) {
			System.err.println("[LOG] ItemAlreadyExistsException when selling item");
			gui.setNotificationMessage("Item already exists");
			return;
		} catch (UknownClientException e) {
			System.err.println("[LOG] UknownClientException when selling item");
			gui.setNotificationMessage("You are not logged in");
			return;
		} catch (RemoteException e) {
			System.err.println("[LOG] RemoteException when selling item");
			gui.setNotificationMessage("Can't sell item");
			return;
		}
		
		gui.setNotificationMessage("Item is selling");
		gui.clearSellItemForm();
	}
	
	/**
	 * Method sends request to the server to be subscribed 
	 * if requested item appears.
	 * 
	 * @param itemName Name of wished items
	 * @param itemPrice Price of wished item
	 */
	public void placeWish(String itemName, String itemPrice){
		gui.setNotificationMessage("Communicating with server, please wait");
		int price = 0;
		try{
			price = Integer.parseInt(itemPrice);
		} catch(NumberFormatException ex){
			gui.setNotificationMessage("Bad price");
			System.err.println("[LOG] NumberFormatException when wishing item");
			return;
		}
		
		Item item = new Item();
		item.setName(itemName);
		item.setOwner(userName);
		item.setPrice(price);
		
		try {
			serverInt.wishItem(item);
		} catch (RemoteException e) {
			System.err.println("[LOG] RemoteException when wishing item");
			gui.setNotificationMessage("Can't wish the item");
			return;
		}
		
		gui.setNotificationMessage("Item is wished");
		gui.clearWishForm();
	}
	
	/**
	 * Methods sends server request to buy the item.
	 * 
	 * @param itemName Name of the item
	 * @param itemPrice Price of the item
	 */
	public void buyItem(String itemName, String itemPrice, String itemOwner){
		gui.setNotificationMessage("Communicating with server, please wait");
		int price = 0;
		try{
			price = Integer.parseInt(itemPrice);
		} catch(NumberFormatException ex){
			gui.setNotificationMessage("Bad price");
			System.err.println("[LOG] NumberFormatException when buying item");
			return;
		}
		
		Item item = new Item();
		item.setName(itemName);
		item.setOwner(itemOwner);
		item.setPrice(price);
		
		try {
			serverInt.buyItem(userName, item);
		} catch (UknownItemException e) {
			System.err.println("[LOG] UknownItemException when buying item");
			gui.setNotificationMessage("Can't buy the item");
			return;
		} catch (AccountNotFoundException e){
			System.err.println("[LOG] AccountNotFoundException when buying item");
			gui.setNotificationMessage("Not enough money to buy item");
			return;
		}	catch (RemoteException e) {
			System.err.println("[LOG] RemoteException when buying item");
			gui.setNotificationMessage("Can't buy the item");
			return;
		}
		
		gui.setNotificationMessage("Item was bought");
	}
	
	/**
	 * Method should be called before closing client, It unregisteres
	 * client on Marketplace and Banking servers.
	 */
	public void unregisterUser(){
		try {
			serverInt.unregisterClient(userName);
			bank.deleteAccount(account);
		} catch (UknownClientException e) {
			System.err.println("[LOG] UknownClientException when unregistering user");
		} catch (RemoteException e) {
			System.err.println("[LOG] RemoteException when unregistering user");
		} catch (Throwable t) {
			System.err.println("[LOG] Can't unregister client");
		}
	}
}
