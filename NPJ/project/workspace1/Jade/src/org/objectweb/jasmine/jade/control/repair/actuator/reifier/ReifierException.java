/**
 * Jade_self-healing
 * fr.jade.service.repair.actuator.reifier
 * ReifierException.java
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * Nov 28, 2006
 */
package org.objectweb.jasmine.jade.control.repair.actuator.reifier;

import org.objectweb.jasmine.jade.control.repair.RepairException;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 *
 */
public class ReifierException extends RepairException {

    /**
     * 
     */
    public ReifierException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param s
     * @param e
     */
    public ReifierException(String s, Exception e) {
        super(s, e);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param s
     */
    public ReifierException(String s) {
        super(s);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public ReifierException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public ReifierException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
