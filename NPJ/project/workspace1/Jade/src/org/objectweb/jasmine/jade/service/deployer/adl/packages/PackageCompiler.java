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

package org.objectweb.jasmine.jade.service.deployer.adl.packages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.objectweb.deployment.scheduling.core.api.Task;
import org.objectweb.deployment.scheduling.core.lib.AbstractTask;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.SuperController;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.deployer.adl.nodes.VirtualNode;
import org.objectweb.jasmine.jade.service.deployer.adl.nodes.VirtualNodeContainer;
import org.objectweb.jasmine.jade.service.deployer.adl.property.Property;
import org.objectweb.jasmine.jade.service.deployer.adl.property.PropertyContainer;

/**
 * A {@link PrimitiveCompiler} to compile {@link Packages} nodes in definitions.
 * 
 * @author <a href="mailto:jakub.kornas@inrialpes.fr">Jakub Kornas</a>, <a
 *         href="mailto:julien.legrand@inrialpes.fr">Julien Legrand</a>
 * 
 */
public class PackageCompiler implements BindingController, PrimitiveCompiler {

    /**
     * Name of the mandatory interface bound to the {@link PackageBuilder} used
     * by this compiler.
     */

    public final static String BUILDER_BINDING = "builder";

    /**
     * The {@link PackageBuilder} used by this compiler.
     */

    public PackageBuilder builder;

    public Component myself;

    // --------------------------------------------------------------------------
    // Implementation of the BindingController interface
    // --------------------------------------------------------------------------

    public String[] listFc() {
        return new String[] { BUILDER_BINDING };
    }

    public Object lookupFc(final String itf) {
        if (itf.equals("component")) {
            return myself;
        }
        if (itf.equals(BUILDER_BINDING)) {
            return builder;
        }
        return null;
    }

    public void bindFc(final String itf, final Object value) {
        if (itf.equals("component")) {
            myself = (Component) value;
        }
        if (itf.equals(BUILDER_BINDING)) {
            builder = (PackageBuilder) value;
        }
    }

    public void unbindFc(final String itf) {
        if (itf.equals("component")) {
            myself = null;
        }
        if (itf.equals(BUILDER_BINDING)) {
            builder = null;
        }
    }

    // --------------------------------------------------------------------------
    // Implementation of the Compiler interface
    // --------------------------------------------------------------------------

    public void compile(final List path, final ComponentContainer container,
            final TaskMap tasks, final Map context) throws ADLException {

        String dirLocal = null;

        /*
         * DEBUG begin 3 juil. 2006 jlegrand
         */
        Task virtualNodeCleanTask = null;
        try {
        virtualNodeCleanTask = tasks.getTask("cleanNodes", "beurk");
        } catch (Exception ignored) {
        }
        
        // if (container instanceof AttributesContainer) {
        // AttributesContainer attributesCont = (AttributesContainer) container;
        //
        // if (attributesCont != null) {
        // Attributes attributes = attributesCont.getAttributes();
        // if (attributes != null) {
        // Attribute[] attribs = attributes.getAttributes();
        // if (attribs != null) {
        // for (int i = 0; i < attribs.length; i++) {
        // String attName = attribs[i].getName();
        // if (attName != null && attName.equals("dirLocal")) {
        //
        // /*
        // * FIXME not only get dirLocal value to put in a
        // * context map ! but how differenciate attribute
        // * to get ?
        // */
        //
        // dirLocal = attribs[i].getValue();
        // }
        // }
        // }
        // }
        // }
        // }
        if (container instanceof PropertyContainer) {

            Map<String,String> propertiesMap = null;
            Property[] properties = ((PropertyContainer) container)
                    .getPropertys();

            if (properties != null && properties.length > 0) {

                propertiesMap = new HashMap<String,String>();

                for (int j = 0; j < properties.length; j++) {

                    System.out.println(properties[j].getName() + " : "
                            + properties[j].getValue());

                    // propertiesMap.put(properties[j].getName(),
                    // properties[j].getValue());

                }
            }
        }

        if (container instanceof PackagesContainer) {
            Packages packages = ((PackagesContainer) container).getPackages();
            if (packages == null) {
                return;
            }

            Package[] pkgs = packages.getPackages();

            for (int i = 0; i < pkgs.length; i++) {

                // if (pkgs[i] instanceof PropertyContainer) {

                Map<String,String> propertiesMap = null;
                Property[] properties = ((PropertyContainer) pkgs[i])
                        .getPropertys();

                if (properties != null && properties.length > 0) {

                    propertiesMap = new HashMap<String,String>();

                    for (int j = 0; j < properties.length; j++) {

                        System.out.println(pkgs[i].getName() + " : "
                                + properties[j].getName() + " = "
                                + properties[j].getValue());

                        // propertiesMap.put(properties[j].getName(),
                        // properties[j].getValue());

                    }
                }
                // }
            }

            String virtualNodeName = null;
            Task virtualNodeCreationTask = null;

            if (container instanceof VirtualNodeContainer) {
                VirtualNode virtualNode = ((VirtualNodeContainer) container)
                        .getVirtualNode();
                if (virtualNode != null) {
                    virtualNodeName = virtualNode.getName();
                    virtualNodeCreationTask = tasks.getTask("allocNode",
                            virtualNodeName);
                }
            }

            if (virtualNodeCreationTask != null) {
                // Package[] pkgs = packages.getPackages();
                for (int i = 0; i < pkgs.length; ++i) {
                    try {
                        // the task may already exist, in case of a shared
                        // component
                        tasks.getTask("pkg" + pkgs[i].getName(), container);
                    } catch (NoSuchElementException e) {

                        /*
                         * FIXME change signature and give a map instead of
                         * String dirLocal
                         */

                        Task t = new RemoteInstallPackageTask(builder,
                                virtualNodeCreationTask, pkgs[i].getName(),
                                dirLocal);

                        /*
                         * DEBUG begin 3 juil. 2006 jlegrand
                         */
                        if (virtualNodeCleanTask!= null)
                        	virtualNodeCleanTask.addPreviousTask(t);
                        /*
                         * end
                         */
                        t.addPreviousTask(virtualNodeCreationTask);
                        tasks.addTask("pkg" + pkgs[i].getName(), container, t);
                    }
                }
            } else {
                // Package[] pkgs = packages.getPackages();
                for (int i = 0; i < pkgs.length; ++i) {
                    try {
                        // the task may already exist, in case of a shared
                        // component
                        tasks.getTask("pkg" + pkgs[i].getName(), container);
                    } catch (NoSuchElementException e) {

                        /*
                         * FIXME change signature and give a map instead of
                         * String dirLocal
                         */

                        Task t = new InstallPackageTask(myself, builder,
                                pkgs[i].getName(), dirLocal);

                        /*
                         * DEBUG begin 3 juil. 2006 jlegrand
                         */
                        if (virtualNodeCleanTask!= null)
                            virtualNodeCleanTask.addPreviousTask(t);
                        /*
                         * end
                         */

                        tasks.addTask("pkg" + pkgs[i].getName(), container, t);
                    }
                }
            }

        }
    }

