/**
 * Jade_self-healing
 * fr.jade.service.repair.actuator.reifier
 * Reifier.java
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * Nov 28, 2006
 */
package org.objectweb.jasmine.jade.control.repair.actuator.reifier;

import org.objectweb.fractal.api.Component;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 *
 */
public interface Reifier {
    
    /**
     * @param metaComponent
     * @throws ReifierException
     */
    void propagate(Component metaComponent) throws ReifierException;

}
