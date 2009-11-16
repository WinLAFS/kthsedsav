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
 * @author <a href="mailto:daniel.hagimont@inrialpes.fr">Daniel Hagimont
 *
 */
public interface ParseConfigFile {
    
    /**
     * open the file for parsing
     * 
     * @param buffSize The size of the buffer used for parsing
     * @param keepCmnts Should comments be kept
     * @throws JadeException
     */
    public void open(int buffSize, boolean keepCmnts) throws JadeException;
    
    /**
     * store the file after parsing
     * 
     * @throws JadeException
     */
    public void close() throws JadeException;
    
    /**
     * set a property 
     * the property is supposed to have the form <prop> <value>
     * 2 strings on one single line 
     * return the previous value of the property, null if not found
     * 
     * @param prop
     *            the property name
     * @param value
     *            the property value
     * @return
     * 			  the previous value of the property, null if not found
     */
    public String setProperty(String prop, String value);
    
    /**
     * get the value associated with a property
     * 
     * @param prop
     *            the property name
     * @return the property value, null if the property was not found
     */
    public String getProperty(String prop);
    
    /**
     * substitute all occurence of a String by another String
     * 
     * @param from
     *            the string to replace
     * @param to
     *            the replacement string
     */
    public void substitute(String from, String to);
    
    /**
     * append a String as a new line at the end of the file
     * 
     * @param line
     *            the string to append
     * @throws JadeException
     */
    public void append(String line) throws JadeException;
    
}
