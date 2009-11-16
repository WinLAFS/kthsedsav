package org.objectweb.jasmine.jade.service.cleaner;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.NamingException;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.jasmine.jade.service.jms.JMSController;

public class RegistryCleanerImpl implements LifeCycleController,
		BindingController {

	/**
	 * 
	 */
	private boolean started = false;

	/**
	 * 
	 */
	private NamingService registry;

	/**
	 * 
	 */
	private JMSController jms;

	/**
	 * 
	 */
	private Context jmsJndi;

	/**
	 * 
	 */
	private TopicConnection topicConnection = null;

	/**
	 * 
	 */
	private TopicSession topicSession = null;

	/**
	 * 
	 */
	private final static String FAILED_NODE_TOPIC_NAME = "FailedNodeTopic";

	// ------------------------------------------------------------------------
	// Implementation of LifecycleController interface
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.LifeCycleController#getFcState()
	 */
	public String getFcState() {
		if (started)
			return LifeCycleController.STARTED;
		return LifeCycleController.STOPPED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.LifeCycleController#startFc()
	 */
	public void startFc() throws IllegalLifeCycleException {

		try {
			subscribeJmsTopic();
		} catch (JMSException e) {
			throw new IllegalLifeCycleException(
					"The registry cleaner can't subscribe to Joram topics");
		}

		started = true;

		System.out.println("[Registry cleaner] started");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
	 */
	public void stopFc() throws IllegalLifeCycleException {

		/*
		 * FIXME : need to unsubscribe to topic ?
		 */
		unsubscribeJmstopic();

		started = false;

		System.out.println("[Registry cleaner] stopped");
	}

	// ------------------------------------------------------------------------
	// Implementation of BindingController interface
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String,
	 *      java.lang.Object)
	 */
	public void bindFc(String clientItfName, Object serverItf)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if (started)
			throw new IllegalLifeCycleException("Component Started");
		if (clientItfName.equals("registry"))
			registry = (NamingService) serverItf;
		else if (clientItfName.equals("jms"))
			jms = (JMSController) serverItf;
		else if (clientItfName.equals("jmsJndi"))
			jmsJndi = (Context) serverItf;
		else
			throw new NoSuchInterfaceException(clientItfName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#listFc()
	 */
	public String[] listFc() {
		return new String[] { "registry", "jms", "jmsJndi" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
	 */
	public Object lookupFc(String clientItfName)
			throws NoSuchInterfaceException {
		if (clientItfName.equals("registry"))
			return registry;
		if (clientItfName.equals("jms"))
			return jms;
		if (clientItfName.equals("jmsJndi"))
			return jmsJndi;
		else
			throw new NoSuchInterfaceException(clientItfName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
	 */
	public void unbindFc(String clientItfName) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if (started)
			throw new IllegalLifeCycleException("Component Started");
		if (clientItfName.equals("registry"))
			registry = null;
		else if (clientItfName.equals("jms"))
			jms = null;
		else if (clientItfName.equals("jmsJndi"))
			jmsJndi = null;
		else
			throw new NoSuchInterfaceException(clientItfName);
	}

	// ------------------------------------------------------------------------
	// Private Methods
	// ------------------------------------------------------------------------

	/**
	 * Subscribe to topics which receive messages concerning the arrival and the
	 * failure of nodes on Jade platform
	 * 
	 * @throws JMSException
	 */
	private void subscribeJmsTopic() throws JMSException {

		Topic failedNodeTopic = null;
		boolean lookup = false;

		// int nbTries = 150;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
		// while (!lookup && nbTries > 0) {
		while (!lookup) {
			try {
				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); 
                
				failedNodeTopic = (Topic) jmsJndi
						.lookup(FAILED_NODE_TOPIC_NAME);
				lookup = true;
			} catch (NamingException ignored) {
				// nbTries--;
			}
		}
		
		Thread.currentThread().setContextClassLoader(cl);
        

		// if (nbTries != 0) {
		topicConnection = (TopicConnection) jms.getTopicConnection();
		topicSession = topicConnection.createTopicSession(true,
				Session.AUTO_ACKNOWLEDGE);
		TopicSubscriber failedNodeSubscriber = topicSession
				.createSubscriber(failedNodeTopic);

		failedNodeSubscriber.setMessageListener(new FailedNodeMsgListener());

		topicConnection.start();

		// }
	}

	/**
	 * Unsubscribe to topics which receive messages concerning the arrival and
	 * the failure of nodes on Jade platform
	 */
	private void unsubscribeJmstopic() {
		try {
			topicSession.unsubscribe(FAILED_NODE_TOPIC_NAME);
			topicConnection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	// ------------------------------------------------------------------------
	// 
	// ------------------------------------------------------------------------

	/**
	 * Message listener for failedNode JMS message
	 * 
	 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
	 * 
	 */
	class FailedNodeMsgListener implements MessageListener {

		@SuppressWarnings("unchecked")
		public void onMessage(Message msg) {
			if (msg instanceof TextMessage) {
				try {
					String failedNode = ((TextMessage) msg).getText();

//					System.out
//							.println("[Registry cleaner] failed JadeNode detected : "
//									+ failedNode);

					this.clean(failedNode);

				} catch (JMSException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void clean(String failedNode) throws IllegalContentException,
				IllegalLifeCycleException, NoSuchInterfaceException {

			//System.out.println("[Registry cleaner] starting ...");

			/*
			 * get failedNode component
			 */
//			Component failedNodeComponent = registry.lookup(failedNode);

			/*
			 * clean Jade platform component
			 */
//			Component jadePlatform = registry.lookup(JadeProperties
//					.getInstance().getJadePlatformName());
//
//			ContentController cc = Fractal.getContentController(jadePlatform);
//
//			for (Component subCmp : cc.getFcSubComponents()) {
//				if (failedNode.equals(Fractal.getNameController(subCmp)
//						.getFcName())) {
//				}
//			}

			/*
			 * clean registry
			 */
			registry.unbind(failedNode);

			//System.out.println("[Registry cleaner] done ");
		}
	}

}
