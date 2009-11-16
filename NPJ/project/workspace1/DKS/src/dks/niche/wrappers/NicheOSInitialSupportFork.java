/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.wrappers;

import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.jasmine.jade.service.componentdeployment.DeploymentParams;
import org.objectweb.jasmine.jade.util.Serialization;

import dks.niche.hiddenEvents.DeployRequestEvent;
import dks.niche.hiddenEvents.DiscoverRequestEvent;
import dks.niche.interfaces.NicheAsynchronousInterface;

/**
 * The <code>NicheOSSInitialSupportFork</code> class
 *
 * @author Joel
 * @version $Id: NicheOSInitialSupportFork.java 294 2006-05-05 17:14:14Z joel $
 */
public class NicheOSInitialSupportFork extends NicheOSSupportFork {
	
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 8121953979308405993L;
	
	
	public NicheOSInitialSupportFork(NicheAsynchronousInterface niche) {
		super(niche, null, null, false);
		//myId = niche.getLocalId();	
	}
	public ArrayList discover(Object description)	{
		synchronized (this) {
			prepareWait();
			myNiche.trigger(new DiscoverRequestEvent(description,
					new NicheNotify(this, operationId)));
			myWait(operationId, 100);
		}
		// System.out.println("Discover done!");
		ArrayList unbalancedList = (ArrayList) waitForResults[operationId];
		ArrayList resultList = new ArrayList();
		if(randomizeDiscover) {
			while(0 < unbalancedList.size()) {
				resultList.add(unbalancedList.remove(myRandom.nextInt(unbalancedList.size())));
			}
			/*#%*/ myNiche.log("Initial discover done, returning "+ resultList.size() + " scrambled matches");
		} else {
			resultList = unbalancedList;
			/*#%*/ myNiche.log("Initial discover done, returning "+ resultList.size() + " matches");
		}
		return resultList;

	}

	public synchronized ArrayList deploy(Object destinations, Object descriptions) {
		prepareWait();
		waitForResults[operationId] = waitForSynchronousReturnValue;
		myNiche.trigger(new DeployRequestEvent(destinations, descriptions, new NicheNotify(this, operationId), "ERROR THIS SHOULD NOT BE USED")); 
		myWait(operationId, 100);	
	
		//System.out.println("Done with deploy!");

		//mySI.addToComponentList((ArrayList<Object[]>) waitForResults[operationId]);
		
		//myFakeDeployment++;
		//System.out.println("Niche-deployer says: deploy-op no: "+myFakeDeployment);
		//if(myFakeDeployment == APPLICATION_SIZE ) {
		

//		Object args = ((ArrayList)descriptions).get(0);
//		DeploymentParams params=null;
//		
//		try {
//			params=(DeploymentParams) Serialization.deserialize((String) args);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		System.out.println("Niche-deployer says: Component name was: "+params.name);
//		if(params.name.equals(SimpleResourceManager.FRONTEND)) {
//			
//			System.out.println("Niche-deployer says: Taking over responsibility");
//			myNiche.trigger(new StartScriptEvent(mySI));
//		}		
//		else if(params.name.startsWith(SimpleResourceManager.MAIN_COMPONENT)) {
//			
//			mySI.setDeployParams(params);
//			mySI.setRequirements();
//			System.out.println("I set the reqs to: "+mySI.getComponentRequirements());
//		}
//		
		
		return (ArrayList)waitForResults[operationId];		

	}

}
