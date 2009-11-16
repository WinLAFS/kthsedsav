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

import java.util.List;
import java.util.Map;

import org.objectweb.deployment.scheduling.core.api.Task;
import org.objectweb.deployment.scheduling.core.lib.AbstractTask;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.jasmine.jade.service.allocator.NoNodeAvailableException;
import org.objectweb.jasmine.jade.service.allocator.NodeNotFoundException;
import org.objectweb.jasmine.jade.service.deployer.adl.packages.PackageBuilder;

/**
 * A {@link PrimitiveCompiler} to compile {@link VirtualNode} nodes in
 * definitions.
 * 
 * @author <a href="mailto:jakub.kornas@inrialpes.fr">Jakub Kornas</a>, <a
 *         href="mailto:julien.legrand@inrialpes.fr">Julien Legrand</a>
 * 
 */
public class VirtualNodeCompiler implements BindingController,
        PrimitiveCompiler {

    /**
     * Name of the mandatory interface bound to the {@link PackageBuilder} used
     * by this compiler.
     */
    public final static String BUILDER_BINDING = "builder";

    /**
     * The {@link PackageBuilder} used by this compiler.
     */

    public VirtualNodeBuilder builder;

    // --------------------------------------------------------------------------
    // Implementation of the BindingController interface
    // --------------------------------------------------------------------------

    public String[] listFc() {
        return new String[] { BUILDER_BINDING };
    }

    public Object lookupFc(final String itf) {
        if (itf.equals(BUILDER_BINDING)) {
            return builder;
        }
        return null;
    }

    public void bindFc(final String itf, final Object value) {
        if (itf.equals(BUILDER_BINDING)) {
            builder = (VirtualNodeBuilder) value;
        }
    }

    public void unbindFc(final String itf) {
        if (itf.equals(BUILDER_BINDING)) {
            builder = null;
        }
    }

    // -------------------------------------------------------------------------
    // Implementation of the Compiler interface
    // -------------------------------------------------------------------------

    public void compile(final List path, final ComponentContainer container,
            final TaskMap tasks, final Map context) throws ADLException {

        if (container instanceof VirtualNodeContainer) {
            VirtualNode virtualNode = ((VirtualNodeContainer) container)
                    .getVirtualNode();
            
            Task virtualNodeCleanTask = null;
            /*
             * look for an existing VirtualNodeCleanTask
             */
            try {
                virtualNodeCleanTask = tasks.getTask("cleanNodes", null);
            } catch (Exception ignored) {
            }
            /*
             * if this task doesn't exist, create it and register it
             */
            if (virtualNodeCleanTask == null) {
                virtualNodeCleanTask = new VirtualNodeCleanTask(builder);
                tasks.addTask("cleanNodes", "beurk", virtualNodeCleanTask);
            }

            Task virtualNodeCreationTask = null;
            if (virtualNode != null) {
                String virtualNodeName = virtualNode.getName();
                String virtualNodeHost = virtualNode.getHost();
                String virtualNodeNumber = virtualNode.getNumber();
                String virtualNodeResourceReqs = virtualNode.getResourceReqs();
                String virtualNodeAllocationProperties = virtualNode.getAllocationProperties();
                String virtualNodeCardinality= virtualNode.getCardinality();
                

                
                /*
                 * look for an existing VirtualNodeCreationTask for the node
                 * 'vitualNodeName'
                 */
                try {
                    virtualNodeCreationTask = tasks.getTask("allocNode",
                            virtualNodeName);
                    /*
                     * check if Task is complete (host and number)
                     */
                    String host = ((VirtualNodeCreationTask) virtualNodeCreationTask)
                            .getHost();
                    String number = ((VirtualNodeCreationTask) virtualNodeCreationTask)
                            .getNumber();

                    if (virtualNodeHost != null && host == null)
                        ((VirtualNodeCreationTask) virtualNodeCreationTask)
                                .setHost(virtualNodeHost);
                    if (virtualNodeNumber != null && number == null)
                        ((VirtualNodeCreationTask) virtualNodeCreationTask)
                                .setNumber(virtualNodeNumber);

                } catch (Exception ignored) {
                }
                /*
                 * if this task doesn't exist, create it and register it
                 */
                if (virtualNodeCreationTask == null) {
                    virtualNodeCreationTask = new VirtualNodeCreationTask(
                            builder, virtualNodeName, virtualNodeHost,
                            virtualNodeNumber, virtualNodeResourceReqs, virtualNodeAllocationProperties, virtualNodeCardinality);

                    scheduleVirtualNodeCreationTask(tasks,
                            (VirtualNodeCreationTask) virtualNodeCreationTask);
                    /*
                     * DEBUG begin 3 juil. 2006 jlegrand
                     */
                    virtualNodeCleanTask
                            .addPreviousTask(virtualNodeCreationTask);
                    /*
                     * end
                     */

                    tasks.addTask("allocNode", virtualNodeName,
                            virtualNodeCreationTask);
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

    private void scheduleVirtualNodeCreationTask(final TaskMap tasks,
            final VirtualNodeCreationTask virtualNodeCreationTask) {

        Task nextTask = null;
        /*
         * 
         */
        if (virtualNodeCreationTask.getNumber() != null) {
            nextTask = getFirstVirtualNodeCreationTaskWithHost(tasks);
            if (nextTask == null)
                nextTask = getFirstVirtualNodeCreationTask(tasks);
        }
        /*
         * 
         */
        else {
            nextTask = getFirstVirtualNodeCreationTask(tasks);
        }

        if (nextTask != null)
            nextTask.addPreviousTask(virtualNodeCreationTask);

    }

    private Task getFirstVirtualNodeCreationTaskWithHost(final TaskMap taskmap) {

        Task tasks[] = taskmap.getTasks();
        int i = 0;
        Task task = null;

        while (i < tasks.length) {
            task = tasks[i++];
            if (task instanceof VirtualNodeCreationTask
                    && ((VirtualNodeCreationTask) task).getNumber() == null) {
                return task;
            }
        }
        return null;
    }

    private Task getFirstVirtualNodeCreationTask(final TaskMap taskmap) {

        Task tasks[] = taskmap.getTasks();
        int i = 0;
        Task task = null;

        while (i < tasks.length) {
            task = tasks[i++];
            if (task instanceof VirtualNodeCreationTask) {
                return task;
            }
        }
        return null;
    }

    // --------------------------------------------------------------------------
    // Inner classes
    // --------------------------------------------------------------------------

    static class VirtualNodeCreationTask extends AbstractTask {

        private VirtualNodeBuilder builder;

        private String virtualNodeName;

        private String virtualNodeHost;

        private String virtualNodeNumber;
        
        private String virtualNodeResourceReqs;
        private String virtualNodeAllocationProperties;
        private String virtualNodeCardinality;

        private Component result;


        public VirtualNodeCreationTask(final VirtualNodeBuilder builder,
                final String virtualNodeName, final String virtualNodeHost,
                final String virtualNodeNumber, final String virtualNodeResourceReqs, final String virtualNodeAllocationProperties, final String virtualNodeCardinality) {
            this.builder = builder;
            this.virtualNodeName = virtualNodeName;
            this.virtualNodeHost = virtualNodeHost;
            this.virtualNodeNumber = virtualNodeNumber;
            this.virtualNodeResourceReqs = virtualNodeResourceReqs;
            this.virtualNodeAllocationProperties=virtualNodeAllocationProperties;
            this.virtualNodeCardinality = virtualNodeCardinality;
            
        }

        public void execute(final Object context)
                throws NoNodeAvailableException, NodeNotFoundException {
            this.result = builder.getVirtualNodeInstance(virtualNodeName,
                    virtualNodeHost, virtualNodeNumber, virtualNodeResourceReqs, virtualNodeAllocationProperties, virtualNodeCardinality);
        }

        public String toString() {
            return "T" + System.identityHashCode(this)
                    + "[VirtualNodeCreationTask(" + virtualNodeName + ")]";
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = (Component) result;
        }

        // ---------------------------------------------------------------------
        // Accessors
        // ---------------------------------------------------------------------

        public String getHost() {
            return virtualNodeHost;
        }

        public void setHost(String virtualNodeHost) {
            this.virtualNodeHost = virtualNodeHost;
        }

        public String getNumber() {
            return virtualNodeNumber;
        }

        public void setNumber(String virtualNodeNumber) {
            this.virtualNodeNumber = virtualNodeNumber;
        }
        
        
        public String getResourceReqs() {
            return virtualNodeResourceReqs;
        }

        public void setResourceReqs(String virtualNodeResourceReqs) {
            this.virtualNodeResourceReqs = virtualNodeResourceReqs;
        }
        
        public String getAllocationProperties() {
            return virtualNodeAllocationProperties;
        }

        public void setAllocationProperties(String virtualNodeAllocationProperties) {
            this.virtualNodeAllocationProperties=virtualNodeAllocationProperties;
        }
        
        public String getCardinality() {
            return virtualNodeCardinality;
        }

        public void setCardinality(String virtualNodeCardinality) {
            this.virtualNodeCardinality = virtualNodeCardinality;
        }
        
    }

    static class VirtualNodeCleanTask extends AbstractTask {

        private VirtualNodeBuilder builder;

        public VirtualNodeCleanTask(final VirtualNodeBuilder builder) {
            this.builder = builder;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.deployment.scheduling.core.api.Task#execute(java.lang.Object)
         */
        public void execute(Object context) throws Exception {
            builder.cleanVirtualNodesMap();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.deployment.scheduling.core.api.Task#getResult()
         */
        public Object getResult() {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.deployment.scheduling.core.api.Task#setResult(java.lang.Object)
         */
        public void setResult(Object result) {
        }

    }
}