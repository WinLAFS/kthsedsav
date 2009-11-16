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
import java.util.Map;

import org.objectweb.fractal.api.type.ComponentType;

import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.ids.NicheId;

/**
 * The <code>ManagementDeployParameters</code> class. An instance of the class
 * is needed as parameter for the call to <code>deploy</code> provided by
 * <code>DCMS</code>.
 * 
 * To deploy management elements, the element specific methods
 * <code>describe...</code> should be used.
 * 
 * @author Ahmad Al-Shishtawy
 * @version $Id: ManagementDeployParameters.java 294 2006-05-05 17:14:14Z
 *          alshishtawy $
 */
public class ManagementDeployParameters implements Serializable {

	private static final long serialVersionUID = 2749411159117271343L;

//	public final static int TYPE_SENSOR = 0;
//
//	public final static int TYPE_WATCHER = 1;
//
//	public final static int TYPE_AGGREGATOR = 2;
//
//	public final static int TYPE_MANAGER = 3;
//
//	public final static int TYPE_BINDING = 4;

	public final static int TYPE_BULK = 42;

	private ArrayList<String[]> deploy;

	private ArrayList<Map> context;
	
	private ArrayList<ComponentType> types;

	private ArrayList<String[]> bind;

	private ArrayList<String> start;

	private ArrayList<String> stop;

	private ArrayList<String> attributes;

	private ArrayList<String> attributesClass;

	private ArrayList<Map> attributesMap;

	private int type;

	private boolean reliable;
	private boolean movable;
	

	private Serializable[] reInitParameters;
	private int startMode;
	

	/**
	 * Standard empty constructor
	 */
	public ManagementDeployParameters() {
		deploy = new ArrayList<String[]>();
		context = new ArrayList<Map>();
		types = new ArrayList<ComponentType>();
		bind = new ArrayList<String[]>();
		start = new ArrayList<String>();
		stop = new ArrayList<String>();
		attributes = new ArrayList<String>();
		attributesClass = new ArrayList<String>();
		attributesMap = new ArrayList<Map>();
		

	}

	/**
	 * Describes an aggregator to be deployed
	 * 
	 * @param className
	 *            The class name of the java class file implementing the
	 *            management element
	 * @param componentName
	 *            The new component name. If null then the name from the ADL
	 *            will be used
	 * @param initialArguments
	 *            An array of initial arguments to be passed to the management
	 *            element init method
	 */
	public void describeAggregator(String className, String componentName, ComponentType componentType, 
			Serializable[] initialArguments) {
		
		deployComponent(
				className,
				componentName,
				componentType,
				null,
				NicheId.TYPE_AGGREGATOR,
				initialArguments,
				false, //reliable = false
				true,  //moveable = true
				true,  //start on deploy = true
				null
			); // default is to start the
		// manager on deploy
	}

	/**
	 * Describes a manager to be deployed
	 * 
	 * @param className
	 *            The class name of the java class file implementing the
	 *            management element
	 * @param componentName
	 *            The new component name. If null then the name from the ADL
	 *            will be used
	 * @param initialArguments
	 *            An array of initial arguments to be passed to the management
	 *            element init method
	 */
	public void describeManager(String className, String componentName, ComponentType componentType, 
			Serializable[] initialArguments) {
		
		deployComponent(
				className,
				componentName,
				componentType,
				null,
				NicheId.TYPE_MANAGER,
				initialArguments,
				false, //reliable = false
				true,
				true,
				null
			); // default is to start the
		// manager on deploy
	}

