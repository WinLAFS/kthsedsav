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

package org.objectweb.jasmine.jade.util;

/**
 * @author <a href="mailto:Daniel.Hagimont@imag.fr">Daniel Hagimont
 * 
 */
public class JadeException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 8424595736358981184L;

    Exception nested_exception = null;

    /**
     * 
     */
    public JadeException() {
        super();
    }

    /**
     * Create a nested exception.
     * 
     * @param s
     *            Comment associtaed with the exception
     * @param e
     *            Nested exception
     */
    public JadeException(String s, Exception e) {
        super(s);
        nested_exception = e;
    }

    /**
     * Create an exception
     * 
     * @param s
     *            Comment associtaed with the exception
     */
    public JadeException(String s) {
        super(s);

    }

    /**
     * @param message
     * @param cause
     */
    public JadeException(String message, Throwable cause) {
        super(message, cause);
        nested_exception = new Exception(cause);
    }

    /**
     * @param cause
     */
    public JadeException(Throwable cause) {
        super(cause);
        nested_exception = new Exception(cause);
    }

    /**
     * Return the nested exception associated with an exception
     * 
     * @return The nested exception
     */
    public Exception getNestedException() {
        return nested_exception;
    }

}
