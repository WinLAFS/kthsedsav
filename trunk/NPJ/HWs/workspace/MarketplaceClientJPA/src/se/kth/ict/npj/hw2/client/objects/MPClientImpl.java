package se.kth.ict.npj.hw2.client.objects;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import se.kth.ict.npj.hw2.Item;
import se.kth.ict.npj.hw2.client.MPClientGUI;
import se.kth.ict.npj.hw2.server.objects.UserStatistics;


public class MPClientImpl extends UnicastRemoteObject implements MPClientInterface {

	private String clientId;
	private MPClientGUI gui;
	
	/**
	 * Constuctor accepts id of the client and link to client gui.
	 * 
	 * @param clientId Id of the client
	 * @param gui Link to client gui
	 * @throws RemoteException
	 */
	public MPClientImpl(String clientId, MPClientGUI gui) throws RemoteException {
		super();
		this.clientId = clientId;
		this.gui = gui;	
	}

	private static final long serialVersionUID = 5555500677813683130L;

	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.client.objects.MPClientInterface#getId()
	 */
	public String getId() throws RemoteException {
		return this.clientId;
	}

	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.client.objects.MPClientInterface#receiveItemSoldNotification(se.kth.ict.npj.hw2.Item)
	 */
	public void receiveItemSoldNotification(se.kth.ict.npj.hw2.server.objects.Item item) throws RemoteException {
		gui.notifyItemSold(item);
	}

	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.client.objects.MPClientInterface#receiveWishedItemNotification(se.kth.ict.npj.hw2.Item)
	 */
	public void receiveWishedItemNotification(se.kth.ict.npj.hw2.server.objects.Item item) throws RemoteException {
		gui.notifyWishListItemFound(item);
	}

	
	/* (non-Javadoc)
	 * @see se.kth.ict.npj.hw2.client.objects.MPClientInterface#receiveStatisticsChange(se.kth.ict.npj.hw2.server.objects.UserStatistics)
	 */
	public void receiveStatisticsChange(UserStatistics statistics)throws RemoteException {
		gui.notifyStatisticsChanged(statistics);
		
	}

}
