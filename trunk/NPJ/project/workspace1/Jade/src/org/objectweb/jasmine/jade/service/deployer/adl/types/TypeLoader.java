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

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.ComponentDefinition;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;

/**
 * A {@link org.objectweb.fractal.adl.Loader} to check {@link TypeInterface}
 * nodes in definitions. This loader checks that the Java interfaces specified
 * in these nodes exist.
 * <p>
 * Contributor : <a href="mailto:jakub.kornas@inrialpes.fr">Jakub Kornas</a>
 */

public class TypeLoader extends AbstractLoader {

	// --------------------------------------------------------------------------
	// Implementation of the Loader interface
	// --------------------------------------------------------------------------

	public Definition load(final String name, final Map context)
			throws ADLException {
		Definition d = clientLoader.load(name, context);
		boolean extend = false;
		if (d instanceof ComponentDefinition) {
			extend = ((ComponentDefinition) d).getExtends() != null;
		}
		checkNode(d, extend, context);
		return d;
	}

	// --------------------------------------------------------------------------
	// Checking methods
	// --------------------------------------------------------------------------

	private void checkNode(final Object node, final boolean extend,
			final Map context) throws ADLException {
		if (node instanceof InterfaceContainer) {
			checkInterfaceContainer((InterfaceContainer) node, extend, context);
		}
		if (node instanceof ComponentContainer) {
			Component[] comps = ((ComponentContainer) node).getComponents();
			for (int i = 0; i < comps.length; i++) {
				checkNode(comps[i], extend, context);
			}
		}
	}

	private void checkInterfaceContainer(final InterfaceContainer container,
			final boolean extend, final Map context) throws ADLException {
		Interface[] itfs = container.getInterfaces();
		for (int i = 0; i < itfs.length; i++) {
			Interface itf = itfs[i];
			if (itf instanceof TypeInterface) {
				String signature = ((TypeInterface) itf).getSignature();
				if (signature == null) {
					if (!extend) {
						throw new ADLException("Signature missing", (Node) itf);
					}
				} else {
					try {
						getClassLoader(context).loadClass(signature);
					} catch (ClassNotFoundException ignored) {
						// TODO: treat the modularity on the JadeBoot side
					}
				}
				String role = ((TypeInterface) itf).getRole();
				if (role == null) {
					if (!extend) {
						throw new ADLException("Role missing", (Node) itf);
					}
				} else {
					if (!role.equals(TypeInterface.CLIENT_ROLE)
							&& !role.equals(TypeInterface.SERVER_ROLE)) {
						throw new ADLException("Invalid role '" + role + "'",
								(Node) itf);
					}
				}
				String contingency = ((TypeInterface) itf).getContingency();
				if (contingency != null) {
					if (!contingency
							.equals(TypeInterface.MANDATORY_CONTINGENCY)
							&& !contingency
									.equals(TypeInterface.OPTIONAL_CONTINGENCY)) {
						throw new ADLException("Invalid contingency '"
								+ contingency + "'", (Node) itf);
					}
				}
				String cardinality = ((TypeInterface) itf).getCardinality();
				if (cardinality != null) {
					if (!cardinality
							.equals(TypeInterface.SINGLETON_CARDINALITY)
							&& !cardinality
									.equals(TypeInterface.COLLECTION_CARDINALITY)) {
						throw new ADLException("Invalid cardinality '"
								+ cardinality + "'", (Node) itf);
					}
				}
			}
		}
	}
}
