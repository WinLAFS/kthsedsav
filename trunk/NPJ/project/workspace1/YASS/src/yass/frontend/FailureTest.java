package yass.frontend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import yass.TestFileGenerator;
import yass.interfaces.Frontend;
import yass.tests.HealingTest;

public class FailureTest implements UserInterfaceInterface{
	
	////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Test Params ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	//
//	private static final boolean TEST_MODE = true;
	private final Semaphore SYNC = new Semaphore(1, true);
	public static int NUMBER_OF_TEST_FILES =
		System.getProperty("yass.test.numberOfWrites") instanceof String ?
				Integer.parseInt(System.getProperty("yass.test.numberOfWrites"))
			:
				200;
 
	private int	Acks = 0;
	private int	AcksFaild = 0;
	private int	Stats = 0;  //{R, W, D}
	private long testOpStart;
	
	private long testStart;
	private long testEnd;
	//
	////////////////////////////////////////////////////////////////////////////

	private Frontend myAI = null;
	private final String WORKING_DIRECTORY = System.getProperty("user.dir"); //""; //"/home/joel/MyWorkbench/MySamples";
	public String name = "FE";
	private final int INITIAL_DELAY = (System.getProperty("yass.test.delay") instanceof String) ? Integer.parseInt(System.getProperty("yass.test.delay")) : 60000;
	
	
	public  void run(Frontend applicationInterface, int mode)
	{
		myAI = applicationInterface;
		
		System.err.println("############################################################");
		System.err.println("##################### Starting Fail Test ###################");
		System.err.println("############################################################");
		

		System.out.println("Will sleep for " + (INITIAL_DELAY / 1000));
		try {
			Thread.sleep(INITIAL_DELAY);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.out.println("Done sleeping");
		
		TestFileGenerator.FILE_NAME = HealingTest.TEST_FILE_NAME_PREFIX;
		TestFileGenerator.FILES_TO_GENERATE = NUMBER_OF_TEST_FILES;
		TestFileGenerator.PATH = WORKING_DIRECTORY+"/testFiles/";
		
		try {
			TestFileGenerator.main(null);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		testStart = System.currentTimeMillis();
		

		//Write files
		for(int i=0; i<NUMBER_OF_TEST_FILES ; i++) {
			try {
				SYNC.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			testOpStart = System.currentTimeMillis();
			myAI.store(new File(WORKING_DIRECTORY+"/testFiles/"+TestFileGenerator.FILE_NAME+i));
			System.out.println("No "+(i+1) + " out of " + NUMBER_OF_TEST_FILES);
			
		}
		
		System.out.println("Done pre-loading the system in " + ((System.currentTimeMillis() - testStart) / 1000) + " sec ");
	
	}
	
	private void releaseLock(String fileName) {
		
		SYNC.release();
	}
	
	
	public void storeAck(final String name, final long step1, final long step1_2, final long step2, final long step3, final int hops, final boolean success) {
		
		if(success) {
		
			
		}
		else {
			AcksFaild++;
			
		}

		Acks++;
			releaseLock(name);
	}


	public void retrieveAck(final String name, final File file) {
		
		System.err.println("FAIL TEST: Should not read!");

	}

	public  void removeAck(final String name, final boolean success) {
		System.err.println("FAIL TEST: Should not remove!");		
	}

	public void setName(String name) {
		this.name = name;
		
	}
	
}
