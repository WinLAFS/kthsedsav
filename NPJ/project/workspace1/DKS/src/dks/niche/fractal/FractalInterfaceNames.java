/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.fractal;

/**
 * The <code>FractalInterfaceNames</code> class
 *
 * @author Joel
 * @version $Id: FractalInterfaceNames.java 294 2006-05-05 17:14:14Z joel $
 */
public final class FractalInterfaceNames {

	public static final String DISTRIBUTED_CLIENT_INTERFACE_PREFIX = "SEND-";
	public static final String DISTRIBUTED_CLIENT_REPLY_INTERFACE_PREFIX = "SEND_WITH_REPLY-";
	
	public static final String DISTRIBUTED_SERVER_INTERFACE_PREFIX = "RECV-";
	
	public static String PROXY_SUFFIX = "proxy";
	
	public static String COMPONENT = "component";
	public static String INIT_CLIENT_INTERFACE = "init";
	public static String INIT_SERVER_INTERFACE = "init";
	public static String MOVABLE_CLIENT_INTERFACE = "movable";
	public static String MOVABLE_SERVER_INTERFACE = "movable";
	public static String EVENT_HANDLER_CLIENT_INTERFACE = "eventHandler";
	public static String EVENT_HANDLER_SERVER_INTERFACE = "eventHandler";
	public static String TRIGGER_CLIENT_INTERFACE = "trigger";
	public static String TRIGGER_SERVER_INTERFACE = "trigger";
	public static String ACTUATOR_CLIENT_INTERFACE = "actuator";
	public static String ACTUATOR_SERVER_INTERFACE = "actuator";
	public static String DEPLOY_SENSOR_CLIENT_INTERFACE = "deploySensor";
	public static String DEPLOY_SENSOR_SERVER_INTERFACE = "deploySensor";
	public static String DEPLOY_ACTUATOR_CLIENT_INTERFACE = "deployActuator";
	public static String DEPLOY_ACTUATOR_SERVER_INTERFACE = "deployActuator";
	
	public static String CONTROLLER_CLIENT_INTERFACE = "controller";
	public static String CONTROLLER_SERVER_INTERFACE = "binding-controller"; //????????
	
	
	public static String ID_REGISTRY = "nicheIdRegistry";
	public static String OVERLAY_ACCESS = "overlayAccess";
	
	public static String MANAGEMENT_ELEMENT_PROXY_ADL_NAME = ManagementElement.class.getName();
	public static String MANAGEMENT_ELEMENT_PROXY_NAME = ManagementElement.class.getSimpleName() + PROXY_SUFFIX;
	
}
