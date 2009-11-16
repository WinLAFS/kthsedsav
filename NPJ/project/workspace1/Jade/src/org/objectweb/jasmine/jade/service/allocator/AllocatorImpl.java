/**
 * Copyright (C) : INRIA - Domaine de Voluceau, Rocquencourt, B.P. 105, 
 * 78153 Le Chesnay Cedex - France 
 * 
 * contributor(s) : SARDES project - http://sardes.inrialpes.fr
 *
 * Contact : jade@inrialpes.fr
 *
 * This software is a computer program whose purpose is to provide a framework
 * to build autonomic systems, following an architecture-based approach.
 *
 * This software is governed by the CeCILL-C license under French law and 
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as 
 * circulated by CEA, CNRS and INRIA at the following URL 
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a 
 * limited warranty and the software's author, the holder of the economic 
 * rights, and the successive licensors have only limited liability. 
 *
 * In this respect, the user's attention is drawn to the risks associated with 
 * loading,  using,  modifying and/or developing or reproducing the software by 
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate,  and  that  also therefore means  that it is
 * reserved for developers  and  experienced professionals having in-depth 
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling 
 * the security of their systems and/or data to be ensured and,  more generally,
 * to use and operate it in the same conditions as regards security. 
 *
 * The fact that you are presently reading this means that you have had 
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package org.objectweb.jasmine.jade.service.allocator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

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

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.jms.JMSController;
import org.objectweb.jasmine.jade.util.JadeException;
import org.objectweb.jasmine.jade.util.Logger;


/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * @author <a href="mailto:noel.depalma@inrialpes.fr">Noel De Palma
 * 
 */
/**
 * @author Nikos
 *
 */
/**
 * @author Nikos
 *
 */
