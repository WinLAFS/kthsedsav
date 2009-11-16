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
 * 
 */

package org.objectweb.jasmine.jade.service.deployer.adl.attributes;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.objectweb.deployment.scheduling.component.api.InstanceProviderTask;
import org.objectweb.deployment.scheduling.component.lib.AbstractAttributeSetterTask;
import org.objectweb.deployment.scheduling.core.api.Task;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.attributes.Attribute;
import org.objectweb.fractal.adl.attributes.AttributeBuilder;
import org.objectweb.fractal.adl.attributes.Attributes;
import org.objectweb.fractal.adl.attributes.AttributesContainer;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.api.control.BindingController;

/**
 * A {@link PrimitiveCompiler} to compile {@link Attributes} nodes in
 * definitions.
 * <p>
 * This class is an extension of the
 * {@link org.objectweb.fractal.adl.attributes.AttributeCompiler}.
 * <p>
 * Contributors : <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 * 
 */
public class AttributeCompiler implements BindingController, PrimitiveCompiler {

    /**
     * Name of the mandatory interface bound to the {@link AttributeBuilder}
     * used by this compiler.
     */

    public final static String BUILDER_BINDING = "builder";

    /**
     * The {@link AttributeBuilder} used by this compiler.
     */

    public AttributeBuilder builder;

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
            builder = (AttributeBuilder) value;
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

        if (container instanceof AttributesContainer) {
            Attributes attributes = ((AttributesContainer) container)
                    .getAttributes();
            if (attributes != null) {
                InstanceProviderTask createTask = (InstanceProviderTask) tasks
                        .getTask("create", container);

                Task startTask = tasks.getTask("start", container);

                Attribute[] attrs = attributes.getAttributes();
                for (int i = 0; i < attrs.length; ++i) {
                    try {
                        // the task may already exist, in case of a shared
                        // component
                        tasks.getTask("attr" + attrs[i].getName(), container);
                    } catch (NoSuchElementException e) {
                        AttributeTask t = new AttributeTask(builder, attributes
                                .getSignature(), attrs[i].getName(), attrs[i]
                                .getValue());
                        t.setInstanceProviderTask(createTask);

                        startTask.addPreviousTask(t);

                        /*
                         * DEBUG begin 3 juil. 2006 jlegrand
                         */
                        virtualNodeCleanTask.addPreviousTask(t);
                        /*
                         * end
                         */

                        tasks
                                .addTask("attr" + attrs[i].getName(),
                                        container, t);
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    // Inner classes
    // --------------------------------------------------------------------------

    static class AttributeTask extends AbstractAttributeSetterTask {

        private AttributeBuilder builder;

        private String attributeController;

        private String name;

        public AttributeTask(final AttributeBuilder builder,
                final String attributeController, final String name,
                final String value) {
            this.builder = builder;
            this.attributeController = attributeController;
            this.name = name;
            setValue(value);
        }

        public void execute(final Object context) throws Exception {
            Object component = getInstanceProviderTask().getInstance();
            builder.setAttribute(component, attributeController, name,
                    (String) getValue(), context);
        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[AttributeTask("
                    + name + "," + getValue() + ")]";
        }
    }
}
