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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.ManagementElementAttributeController;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.WatcherInterface;
import dks.niche.messages.DelegationRequestMessage;

/**
 * The <code>ContainerWatcher</code> class
 *
 * @author Joel
 * @version $Id: ContainerWatcher.java 294 2006-05-05 17:14:14Z joel $
 */
public class WatcherInfo implements WatcherInterface, Serializable {

	NicheId id;
	String sensorClassName;
	Serializable[]sensorParameters;
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -6379652154106805259L;
	
	public WatcherInfo() {
		
	}
	public WatcherInfo(NicheId id, String sensorClassName, Serializable[]sensorParameters ) { //all we care about here!
		this.id = id;
		this.sensorClassName = sensorClassName;
		this.sensorParameters = sensorParameters;
	}

	public NicheId getId() {
		return id;
	}

//	public enum SensorParameters {
//		watchedComponentsId,	
//		SensorClassName,
//		SensorEventClassName,
//		SensorCollocation,
//		ClientInterfaces,
//		ServerInterfaces,
//		SensorParameters,
//		Id
//	}	
	
	/* (non-Javadoc)
	 * @see dks.niche.interfaces.WatcherInterface#getSensorMessage(dks.niche.wrappers.NicheId, dks.niche.wrappers.ComponentId)
	 */
	public DelegationRequestMessage getSensorMessage(NicheId idOfSensor, ComponentId cid) {
		return getSensorMessage(idOfSensor, cid, cid.getComponentName());
	}
	public DelegationRequestMessage getSensorMessage(NicheId idOfSensor, ComponentId cid, String sensedComponentName) {
		
		//public DelegationRequestMessage(NicheId destination, int type, String meClassName, Object[] infrastructureParameters, Object[] applicationParameters)
		
		// if it is a fractal sensor
		//if(sensorParameters.length>=6 && sensorParameters[5] instanceof String && ((String)sensorParameters[5]).equals("FractalSensor") ) {
			ManagementDeployParameters params = new ManagementDeployParameters();
			//[0] is unused...
			String sensorName = (String)sensorParameters[1];
			
			//Object[] initialArguments = sensorParameters[6];
			params.describeSensor(sensorName, sensorName, (Serializable[])sensorParameters[6]);
			
			//ClientInterfaces
			if(sensorParameters[4] != null) {
				String[] clientInterfaces = (String[])sensorParameters[4];
				for (int i = 0; i < clientInterfaces.length; i++) {
					params.bind(sensorName, clientInterfaces[i],sensedComponentName, clientInterfaces[i]);
				}
			}
			
			//ServerInterfaces
			if(sensorParameters[5] != null) {
				String[] serverInterfaces = (String[])sensorParameters[5];
				for (int i = 0; i < serverInterfaces.length; i++) {
					params.bind(sensedComponentName, serverInterfaces[i],sensorName , serverInterfaces[i]);
				}
			}	
			
			HashMap<String, Object> m = new HashMap<String, Object>();
			m.put(ComponentId.class.getSimpleName(), cid);
		
			params.setAttributes(FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_NAME, ManagementElementAttributeController.class.getName(), m);
			
			ArrayList<Subscription> ta = new ArrayList<Subscription>();
			
			ta.add(
					new Subscription(
							cid.getId(),
							id,
							(String)sensorParameters[2] //SensorEventClassName
					)
			);
			
			
			
			//System.out.println("The sensorEventClassName is " + sensorParameters[2]);
			
			params.setReInitParameters(
					new Serializable[] {
						sensorParameters[6],
						new HashMap<String, Subscription>(),
						ta,
						cid
					}
			);
					/*
					 * Should be this:
					 
					applicationParameters,
					systemSinks = (HashMap<String, Subscription>) reInitParams[1];
					userSinks = (ArrayList<Subscription>) reInitParams[2];
					watchedComponentsId

					*/ 
			
			//TODO - investigate replication of sensors!
			return new DelegationRequestMessage(
							idOfSensor.setType(NicheId.TYPE_SENSOR),
							DelegationRequestMessage.TYPE_FRACTAL_MANAGER,
							params
						);	
		//}
		//return new DelegationRequestMessage(idOfSensor, DelegationRequestMessage.TYPE_SENSOR, sensorClassName, new Object[]{ new Subscription(idOfSensor, this, (String) sensorParameters[1]), cid}, sensorParameters);
		
	}


	
//	public DelegationRequestMessage getSensorMessage(NicheId idOfSensor, ComponentId cid) {
//		return new DelegationRequestMessage(idOfSensor, DelegationRequestMessage.TYPE_SENSOR, sensorClassName, new Object[]{ new Subscription(idOfSensor, this, (String) sensorParameters[1]), cid}, sensorParameters);
//	}

}
