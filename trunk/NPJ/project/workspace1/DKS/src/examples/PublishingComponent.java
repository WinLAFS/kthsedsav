///*
// * Distributed K-ary System (DKS)
// * A Peer-to-Peer Middleware
// * Copyright (c) 2003-2007, all rights reserved 
// * 		Royal Institute of Technology (KTH)
// * 		Swedish Institute of Computer Science (SICS)
// * 
// * See the file DKSLICENSE.TXT included in this distribution for details.
// */
//package examples;
//
//import java.util.List;
//import java.util.Properties;
//
//import dks.arch.Component;
//import dks.arch.ComponentRegistry;
//import dks.arch.Scheduler;
//import dks.web.Parameter;
//
///**
// * The <code>TestComponent</code> class
// * 
// * @author Roberto Roverso
// * @author Cosmin Arad
// * @version $Id: TestComponent.java 294 2006-05-05 17:14:14Z roberto $
// */
//public class PublishingComponent extends Component {
//
//	public PublishingComponent(Scheduler scheduler, ComponentRegistry registry) {
//		super(scheduler, registry);
//
//		/**
//		 * ------------Registering for a WebServer request-----------
//		 * 
//		 * The method "handlerWebTestRequest" is called when a browser connects
//		 * to the address:
//		 * 
//		 * http://IP:port/test/testme
//		 * 
//		 */
//		registry.getWebServerComponent().registerForWebPublishRequest("testme",
//				this, "handlerWebPublishRequest");
//
//	}
//
//	@Override
//	protected void registerForEvents() {
//
//	}
//
//	public Properties handlerWebPublishRequest(List<Parameter> params) {
//
//		/**
//		 * Some parameters can be specified in the GET request to the WebServer
//		 * as:
//		 * 
//		 * http://IP:port/test/testme?par1=val1&par2=va1&par3=val1
//		 * 
//		 * The passed parameters are wrapped in a Parameter object which
//		 * contains a Key and a Value attribute.
//		 * 
//		 * and they can be processed in the following way
//		 */
//
//		/**
//		 * The WebServer component is able to export information esclusively in
//		 * the Java Properties format
//		 */
//		Properties props = new Properties();
//
//		for (Parameter parameter : params) {
//
//			/**
//			 * This if will be executed if one of the parameters passed was
//			 * 
//			 * doYou=work
//			 * 
//			 */
//			if (parameter.getKey().equalsIgnoreCase("doYou")
//					&& parameter.getValue().equalsIgnoreCase("work")) {
//
//				props.setProperty("Iwork", "fine");
//
//			}
//
//		}
//
//		return props;
//	}
//
//}