public class AllocatorImpl implements LifeCycleController, BindingController,
        Allocator {

    /**
     * 
     */
    private boolean started = false;

    /**
     * 
     */
    private final String[] bindingList = { "jms_jndi", "registry", "jms" };

    /**
     * 
     */
    private Component myself;

    /**
     * 
     */
    private Context jmsJndi;

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
    private Set<String> freeNodes = null;

    /**
     * 
     */
    private Set failedNodes = null;

    /**
     * 
     */
    private Map<String,Component> allocatedNodes = null;

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
    private final static String NEW_NODE_TOPIC_NAME = "AddedNodeTopic";

    /**
     * 
     */
    private final static String FAILED_NODE_TOPIC_NAME = "FailedNodeTopic";

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public AllocatorImpl() {
        // freeNodes = new HashSet();
        freeNodes = new TreeSet<String>();
        failedNodes = new HashSet();
        allocatedNodes = new HashMap<String,Component>();
    }

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
        if (!started) {
            registry.bind("allocator", myself);
            started = true;
            try {
                subscribeJmsTopic();
            } catch (JMSException e) {
                throw new IllegalLifeCycleException(
                        "The allocator can't subscribe to Joram topics");
            }
            // Add this node (jadeboot) to free nodes
            String jadebootname = null;
            try {
    			jadebootname = InetAddress.getLocalHost().getCanonicalHostName();
    		} catch (UnknownHostException e) {
    			e.printStackTrace();	
    		}
    		jadebootname += "_" + 0;
            freeNodes.add(jadebootname);
            Logger.println("[Allocator] started");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
     */
    public void stopFc() throws IllegalLifeCycleException {
        if (started) {
            /*
             * FIXME : need to unsubscribe to topic ?
             */
            unsubscribeJmstopic();
            started = false;
        }
    }

    // ------------------------------------------------------------------------
    // Implementation of BindingController interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        return bindingList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String itfName) throws NoSuchInterfaceException {
        if (itfName.equals("component"))
            return myself;
        if (itfName.equals("jms_jndi"))
            return jmsJndi;
        if (itfName.equals("registry"))
            return registry;
        if (itfName.equals("jms"))
            return jms;
        else
            throw new NoSuchInterfaceException(itfName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String,
     *      java.lang.Object)
     */
    public void bindFc(String itfName, Object itfValue)
            throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (started) {
            throw new IllegalLifeCycleException(itfName);
        }
        if (itfName.equals("component"))
            myself = (Component) itfValue;
        if (itfName.equals("jms_jndi"))
            jmsJndi = (Context) itfValue;
        else if (itfName.equals("registry"))
            registry = (NamingService) itfValue;
        else if (itfName.equals("jms"))
            jms = (JMSController) itfValue;
        else
            throw new NoSuchInterfaceException(itfName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String itfName) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (started) {
            throw new IllegalLifeCycleException(itfName);
        }
        if (itfName.equals("component"))
            myself = null;
        if (itfName.equals("jms_jndi"))
            jmsJndi = null;
        else if (itfName.equals("registry"))
            registry = null;
        else if (itfName.equals("jms"))
            jms = null;
        else
            throw new NoSuchInterfaceException(itfName);
    }

    // ------------------------------------------------------------------------
    // Implementation of Allocator interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.deployment.allocator.Allocator#alloc()
     */
    public Component alloc() throws NoNodeAvailableException {

        Component res = null;

        if (!freeNodes.isEmpty()) {
            String node = (String) freeNodes.iterator().next();
            res = (Component) registry.lookup(node);
            freeNodes.remove(node);
            allocatedNodes.put(node, res);
        } else if (!allocatedNodes.isEmpty()) {
            /*
             * TODO define an allocation policy
             */
            int random = new Random().nextInt(allocatedNodes.size());
            Iterator keys = allocatedNodes.keySet().iterator();
            for (int i = 0; i < random - 1; i++) {
                keys.next();
            }
            res = (Component) allocatedNodes.get(keys.next());
        } else {
            throw new NoNodeAvailableException("No node available to allocate");
        }

        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.allocator.Allocator#alloc(java.lang.String)
     */
    public Component alloc(String host) throws NodeNotFoundException {

        Component res = null;
        String node = null;
        boolean found = false;

        /*
         * search a JadeNode with name begining with "host" in free nodes set.
         */
        Iterator it = freeNodes.iterator();
        while (it.hasNext() && !found) {
            node = (String) it.next();
            found = node.startsWith(host);
        }
        /*
         * search succeed
         */
        if (found) {
            res = (Component) registry.lookup(node);
            freeNodes.remove(node);
            allocatedNodes.put(node, res);
        }
        /*
         * if search failed, search in allocated nodes map.
         */
        else {
            it = allocatedNodes.keySet().iterator();
            while (it.hasNext() && !found) {
                node = (String) it.next();
                found = node.startsWith(host);
            }
            /*
             * if search succeed
             */
            if (found) {
                res = (Component) allocatedNodes.get(node);
            }
            /*
             * search failed
             */
            else {
                throw new NodeNotFoundException(
                        "No JadeNode with name begining with \"" + host
                                + "\" found");
            }
        }
        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.allocator.Allocator#alloc(java.lang.String,
     *      java.lang.String)
     */
    public Component alloc(String host, String number)
            throws NodeNotFoundException {

        String node = host + "_" + number;
        Component res = null;

        /*
         * check if the JadeNode is free
         */
        if (freeNodes.contains(node)) {
            res = (Component) registry.lookup(node);
            freeNodes.remove(node);
            allocatedNodes.put(node, res);
        }
        /*
         * check if the JadeNode is allocated
         */
        else if (allocatedNodes.containsKey(node)) {
            res = (Component) allocatedNodes.get(node);
        }
        /*
         * the JadeNode doesn't exist.
         */
        else {
            throw new NodeNotFoundException("The JadeNode " + node
                    + " doesn't exist.");
        }

        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.deployment.allocator.Allocator#free(org.objectweb.fractal.api.Component)
     */
    public void free(Component c) throws JadeException {
        try {
            String node = ((NameController) (c
                    .getFcInterface("name-controller"))).getFcName();
            if (!allocatedNodes.containsKey(node)) {
                throw new JadeException(
                        "[Allocator] Unable to free the node \"" + node
                                + "\" : node not found");
            } else {
                allocatedNodes.remove(node);
                /*
                 * FIXME when freeing a node, must i add it on freeNodes list ?
                 */
                freeNodes.add(node);
            }
        } catch (NoSuchInterfaceException e) {
            throw new JadeException("[Allocator] Unable to free node : "
                    + e.getLocalizedMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.allocator.Allocator#getAllocatedComponent()
     */
    public Component[] getAllocatedComponent() {
        return (Component[]) (allocatedNodes.values().toArray(new Component[0]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.allocator.Allocator#getFreeComponent()
     */
    public Component[] getFreeComponent() {
    	
    	Component c =null;
    	String node;
    	Component[] result = new Component[freeNodes.size()];
    	int i=0;
    	
    	
    	Iterator it = freeNodes.iterator();
        while (it.hasNext()) {
            node = (String) it.next();
            c = (Component) registry.lookup((String)node);
            result[i]=c;
    		i++;
        }
    	
    	return result;
   
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.allocator.Allocator#isAllocated(org.objectweb.fractal.api.Component)
     */
    public boolean isAllocated(Component c) throws NodeNotFoundException {
        try {
            String node = Fractal.getNameController(c).getFcName();

            if (allocatedNodes.containsKey(node))
                return true;
            if (freeNodes.contains(node))
                return false;

        } catch (NoSuchInterfaceException ignored) {
        }

        throw new NodeNotFoundException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.allocator.Allocator#isAllocated(java.lang.String)
     */
    public boolean isAllocated(String name) throws NodeNotFoundException {
        if (allocatedNodes.containsKey(name))
            return true;
        if (freeNodes.contains(name))
            return false;
        throw new NodeNotFoundException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.deployment.allocator.Allocator#getAllocated()
     */
    public String[] getAllocatedComponentName() {
        return (String[]) (allocatedNodes.keySet().toArray(new String[0]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.allocator.Allocator#getFree()
     */
    public String[] getFreeComponentName() {
        return (String[]) (freeNodes.toArray(new String[0]));
    }
    
    
    // This is for direct (not through JMS) notification
    public void newNodeDetected(String newNode) {
    	//System.out.println("[Allocator] detect newNode : "+ newNode);
    	freeNodes.add(newNode);
    	if (failedNodes.contains(newNode))
    			failedNodes.remove(newNode);
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

        Topic newNodeTopic = null;
        Topic failedNodeTopic = null;
        boolean lookup = false;

        // int nbTries = 150;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
       
        // while (!lookup && nbTries > 0) {
        while (!lookup) {
            try {
            	/*ClassLoader*/ //cl = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); 
                newNodeTopic = (Topic) jmsJndi.lookup(NEW_NODE_TOPIC_NAME);
                failedNodeTopic = (Topic) jmsJndi
                        .lookup(FAILED_NODE_TOPIC_NAME);
                //Thread.currentThread().setContextClassLoader(cl);
                lookup = true;
            } catch (NamingException ignored) {
                // nbTries--;
            	//ignored.printStackTrace();
            }
        }
        Thread.currentThread().setContextClassLoader(cl);
       
        // if (nbTries != 0) {
        topicConnection = (TopicConnection) jms.getTopicConnection();
        topicSession = topicConnection.createTopicSession(true,
                Session.AUTO_ACKNOWLEDGE);
        TopicSubscriber newNodeSubscriber = topicSession
                .createSubscriber(newNodeTopic);
        TopicSubscriber failedNodeSubscriber = topicSession
                .createSubscriber(failedNodeTopic);

        newNodeSubscriber.setMessageListener(new NewNodeMsgListener());
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
            topicSession.unsubscribe(NEW_NODE_TOPIC_NAME);
            topicSession.unsubscribe(FAILED_NODE_TOPIC_NAME);
            topicConnection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    
    
    
    
    
    // ------------------------------------------------------------------------
    // Inner Classes
    // ------------------------------------------------------------------------

    /**
     * Message listener for newNode JMS message
     * 
     * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
     * 
     */
    class NewNodeMsgListener implements MessageListener {

        @SuppressWarnings("unchecked")
        public void onMessage(Message msg) {
            if (msg instanceof TextMessage) {
                try {
                    String newNode = ((TextMessage) msg).getText();
                    System.out
                            .println("[Allocator] receive newNode jmsMessage : "
                                    + newNode);
                    freeNodes.add(newNode);
                    /*
                     * FIXME mandatory ???
                     */
                    if (failedNodes.contains(newNode))
                        failedNodes.remove(newNode);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }

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
//                    System.out
//                            .println("[Allocator] receive failedNode jmsMessage : "
//                                    + failedNode);
                    failedNodes.add(failedNode);
                    if (freeNodes.contains(failedNode))
                        freeNodes.remove(failedNode);
                    /*
                     * FIXME what do we do if the failed node is an allocated
                     * node?
                     */
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
