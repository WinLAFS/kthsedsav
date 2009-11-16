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

package org.objectweb.jasmine.jade.service.deployer.adl.implementations;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.implementations.Implementation;
import org.objectweb.fractal.adl.implementations.ImplementationContainer;

/**
 * A {@link org.objectweb.fractal.adl.Loader}to check {@link Implementation}
 * nodes in definitions.
 * <p>
 * This class is an extension of the
 * {@link org.objectweb.fractal.adl.implementations.ImplementationLoader} (no
 * classes building the components are loaded to avoid
 * {@link ClassNotFoundException} at runtime).
 * <p>
 * Contributors : <a href="mailto:jakub.kornas@inrialpes.fr">Jakub Kornas</a>,
 * <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand</a>
 * 
 */

public class ImplementationLoader extends AbstractLoader {

    // --------------------------------------------------------------------------
    // Implementation of the Loader interface
    // --------------------------------------------------------------------------

    /**
     * @param name
     *            name
     * @param context
     *            context
     * @throws ADLException
     *             exception
     * @return definition
     */
    public Definition load(final String name, final Map context)
            throws ADLException {
        Definition d = clientLoader.load(name, context);
        checkNode(d);
        return d;
    }

    // --------------------------------------------------------------------------
    // Checking methods
    // --------------------------------------------------------------------------

    private void checkNode(final Object node) throws ADLException {
        if (node instanceof ImplementationContainer) {
            checkImplementationContainer((ImplementationContainer) node);
        }
        if (node instanceof ControllerContainer) {
            checkControllerContainer((ControllerContainer) node);
        }
        if (node instanceof ComponentContainer) {
            Component[] comps = ((ComponentContainer) node).getComponents();
            for (int i = 0; i < comps.length; i++) {
                checkNode(comps[i]);
            }
        }
    }

    private void checkImplementationContainer(
            final ImplementationContainer container) throws ADLException {
        Implementation impl = container.getImplementation();
        if (impl != null) {
            String className = impl.getClassName();
            if (className == null) {
                throw new ADLException("Implementation class name missing",
                        (Node) impl);
            }
        }
    }

    private void checkControllerContainer(final ControllerContainer container)
            throws ADLException {
        Controller ctrl = container.getController();
        if (ctrl != null) {
            if (ctrl.getDescriptor() == null) {
                throw new ADLException("Controller descriptor missing",
                        (Node) ctrl);
            }
        }
    }
}