package se.kth.ict.npj.hw2.client.objects;

import java.rmi.Remote;
import java.rmi.RemoteException;

import se.kth.ict.npj.hw2.Item;
import se.kth.ict.npj.hw2.server.objects.UserStatistics;

public interface MPClientInterface extends Remote{
	
	/**
	 * The method returns client's ID
	 * 
	 * @return String with client's ID
	 * @throws RemoteException
	 */
	String getId() throws RemoteException;
	
	/**
	 * The method is called to notify client about selling his item
	 * 
	 * @param item Item that was sold
	 * @throws RemoteException
	 */
	void receiveItemSoldNotification(se.kth.ict.npj.hw2.server.objects.Item item) throws RemoteException;
	
	
	/**
	 * The method is called when an item that client wished appears at
	 * marketplace.
	 * 
	 * @param item Item that client wished.
	 * @throws RemoteException
	 */
	void receiveWishedItemNotification(Item item) throws RemoteException;
	
	/**
	 * The method is called when user statistics changed
	 * 
	 * @param statistics User statistics object
	 * @throws RemoteException
	 */
	void receiveStatisticsChange(UserStatistics statistics) throws RemoteException;
}
