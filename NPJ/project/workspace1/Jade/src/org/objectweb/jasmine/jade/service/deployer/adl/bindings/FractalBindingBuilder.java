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

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.componentbinding.ComponentBinding;
import org.objectweb.jasmine.jade.service.componentdeployment.ComponentDeployment;
import org.objectweb.jasmine.jade.util.Logger;


/**
 * A Fractal based implementation of the {@link BindingBuilder} interface. 
 * This implementation uses the Fractal API to bind components. 
 */

public class FractalBindingBuilder implements BindingBuilder, BindingController {
  
  private ComponentBinding componentBinding;
  private static String[] bindingList = { "componentBinding"};
// --------------------------------------------------------------------------
  // Implementation of the BindingBuilder interface
  // --------------------------------------------------------------------------
  
  public void bindComponent (
    final int type,
    final Object client, 
    final String clientItf, 
    final Object server, 
    final String serverItf, 
    final String bindingType,
    final Object context) throws Exception
  {
    /*BindingController bc = Fractal.getBindingController((Component)client);
    Object itf;
    if (type == IMPORT_BINDING) {
      itf = Fractal.getContentController((Component)server).getFcInternalInterface(serverItf);
    } else {
      itf = ((Component)server).getFcInterface(serverItf);
    }
    
   bc.bindFc(clientItf, itf);*/
   componentBinding.bindComponent(type, client,clientItf, server, serverItf, bindingType, context);
  }

//------------------------------------------------------------------------
  // Implementation of BindingController interface
  // ------------------------------------------------------------------------

  /*
   * (non-Javadoc)
   * 
   * @see org.objectweb.fractal.api.control.BindingController#listFc()
   */
  public String[] listFc() {
      return bindingList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
   */
  public Object lookupFc(String clientItfName)
          throws NoSuchInterfaceException {
      if (clientItfName.equals("componentBinding"))
          return componentBinding;
      else
          throw new NoSuchInterfaceException(clientItfName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String,
   *      java.lang.Object)
   */
  public void bindFc(String clientItfName, Object serverItf)
          throws NoSuchInterfaceException, IllegalBindingException,
          IllegalLifeCycleException {
      if (clientItfName.equals("componentBinding")) {
      	componentBinding = (ComponentBinding) serverItf;
      } else
          throw new NoSuchInterfaceException(clientItfName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
   */

  public void unbindFc(String clientItfName) throws NoSuchInterfaceException,
          IllegalBindingException, IllegalLifeCycleException {
      if (clientItfName.equals("componentBinding"))
      	componentBinding= null;
      else
          throw new NoSuchInterfaceException(clientItfName);
  }
}
