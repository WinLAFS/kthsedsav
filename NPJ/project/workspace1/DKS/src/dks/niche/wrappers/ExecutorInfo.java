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
import dks.niche.interfaces.ExecutorInterface;
import dks.niche.messages.DelegationRequestMessage;



/**
 * The <code>WatcherInfo</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: ExecutorInfo.java 294 2006-05-05 17:14:14Z alshishtawy $
 */

public class ExecutorInfo implements ExecutorInterface, Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 5898155679520414551L;
	
	NicheId id;
	String actuatorClassName;
	Serializable[] actuatorParameters;

	public ExecutorInfo() {
		
	}
	
	/**
	 * @param id
	 * @param actuatorClassName
	 * @param actuatorParameters
	 */
	public ExecutorInfo(NicheId id, String actuatorClassName, Serializable[]actuatorParameters ) { //all we care about here!
		this.id = id;
		this.actuatorClassName = actuatorClassName;
		this.actuatorParameters = actuatorParameters;
	}

	public NicheId getId() {
		return id;
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ExecutorInterface#getActuatorMessage(dks.niche.ids.NicheId, dks.niche.ids.ComponentId)
	 */
	@Override
	public DelegationRequestMessage getActuatorMessage(NicheId id, ComponentId cid) {
		return getActuatorMessage(id, cid, cid.getComponentName());
	}

	
	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ExecutorInterface#getActuatorMessage(dks.niche.ids.NicheId, dks.niche.ids.ComponentId, java.lang.String)
	 */
	@Override
	public DelegationRequestMessage getActuatorMessage(NicheId idOfActuator, ComponentId cid, String actuatedComponentName) {
		
		int type=0;
		
			ManagementDeployParameters params = new ManagementDeployParameters();
			//[0] is unused...
			String actuatorName = (String)actuatorParameters[1];
			
			//Serializable[] initialArguments = actuatorParameters[6];
			params.describeSensor(actuatorName, actuatorName, (Serializable[])actuatorParameters[6]);
			
			//ClientInterfaces
			if(actuatorParameters[4] != null) {
				String[] clientInterfaces = (String[])actuatorParameters[4];
				for (int i = 0; i < clientInterfaces.length; i++) {
					params.bind(actuatorName, clientInterfaces[i],actuatedComponentName, clientInterfaces[i]);
				}
			}
			
			//ServerInterfaces
			if(actuatorParameters[5] != null) {
				String[] serverInterfaces = (String[])actuatorParameters[5];
				for (int i = 0; i < serverInterfaces.length; i++) {
					params.bind(actuatedComponentName, serverInterfaces[i],actuatorName , serverInterfaces[i]);
				}
			}	
			
			HashMap<String, Object> m = new HashMap<String, Object>();
			m.put(ComponentId.class.getSimpleName(), cid);
		
			params.setAttributes(FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_NAME, ManagementElementAttributeController.class.getName(), m);
			
			ArrayList<Subscription> ta = new ArrayList<Subscription>();
			
			

//			System.err.println("Adding subscription to " + actuatorParameters[2]);
			ta.add( new Subscription(id, cid.getId(), (String)actuatorParameters[2])); //EffectorEventClassName
			
			
			//System.out.println("The sensorEventClassName is " + sensorParameters[2]);
			
			params.setReInitParameters(
				new Serializable[] {
						actuatorParameters[6],
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
							idOfActuator.setType(NicheId.TYPE_ACTUATOR),
							DelegationRequestMessage.TYPE_FRACTAL_MANAGER,
							params
						);	
	}



}
