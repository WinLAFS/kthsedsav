package se.kth.ict.npj.hw2.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.jws.soap.SOAPBinding.Use;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.security.auth.login.AccountNotFoundException;

import bankrmi.Rejected;

import se.kth.ict.npj.hw2.Item;
import se.kth.ict.npj.hw2.client.objects.MPClientInterface;
import se.kth.ict.npj.hw2.exception.ClientAlreadyExistsException;
import se.kth.ict.npj.hw2.exception.IllegalItemException;
import se.kth.ict.npj.hw2.exception.ItemAlreadyExistsException;
import se.kth.ict.npj.hw2.exception.UknownClientException;
import se.kth.ict.npj.hw2.exception.UnknownClientException;
import se.kth.ict.npj.hw2.exception.UnknownItemException;
import se.kth.ict.npj.hw2.server.objects.User;
import se.kth.ict.npj.hw2.server.objects.UserStatistics;

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
	ArrayList<se.kth.ict.npj.hw2.server.objects.Item> wishList = null;
	bankrmi.Bank bank = null;
	
	private EntityManagerFactory emf = null;
	private EntityManager entityManager = null;
	
	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Constructor. 
	 * Connects to bank server & initiates the connection to the database.
	 * 
	 * @param bankUrl URL of the bank server
	 * @throws RemoteException
	 */
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
		wishList = new ArrayList<se.kth.ict.npj.hw2.server.objects.Item>();
		
		try {
			emf = Persistence.createEntityManagerFactory("MarketplaceServerJPA");
			entityManager = emf.createEntityManager();
		}
		catch (Exception e) {
			System.err.println("[LOG] Could not connect to the database. Try again.");
			System.exit(0);
		}
		
		System.out.println("[LOG] Server started");
	}

	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServerInterface#buyItem(se.kth.ict.npj.hw2.Item)
	 */
	public synchronized void buyItem(String username, se.kth.ict.npj.hw2.server.objects.Item item) throws IllegalItemException, UnknownItemException,
			RemoteException, AccountNotFoundException, UknownClientException {
		
		System.out.println("[LOG] Client " + username + " trying to buy item: " + item.getItemName());
		
		EntityTransaction et = getEntityManager().getTransaction();
		et.begin();
		
		User user = getEntityManager().find(User.class, username);
		if (user != null) {
			Query query = getEntityManager().createQuery("select x from Item x where x.itemName LIKE '" + item.getItemName() + "'");
			se.kth.ict.npj.hw2.server.objects.Item item2 = (se.kth.ict.npj.hw2.server.objects.Item) query.getSingleResult();
			if (item2 != null) {
				
				User seller = item2.getSeller();
				
				try {
					bankrmi.Account sellerAccount = bank.getAccount(seller.getUsername());
					if (sellerAccount == null) {
						throw new AccountNotFoundException("Could not get the seller's account.");
					}
					bankrmi.Account buyerAccount = bank.getAccount(user.getUsername());
					if (buyerAccount == null) {
						throw new AccountNotFoundException("Could not get the buyer's account.");
					}
					try {
						buyerAccount.withdraw(item2.getPrice());
						try { 
							sellerAccount.deposit(item2.getPrice());
							if (item2.getQuantity() == 1) {
								//Query query2 = getEntityManager().createNativeQuery("delete from item where itemname LIKE '" + item.getItemName() + "'");
								//query2.executeUpdate();
								Vector<se.kth.ict.npj.hw2.server.objects.Item> sellerItems = (Vector<se.kth.ict.npj.hw2.server.objects.Item>) seller.getSellingItemList();
								sellerItems.remove(item2);
								getEntityManager().remove(item2);
								
							}
							else {
								int exQuantity = item2.getQuantity();
								item2.setQuantity(--exQuantity);
							}
							UserStatistics buyerStats = user.getUserStatistics();
							UserStatistics sellerStats = seller.getUserStatistics();
							int buys = buyerStats.getBuysNumber();
							buyerStats.setBuysNumber(++buys);
							int sells = sellerStats.getSellsNumber();
							sellerStats.setSellsNumber(++sells);
							
						} catch (Rejected e) {
							buyerAccount.deposit(item2.getPrice());
							throw e;
						}
					}
					catch (Rejected e) {
						System.out.println("[LOG] The bank transaction was rejected: " + e.getMessage());
						throw e;
					}
				}
				catch (Rejected e) {
					throw e;
				}
				catch (AccountNotFoundException e) {
					et.rollback();
					throw e;
				}
				catch (RemoteException e) {
					et.rollback();
					throw new AccountNotFoundException("Could not update the clients' accounts.");
				}
				if(et.isActive())
					et.commit();
				
				try {
					MPClientInterface mpci = (MPClientInterface) Naming.lookup(seller.getUserURL());
					mpci.receiveItemSoldNotification(item2);
					updateUserStatistics(user);
					updateUserStatistics(seller);
				} 
				catch (MalformedURLException e) {
					System.out.println("[LOG] The seller url was not correct: " + e.getMessage());
				} 
				catch (NotBoundException e) {
					System.out.println("[LOG] The seller object was not found: " + e.getMessage());
				}
				catch (RemoteException e) {
					System.out.println("[LOG] The seller object could not be retrieved: " + e.getMessage());
				}
			}
			else {
				et.rollback();
				throw new UnknownItemException();
			}
		}
		else {
			et.rollback();
			throw new UknownClientException();
		}
		
	}

	public synchronized Vector<se.kth.ict.npj.hw2.server.objects.Item> inspectItems() throws RemoteException {
		System.out.println("[LOG] inspectItems()");
		Query query = getEntityManager().createQuery("SELECT x FROM Item x");
		
		Vector<se.kth.ict.npj.hw2.server.objects.Item> itemListt = (Vector<se.kth.ict.npj.hw2.server.objects.Item>) query.getResultList();
		return itemListt;
	}
	
	
	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServerInterface#registerClient(java.lang.String)
	 */
	public synchronized void registerClient(String id, String password, String userURL) throws ClientAlreadyExistsException, RemoteException {
		System.out.println("[LOG] Client registering: " + id);
		
		UserStatistics userStatistics = new UserStatistics();
		userStatistics.setBuysNumber(0);
		userStatistics.setSellsNumber(0);
		
		User user = new User();
		user.setUsername(id);
		user.setPassword(password);
		user.setUserStatistics(userStatistics);
		user.setUserURL(userURL);
		
		try {
			EntityTransaction et = getEntityManager().getTransaction();
			et.begin();
			getEntityManager().persist(userStatistics);
			getEntityManager().persist(user);
			et.commit();
		}
		catch (EntityExistsException e) {
			throw new ClientAlreadyExistsException();
		}
	}


	public synchronized se.kth.ict.npj.hw2.server.objects.Item wishItem(String username, se.kth.ict.npj.hw2.server.objects.Item wish) throws ItemAlreadyExistsException, RemoteException, IllegalItemException {
		System.out.println("[LOG] Wish item: " + wish.getItemName());
		
		//TODO
		Query query = getEntityManager().createQuery("SELECT x FROM Item x");
		Vector<se.kth.ict.npj.hw2.server.objects.Item> itemListt = (Vector<se.kth.ict.npj.hw2.server.objects.Item>) query.getResultList();
		
		User user = getEntityManager().find(User.class, username);
		
		Iterator<se.kth.ict.npj.hw2.server.objects.Item > iIterator = itemListt.iterator();
		while (iIterator.hasNext()) {
			se.kth.ict.npj.hw2.server.objects.Item item = iIterator.next();
			
			if (item.getItemName().equalsIgnoreCase(wish.getItemName()) && item.getPrice() <= wish.getPrice()) {
				try {
					MPClientInterface mpci = (MPClientInterface) Naming.lookup(user.getUserURL());
					mpci.receiveWishedItemNotification(item);
				} 
				catch (MalformedURLException e) {
					System.out.println("[LOG] The seller url was not correct: " + e.getMessage());
				} 
				catch (NotBoundException e) {
					System.out.println("[LOG] The seller object was not found: " + e.getMessage());
				}
				catch (RemoteException e) {
					System.out.println("[LOG] The seller object could not be retrieved: " + e.getMessage());
				}
				return item;
			}
		}
		wish.setSeller(user);
		wishList.add(wish);
		
		return null;
		
	}

	
	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServerInterface#sellItem(java.lang.String, se.kth.ict.npj.hw2.Item)
	 */
	public synchronized void sellItem(se.kth.ict.npj.hw2.server.objects.Item item, String username) throws RemoteException,
			IllegalItemException, ItemAlreadyExistsException, UnknownClientException {
		
		System.out.println("[LOG] Selling item: " + item.getItemName());

		boolean updated = false;
		EntityTransaction et = getEntityManager().getTransaction();
		et.begin();
		
		User user = getEntityManager().find(User.class, username);
		Vector<se.kth.ict.npj.hw2.server.objects.Item> itemList = (Vector<se.kth.ict.npj.hw2.server.objects.Item>) user.getSellingItemList();
		Iterator<se.kth.ict.npj.hw2.server.objects.Item> iIterator = itemList.iterator();
		while (iIterator.hasNext()) {
			se.kth.ict.npj.hw2.server.objects.Item item2 = (se.kth.ict.npj.hw2.server.objects.Item) iIterator.next();
			if (item.getItemName().equals(item2.getItemName()) && item.getPrice() == item2.getPrice()) {
				
				try {
					int exQuantity = item2.getQuantity();
					item2.setQuantity(exQuantity + item.getQuantity());
					et.commit();
					
					break;
				}
				catch(Exception e) {
					System.err.println("[LOG] Could not store the new item: " + e.getMessage());
				}
				finally {
					updated = true;
				}
			}
		}
		if (!updated) {
			try {
				item.setSeller(user);
				itemList.add(item);
				getEntityManager().persist(item);
				
				//TODO
				Iterator<se.kth.ict.npj.hw2.server.objects.Item> iIterator2 = wishList.iterator();
				while (iIterator2.hasNext()) {
					se.kth.ict.npj.hw2.server.objects.Item item2 = (se.kth.ict.npj.hw2.server.objects.Item) iIterator2.next();
					if (item.getItemName().equals(item2.getItemName()) && item.getPrice() <= item2.getPrice()) {
						try {
							User user2 = item2.getSeller();
							MPClientInterface mpci = (MPClientInterface) Naming.lookup(user2.getUserURL());
							mpci.receiveWishedItemNotification(item);
						} 
						catch (MalformedURLException e) {
							System.out.println("[LOG] The seller url was not correct: " + e.getMessage());
						} 
						catch (NotBoundException e) {
							System.out.println("[LOG] The seller object was not found: " + e.getMessage());
						}
						catch (RemoteException e) {
							System.out.println("[LOG] The seller object could not be retrieved: " + e.getMessage());
						}
					}
				}
				
				et.commit();
			}
			catch(Exception e) {
				System.err.println("[LOG] Could not store the new item: " + e.getMessage());
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.server.MarketplaceServerInterface#unregisterClient(java.lang.String)
	 */
	public synchronized void unregisterClient(String id) throws RemoteException,
			UnknownClientException {
		
//		try{
//			System.out.println("[LOG] Unregister user: " + id);
//			if (!clientList.contains(id)) {
//				throw new UnknownClientException();
//			}
//			
//			for(int i=0; i<itemList.size(); i++){
//				Item item = itemList.get(i);
//				if (item.getOwner().equals(id)) {
//					itemList.remove(item);
//				}
//			}
//			
//			Iterator<Item> wlIterator = wishList.iterator();
//			while (wlIterator.hasNext()) {
//				Item item2 = (Item) wlIterator.next();
//				if (item2.getOwner().equals(id)) {
//					wishList.remove(item2);
//				}
//			}
//		} catch (Throwable e) {
//			System.err.println("[LOG] Can't correctly unregister client");
//		}
//		
//		clientList.remove(id);
		
		System.out.println("[LOG] User logged out : " + id);
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

	@Override
	public void loginUser(String id, String password, String userURL) throws RemoteException,
			UknownClientException {

		boolean login = false;
		
		EntityTransaction et = getEntityManager().getTransaction();
		et.begin();
		User user = getEntityManager().find(User.class, id);
		if (user != null) {
			if (user.getPassword().equals(password)) {
				user.setUserURL(userURL);
				updateUserStatistics(user);
				et.commit();
				login = true;
			}
			else {
				login = false;
			}
		}
		else {
			login = false;
		}
		
		if (!login) {
			et.commit();
			throw new UknownClientException();
		}
		
	}
	
	private void updateUserStatistics(User user) {
		UserStatistics userStatistics = user.getUserStatistics();
		try {
			MPClientInterface mpci = (MPClientInterface) Naming.lookup(user.getUserURL());
			mpci.receiveStatisticsChange(userStatistics);
		} 
		catch (MalformedURLException e) {
			System.out.println("[LOG] The seller url was not correct: " + e.getMessage());
		} 
		catch (NotBoundException e) {
			System.out.println("[LOG] The seller object was not found: " + e.getMessage());
		}
		catch (RemoteException e) {
			System.out.println("[LOG] The seller object could not be retrieved: " + e.getMessage());
		}
	}

}
