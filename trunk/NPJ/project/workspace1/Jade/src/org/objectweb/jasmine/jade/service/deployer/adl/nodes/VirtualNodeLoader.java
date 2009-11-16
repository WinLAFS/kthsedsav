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

package org.objectweb.jasmine.jade.service.deployer.adl.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.jasmine.jade.service.allocator.Allocator;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class VirtualNodeLoader extends AbstractLoader implements
        BindingController {

    /**
     * 
     */
    private Map<String,String> nodes;

    /**
     * 
     */
    private Allocator allocator;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * 
     */
    public VirtualNodeLoader() {
        nodes = new HashMap<String,String>();
    }

    // ------------------------------------------------------------------------
    // Implementation of Loader interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.adl.Loader#load(java.lang.String,
     *      java.util.Map)
     */
    public Definition load(String name, Map context) throws ADLException {

        Definition d = clientLoader.load(name, context);
        checkNode(d);

        checkVirtualNodeNumber();

        return d;
    }

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

    /**
     * @param d
     */
    private void checkNode(Object node) throws ADLException {
        if (node instanceof VirtualNodeContainer) {
            VirtualNodeContainer virtualNodeContainer = (VirtualNodeContainer) node;
            if (virtualNodeContainer != null) {
                VirtualNode virtualNode = virtualNodeContainer.getVirtualNode();
                // Allow having specifying no virtual node 
                if (virtualNode!=null)
                	checkVirtualNode(virtualNode);
            }
        }
        if (node instanceof ComponentContainer) {
            Component[] comps = ((ComponentContainer) node).getComponents();
            for (int i = 0; i < comps.length; i++) {
                checkNode(comps[i]);
            }
        }
    }

    private void checkVirtualNode(final VirtualNode virtualNode)
            throws ADLException {

        String name = virtualNode.getName();
        String host = virtualNode.getHost();
        String number = virtualNode.getNumber();
        String resourceReqs = virtualNode.getResourceReqs();

        
//        Resource requirements cannot be defined together with host/number
        if ((resourceReqs!=null) && (host!=null || number!=null))
        	throw new ADLException(
                    "host cannot be defined if resource requirements are defined",
                    (Node) virtualNode);
        
        /*
         * check that jadeNodeHost and jadeNodeNumber exists in the Jade
         * platform
         */

        /*
         * the number is defined but not the host
         */
        if (host == null && number != null)
            throw new ADLException(
                    "virtual-node number can't be defined if virtual-node host isn't defined",
                    (Node) virtualNode);

        /*
         * the host is defined ...
         */
        if (host != null) {

            List<String> allocatorNodes = new ArrayList<String>(Arrays.asList(allocator
                    .getAllocatedComponentName()));
            allocatorNodes.addAll(new ArrayList<String>(Arrays.asList(allocator
                    .getFreeComponentName())));

            /*
             * ... the number too.
             */
            if (number != null) {
                /*
                 * check if JadeNode host_number exists
                 */
                if (!allocatorNodes.contains(host + "_" + number)) {
                    throw new ADLException("The JadeNode " + host + "_"
                            + number + " doesnt exist.", (Node) virtualNode);
                } else {
                    /*
                     * check if there is conflict (a virtual-node with 2 <>
                     * host_number value) in virtual-node attributes
                     */
                    if (nodes.containsKey(name)) {
                        String value = (String) nodes.get(name);
                        String jadenodename = host + "_" + number;
                        if (!jadenodename.startsWith(value))
                            throw new ADLException(
                                    "There is a conflict in virtual-node "
                                            + name
                                            + ". 2 different JadeNode specified : "
                                            + value + " <> " + host + "_"
                                            + number, (Node) virtualNode);

                    }
                    /*
                     * check if there is conflict (a host_number value in 2 <>
                     * virtual-node) in virtual-node attributes
                     */
                    else if (nodes.containsValue(host + "_" + number)) {
                        Set keys = nodes.keySet();
                        Iterator it = keys.iterator();
                        while (it.hasNext()) {
                            String key = (String) it.next();
                            if (!nodes.get(key).equals(host + "_" + number))
                                nodes.remove(key);
                        }
                        if (nodes.size() != 0) {
                            throw new ADLException(
                                    "There is a conflict : The JadeNode "
                                            + host
                                            + "_"
                                            + number
                                            + " is specified for 2 <> virtual-nodes : "
                                            + nodes.keySet().iterator().next()
                                            + " & " + name, (Node) virtualNode);
                        }
                    } else {
                        nodes.put(name, host + "_" + number);
                    }
                }
            }
            /*
             * ... and the number is not defined.
             */
            else {
                /*
                 * check if there is a JadeNode on the host
                 */
                Iterator it = allocatorNodes.iterator();
                boolean found = false;
                while (it.hasNext() && !found) {
                    found = ((String) it.next()).startsWith(host);
                }
                if (!found) {
                    throw new ADLException("The JadeNode " + host + "_"
                            + number + " doesnt exist.", (Node) virtualNode);
                } else {
                    /*
                     * check if there is conflict in virtual-node attributes
                     */
                    if (nodes.containsKey(name)) {
                        String value = (String) nodes.get(name);
                        if (!value.startsWith(host))
                            throw new ADLException(
                                    "There is a conflict in virtual-node "
                                            + name
                                            + ". 2 different host values : "
                                            + value + " <> " + host,
                                    (Node) virtualNode);

                    } else {
                        nodes.put(name, host);
                    }
                }
            }
        }
    }

    /**
     * check if there is JadeNode enough to allow the deployment
     * 
     * @throws ADLException
     */
    private void checkVirtualNodeNumber() throws ADLException {

        int freeNodeNumber = allocator.getFreeComponentName().length;
        int allocatedNodeNumber = allocator.getAllocatedComponent().length;

        if (nodes.size() > freeNodeNumber + allocatedNodeNumber) {
            throw new ADLException("Not enough nodes available", null);
        }

        nodes.clear();

    }

    // ------------------------------------------------------------------------
    // Implementation of the BindingController interface
    // ------------------------------------------------------------------------

    public String[] listFc() {

        List<String> bindings = new ArrayList<String>(Arrays.asList(super.listFc()));
        bindings.add("allocator");
        return (String[]) bindings.toArray(new String[bindings.size()]);
    }

    public Object lookupFc(final String itfName) {
        if (itfName.equals("allocator")) {
            return allocator;
        }
        return super.lookupFc(itfName);
    }

    public void bindFc(final String itfName, final Object itfValue) {
        if (itfName.equals("allocator")) {
            allocator = (Allocator) itfValue;
        }
        super.bindFc(itfName, itfValue);
    }

    public void unbindFc(final String itfName) {
        if (itfName.equals("allocator")) {
            allocator = null;
        }
        super.unbindFc(itfName);
    }

}
