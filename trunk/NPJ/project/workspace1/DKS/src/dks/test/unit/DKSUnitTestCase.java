/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.test.unit;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import dks.DKSParameters;
import dks.arch.Event;
import dks.boot.DKSPropertyLoader;

/**
 * The <code>DKSTest</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DKSUnitTestCase.java 205 2007-02-16 15:02:49Z Roberto $
 */
public class DKSUnitTestCase extends TestCase {

	public Random random;

	public DKSParameters dksParameters;

	public LinkedBlockingQueue<Event> eventQueue;

	/**
	 * General Class for starting DKSTests
	 */
	public DKSUnitTestCase() {
		super();
	}

	/**
	 * @param arg0
	 */
	public DKSUnitTestCase(String arg0) {
		super(arg0);
	}

	public void setUp() {
		random = new Random();
		dksParameters = (new DKSPropertyLoader()).getDKSParameters();
		eventQueue = new LinkedBlockingQueue<Event>();

		PropertyConfigurator.configure(System
				.getProperty("org.apache.log4j.config.file"));

	}

	public void tearDown() {

	}

	public void enqueue(Event event) {
		this.eventQueue.add(event);
	}

}
