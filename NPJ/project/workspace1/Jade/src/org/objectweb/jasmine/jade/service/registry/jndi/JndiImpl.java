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

package org.objectweb.jasmine.jade.service.registry.jndi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.objectweb.jasmine.jade.util.JadeException;

import fr.jade.fractal.api.control.GenericAttributeController;
import fr.jade.fractal.api.control.NoSuchAttributeException;

/**
 * Implementation class of a Fractal component wrapping a JNDI
 * 
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class JndiImpl implements Context, GenericAttributeController {

    private boolean registryLaunched = false;

    private Context context = null;

    private String host = null;

    private String[] attList = new String[] { "port", "protocol",
            "initialContextFactory" };

    private String port = "1239";

    private String protocol = "scn";

    private String initialContextFactory = "fr.dyade.aaa.jndi2.client.NamingContextFactory";

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * 
     */
    public JndiImpl() throws JadeException {

    }

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void connectRegistry() throws JadeException {

        if (initialContextFactory
                .equals("com.sun.jndi.rmi.registry.RegistryContextFactory")
                && registryLaunched == false) {
            try {
                LocateRegistry.createRegistry(1099);
                registryLaunched = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        try {
            // TODO: modified
            // AdminModule.collocatedConnect("root", "root");
            host = InetAddress.getLocalHost().getHostName();
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
            env.put(Context.PROVIDER_URL, protocol + "://" + host + ":" + port);
            // TODO: modified
            ClassLoader currentCl = Thread.currentThread()
                    .getContextClassLoader();
            Thread.currentThread().setContextClassLoader(
                    this.getClass().getClassLoader());
            context = new InitialContext(env);
            Thread.currentThread().setContextClassLoader(currentCl);
            System.out.println("[JNDI] connected to " + initialContextFactory
                    + " on " + host + ":" + port);
        } catch (UnknownHostException e) {
            throw new JadeException("[JNDI] Cannot set registry host", e);
        } catch (NamingException e) {
            throw new JadeException("[JNDI] Cannot create InitialContext", e);
        }
        // } catch (ConnectException e) {
        // throw new JadeException("[JNDI] Cannot conect to Joram server", e);
        // } catch (AdminException e) {
        // e.printStackTrace();
        // }
    }

    // ------------------------------------------------------------------------
    // Implementation of GenericAttributeController interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.meta.api.control.GenericAttributeController#getAttribute(java.lang.String)
     */
    public String getAttribute(String name) throws NoSuchAttributeException {
        if (name.equals("port"))
            return port;
        if (name.equals("protocol"))
            return protocol;
        if (name.equals("initialContextFactory"))
            return initialContextFactory;
        throw new NoSuchAttributeException(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.meta.api.control.GenericAttributeController#setAttribute(java.lang.String,
     *      java.lang.String)
     */
    public void setAttribute(String name, String value)
            throws NoSuchAttributeException {
        if (name.equals("port"))
            port = value;
        else if (name.equals("protocol"))
            protocol = value;
        else if (name.equals("initialContextFactory"))
            initialContextFactory = value;
        else
            throw new NoSuchAttributeException(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.meta.api.control.GenericAttributeController#listFcAtt()
     */
    public String[] listFcAtt() {
        return attList;
    }

    // ------------------------------------------------------------------------
    // Implementation of Context interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#lookup(javax.naming.Name)
     */
    public Object lookup(Name name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.lookup(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    public Object lookup(String name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.lookup(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#bind(javax.naming.Name, java.lang.Object)
     */
    public void bind(Name name, Object obj) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        context.bind(name, obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
     */
    public void bind(String name, Object obj) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        context.bind(name, obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#rebind(javax.naming.Name, java.lang.Object)
     */
    public void rebind(Name name, Object obj) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        context.rebind(name, obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
     */
    public void rebind(String name, Object obj) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        context.rebind(name, obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#unbind(javax.naming.Name)
     */
    public void unbind(Name name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        context.unbind(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#unbind(java.lang.String)
     */
    public void unbind(String name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        context.unbind(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#rename(javax.naming.Name, javax.naming.Name)
     */
    public void rename(Name oldName, Name newName) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        context.rename(oldName, newName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
     */
    public void rename(String oldName, String newName) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        context.rename(oldName, newName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#list(javax.naming.Name)
     */
    @SuppressWarnings("unchecked")
    public NamingEnumeration list(Name name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.list(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#list(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public NamingEnumeration list(String name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.list(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#listBindings(javax.naming.Name)
     */
    @SuppressWarnings("unchecked")
    public NamingEnumeration listBindings(Name name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.listBindings(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#listBindings(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public NamingEnumeration listBindings(String name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.listBindings(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#destroySubcontext(javax.naming.Name)
     */
    public void destroySubcontext(Name name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        context.destroySubcontext(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#destroySubcontext(java.lang.String)
     */
    public void destroySubcontext(String name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        context.destroySubcontext(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#createSubcontext(javax.naming.Name)
     */
    public Context createSubcontext(Name name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.createSubcontext(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#createSubcontext(java.lang.String)
     */
    public Context createSubcontext(String name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.createSubcontext(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#lookupLink(javax.naming.Name)
     */
    public Object lookupLink(Name name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.lookupLink(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#lookupLink(java.lang.String)
     */
    public Object lookupLink(String name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.lookupLink(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#getNameParser(javax.naming.Name)
     */
    public NameParser getNameParser(Name name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.getNameParser(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#getNameParser(java.lang.String)
     */
    public NameParser getNameParser(String name) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.getNameParser(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#composeName(javax.naming.Name,
     *      javax.naming.Name)
     */
    public Name composeName(Name name, Name prefix) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.composeName(name, prefix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
     */
    public String composeName(String name, String prefix)
            throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.composeName(name, prefix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#addToEnvironment(java.lang.String,
     *      java.lang.Object)
     */
    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.addToEnvironment(propName, propVal);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#removeFromEnvironment(java.lang.String)
     */
    public Object removeFromEnvironment(String propName) throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.removeFromEnvironment(propName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#getEnvironment()
     */
    @SuppressWarnings("unchecked")
    public Hashtable getEnvironment() throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.getEnvironment();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#close()
     */
    public void close() throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        context.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.Context#getNameInNamespace()
     */
    public String getNameInNamespace() throws NamingException {
        if (context == null) {
            try {
                connectRegistry();
            } catch (JadeException e) {
                e.printStackTrace();
            }
        }
        return context.getNameInNamespace();
    }
}
