/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.interfaces;

import java.util.ArrayList;

import dks.niche.ids.NicheId;
import dks.niche.wrappers.SensorSubscription;
import dks.niche.wrappers.Subscription;

/**
 * The <code>SensorInterface</code> class
 *
 * @author Joel
 * @version $Id: SensorInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface SensorInterface {

	public final static String NO_EVENT_SENSOR = "no event sensor";
	public void addSinks(ArrayList<SensorSubscription> ws);
	//public ArrayList<SensorSubscription> getSinks();
	public void addSink(SensorSubscription ss);
	//Silly toArray
	//public Object[] getEventNames();
	public String getEventName();
	public void eventHandler(Object e);
	//public String getTargetNodeId();
	//public NicheId getId();
	//public void connect(NicheId id, NicheManagementContainerComponent host, Object[] infrastructureParameters); 	public void init(Object[] applicationParameters);
	
}
