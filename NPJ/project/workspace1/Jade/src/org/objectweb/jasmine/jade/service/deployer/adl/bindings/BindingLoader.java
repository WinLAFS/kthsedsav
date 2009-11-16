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

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link org.objectweb.fractal.adl.Loader} to check {@link Binding} nodes 
 * in definitions. This loader checks that the from and to interfaces
 * specified in these nodes exist. 
 */

public class BindingLoader extends AbstractLoader {

  // --------------------------------------------------------------------------
  // Implementation of the Loader interface
  // --------------------------------------------------------------------------
  
  public Definition load (final String name, final Map context) 
    throws ADLException 
  {
    Definition d = clientLoader.load(name, context);
    checkNode(d, context);
    return d;
  }

  // --------------------------------------------------------------------------
  // Checking methods
  // --------------------------------------------------------------------------
  
  private void checkNode (final Object node, final Map context) 
    throws ADLException 
  {
    if (node instanceof BindingContainer) {
      Map itfMap = new HashMap();
      if (node instanceof InterfaceContainer) {
        Interface[] itfs = ((InterfaceContainer)node).getInterfaces();
        for (int i = 0; i < itfs.length; i++) {
          Interface itf = itfs[i];
          itfMap.put("this." + itf.getName(), itf);
        }
      }
      if (node instanceof ComponentContainer) {
        Component[] comps = ((ComponentContainer)node).getComponents();
        for (int i = 0; i < comps.length; i++) {
          Component comp = comps[i];
          if (comp instanceof InterfaceContainer) {
            Interface[] itfs = ((InterfaceContainer)comp).getInterfaces();
            for (int j = 0; j < itfs.length; j++) {
              Interface itf = itfs[j];
              itfMap.put(comp.getName() + "." + itf.getName(), itf);
            }
          }
        }
      }
      Binding[] bindings = ((BindingContainer)node).getBindings();
      for (int i = 0; i < bindings.length; i++) {
        Binding binding = bindings[i];
        checkBinding(binding, itfMap, context);
      }
      Set fromItfs = new HashSet();
      for (int i = 0; i < bindings.length; i++) {
        if (fromItfs.contains(bindings[i].getFrom())) {
          throw new ADLException(
            "Multiple bindings from the same interface", (Node)bindings[i]);
        }
        fromItfs.add(bindings[i].getFrom());
      }
    }
    if (node instanceof ComponentContainer) {
      Component[] comps = ((ComponentContainer)node).getComponents();
      for (int i = 0; i < comps.length; i++) {
        checkNode(comps[i], context);
      }
    }
  }

  private void checkBinding (
    final Binding binding, 
    final Map itfs,
    final Map context) throws ADLException
  {
    if (binding.getFrom() == null) {
      throw new ADLException("'from' interface missing", (Node)binding);
    }
    if (binding.getTo() == null) {
      throw new ADLException("'to' interface missing", (Node)binding);
    }
    Interface fromItf = getInterface(binding.getFrom(), itfs);
    Interface toItf = getInterface(binding.getTo(), itfs);
    if (fromItf == null && 
        !binding.getFrom().endsWith(".component") && 
        !binding.getFrom().endsWith("-controller")) 
    {
      throw new ADLException(
        "No such interface '" + binding.getFrom() + "'", (Node)binding);
    } else if (toItf == null && 
        !binding.getTo().endsWith(".component") && 
        !binding.getTo().endsWith("-controller")) 
    {
      throw new ADLException(
        "No such interface '" + binding.getTo() + "'", (Node)binding);
    }
    if (fromItf != null && toItf != null) {
      checkBinding(binding, fromItf, toItf, context);
    }
  }

  void checkBinding (
    final Binding binding,
    final Interface fromItf,
    final Interface toItf,
    final Map context) throws ADLException
  {
  }

  Interface getInterface (final String name, final Map itfs) {
    return (Interface)itfs.get(name);
  }
}
