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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.adl.FactoryFactory;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.messages.DelegationRequestMessage;

/**
 * The <code>DeployMEClass</code> class
 * 
 * @author joel
 * @version $Id: DeployMEClass.java 294 2006-05-05 17:14:14Z joel $
 */
public class DeployMEClass implements Runnable {

	DelegationRequestMessage message;

	// NicheManagementContainerComponent env;
	static Object mySyncVariable = new Object();

	NicheManagementInterface myNicheManagementInterface;

	NicheAsynchronousInterface logger;

	SimpleResourceManager rm;

	int flag;
	
	public DeployMEClass(DelegationRequestMessage message,
			int flag,
			NicheManagementInterface nicheManagementInterface) {

		this.message = message;
		this.flag = flag;
		this.myNicheManagementInterface = nicheManagementInterface;
		this.logger = myNicheManagementInterface.getNicheAsynchronousSupport();
		this.rm = logger.getResourceManager();
		// this.env = env;
	}

	public void run() {

		boolean dynamicFlag = false;
		// PLAY HERE!!!
		ManagementDeployParameters params = (ManagementDeployParameters) message
				.getManagementDeployParameters();

		/*#%*/ logger.log("Synchronized ManagementDeployerClass with deploy content: "
		/*#%*/ 		+ Arrays.deepToString(params.getDeploy().toArray()));

		// when joining nodes to an existing system, they might receive
		// delegation requests before they're properly started!
		while (!rm.isStarted()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Object deploymentLock = rm.getDeploymentLock();
		
		synchronized (deploymentLock) {
			
			/*#%*/ String logMessage;
			org.objectweb.fractal.api.Component newComponent = null;

			ArrayList<String[]> deploys = params.getDeploy();
			ArrayList<ComponentType> types = params.getTypes();
			ArrayList<Map> contexts = params.getContext();

			ContentController cc = rm.getContentController();

			for (int i = 0; i < deploys.size(); i++) {

				String[] deploy = deploys.get(i);
				ComponentType componentType = types.get(i);
				Map context = contexts.get(i);
				// //////////////////////////////////////////////////////////////////
				// //// create the new component
				// //////////////////////////////////////////////////////////////////

				// myNicheManagementInterface.getNicheAsynchronousSupport().log("creating
				// comp: " + deploy[0] + " "+ deploy[1] );

				/*#%*/ logMessage = "New Component " + deploy[0] + " named "
				/*#%*/ 		+ deploy[1] + " with id "
				/*#%*/ 		+ message.getDestination().toString();
				if (componentType != null) {

					dynamicFlag = true;

					newComponent = rm.localJavaDeploy(deploy[0], "primitive", componentType);

					/*#%*/ logMessage += " generated using type sent over the network!";
					// myNicheManagementInterface.getNicheAsynchronousSupport().trigger(e)

				} else if (dynamicFlag) {

					newComponent = rm.localJavaDeploy(deploy[0], "primitive", null);

					/*#%*/ logMessage += " generated using type from local resource management!";

				} else {
					// old

					newComponent = rm.localADLDeploy(deploy[0], context);

					/*#%*/ logMessage += " generated using locally processed ADL";
				}

				/*#%*/ logger.log(logMessage);
				//System.out.println(logMessage);

				
					// //////////////////////////////////////////////////////////////////
					// //// name it
					// //////////////////////////////////////////////////////////////////
					if (deploy[1] != null) {
						try {
							Fractal.getNameController(newComponent).setFcName(
									deploy[1]);
						} catch (NoSuchInterfaceException e1) {
							e1.printStackTrace();
						}
					}

					// //////////////////////////////////////////////////////////////////
					// //// add the new component, seems to be needed also for replicas..
					// //////////////////////////////////////////////////////////////////

					try {

						cc.addFcSubComponent(newComponent);
						
						if (message.getReplicaNumber() < 1) {
						// deploy[0] is description, deploy[1] is name
							myNicheManagementInterface.getResourceManager()
								.addManagementBindReceiver(
										message.getDestination(),
										newComponent,
										deploy[0],
										deploy[1]
								);
						}
					} catch (IllegalContentException e) {
						e.printStackTrace();
					} catch (IllegalLifeCycleException e) {
						e.printStackTrace();
					}
				}

			

			// //////////////////////////////////////////////////////////////////
			// //// bindings
			// //////////////////////////////////////////////////////////////////
			org.objectweb.fractal.api.Component clientComp = null;
			org.objectweb.fractal.api.Component serverComp = null;
			org.objectweb.fractal.api.Component subComponents[] = null;
			Hashtable<String, org.objectweb.fractal.api.Component> subCompMap = new Hashtable<String, org.objectweb.fractal.api.Component>();

			// get sub components

			try {
				subComponents = cc.getFcSubComponents();

				for (int i = 0; i < subComponents.length; i++) {
					subCompMap.put(Fractal.getNameController(subComponents[i])
							.getFcName(), subComponents[i]);
				}
			} catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}

			ArrayList<String[]> binds = params.getBind();
			
			/*#%*/ logMessage = "";
			boolean added = true;
			for (String[] bind : binds) {
				
				added = true;
				clientComp = subCompMap.get(bind[0]);
				serverComp = subCompMap.get(bind[2]);
				if (clientComp != null && serverComp != null) {
					
					try {
						Fractal.
							getBindingController(clientComp).
							bindFc(
								bind[1],
								serverComp.getFcInterface(bind[3])
							)
						;
						
					} catch (NoSuchInterfaceException e) {
						//Trigger & deploySensor - interfaces are/should be optional!
						added = false;
						
						if (bind[1].equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE)
								||
							bind[1].equals(FractalInterfaceNames.DEPLOY_SENSOR_CLIENT_INTERFACE)
								||
							bind[1].equals(FractalInterfaceNames.EVENT_HANDLER_CLIENT_INTERFACE) 
								||
							bind[1].equals(FractalInterfaceNames.DEPLOY_ACTUATOR_CLIENT_INTERFACE)
								) {	
							//IGNORE
							/*#%*/ logMessage += "No " + bind[1] +"Interface - ignored\n";
						} else {
							/*#%*/ logMessage += "A bind failure was caused by: " + bind[0] + "-"+  bind[1] + " or " + bind[2] + "-" + bind[3];
							e.printStackTrace();
						}
					} catch (IllegalBindingException e) {
						e.printStackTrace();
					} catch (IllegalLifeCycleException e) {
						e.printStackTrace();
					}
					
					/*#%*/ if(added) {
						/*#%*/ logMessage += "Has bound: " + bind[1] + " to " + bind[3]+ "\n";
					/*#%*/ }
					
				} // else {
					// System.err.println("clientComp "+ clientComp +" or
					// serverComp
					// " + serverComp + " was null" );
				// }

			}
			/*#%*/ logger.log(logMessage);
			// //////////////////////////////////////////////////////////////////
			// //// print for testing
			// //////////////////////////////////////////////////////////////////

			// for (int i = 0; i < subComponents.length; i++) {
			// try {
			// logger.log(
			// "Sub component of managed_resources: "
			// + Fractal.getNameController(subComponents[i])
			// .getFcName());
			// } catch (NoSuchInterfaceException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }

			// //////////////////////////////////////////////////////////////////
			// //// set attributes
			// //////////////////////////////////////////////////////////////////

			ArrayList<String> attributes = params.getAttributes();
			ArrayList<String> attributesClass = params.getAttributesClass();
			ArrayList<Map> attributesMap = params.getAttributesMap();

			for (int i = 0; i < attributes.size(); i++) {
				//System.out.println("Attribute is " + attributes.get(i) + " controllerclass is " + attributesClass.get(i));
				org.objectweb.fractal.api.Component c = subCompMap
						.get(attributes.get(i));
				Class controllerClass = null;
				try {
					controllerClass = Class.forName(attributesClass.get(i));
				} catch (ClassNotFoundException e2) {
					e2.printStackTrace();
				}
				Map<String, Object> m = attributesMap.get(i);

				// get attribute controller
				AttributeController ac = null;
				try {
					ac = Fractal.getAttributeController(c);
				} catch (NoSuchInterfaceException e1) {
					e1.printStackTrace();
				}
				Object nac = controllerClass.cast(ac);

				Method methods[] = controllerClass.getMethods();
				for (int j = 0; j < methods.length; j++) {
					String methodName = methods[j].getName();
					if (methodName.startsWith("set")) {
						methodName = methodName.substring(3);
						Object value[] = { m.get(methodName) };
						if (value[0] == null) {
							methodName = methodName.substring(0, 1)
									.toLowerCase()
									+ methodName.substring(1);
							value[0] = m.get(methodName);
						}

						if (value[0] != null) {

							try {
								/*#%*/ logger.log("Deployment: Setting params. methodName: "
								/*#%*/ 				+ methodName
								/*#%*/ 				+ " value: "
								/*#%*/ 				+ value);

								methods[j].invoke(nac, value);

							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
						} else { // Assume it's time to feed the
							// infrastructure
							// reference //FIXME hardhack for testing
							// System.out.println("Set id and infrastructure
							// references for: "+methodName + " ");
							Object argument = null;

							if (methodName.equalsIgnoreCase("id")) { // FIXME
								// I really don't like hardcoded strings
								argument = message.getDestination();

							} else if (methodName
									.equalsIgnoreCase("replicaNumber")) { // FIXME
								// I really don't like hardcoded strings
								argument = message.getReplicaNumber();

							} else if (methodName
									.equalsIgnoreCase(NicheManagementInterface.class
											.getSimpleName())) {
								// System.out.println("Setting
								// NicheManagementInterface");
								argument = myNicheManagementInterface;
							} else if (methodName
									.equalsIgnoreCase(ManagementDeployParameters.class
											.getSimpleName())) {
								// System.out.println("Setting the deployement
								// parameters!");
								argument = params;
							} else if (methodName.equalsIgnoreCase("StartupMode")) {
								argument = flag;
							}
							if (argument != null) {
								try {
									/*#%*/ logger.log("Deployment: Setting params. methodName: "
									/*#%*/ 				+ methodName
									/*#%*/ 				+ " value: "
									/*#%*/ 				+ argument
									/*#%*/ 				+ " argument-type: "
									/*#%*/ 				+ argument.getClass()
									/*#%*/ 						.getSimpleName());
									methods[j].invoke(nac,
											new Object[] { argument });
								} catch (IllegalArgumentException e) {
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									e.printStackTrace();
								} catch (InvocationTargetException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}

			// //////////////////////////////////////////////////////////////////
			// //// start components
			// //////////////////////////////////////////////////////////////////
			for (String compName : params.getStart()) {
				try {
					Fractal.getLifeCycleController(subCompMap.get(compName))
							.startFc();
				} catch (IllegalLifeCycleException e) {
					e.printStackTrace();
				} catch (NoSuchInterfaceException e) {
					e.printStackTrace();
				}
			}

			// //////////////////////////////////////////////////////////////////
			// //// stop components
			// //////////////////////////////////////////////////////////////////
			for (String compName : params.getStop()) {
				try {
					Fractal.getLifeCycleController(subCompMap.get(compName))
							.stopFc();
				} catch (IllegalLifeCycleException e) {
					e.printStackTrace();
				} catch (NoSuchInterfaceException e) {
					e.printStackTrace();
				}
			}

		} // end synch on RM
	} // end run

} // end class
