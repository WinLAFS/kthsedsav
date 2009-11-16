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

package org.objectweb.jasmine.jade.service.deployer.adl.components;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.objectweb.deployment.scheduling.component.api.InstanceProviderTask;
import org.objectweb.deployment.scheduling.component.lib.AbstractInitializationTask;
import org.objectweb.deployment.scheduling.component.lib.AbstractRequireInstanceProviderTask;
import org.objectweb.deployment.scheduling.core.api.Task;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentBuilder;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.ComponentPair;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.api.control.BindingController;

/**
 * A {@link PrimitiveCompiler} to compile {@link Component} nodes in
 * definitions.
 * <p>
 * This class is an extension of the
 * {@link org.objectweb.fractal.adl.components.PrimitiveComponentCompiler}.
 * <p>
 * Contributor : <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand</a>,
 * <a href="mailto:jakub.kornas@inrialpes.fr">Jakub Kornas</a>
 */
public class PrimitiveComponentCompiler implements BindingController,
        PrimitiveCompiler {

    /**
     * Name of the mandatory interface bound to the {@link ComponentBuilder}
     * used by this compiler.
     */

    public final static String BUILDER_BINDING = "builder";

    /**
     * The {@link ComponentBuilder} used by this compiler.
     */

    public ComponentBuilder builder;

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
            builder = (ComponentBuilder) value;
        }
    }

    public void unbindFc(final String itf) {
        if (itf.equals(BUILDER_BINDING)) {
            builder = null;
        }
    }

    // --------------------------------------------------------------------------
    // Implementation of the PrimitiveCompiler interface
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

        InstanceProviderTask createTask = (InstanceProviderTask) tasks.getTask(
                "create", container);

        StartTask startTask = new StartTask(builder);
        startTask.setInstanceProviderTask(createTask);
        tasks.addTask("start", container, startTask);

        Component[] comps = ((ComponentContainer) container).getComponents();
        for (int i = 0; i < comps.length; i++) {
            InstanceProviderTask createSubComponentTask = (InstanceProviderTask) tasks
                    .getTask("create", comps[i]);

            ComponentPair pair = new ComponentPair(container, comps[i]);
            try {
                // the task may already exist, in case of a shared component
                tasks.getTask("add", pair);
            } catch (NoSuchElementException e) {
                AddTask addTask = new AddTask(builder, comps[i].getName());
                addTask.setInstanceProviderTask(createTask);
                addTask.setSubInstanceProviderTask(createSubComponentTask);

                /*
                 * DEBUG begin 3 juil. 2006 jlegrand
                 */
                virtualNodeCleanTask.addPreviousTask(addTask);
                /*
                 * end
                 */

                tasks.addTask("add", pair, addTask);
            }

            // to start super component only after all sub components are added:
            // startTask.getPreviousTasks().addTask(addTask);
        }
    }

    // --------------------------------------------------------------------------
    // Inner classes
    // --------------------------------------------------------------------------

    static class AddTask extends AbstractRequireInstanceProviderTask {

        private ComponentBuilder builder;

        private InstanceProviderTask subInstanceProviderTask;

        private String name;

        public AddTask(final ComponentBuilder builder, final String name) {
            this.builder = builder;
            this.name = name;
        }

        public InstanceProviderTask getSubInstanceProviderTask() {
            return subInstanceProviderTask;
        }

        public void setSubInstanceProviderTask(final InstanceProviderTask task) {
            if (subInstanceProviderTask != null) {
                removePreviousTask(subInstanceProviderTask);
            }
            subInstanceProviderTask = task;
            if (subInstanceProviderTask != null) {
                addPreviousTask(subInstanceProviderTask);
            }
        }

        public void execute(final Object context) throws Exception {
            Object parent = getInstanceProviderTask().getInstance();
            Object child = getSubInstanceProviderTask().getInstance();
            builder.addComponent(parent, child, name, context);
        }

        public Object getResult() {
            return null;
        }

        public void setResult(Object result) {
        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[AddTask(" + name
                    + ")]";
        }
    }

    static class StartTask extends AbstractInitializationTask {

        private ComponentBuilder builder;

        public StartTask(final ComponentBuilder builder) {
            this.builder = builder;
        }

        public void execute(final Object context) throws Exception {
            builder.startComponent(getInstanceProviderTask().getInstance(),
                    context);
        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[StartTask()]";
        }
    }
}
