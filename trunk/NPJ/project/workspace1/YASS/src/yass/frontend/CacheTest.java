package yass.frontend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Semaphore;

import yass.TestFileGenerator;
import yass.interfaces.Frontend;

public class CacheTest implements UserInterfaceInterface {

	// //////////////////////////////////////////////////////////////////////////
	// ///////////////////////////// Test Params
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	//
	// private static final boolean TEST_MODE = true;
	private final Semaphore SYNC = new Semaphore(1, true);

	public static int NUMBER_OF_WRITES = System
	.getProperty("yass.test.numberOfWrites") instanceof String ? Integer
	.parseInt(System.getProperty("yass.test.numberOfWrites"))
	: 200;

	public static int NUMBER_OF_READS = System
	.getProperty("yass.test.numberOfReads") instanceof String ? Integer
	.parseInt(System.getProperty("yass.test.numberOfReads"))
	: 4*NUMBER_OF_WRITES;
	
	public static int NUMBER_OF_DELETES = System
	.getProperty("yass.test.numberOfDeletes") instanceof String ? Integer
	.parseInt(System.getProperty("yass.test.numberOfDeletes"))
	: NUMBER_OF_WRITES / 2;
	
	public static long IGNORE_THRESHOLD = System
	.getProperty("yass.test.ignoreThreshold") instanceof String ? Integer
	.parseInt(System.getProperty("yass.test.ignoreThreshold"))
	: Integer.MAX_VALUE;
	
	public static int operationDelay =
		System.getProperty("yass.test.operationDelay") instanceof String ?
				Integer.parseInt(System.getProperty("yass.test.operationDelay"))
			:
				10
	;
	
	

	private int skipX = 5;

	private String skipPrefix = "warmUp";

	static int reads = NUMBER_OF_READS, writes = NUMBER_OF_WRITES,
			deletes = NUMBER_OF_DELETES;

	private static final int[] TEST_CACHE_SIZE = { reads, writes, deletes }; // {R,
	// W,
	// D}

	private static final int[] TEST_CACHE_RATIO = { reads / 10, writes / 10,
			deletes / 10 }; // {R, W, D}

	// private int[] testCacheAck = { 0, 0, 0, 0, 0, 0 }; // {R, W, D, RError,
	// // WError, DError}

	private int[] testCacheAck = { 0, 0, 0 }; // {R, W}
	private int[] testCacheFail = { 0, 0, 0 }; // {RError, WError, DError}
	private int[] testCacheTimeoutFail = { 0, 0, 0 }; // {ReadTimeout, WriteTimeout}

	
	private int[] testCacheStat = { 0, 0, 0 }; // {R, W, D}

	private int[] writeTestCacheStat = { 0, 0, 0, 0 }; // {Request,
														// FindReplicas, Bind,
														// Write}

	// private int[] latestKnownWriteAvg = { 0, 0, 0, 0};

	private int testCacheOpId;

	private long testCacheOpStart;

	private int totalNumberOfStoreHops = 0;

	private int highestNumberOfStoreHops = -1;

	private int lowestNumberOfStoreHops = 100;

	private long testCacheStart;

	private long testCacheEnd;

	final int WRITE = 1;
	final int READ = 0;
	final int DELETE = 2;
	

	public static final String DELAY_FLAG = System
			.getProperty("yass.test.delay");

	private final int INITIAL_DELAY = (DELAY_FLAG instanceof String) ? Integer
			.parseInt(DELAY_FLAG) : 60000;

	//
	// //////////////////////////////////////////////////////////////////////////

	private Frontend myAI = null;

	private final String WORKING_DIRECTORY = System.getProperty("user.dir"); // "";
	// //"/home/joel/MyWorkbench/MySamples";

	private ArrayList<String> filesList;

	public String name = "FE";