	/**
	 * Describes a watcher to be deployed
	 * 
	 * @param className
	 *            The class name of the java class file implementing the
	 *            management element
	 * @param componentName
	 *            The new component name. If null then the name from the ADL
	 *            will be used
	 * @param initialArguments
	 *            An array of initial arguments to be passed to the management
	 *            element init method
	 * @param watchedComponentId
	 *            The new id of the component, or group, with which the watcher
	 *            is associated
	 * 
	 */
	public void describeWatcher(String className, String componentName, ComponentType componentType, 
			Serializable[] initialArguments, NicheId watchedComponentId) {
		
		deployComponent(className,
				componentName,
				componentType,
				null,
				NicheId.TYPE_WATCHER,
				initialArguments,
				false, //reliable = false
				true,
				true,
				watchedComponentId
			);
		// default is to start the manager on deploy
	}

	
public void describeExecutor(String className, String componentName, ComponentType componentType, 
			Serializable[] initialArguments, NicheId actuatedComponentId) {
		
		deployComponent(className,
				componentName,
				componentType,
				null,
				NicheId.TYPE_EXECUTOR,
				initialArguments,
				false, //reliable = false
				true,
				true,
				actuatedComponentId
			);
		// default is to start the manager on deploy
	}
	
	
	/**
	 * Describes a sensor to be deployed. This deployment can only be done by
	 * the responsible watcher
	 * 
	 * @param className
	 *            The class name of the java class file implementing the sensor
	 * @param componentName
	 *            The new component name.
	 * @param initialArguments
	 *            An array of initial arguments to be passed to the sensor init
	 *            method
	 * 
	 */
	public void describeSensor(String className, String componentName,
			Serializable[] initialArguments) {
		
		deployComponent(
				className,
				componentName,
				null,
				null,
				NicheId.TYPE_SENSOR,
				initialArguments,
				false, //reliable = false
				false,
				true,
				null
			);
		// default is to start the component on deploy
	}

	/**
	 * @param ADL
	 *            The ADL file name containing the component description
	 * @param componentName
	 *            The new component name. If null then the name in the ADL will
	 *            be used
	 * @param context
	 *            used for example to set attribute=value
	 */
	public void deployComponent(String ADL, String componentName, ComponentType componentType, Map context) {
		
		String[] d = { ADL, componentName };
		
		this.deploy.add(d);
		this.types.add(componentType);
		this.context.add(context);
	}

	/**
	 * @param ADL
	 *            The ADL file name containing the component description
	 * @param componentName
	 *            The new component name. If null then the name in the ADL will
	 *            be used
	 * @param context
	 *            used for example to set attribute=value
	 * @param type
	 *            used for the framework to automatically bind the management
	 *            element depending on type
	 */
	public void deployComponent(String ADL, String managementElementName, ComponentType componentType,
			Map context, int type, Serializable[] initialArguments, boolean reliable, boolean movable, boolean start,
			NicheId managedComponentId) {

		deployComponent(ADL, managementElementName, componentType, context);

		this.reliable = reliable;
		
		
		Map attr;

			deployComponent(
					FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_ADL_NAME,
					FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_NAME,
					null,
					null
				);

			attr = new HashMap<String, Object>();
			attr.put("InitialParameters", initialArguments); // FIXME
			if(managedComponentId != null) {
				if(type == NicheId.TYPE_WATCHER) {
					attr.put("watchedComponentId", managedComponentId);
				} else if (type == NicheId.TYPE_EXECUTOR) {
					attr.put("actuatedComponentId", managedComponentId);
				}
			}
			
			// attr.put("InitialArguments", initialArguments);
			setAttributes(
					FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_NAME,
					"dks.niche.fractal.interfaces.ManagementElementAttributeController",
					attr);

			bind.add(new String[] { FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_NAME,
					FractalInterfaceNames.EVENT_HANDLER_CLIENT_INTERFACE,
					managementElementName,
					FractalInterfaceNames.EVENT_HANDLER_SERVER_INTERFACE });
			
			bind.add(new String[] { FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_NAME,
					FractalInterfaceNames.INIT_CLIENT_INTERFACE,
					managementElementName,
					FractalInterfaceNames.INIT_SERVER_INTERFACE });
			
			bind.add(new String[] { FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_NAME,
					FractalInterfaceNames.CONTROLLER_CLIENT_INTERFACE,
					managementElementName,
					FractalInterfaceNames.CONTROLLER_SERVER_INTERFACE });

			bind.add(new String[] {					
					managementElementName,					
					FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE,
					FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_NAME,
					FractalInterfaceNames.TRIGGER_SERVER_INTERFACE
					}
			);
			
			bind.add(new String[] { 
					managementElementName,
					FractalInterfaceNames.DEPLOY_SENSOR_CLIENT_INTERFACE,
					FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_NAME,
					FractalInterfaceNames.DEPLOY_SENSOR_SERVER_INTERFACE
				}
			);
			
			bind.add(new String[] { 
					managementElementName,
					FractalInterfaceNames.DEPLOY_ACTUATOR_CLIENT_INTERFACE,
					FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_NAME,
					FractalInterfaceNames.DEPLOY_ACTUATOR_SERVER_INTERFACE
				}
			);

			
			if(movable) {
				bind(FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_NAME,
						FractalInterfaceNames.MOVABLE_CLIENT_INTERFACE,
						managementElementName,
						FractalInterfaceNames.MOVABLE_SERVER_INTERFACE);
			}
	
			if (start) {
				this.start.add(managementElementName);
				this.start.add(FractalInterfaceNames.MANAGEMENT_ELEMENT_PROXY_NAME);
			}
		
	}

