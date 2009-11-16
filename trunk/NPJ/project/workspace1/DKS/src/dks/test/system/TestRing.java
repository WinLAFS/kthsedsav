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
import java.util.Properties;

import junit.framework.TestCase;

/**
 * The <code>TestRing</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TestRing.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class TestRing extends TestCase{

	/**
	 * @param args
	 */
//	public static void main(String[] args) {
//		new TestRing().testRing();
//
//	}
	

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestRing.class);
	}

	public void testRing() {
		asserT("5000", 22346, "successor", "190000",
				"ring");
		asserT("5000", 22346, "predecessor", "190000",
		"ring");
	
		System.out.println("Tests passed");
	}

	public void asserT(String Id, int port, String propertyName,
			String assertedValue, String testCommand) {

		Properties properties = new Properties();

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

			url = new URL("http://" + "localhost" + ":" + port + "/"
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

}
