/**
 * Jade_self-healing
 * fr.jade.service.repair
 * RepairException.java
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * Nov 28, 2006
 */
package org.objectweb.jasmine.jade.control.repair;

import org.objectweb.jasmine.jade.util.JadeException;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 *
 */
public class RepairException extends JadeException {

    /**
     * 
     */
    public RepairException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param s
     * @param e
     */
    public RepairException(String s, Exception e) {
        super(s, e);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param s
     */
    public RepairException(String s) {
        super(s);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public RepairException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public RepairException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
