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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.jasmine.jade.service.allocator.Allocator;
import org.objectweb.jasmine.jade.service.allocator.NoNodeAvailableException;
import org.objectweb.jasmine.jade.service.allocator.NodeNotFoundException;
import org.objectweb.jasmine.jade.service.resourcediscovery.ResourceDiscovery;


/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 *
 */
public class BasicVirtualNodeBuilder implements VirtualNodeBuilder,
        BindingController {

    /**
     * 
     */
    private Map<String,Component> virtualNodes;

    /**
     * 
     */
    private Allocator alloc;

	private ResourceDiscovery resourceDiscovery;

    /**
     * 
     */
    private static String[] bindingList = { "allocator","resourceDiscovery" };

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public BasicVirtualNodeBuilder() {
        virtualNodes = new HashMap<String,Component>();
    }

    // ------------------------------------------------------------------------
    // Implementation of VirtualNodeBuilder interface
    // ------------------------------------------------------------------------

    public Component getVirtualNodeInstance(String virtualNodeName,
            String virtualNodeHost, String virtualNodeNumber, String virtualNodeResourceReqs, String virtualNodeAllocationProperties, String virtualNodeCardinality)
            throws NoNodeAvailableException, NodeNotFoundException {

        Component res = null;

        /*
         * the virtual-node has already been defined
         */
        if (virtualNodes.containsKey(virtualNodeName)) {
            return (Component) virtualNodes.get(virtualNodeName);
        }
        /*
         * 
         */
        else {
            res = resourceDiscovery.discover(virtualNodeResourceReqs, virtualNodeAllocationProperties, virtualNodeCardinality);
        }/*
         * 
         */
//        else if (virtualNodeNumber != null) {
//            res = alloc.alloc(virtualNodeHost, virtualNodeNumber);
//        }
//        /*
//         * 
//         */
//        else if (virtualNodeHost != null) {
//            res = alloc.alloc(virtualNodeHost);
//        }
//        /*
//         * 
//         */
//        else {
//            res = alloc.alloc();
//        }

        virtualNodes.put(virtualNodeName, res);
        return res;
    }
    
    /* (non-Javadoc)
     * @see org.objectweb.jasmine.jade.service.deployer.adl.nodes.VirtualNodeBuilder#cleanVirtualNodesMap()
     */
    public void cleanVirtualNodesMap() {
        virtualNodes.clear();
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
        if (itfName.equals("allocator"))
            return alloc;
        else if (itfName.equals("resourceDiscovery"))
            return resourceDiscovery;
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
        if (itfName.equals("allocator"))
            alloc = (Allocator) itfValue;
        else if (itfName.equals("resourceDiscovery"))
            resourceDiscovery = (ResourceDiscovery) itfValue;
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
        if (itfName.equals("allocator"))
            alloc = null;
        else if (itfName.equals("resourceDiscovery"))
            resourceDiscovery = null;
        else
            throw new NoSuchInterfaceException(itfName);
    }
}
