/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * The <code>AddressResolution</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: AddressResolution.java 220 2007-03-03 17:54:27Z Roberto $
 */
public class AddressResolution {
	/**
	 * Returns the IPv4 non-loopback-address of the machine
	 * 
	 * @return The Inet4Address nonloopback address requested
	 */
	public static Inet4Address getNonLoopbackInet4Address() {
		try {
			Enumeration<NetworkInterface> ifaces = NetworkInterface
					.getNetworkInterfaces();

			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();
				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (!address.isLoopbackAddress()
							&& !address.isAnyLocalAddress()) {
						if (address.getAddress().length == 4) {
							return (Inet4Address) address;
						}
					}
				}
			}
			return null;
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		}
	}
}
