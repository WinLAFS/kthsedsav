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

package org.objectweb.jasmine.jade.service.jms.joram;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.StringTokenizer;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.jasmine.jade.service.jms.JMSController;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.Logger;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.dyade.aaa.agent.AgentServer;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class JoramWrapperImpl implements JMSController, LifeCycleController,
        BindingController {

    /**
     * 
     */
    private boolean started = false;

    /**
     * 
     */
    private final String[] bindingList = { "registry", "jms_jndi" };

    /**
     * 
     */
    private NamingService registry = null;

    /**
     * 
     */
    private Context jmsJndi = null;

    /**
     * 
     */
    private javax.jms.TopicConnectionFactory topicConnectionFactory = null;

    /**
     * 
     */
    private javax.jms.QueueConnectionFactory queueConnectionFactory = null;

    /**
     * 
     */
    private String connectionLogin = "root";

    /**
     * 
     */
    private String connectionPasswd = "root";

    /**
     * 
     */
    private int tcpProxyPort = 12080;

    /**
     * 
     */
    private String serverHost = "localhost";

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
            getJoramParameterFromA3serverFile();
            startJoramServer();
            AdminModule.collocatedConnect(connectionLogin, connectionPasswd);
            started = true;
            Logger.println(DebugService.info, "[Joram server] started");

        } catch (Exception e) {
            throw new IllegalLifeCycleException(
                    "Error while launching joram server");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
     */
    public void stopFc() throws IllegalLifeCycleException {
        AdminModule.disconnect();
        stopJoramServer();
        started = false;
        Logger.println(DebugService.info, "[Joram server] stopped");
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
        if (itfName.equals("registry"))
            return registry;
        if (itfName.equals("jms_jndi"))
            return jmsJndi;
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
        if (started)
            throw new IllegalLifeCycleException("Component Started");
        else if (itfName.equals("registry"))
            registry = (NamingService) itfValue;
        else if (itfName.equals("jms_jndi"))
            jmsJndi = (Context) itfValue;
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
        else if (itfName.equals("registry"))
            registry = null;
        else if (itfName.equals("jms_jndi"))
            jmsJndi = null;
        else
            throw new NoSuchInterfaceException(itfName);
    }

    // ------------------------------------------------------------------------
    // Implementation of JMSController interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.deployment.jms.JoramServerAdmin#createTopic(java.lang.String)
     */
    public Topic createTopic(String name) throws ConnectException, AdminException, NamingException {

        Topic topic = Topic.create(name);
        
        /*
         * default rigths for new 
         */
        topic.setFreeReading();
        topic.setFreeWriting();

        jmsJndi.bind(name, topic);

        return topic;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.deployment.jms.JMSController#createQueue(java.lang.String)
     */
    public Queue createQueue(String name) throws ConnectException,
            AdminException, NamingException {
        Queue queue = Queue.create(name);
        /*
         * FIXME set queue rights
         */
        queue.setFreeReading();
        queue.setFreeWriting();
        jmsJndi.bind(name, queue);
        return queue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.deployment.jms.JMSController#getTopicConnection()
     */
    public Connection getTopicConnection() throws JMSException {
        if (topicConnectionFactory == null)
            /*
             * FIXME tcp or local connection
             */
            topicConnectionFactory = new TopicTcpConnectionFactory(serverHost,
                    tcpProxyPort);
        return topicConnectionFactory.createTopicConnection(connectionLogin,
                connectionPasswd);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.deployment.jms.JMSController#getQueueConnection()
     */
    public Connection getQueueConnection() throws JMSException {
        if (queueConnectionFactory == null)
            /*
             * FIXME tcp or local connection
             */
            queueConnectionFactory = new QueueTcpConnectionFactory(serverHost,
                    tcpProxyPort);
        return queueConnectionFactory.createConnection(connectionLogin,
                connectionPasswd);
    }

    // ------------------------------------------------------------------------
    // Private methods
    // ------------------------------------------------------------------------

    /**
     * 
     */
    private void startJoramServer() {
        try {
            AgentServer.init((short) 0, "./s0", null);
            AgentServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    private void stopJoramServer() {
        AgentServer.stop();
    }

    /**
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * 
     */
    private void getJoramParameterFromA3serverFile()
            throws ParserConfigurationException, SAXException, IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream("a3servers.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(is);

        NodeList nodesService = document.getElementsByTagName("service");

        for (int i = 0; i < nodesService.getLength(); i++) {

            Element nodeServer = (Element) nodesService.item(i);

            String classValue = nodeServer.getAttribute("class");
            if (classValue
                    .equals("org.objectweb.joram.mom.proxies.ConnectionManager")) {
                String argsValue = nodeServer.getAttribute("args");
                StringTokenizer tokenizer = new StringTokenizer(argsValue, " ");
                connectionLogin = tokenizer.nextToken();
                connectionPasswd = tokenizer.nextToken();
            }
            if (classValue
                    .equals("org.objectweb.joram.mom.proxies.tcp.TcpProxyService")) {
                tcpProxyPort = new Integer(nodeServer.getAttribute("args"));
            }
            // if(classValue.equals("fr.dyade.aaa.jndi2.server.JndiServer")){
            // String argsValue = nodeServer.getAttribute("args");
            // System.out.println("JNDI port : " + argsValue);
            // }
        }
    }
}