	/**
	 * Allows the user to specify local bindings which should be initiated at
	 * component deploy time
	 * 
	 * @param clientComponentName
	 *            Local component ADL name
	 * @param clientInterfaceName
	 *            Local component client interface name
	 * @param serverComponentName
	 *            Local component ADL name
	 * @param serverInterfaceName
	 *            Local component server interface name
	 */
	public void bind(String clientComponentName, String clientInterfaceName,
			String serverComponentName, String serverInterfaceName) {
		String[] b = { clientComponentName, clientInterfaceName,
				serverComponentName, serverInterfaceName };
		bind.add(b);
	}

	/**
	 * The method can be used together with the 'deploy' settings to specify
	 * whether the component should be started directly after deployment. It can
	 * also be used on its own to remotly start an already deployed component
	 * 
	 * @param componentName
	 *            The component name you want to start or stop
	 * @param start
	 *            true to start it & false to stop it
	 */
	public void lifeCycle(String componentName, boolean start) {
		if (start) {
			this.start.add(componentName);
		} else {
			this.stop.add(componentName);
		}
	}

	/**
	 * Method used to specify initial attribute values of component attributes.
	 * Requires the component to implement corresponding attribute controller.
	 * 
	 * @param componentName
	 *            Component ADL name
	 * @param controllerName
	 *            Classname of component attribute controller
	 * @param attributes
	 *            A map specifying <attribute, value> pairs
	 */
	public void setAttributes(String componentName, String controllerName,
			Map attributes) {
		this.attributes.add(componentName);
		this.attributesClass.add(controllerName);
		this.attributesMap.add(attributes);
	}

	/**
	 * *Future work*
	 * 
	 * Specifies whether the new component should be reliable
	 */
	public void setReliable(boolean reliable) {
		this.reliable = reliable;
	}

	/**
	 * *Future work*
	 * 
	 * @return Tells whether the new component is declared to be reliable
	 */
	public boolean keepAlive() {
		return reliable;
	}

	/*
	 * Getters and setters
	 */

	public ArrayList<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<String> attributes) {
		this.attributes = attributes;
	}

	public ArrayList<String> getAttributesClass() {
		return attributesClass;
	}

	public void setAttributesClass(ArrayList<String> attributesClass) {
		this.attributesClass = attributesClass;
	}

	public ArrayList<Map> getAttributesMap() {
		return attributesMap;
	}

	public void setAttributesMap(ArrayList<Map> attributesMap) {
		this.attributesMap = attributesMap;
	}

	public ArrayList<String[]> getBind() {
		return bind;
	}

	public void setBind(ArrayList<String[]> bind) {
		this.bind = bind;
	}

	public ArrayList<Map> getContext() {
		return context;
	}

	public void setContext(ArrayList<Map> context) {
		this.context = context;
	}

	public ArrayList<String[]> getDeploy() {
		return deploy;
	}

	public void setDeploy(ArrayList<String[]> deploy) {
		this.deploy = deploy;
	}

	public ArrayList<ComponentType> getTypes() {
		return types;
	}
	public ArrayList<String> getStart() {
		return start;
	}

	public void setStart(ArrayList<String> start) {
		this.start = start;
	}

	public ArrayList<String> getStop() {
		return stop;
	}

	public void setStop(ArrayList<String> stop) {
		this.stop = stop;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isReliable() {
		return reliable;
	}
	
	public void setReInitParameters(Serializable[] param) {
		this.reInitParameters = param;
	}
	public Serializable[] getReInitParameters() {
		return reInitParameters;		
	}

	/**
	 * @return Returns the startMode.
	 */
	public int getStartMode() {
		return startMode;
	}

	/**
	 * @param startMode The startMode to set.
	 */
	public void setStartMode(int startMode) {
		this.startMode = startMode;
	}
	
}
