package se.kth.ict.npj.hw2.server;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class MarketplaceServer {
	final static String USAGE = "use: java MarketplaceServer serverName bankAddress bankName";
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println(MarketplaceServer.USAGE);
			System.exit(0);
		}
		
		String bankURL = "rmi://" + args[1] + "/" + args[2];
		try {
			System.out.println("[LOG] Bank url: " + bankURL);
			MarketplaceServerImp marketplaceServerImp = new MarketplaceServerImp(bankURL);
			String mpsURL = "rmi://" + args[1] + "/" + args[0];
			
			System.out.println("[LOG] Marketplace server url: " + mpsURL);
			
			try {
				Naming.rebind(mpsURL, marketplaceServerImp);
			} catch (MalformedURLException e) {
				System.out.println("[LOG] The url was not correct formed: " + e.getMessage());
			}
			
		} catch (RemoteException e) {
			System.out.println("[LOG] Could not start the Marketplace server: " + e.getMessage());
		}
		

	}

}
