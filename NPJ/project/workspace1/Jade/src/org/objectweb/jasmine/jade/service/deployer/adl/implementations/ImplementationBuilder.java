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

package org.objectweb.jasmine.jade.service.deployer.adl.implementations;

/**
 * A builder interface to create components, using a package decriptor.
 * <p>
 * This interface is an extension of the
 * {@link org.objectweb.fractal.adl.implementations.ImplementationBuilder}.
 * <p>
 * Contributors : <a href="mailto:jakub.kornas@inrialpes.fr">Jakub Kornas</a>
 * 
 */

public interface ImplementationBuilder {
  
  Object createComponent (
    Object type, 
    String name,
    String definition,
    Object controllerDesc, 
    Object contentDesc,
    Object[] packageDesc,
    Object context) throws Exception;
}