	public void run(Frontend applicationInterface, int testMode) {
		myAI = applicationInterface;

		System.err
				.println("############################################################");
		System.err
				.println("##################### Starting Cache Test ##################");
		System.err
				.println("############################################################");
		System.err.println("yass.test.mode = "
				+ System.getProperty("yass.test.mode"));
		System.err.println("niche.cache.mode = "
				+ System.getProperty("niche.cache.mode"));
		System.err.println("yass.test.delay = "
				+ System.getProperty("yass.test.delay"));
		System.err
				.println("dks.timeout = " + System.getProperty("dks.timeout"));
		System.err.println("niche.stableid.mode = "
				+ System.getProperty("niche.stableid.mode"));
		System.err
				.println("############################################################");

		try {
			Thread.sleep(INITIAL_DELAY);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		TestFileGenerator.FILE_NAME = skipPrefix + "_" + name + "_";
		TestFileGenerator.FILES_TO_GENERATE = skipX;
		TestFileGenerator.PATH = WORKING_DIRECTORY + "/testFiles/";
		try {
			TestFileGenerator.main(null);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		TestFileGenerator.FILE_NAME = name + "_";
		TestFileGenerator.FILES_TO_GENERATE = TEST_CACHE_SIZE[1];
		TestFileGenerator.PATH = WORKING_DIRECTORY + "/testFiles/";
		try {
			TestFileGenerator.main(null);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		filesList = new ArrayList<String>();

		testCacheStart = System.currentTimeMillis();
		int r = 0, w = 0, d = 0, i = 0;

		// warm-up
		for (i = 0; i < skipX; i++) {
			try {
				SYNC.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			testCacheOpId = 1;
			myAI.store(new File(WORKING_DIRECTORY + "/testFiles/" + skipPrefix
					+ "_" + name + "_" + i));
		}

		while (r < TEST_CACHE_SIZE[0] || w < TEST_CACHE_SIZE[1] || d < TEST_CACHE_SIZE[2]) {
			// Write files
			for (i = 0; i < TEST_CACHE_RATIO[1] && w < TEST_CACHE_SIZE[1]; i++, w++) {
				try {
					SYNC.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				testCacheOpId = WRITE;
				testCacheOpStart = System.currentTimeMillis();
				myAI.store(new File(WORKING_DIRECTORY + "/testFiles/" + name
						+ "_" + w));
				filesList.add(name + "_" + w);
			}
			
			// Read files
			for (i = 0; i < TEST_CACHE_RATIO[0] && r < TEST_CACHE_SIZE[0]; i++, r++) {
				try {
					SYNC.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int fileId = (int) (Math.random() * filesList.size());
				testCacheOpId = READ;
				testCacheOpStart = System.currentTimeMillis();
				myAI.retrieve(filesList.get(fileId));
			}
			
			//System.out.println("Kommer jag ens hit...!?! TEST_CACHE_RATIO[2]: " + TEST_CACHE_RATIO[2] + " TEST_CACHE_SIZE[2]: " + TEST_CACHE_SIZE[2]);
			
			// Delete files
			 for (i = 0; i < TEST_CACHE_RATIO[2] && d < TEST_CACHE_SIZE[2];	 i++, d++) {
				 
				 try {
					 SYNC.acquire();
				 } catch (InterruptedException e) {
					 e.printStackTrace();
				 }
				 int fileId = (int) (Math.random() * filesList.size());
				 testCacheOpId = DELETE;
				 testCacheOpStart = System.currentTimeMillis();
				 myAI.remove(filesList.remove(fileId));
			
			 }
		}
	}

	private void testCacheDone(String fileName, long step1, long step1_2,
			long step2, long step3, int hops, boolean success) {

		int[] writeAvg = new int[4];
		int avg[] = new int[3];
		
		long totalOpTime = System.currentTimeMillis() - testCacheOpStart;
		
		if (success && totalOpTime < IGNORE_THRESHOLD) {
			// Calc Stat
			

			testCacheStat[testCacheOpId] += totalOpTime;

			// get Average;
			
			avg[READ] = testCacheAck[READ] == 0 ?
					0 
				:
					testCacheStat[READ] / (testCacheAck[READ] - testCacheTimeoutFail[READ]);
			
			avg[WRITE] = testCacheAck[WRITE] == 0 ?
					0
				:
					testCacheStat[WRITE] / (testCacheAck[WRITE]  - testCacheTimeoutFail[WRITE]);

			// get refined Averages;
			
			if (testCacheOpId == WRITE) {

				// System.out.println(
				// step1
				// + "\n"
				// + step1_2
				// + "\n"
				// + step2
				// + "\n"
				// + step3
				// + (step1 - testCacheOpStart)
				// + "\n"
				// + (step2 - testCacheOpStart)
				// + "\n"
				// + (step3 - testCacheOpStart)
				// );

				step1 = step1 - testCacheOpStart;
				step2 = step2 - testCacheOpStart - step1;
				step3 = step3 - testCacheOpStart - step1 - step2;
				step1 = step1 - step1_2;

				writeTestCacheStat[0] += step1;
				writeTestCacheStat[1] += step1_2;
				writeTestCacheStat[2] += step2;
				writeTestCacheStat[3] += step3;
			}

			writeAvg[0] = writeTestCacheStat[0] / (testCacheAck[WRITE] - testCacheTimeoutFail[WRITE]);
			writeAvg[1] = writeTestCacheStat[1] / (testCacheAck[WRITE] - testCacheTimeoutFail[WRITE]);
			writeAvg[2] = writeTestCacheStat[2] / (testCacheAck[WRITE] - testCacheTimeoutFail[WRITE]);
			writeAvg[3] = writeTestCacheStat[3] / (testCacheAck[WRITE] - testCacheTimeoutFail[WRITE]);

		} else  if(!success) { // end if success

			
			System.out.println("The operation for file "
					+ fileName
					+ " terminated with an error"
			);

		} else { //this means > Threshold

			System.out.println("The operation for file "
					+ fileName
					+ " timed out"
			);
			
			//This is discarded, because this is indication of something else we need to track down/
			//deal with separately
			//Still, we need to update the total number of files to divide with to get the proper
			//averages for those files we actually include!
			testCacheTimeoutFail[testCacheOpId]++;
		}
			
			
		System.err.println(
				"C-TEST: " + Arrays.toString(testCacheAck)
				+ ", Avg=" + Arrays.toString(avg) + ", WAvg="
				+ Arrays.toString(writeAvg) + ", fn= " + fileName
				+ "\top= " + testCacheOpId + ", t= " + totalOpTime + " "
				+ step1 + " " + step1_2 + " " + step2 + " " + step3
				+ " hops= " + hops + " @ " + name);

		
		if (TEST_CACHE_SIZE[0] == testCacheAck[0]
				&& TEST_CACHE_SIZE[1] == testCacheAck[1]) {

			testCacheEnd = System.currentTimeMillis();

			Object[] allProperites = System.getProperties().entrySet()
					.toArray();
			String dksProperties = "";
			String nicheProperties = "";
			String yassProperties = "";
			String javaProperties = "";
			String systemProperites = "";

			for (int i = 0; i < allProperites.length; i++) {
				if (allProperites[i].toString().startsWith("dks")) {
					dksProperties += allProperites[i] + "\n";
				} else if (allProperites[i].toString().startsWith("niche")) {
					nicheProperties += allProperites[i] + "\n";
				} else if (allProperites[i].toString().startsWith("yass")) {
					yassProperties += allProperites[i] + "\n";
				} else if (allProperites[i].toString().startsWith("java")) {
					javaProperties += allProperites[i] + "\n";
				} else {
					systemProperites += allProperites[i] + "\n";
				}

			}

			System.err.println("Cache test done in "
					+ ((testCacheEnd - testCacheStart) / 1000)
					+ " seconds\n"
					+ "Number of reads: "
					+ TEST_CACHE_SIZE[0]
					+ "\nNumber of writes: "
					+ TEST_CACHE_SIZE[1]
					+ "\nNumber of deletes: "
					+ TEST_CACHE_SIZE[2]
					// System.err.println("ACKs " +
					// Arrays.toString(testCacheAck));
					+ "\nCache test average times for full ops: "
					+ Arrays.toString(avg)
					+ "\nCache test average times for Reqest - Bind - Store "
					+ Arrays.toString(writeAvg)
					+ "\nAverage number of hops used for store operation: "
					+ (double) totalNumberOfStoreHops
							/ (double) testCacheAck[WRITE]
					+ "\nHighest number of hops used for store operation: "
					+ highestNumberOfStoreHops
					+ "\nLowest number of hops used for store operation: "
					+ lowestNumberOfStoreHops
					+ "\nNumber of failures: "
					+ Arrays.toString(testCacheFail)
					+ "\nNumber of timeouts: "
					+ Arrays.toString(testCacheTimeoutFail)
					+ "\nThe System Properties used are:" + "For dks:\n"
					+ dksProperties + "For niche:\n" + nicheProperties
					+ "For yass:\n" + yassProperties + "For java:\n"
					+ javaProperties + "For the rest of the sys:\n"
					+ systemProperites);

		}
		if(0 < operationDelay) {
			try {
				Thread.sleep(operationDelay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		SYNC.release();
	}

	public void storeAck(final String name, final long step1,
			final long step1_2, final long step2, final long step3,
			final int hops, final boolean success) {

		if (!name.startsWith(skipPrefix)) {
			if (success) {
				testCacheAck[1]++;
				totalNumberOfStoreHops += hops;
				if (hops < lowestNumberOfStoreHops) {
					lowestNumberOfStoreHops = hops;
				} else if (highestNumberOfStoreHops < hops) {
					highestNumberOfStoreHops = hops;
				}

			} else {
				System.err.println("The attempt to store file " + name
						+ " FAILED!");
				testCacheFail[1]++;
				filesList.remove(name);
			}

			// if(TEST_MODE) {

			testCacheDone(name, step1, step1_2, step2, step3, hops, success);
			// }
		} else {
			System.out.println("Warming up system!");
			SYNC.release();
		}
	}

	public void retrieveAck(final String name, final File file) {
		if (file != null) {
			// storeFile(file);
			// loadFile(file, false);
		} else {
			System.err.println("A file read for file " + name + " FAILED!");
			testCacheFail[0]++;
		}
		// if(TEST_MODE) {
		testCacheAck[0]++;
		testCacheDone(name, 0, 0, 0, 0, 0, file != null);
		// }

	}

	public void removeAck(final String name, final boolean success) {
		if (success) {

		} else {
			testCacheAck[5]++;
		}

		// if(TEST_MODE) {
		testCacheAck[2]++;
		testCacheDone(name, 0, 0, 0, 0, 0, success);
		// }

	}

	public void setName(String name) {
		this.name = name;

	}

}
