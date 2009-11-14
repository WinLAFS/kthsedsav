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

	public MPClientLogic(MPClientGUI gui){
		this.gui=gui;
	}
	
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
				
				String bankUrl = "rmi://localhost/Nordea";
				bankrmi.Bank bank = (bankrmi.Bank) Naming.lookup(bankUrl);
				Account account = bank.newAccount(userName);
				account.deposit(10000000);
			} catch (UnknownHostException e) {
				gui.connectionError("Can't conect to server");
				System.err.println("[LOG] UnknownHostException when connecting to server");
				return;
				//e.printStackTrace();
			}
			
			
			gui.connectionSuccessful();
		} catch (ClientAlreadyExistsException e){
			gui.connectionError("Choose another name");
			//e.printStackTrace();
			System.err.println("[LOG] ClientAlreadyExistsException when connecting to server");
		} catch (MalformedURLException e) {
			gui.connectionError("Bad server address or port");
			//e.printStackTrace();
			System.err.println("[LOG] MalformedURLException when connecting to server");
		} catch (RemoteException e) {
			gui.connectionError("Can't conect to server");
			//e.printStackTrace();
			System.err.println("[LOG] RemoteException when connecting to server");
		} catch (NotBoundException e) {
			gui.connectionError("Can't connect to server");
			//e.printStackTrace();
			System.err.println("[LOG] NotBoundException when connecting to server");
		}
	}
	
	public void updateItems(){
		try {
			if(serverInt!=null){
				ArrayList<Item> items = serverInt.inspectItems();
				gui.updateItemsList(items);
			} else {
				gui.setNotificationMessage("Can't connect to the server");
			}
		} catch (RemoteException e) {
			gui.setNotificationMessage("Error connecting to server");
			System.err.println("[LOG] RemoteException when updating items list");
		}
	}
	
	public void sellItem(String itemName, String itemPrice){
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
	
	public void placeWish(String itemName, String itemPrice){
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
			//e.printStackTrace();
			System.err.println("[LOG] RemoteException when wishing item");
			gui.setNotificationMessage("Can't wish the item");
			return;
		}
		
		gui.setNotificationMessage("Item is wished");
		gui.clearWishForm();
	}
	
	public void buyItem(String itemName, String itemPrice){
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
		item.setOwner(userName);
		item.setPrice(price);
		
		try {
			serverInt.buyItem(userName, item);
		} catch (UknownItemException e) {
			System.err.println("[LOG] UknownItemException when buying item");
			gui.setNotificationMessage("Can't buy the item");
			return;
			//e.printStackTrace();
		} catch (AccountNotFoundException e){
			System.err.println("[LOG] AccountNotFoundException when buying item");
			gui.setNotificationMessage("Wrong user account");
			return;
		}	catch (RemoteException e) {
			System.err.println("[LOG] RemoteException when buying item");
			gui.setNotificationMessage("Can't buy the item");
			return;
			//e.printStackTrace();
		}
		
		gui.setNotificationMessage("Item was bought");
	}
	
	public void unregisterUser(){
		try {
			serverInt.unregisterClient(userName);
		} catch (UknownClientException e) {
			System.err.println("[LOG] UknownClientException when unregistering user");
			//e.printStackTrace();
		} catch (RemoteException e) {
			System.err.println("[LOG] RemoteException when unregistering user");
			//e.printStackTrace();
		}
	}
}