    // --------------------------------------------------------------------------
    // Inner classes
    // --------------------------------------------------------------------------

    static class InstallPackageTask extends AbstractTask {

        protected PackageBuilder builder;

        protected String packageName;

        protected String dirLocal;

        protected Component cmp;

        public InstallPackageTask(final Component cmp,
                final PackageBuilder builder, final String packageName,
                final String dirLocal) {

            this.cmp = cmp;
            this.builder = builder;
            this.packageName = packageName;
            this.dirLocal = dirLocal;
        }

        public void execute(final Object context) throws Exception {
            Component node = getMyNodeComponent();

            /*
             * FIXME Change InstallPackageTask
             */
            Map<String,String> ctx = new HashMap<String,String>();
            /*
             * DEBUG begin 15 juin 2006 jlegrand
             */
            if (dirLocal != null)
                ctx.put("dirLocal", dirLocal);
            /*
             * end
             */

            builder.installPackage(node, packageName, ctx);
        }

        public String toString() {
            return "T" + System.identityHashCode(this) + "[InstallPackageTask("
                    + packageName + "," + dirLocal + ")]";
        }

        public Object getResult() {
            return null;
        }

        public void setResult(Object arg0) {
        }

        /**
         * @return
         */
        private Component getMyNodeComponent() {

            Component res = cmp;
            boolean found = false;
            while (!found) {
                try {
                    SuperController sc = Fractal.getSuperController(res);
                    Component cmps[] = sc.getFcSuperComponents();
                    res = cmps[0];
                } catch (NoSuchInterfaceException e) {
                    return null;
                }
                try {
                    Interface installer = (Interface) Fractal
                            .getContentController(res).getFcInternalInterface(
                                    "installer");
                    InterfaceType installerType = (InterfaceType) installer
                            .getFcItfType();
                    if (installerType.isFcClientItf())
                        found = true;
                } catch (NoSuchInterfaceException e) {
                }
            }

            return res;
        }
    }

    static class RemoteInstallPackageTask extends InstallPackageTask {

        private Task nodeCreationTask;

        public RemoteInstallPackageTask(final PackageBuilder builder,
                final Task nodeCreationTask, final String packageName,
                final String dirLocal) {
            super(null, builder, packageName, dirLocal);
            this.nodeCreationTask = nodeCreationTask;
        }

        public void execute(final Object context) throws Exception {
            Component virtualNode = (Component) nodeCreationTask.getResult();

            /*
             * FIXME change too
             */
            Map<String,String> ctx = new HashMap<String,String>();
            /*
             * DEBUG begin 15 juin 2006 jlegrand
             */
            if (dirLocal != null)
                ctx.put("dirLocal", dirLocal);
            /*
             * end
             */
            builder.installPackage(virtualNode, packageName, ctx);
        }

        public String toString() {
            return "T" + System.identityHashCode(this)
                    + "[RemoteInstallPackageTask(" + packageName + ","
                    + dirLocal + ")]";
        }
    }
}
