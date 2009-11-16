/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package examples;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.apache.log4j.PropertyConfigurator;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.arch.Component;
import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
import dks.boot.DKSPropertyLoader;
import dks.boot.DKSWebCacheManager;
import dks.dht.events.PutRequestEvent;

/**
 * The <code>DHTExample</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DHTExample.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class DHTExample extends Component {

	
	static DKSModifiedNode node;
	static DKSParameters dksParameters;
	static DKSRef myRef;
	/**
	 * @param scheduler
	 * @param registry
	 */
	public DHTExample(Scheduler scheduler, ComponentRegistry registry) {
		super(scheduler, registry);
		registerForEvents();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		/*
		 * + Follow these steps to run DKS: - Define this JVM properties in your
		 * launcher: dks.propFile=./dksParam.prop
		 * org.apache.log4j.config.file=log4j.config - For creating the first
		 * node of the ring start this program with the following parameters:
		 * create [id] [port] [IPAddr] - For starting any other node use the
		 * following parameters: join [id] [port] [IPAddr]
		 */

		System.out.println("DHT Example");
		PropertyConfigurator.configure(System
				.getProperty("org.apache.log4j.config.file"));

		DKSPropertyLoader propertyLoader = new DKSPropertyLoader();

		dksParameters = (propertyLoader).getDKSParameters();

		if (args.length < 4) {
			System.err
			.println("Usage: Test <create|join> <id> <port> <bind_ip>");
		}

		for (int i = 0; i < args.length; i++) {
			System.out.println(i + "=" + args[i]);
		}

		try {
			boolean create = args[0].equals("create");
			BigInteger id = new BigInteger(args[1]);
			int port = Integer.parseInt(args[2]);
			InetAddress ip = InetAddress.getByName(args[(args.length - 1)]);

			if (create) {
				System.out.println("First node. Creating a ring...");
				myRef = new DKSRef(ip, port, id.abs());
				node = new DKSModifiedNode(myRef,dksParameters, propertyLoader.getWebcacheAddress());
				node.getDksImplementation().create();

			} else {
				try {
					String webCacheAddres = propertyLoader.getWebcacheAddress();
					DKSWebCacheManager dksCacheManager = new DKSWebCacheManager(
							webCacheAddres);

					myRef = new DKSRef(ip, port, id.abs());

					node = new DKSModifiedNode(myRef, dksParameters, webCacheAddres);

					String rawDKSRef = dksCacheManager.getFirstDKSRef();

					DKSRef dksRef = null;

					dksRef = new DKSRef(rawDKSRef);

					System.out.println("Joining ring using node " + dksRef
							+ "...");
					node.getDksImplementation().join(dksRef);
															
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		}

		DHTExample app = new DHTExample(node.getScheduler(), node.getComponentRegistry());

		// if I'm node 30 then I'll put
		if(args[1].equals("030")) {
			System.out.println("DEBUG: I am node 30 and I'll Put to others!");
			app.put();
		}
	}

	/* (non-Javadoc)
	 * @see dks.arch.Component#registerForEvents()
	 */
	@Override
	protected void registerForEvents() {
		
	}
	
	void put() {
		PutRequestEvent pre = new PutRequestEvent();
		pre.setKey("This is a key");
		pre.setValue("This is the value");
		//pre.setFlavor(putFlavor.PUT_OVERWRITE); //default
		//pre.setMultiVal(no multiVal); //default
		trigger(pre);
	}
	
}
