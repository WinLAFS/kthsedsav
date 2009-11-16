/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche;

//import jade.JadeDeploymentInterface;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.boot.DKSNode;
import dks.niche.components.NicheCommunicatingComponent;
import dks.niche.components.NicheManagementContainerComponent;
import dks.niche.components.NicheOverlayServiceComponent;
import dks.niche.ids.ComponentId;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.wrappers.SimpleResourceManager;
import dks.test.niche.NicheServlet;

/**
 * The <code>NicheOSSupport</code> class
 *
 * @author Joel
 * @version $Id: NicheOSSupport.java 294 2006-05-05 17:14:14Z joel $
 */
public class NicheNode extends DKSNode  {
	
	NicheOverlayServiceComponent serviceComponent;
	
	private NicheCommunicatingComponent communicationComponent;
	
	NicheManagementContainerComponent container;
	//SimpleIntervalBroadcastComponent mySBC;
	//BulkSendFilterComponent myBSFC;


	public NicheNode(NicheManagementInterface niche, DKSRef myRef, DKSParameters dksParameters, String webcacheAddress, SimpleResourceManager rm, ArrayList<NicheServlet> testServlets) {

		super(myRef, dksParameters, webcacheAddress, rm, testServlets);

//		ExecutorService executor = Executors.newCachedThreadPool(); //.newFixedThreadPool(NicheOSSupport.MAX_CONCURRENT_OPERATIONS);
		//.newCachedThreadPool(); //
		/*
		 * Here you can add all the components that you implemented. The only
		 * fact of extending the Class Component gives you the capability to
		 * register for events and then receive them.
		 */
		
		communicationComponent = new NicheCommunicatingComponent(niche, scheduler, registry, executor);
		
		serviceComponent = new NicheOverlayServiceComponent(niche, communicationComponent, scheduler, registry, executor, myRef);
				
		container = new NicheManagementContainerComponent(niche, scheduler, registry, executor, myRef);
		//myBSFC = new BulkSendFilterComponent(scheduler, registry, receiver);
		
		
	}
	
	
	
	
	

//	public BindElement asynchronousBind(NicheId bindId, ComponentId callerSideCId, ArrayList<ComponentId> receiverSideCIds, Object callerSideDescription, Object receiverSideDescription, int type, String receiverHandlerId, Object localHandlerObject, String localHandlerMethod)  {
//		return serviceComponent.asynchronousBind(bindId, callerSideCId, receiverSideCIds, callerSideDescription, receiverSideDescription, type, receiverHandlerId, localHandlerObject, localHandlerMethod);
//	}
	
//	public void asynchronousSend(BigInteger integer, Object requestMessage, Object handlerObject, String methodName) {
//		communicationComponent.asynchronousSend(integer, requestMessage, handlerObject, methodName);
//		
//	}

	
	public void send(Object localBindId, Object message) {
		serviceComponent.send(localBindId, message); 
	}
	
	public void send(Object localBindId, Object message, ComponentId id) {
		serviceComponent.send(localBindId, message, id);		
	}

	





}
	
