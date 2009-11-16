/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.test.system;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import dks.arch.Event;
import dks.boot.DKSNode;
import dks.test.unit.DKSUnitTestCase;
import dks.test.unit.DummyComponent;
import dks.web.Parameter;

/**
 * The <code>DKSSystemTestCase</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DKSSystemTestCase.java 586 2008-03-26 11:03:21Z ahmad $
 */
public class DKSSystemTestCase extends DKSUnitTestCase {

	private HashMap<String, Address> addresses;

	// private Random random;
	//
	// private DKSParameters dksParameters;
	//
	// private DKSRef ref1;

	protected DKSNode node1;

//	private InetAddress ip = null;
//
//	private int port;

	Properties properties;

	protected DummyComponent dummyComponent;

//	private BigInteger chosenId = new BigInteger("1000");

	// private HashMap<Integer, Process> nodes;

	/**
	 * 
	 */
	public DKSSystemTestCase() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public DKSSystemTestCase(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public void setUp() {
		super.setUp();

		this.addresses = new HashMap<String, Address>();

		build();

		sleep(2000);
		//
		properties = new Properties();
		//
		// // random = new Random();
		// //
		// // nodes = new HashMap<Integer, Process>();
		//
		// DKSPropertyLoader propertyLoader = new DKSPropertyLoader();
		//
		// DKSParameters dksParameters = (propertyLoader).getDKSParameters();
		//
		// PropertyConfigurator.configure(System
		// .getProperty("org.apache.log4j.config.file"));
		//
		// try {
		// ip = propertyLoader.getIP();
		// } catch (UnknownHostException e) {
		// // TODO Handle the exception
		// }
		// port = propertyLoader.getPort();
		//
		// // byte[] hash = AddressHashing.hash(ip, port);
		//
		// DKSRef ref2 = new DKSRef(ip, port, chosenId.abs());
		//
		// System.out.println("Starting first node with id=" + chosenId);
		//
		// node1 = new ExampleDKSModifiedNode(ref2, dksParameters,
		// propertyLoader
		// .getWebcacheAddress());
		//
		// System.out.println("Creating the ring..");
		// node1.getDksImplementation().create();

	}

	public void trigger(Event event) {
		node1.getScheduler().dispatch(event);
	}

	public void asserT(String Id, String propertyName, String assertedValue,
			String testCommand, List<Parameter> params) {

		Address address = addresses.get(Id);

		properties = new Properties();

		// Contacting first d and retrieving infos
		URL url = null;

		String parameters = "";

		if (params != null) {

			int j = 0;
			for (Parameter element : params) {
				j++;
				parameters += element.getKey() + "=" + element.getValue();

				if (j != params.size())
					parameters += "&";
			}

		}
		try {

			url = new URL("http://" + address.Ip + ":" + address.port
					+ "/test/" + testCommand + "?" + parameters);

			// Get an input stream for reading
			InputStream in = url.openStream();

			// System.out.println(bufIn);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuffer sb = new StringBuffer();
			String line = null;
			do {
				line = br.readLine();
				if (line != null)
					if (!line.equalsIgnoreCase("STOP"))
						sb.append(line + "\n");
					else
						break;
				// System.out.println(sb.toString());
			} while (line != null);

			properties.load(new ByteArrayInputStream(sb.toString().getBytes()));

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
			System.out.println("Connection reset");
			System.out.println(properties.toString());
		}

		assertEquals(assertedValue, properties.getProperty(propertyName));
	}

	public void asserT(String Id, String propertyName, String assertedValue,
			String testCommand) {

		Address address = addresses.get(Id);

		properties = new Properties();

		// Contacting first d and retrieving infos
		URL url = null;

		// String parameters = "";
		//
		// if (params != null) {
		//
		// int j = 0;
		// for (Parameter element : params) {
		// j++;
		// parameters += element.getKey() + "=" + element.getValue();
		//
		// if (j != params.size())
		// parameters += "&";
		// }
		//
		// }
		try {

			url = new URL("http://" + address.Ip + ":" + address.port + "/"
					+ testCommand);

			// Get an input stream for reading
			InputStream in = url.openStream();

			// System.out.println(bufIn);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuffer sb = new StringBuffer();
			String line = null;
			do {
				line = br.readLine();
				if (line != null)
					if (!line.equalsIgnoreCase("STOP"))
						sb.append(line + "\n");
					else
						break;
				// System.out.println(sb.toString());
			} while (line != null);

			properties.load(new ByteArrayInputStream(sb.toString().getBytes()));

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
			System.out.println("Connection reset");
			System.out.println(properties.toString());
		}

		assertEquals(assertedValue, properties.getProperty(propertyName));
	}

	protected void startNode(String class1, String op, String id, int port,
			String Ip) {
		Runtime rt = Runtime.getRuntime();

		// Process execute the slave app
		try {
			rt.exec("./launchTest.sh  " + class1 + " " + op + " " + id + " "
					+ port + " " + Ip);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		addresses.put(id, new Address(Ip, port));

		sleep(2000);

		System.out.println("Node " + id + " started on port=" + port);
	}

	protected void startExternalNodes(int number) {
		Runtime rt = Runtime.getRuntime();

		// Process execute the slave app
		try {
			rt.exec("./launchNodes.sh " + number);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void tearDown() {
		stopAllNodes();
	}

	protected void stopExternalNode(int id) {
		// Linux Bash
		String[] cmd = { "/bin/bash", "-c",
		// "kill -9 `ps -aux | grep java | grep dks | grep "+id+" | tr -s '
				// ' | cut -d '
				// ' -f2` > ciao"
				"ps -axo pid,command | grep dks"
		// | grep "+id+" | tr -s ' ' | cut -d ' ' -f2 > ciao" " +
		};
		@SuppressWarnings("unused")
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
		}

		// try {
		// p.waitFor();
		//			
		//			
		// } catch (InterruptedException e) {
		// Thread.currentThread().interrupt();
		// }
		//
		// // You must close these even if you never use them!
		// try {
		// p.getInputStream().close();
		// p.getOutputStream().close();
		// p.getErrorStream().close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	protected void stopAllNodes() {
		Runtime rt = Runtime.getRuntime();
		// Process execute the slave app
		try {
			rt.exec("./killserver.sh");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void build() {
		Runtime rt = Runtime.getRuntime();
		// Process execute the slave app
		try {
			rt.exec("ant");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("static-access")
	protected void sleep(long millisecs) {
		try {
			Thread.currentThread().sleep(millisecs);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}

class Address {

	String Ip;

	int port;

	public Address(String Ip, int port) {
		this.Ip = Ip;
		this.port = port;
	}

	public String getAddress() {
		return Ip + port;
	}
}
