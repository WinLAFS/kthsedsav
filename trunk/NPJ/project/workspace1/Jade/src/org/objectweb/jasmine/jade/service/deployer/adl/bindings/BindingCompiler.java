/***
 * Fractal ADL Parser
 * Copyright (C) 2002-2004 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

package org.objectweb.jasmine.jade.service.deployer.adl.bindings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.objectweb.deployment.scheduling.component.api.InstanceProviderTask;
import org.objectweb.deployment.scheduling.component.lib.AbstractRequireInstanceProviderTask;
import org.objectweb.deployment.scheduling.core.api.Task;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.ComponentPair;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.api.control.BindingController;




/**
 * A {@link PrimitiveCompiler} to compile {@link Binding} nodes in definitions.
 * 
 * <p>
 * This class is an extension of the {@link org.objectweb.fractal.adl.bindings.BindingCompiler}.
 * <p>
 * Contributor : <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * Contributor : <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos Parlavantzas
 */

public class BindingCompiler implements BindingController, PrimitiveCompiler {

    /**
     * Name of the mandatory interface bound to the {@link BindingBuilder} used
     * by this compiler.
     */

    public final static String BUILDER_BINDING = "builder";

    /**
     * The {@link BindingBuilder} used by this compiler.
     */

    public BindingBuilder builder;

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
            builder = (BindingBuilder) value;
        }
    }

    public void unbindFc(final String itf) {
        if (itf.equals(BUILDER_BINDING)) {
            builder = null;
        }
    }

    // --------------------------------------------------------------------------
    // Implementation of the Compiler interface
    // --------------------------------------------------------------------------

    public void compile(final List path, final ComponentContainer container,
            final TaskMap tasks, final Map context) throws ADLException {

        /*
         * DEBUG begin 3 juil. 2006 jlegrand
         */
        Task virtualNodeCleanTask = tasks.getTask("cleanNodes", "beurk");
        /*
         * end
         */

        Map<String,ComponentContainer> subComponents = new HashMap<String,ComponentContainer>();
        subComponents.put("this", container);
        Component[] comps = container.getComponents();
        for (int i = 0; i < comps.length; i++) {
            subComponents.put(comps[i].getName(), comps[i]);
        }

        if (container instanceof BindingContainer) {
            Binding[] bindings = (Binding[]) ((BindingContainer) container).getBindings();
            for (int i = 0; i < bindings.length; i++) {
                Binding binding = bindings[i];

                String value = binding.getFrom();
                int index = value.indexOf('.');
                Object clientComp = subComponents
                        .get(value.substring(0, index));
                String clientItf = value.substring(index + 1);

                value = binding.getTo();
                index = value.indexOf('.');
                Object serverComp = subComponents
                        .get(value.substring(0, index));
                String serverItf = value.substring(index + 1);
                
                String bt=binding.getBindingType();

                InstanceProviderTask createClientTask = (InstanceProviderTask) tasks
                        .getTask("create", clientComp);
                InstanceProviderTask createServerTask = (InstanceProviderTask) tasks
                        .getTask("create", serverComp);

                int type = BindingBuilder.NORMAL_BINDING;
                if (binding.getFrom().startsWith("this.")) {
                    type = BindingBuilder.EXPORT_BINDING;
                }
                if (binding.getTo().startsWith("this.")) {
                    type = BindingBuilder.IMPORT_BINDING;
                }

                try {
                    // the task may already exist, in case of a shared component
                    tasks.getTask("bind" + clientItf, clientComp);
                } catch (NoSuchElementException e) {
                    BindTask bindTask = new BindTask(builder, type, clientItf,
                            serverItf, bt);
                    bindTask.setInstanceProviderTask(createClientTask);
                    bindTask.setServerInstanceProviderTask(createServerTask);

                    tasks.addTask("bind" + clientItf, clientComp, bindTask);

                    if (clientComp != container) {
                        Task addTask = tasks.getTask("add", new ComponentPair(
                                container, (Component) clientComp));
                        bindTask.addPreviousTask(addTask);
                    }
                    if (serverComp != container) {
                        Task addTask = tasks.getTask("add", new ComponentPair(
                                container, (Component) serverComp));
                        bindTask.addPreviousTask(addTask);
                    }

                    /*
                     * DEBUG begin 3 juil. 2006 jlegrand
                     */
                    virtualNodeCleanTask.addPreviousTask(bindTask);
                    /*
                     * end
                     */

                    Task startTask = tasks.getTask("start", clientComp);
                    startTask.addPreviousTask(bindTask);
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    // Inner classes
    // --------------------------------------------------------------------------

    static class BindTask extends AbstractRequireInstanceProviderTask {

        private InstanceProviderTask serverInstanceProviderTask;

        private BindingBuilder builder;

        private int type;

        private String clientItf;

        private String serverItf;

		private String bindingType;

        public BindTask(final BindingBuilder builder, final int type,
                final String clientItf, final String serverItf, final String bindingType) {
            this.builder = builder;
            this.type = type;
            this.clientItf = clientItf;
            this.serverItf = serverItf;
            this.bindingType = bindingType;
        }

        public InstanceProviderTask getServerInstanceProviderTask() {
            return serverInstanceProviderTask;
        }

        public void setServerInstanceProviderTask(
                final InstanceProviderTask task) {
            if (serverInstanceProviderTask != null) {
                removePreviousTask(serverInstanceProviderTask);
            }
            serverInstanceProviderTask = task;
            if (serverInstanceProviderTask != null) {
                addPreviousTask(serverInstanceProviderTask);
            }
        }

        public void execute(final Object context) throws Exception {
            Object client = getInstanceProviderTask().getInstance();
            Object server = getServerInstanceProviderTask().getInstance();
            builder.bindComponent(type, client, clientItf, server, serverItf, bindingType, context);
        }

        public Object getResult() {
            return null;
        }

        public void setResult(final Object result) {
        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[BindTask("
                    + clientItf + "," + serverItf + "," + bindingType +")]";
        }
    }
}
