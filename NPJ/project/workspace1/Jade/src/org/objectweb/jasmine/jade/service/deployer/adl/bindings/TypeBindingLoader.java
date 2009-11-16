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

import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.types.TypeInterface;

import java.util.Iterator;
import java.util.Map;

/**
 * An extended {@link BindingLoader} for components with typed interfaces. In
 * addition to the checks performed by {@link BindingLoader}, this 
 * implementation checks that from interfaces are client interfaces, that
 * to interfaces are server interfaces, and that the signatures and contingency
 * of client and server interfaces are compatible.
 */

public class TypeBindingLoader extends BindingLoader {

  // --------------------------------------------------------------------------
  // Overriden methods
  // --------------------------------------------------------------------------
  
  void checkBinding (
    final Binding binding,
    final Interface fromItf,
    final Interface toItf,
    final Map context) throws ADLException
  {
    if (fromItf instanceof TypeInterface && toItf instanceof TypeInterface) {
      TypeInterface cItf = (TypeInterface)fromItf;
      TypeInterface sItf = (TypeInterface)toItf;
      if (binding.getFrom().startsWith("this.")) {
        if (!TypeInterface.SERVER_ROLE.equals(cItf.getRole())) {
          throw new ADLException(
            "Invalid binding: 'from' interface is not an internal client interface", (Node)binding);
        }
      } else {
        if (!TypeInterface.CLIENT_ROLE.equals(cItf.getRole())) {
          throw new ADLException(
            "Invalid binding: 'from' interface is not a client interface", (Node)binding);
        }
      }
      if (binding.getTo().startsWith("this.")) {
        if (!TypeInterface.CLIENT_ROLE.equals(sItf.getRole())) {
          throw new ADLException(
            "Invalid binding: 'to' interface is not an internal server interface", (Node)binding);
        }
      } else {
        if (!TypeInterface.SERVER_ROLE.equals(sItf.getRole())) {
          throw new ADLException(
            "Invalid binding: 'to' interface is not a server interface", (Node)binding);
        }
      }
            
      boolean cIsMandatory = true;
      if (TypeInterface.OPTIONAL_CONTINGENCY.equals(cItf.getContingency())) {
        cIsMandatory = false;
      }
      boolean sIsMandatory = true;
      if (TypeInterface.OPTIONAL_CONTINGENCY.equals(sItf.getContingency())) {
        sIsMandatory = false;
      }
      if (cIsMandatory && !sIsMandatory) {
        throw new ADLException(
          "Invalid binding: binding from mandatory to optional interface", (Node)binding);
      }

      try {
        ClassLoader cl = getClassLoader(context);
        Class cClass = cl.loadClass(cItf.getSignature());
        Class sClass = cl.loadClass(sItf.getSignature());
        if (!cClass.isAssignableFrom(sClass)) {
          throw new ADLException(
            "Invalid binding: incompatible signatures", (Node)binding);
        }
      } catch (ClassNotFoundException e) {
      }
    }
  }

  Interface getInterface (final String name, final Map itfs) {
    Interface itf = super.getInterface(name, itfs);
    if (itf == null) {
      Iterator i = itfs.keySet().iterator();
      while (i.hasNext()) {
        String n  = (String)i.next();
        itf = (Interface)itfs.get(n);
        if (itf instanceof TypeInterface) {
          TypeInterface typeItf = (TypeInterface)itf;
          String cardinality = typeItf.getCardinality();
          if (TypeInterface.COLLECTION_CARDINALITY.equals(cardinality)) {
            if (name.startsWith(n)) {
              return itf;
            }
          }
        }
      }
      return null;
    }
    return itf;
  }
}
