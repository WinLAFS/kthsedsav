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

package org.objectweb.jasmine.jade.service.nodediscovery.analyser;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.NamingException;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.allocator.Allocator;
import org.objectweb.jasmine.jade.service.jms.JMSController;
import org.objectweb.jasmine.jade.service.nodediscovery.NodeDiscovery;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;
import org.objectweb.joram.client.jms.admin.AdminException;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class NodeDiscoveryAnalyserImpl implements NodeDiscoveryAnalyser,
        NodeDiscovery, LifeCycleController, BindingController, Runnable {

    private boolean started = false;

    private Map nodeList = null;

    private Thread thread;

    private final static int ANALYSE_TIMEOUT = 15;

    private final String[] bindingList = { "jms", "registry" };

    private JMSController jms = null;

    private NamingService registry = null;

    private Topic addedNodeTopic = null;

    private Topic failedNodeTopic = null;

    private TopicConnection connexion = null;

    private TopicSession session = null;

    private TopicPublisher addedNodePublisher = null;

    private TopicPublisher failedNodePublisher = null;
    
    private Component myself;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public NodeDiscoveryAnalyserImpl() {
        nodeList = new HashMap();
    }

    // ------------------------------------------------------------------------
    // Implementation of LifeCycleController interface
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
            thread = new Thread(this, "NodeDiscoveryAnalyser");
            // thread.setDaemon(true);
            thread.start();
            // started = true;
        } else
            throw new IllegalLifeCycleException(
                    "Component NodeDiscoverynalyser already started");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
     */
    public void stopFc() throws IllegalLifeCycleException {
        if (started) {
            thread.interrupt();
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
        if (itfName.equals("jms"))
            return jms;
        if (itfName.equals("registry"))
            return registry;
        if (itfName.equals("component"))
            return myself;
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
        if (started)
            throw new IllegalLifeCycleException("Component Started");
        if (itfName.equals("jms"))
            jms = (JMSController) itfValue;
        else if (itfName.equals("registry"))
            registry = (NamingService) itfValue;
        else if (itfName.equals("component"))
			myself = (Component) itfValue;
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
        if (started)
            throw new IllegalLifeCycleException("Component Started");
        if (itfName.equals("jms"))
            jms = null;
        else if (itfName.equals("registry"))
            registry = null;
        else if (itfName.equals("component"))
            myself = null;
        else
            throw new NoSuchInterfaceException(itfName);
    }

    // -----------------------------------------------------------------------
    // Implementation of Analyser interface
    // -----------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.deployment.nodediscovery.analyser.Analyser#analyse()
     */
    @SuppressWarnings("unchecked")
    public void analyse(String nodeName) {

        if (started) {

            // if(((NodeState) nodeList.get(nodeName)).nodeState ==
            // NodeState.NODE_FAILED){
            if (nodeList.containsKey(nodeName)) {
                ((NodeState) nodeList.get(nodeName)).lastHeartBeat = System
                        .currentTimeMillis();
            } else {
                /*
                 * update nodeList
                 */
                nodeList.put(nodeName, new NodeState());

                /*
                 * send message to topic
                 */
                try {
                    TextMessage msg = session.createTextMessage();
                    msg.setText(nodeName);
                    msg.setJMSExpiration(0);
                    addedNodePublisher.publish(msg);
                    session.commit();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                
                // Direct notification (not through JMS)
            	Component jadenode=null;
            	Component allocator = null;
                try {
        			jadenode = FractalUtil.getFirstFoundSuperComponentByName(myself,"managed_resources");
        			allocator = FractalUtil.getSubComponentByPath(jadenode,"allocator");
        		} catch (NoSuchComponentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		try {
					((Allocator) allocator.getFcInterface("allocator")).newNodeDetected(nodeName);
				} catch (NoSuchInterfaceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	
            }
            
        
    
        }
    }
    

    // -----------------------------------------------------------------------
    // Implementation of NodeDiscovery interface
    // -----------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.deployment.nodediscovery.NodeDiscovery#getNodeList()
     */
    public Set getNodeList() {
        return nodeList.keySet();
    }

    // ------------------------------------------------------------------------
    // Implementation of Runnable interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        /*
         * Request JmsWrapper to create topics AddedNode & FailedNode &
         * initialize jms stuff
         */
        try {
            addedNodeTopic = jms.createTopic("AddedNodeTopic");
            failedNodeTopic = jms.createTopic("FailedNodeTopic");
            connexion = (TopicConnection) jms.getTopicConnection();
            session = connexion.createTopicSession(true,
                    Session.AUTO_ACKNOWLEDGE);
            addedNodePublisher = session.createPublisher(addedNodeTopic);
            failedNodePublisher = session.createPublisher(failedNodeTopic);

            /*
             * FIXME wait until the allocator subscribes the addedNodeTopic ok
             * for now ... but ... when an other service will subscribe to
             * addedNodeTopic, if it subscribes before allocator service ...
             * fuck, fuck, fuck !!!
             */
            /*
             * resolve avec attente passive (notification) plutot qu'attente
             * active.
             */
            org.objectweb.joram.client.jms.Topic ant = (org.objectweb.joram.client.jms.Topic) addedNodeTopic;
            while (ant.getSubscriptions() == 0)
                ;

            /*
             * FIXME manage exceptions
             */
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (AdminException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        started = true;

        /*
         * FIXME notify that jadeboot is a node as the other. useful ? here ?
         */
        // analyse("jadeboot");
        /*
         * Every A
 seconds, check for failed node
         */

        List<String> nodeToRemove = new ArrayList<String>();

        while (started && !thread.isInterrupted()) {
            long currentTime = System.currentTimeMillis();

            Iterator it = nodeList.keySet().iterator();
            String nodeName = null;

            while (it.hasNext()) {
                nodeName = (String) it.next();
                if (!nodeName.equals("jadeboot")) {
                    NodeState nodeState = (NodeState) nodeList.get(nodeName);
                    if (nodeState.nodeState == NodeState.NODE_OK
                            && (currentTime - nodeState.lastHeartBeat) > (ANALYSE_TIMEOUT * 1000)) {

                        /*
                         * FIXME update nodeList : which behaviour ?
                         */

                        // ((NodeState) nodeList.get(nodeName)).nodeState =
                        // NodeState.NODE_FAILED;
                        /*
                         * stock the nodes to remove later to avoid a
                         * ConcurrentModificationException
                         */
                        nodeToRemove.add(nodeName);

                        /*
                         * send message to topic
                         */
                        try {
                            TextMessage msg = session.createTextMessage();
                            msg.setText(nodeName);
                            failedNodePublisher.publish(msg);
                            session.commit();
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }
                        /*
                         * remove reference to node and node factory in Fractal
                         * RMI registry
                         */
                        /*
                         * DEBUG begin 11 sept. 06 jlegrand commented for
                         * demonstration
                         */
                        // registry.unbind(nodeName);
                        // String string = "_factory";
                        // registry.unbind(nodeName + string);
                        /*
                         * end
                         */

                    }
                }
            }

            for (int i = 0; i < nodeToRemove.size(); i++)
                nodeList.remove(nodeToRemove.get(i));

            nodeToRemove.clear();

            /*
             * Sleep during ANALYSE_TIMEOUT
             */
            try {
                Thread.sleep(ANALYSE_TIMEOUT * 1000);
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
        }
    }
}
