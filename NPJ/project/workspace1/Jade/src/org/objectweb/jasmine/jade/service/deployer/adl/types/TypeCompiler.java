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
 * Contributor: Philippe Merle
 */

package org.objectweb.jasmine.jade.service.deployer.adl.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.objectweb.deployment.scheduling.component.lib.AbstractFactoryProviderTask;
import org.objectweb.deployment.scheduling.core.api.Task;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeBuilder;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.api.control.BindingController;

/**
 * This TypeCompiler replace the standard TypeCompiler. The standard compiler is
 * linked with the attribute controller : When creating a type, it adds the
 * attribute controller type. We redefine this compiler since we want generic
 * attribute controller. This type compiler does not modify the original type.
 * <p>
 * Contributor : <a href="mailto:noel.depalma@inrialpes.fr">Noel De Palma</a>
 * 
 */
public class TypeCompiler implements BindingController, PrimitiveCompiler {

    /**
     * Name of the mandatory interface bound to the {@link TypeBuilder} used by
     * this compiler.
     */

    public final static String BUILDER_BINDING = "builder";

    /**
     * The {@link TypeBuilder} used by this compiler.
     */

    public TypeBuilder builder;

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
            builder = (TypeBuilder) value;
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

        if (container instanceof InterfaceContainer) {
            try {
                // the task may already exist, in case of a shared component
                tasks.getTask("type", container);
            } catch (NoSuchElementException e) {
                CreateTypeTask createTypeTask = new CreateTypeTask(builder,
                        (InterfaceContainer) container);
                /*
                 * DEBUG begin 3 juil. 2006 jlegrand
                 */
                virtualNodeCleanTask.addPreviousTask(createTypeTask);
                /*
                 * end
                 */

                tasks.addTask("type", container, createTypeTask);
            }
        }
    }

    // --------------------------------------------------------------------------
    // Inner classes
    // --------------------------------------------------------------------------

    static class CreateTypeTask extends AbstractFactoryProviderTask {

        private TypeBuilder builder;

        private InterfaceContainer container;

        public CreateTypeTask(final TypeBuilder builder,
                final InterfaceContainer container) {
            this.builder = builder;
            this.container = container;
        }

        @SuppressWarnings("unchecked")
        public void execute(final Object context) throws Exception {
            if (getFactory() != null) {
                return;
            }
            List itfTypes = new ArrayList();
            Interface[] itfs = container.getInterfaces();
            for (int i = 0; i < itfs.length; i++) {
                if (itfs[i] instanceof TypeInterface) {
                    TypeInterface itf = (TypeInterface) itfs[i];
                    Object itfType = builder.createInterfaceType(itf.getName(),
                            itf.getSignature(), itf.getRole(), itf
                                    .getContingency(), itf.getCardinality(),
                            context);
                    itfTypes.add(itfType);
                }
            }
            /*
             * TODO: FIX THE PROBLEM HERE (?) if (container instanceof
             * AttributesContainer) { // TODO improve module separation
             * Attributes attr =
             * ((AttributesContainer)container).getAttributes(); if (attr !=
             * null) { Object itfType = builder.createInterfaceType(
             * "attribute-controller", attr.getSignature(), "server",
             * "mandatory", "singleton", context); itfTypes.add(itfType); } }
             */
            String name = null;
            if (container instanceof Definition) {
                name = ((Definition) container).getName();
            } else if (container instanceof Component) {
                name = ((Component) container).getName();
            }
            setFactory(builder.createComponentType(name, itfTypes.toArray(),
                    context));
        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[CreateTypeTask()]";
        }
    }
}
