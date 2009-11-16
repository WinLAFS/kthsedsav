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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:daniel.hagimont@inrialpes.fr">Daniel Hagimont
 *
 */
public class ParseConfigFileImpl implements ParseConfigFile {
    
    String filename;
    String buffer[];
    boolean keepComments;
    int index = 0;
    
    /**
     * Creation of a parser for a configuration file
     * 
     * @param f
     *            the configuration file
     */
    public ParseConfigFileImpl(String f) {
        filename = f;
    }
    
    /**
     * open the file for parsing
     * 
     * @param buffSize The size of the buffer used for parsing
     * @param keepCmnts Should comments be kept
     * @throws JadeException
     */
    public void open(int buffSize, boolean keepCmnts) throws JadeException {
        RandomAccessFile in;
        String line;

        keepComments = keepCmnts;
        buffer = new String[buffSize];
        
        try {
            in = new RandomAccessFile(filename, "r");
            line = in.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            throw new JadeException("ParseConfigFileImpl: pb opening the file : " + filename, e);
        }
        
        while (line != null) {
            
        	try {
        		if ((!keepComments) && (line.startsWith("#"))) {
                    line = in.readLine();
                    continue;
        		}
        		
                if (index == buffer.length)
                    throw new JadeException("ParseConfigFileImpl: Buffer overflow");
                
                buffer[index++] = line;
                
                line = in.readLine();
        	} catch (IOException e) {
                    throw new JadeException("ParseConfigFileImpl: pb reading a file",
                            e);
            }
        }
        
        try {
            in.close();
        } catch (IOException e) {
            throw new JadeException("ParseConfigFileImpl: pb closing a file", e);
        }
    }
    
    /**
     * store the file after parsing
     * 
     * @throws JadeException
     */
    public void close() throws JadeException {
        
        File fname = new File(filename);
        fname.delete();
        
        RandomAccessFile output;
        try {
            output = new RandomAccessFile(filename, "rw");
        } catch (FileNotFoundException e) {
            throw new JadeException("ParseConfigFileImpl: pb creating a file", e);
        }
        int i;
        
        for (i = 0; i < index; i++)
            try {
                output.writeBytes(buffer[i] + "\n");
            } catch (IOException e) {
                throw new JadeException("ParseConfigFileImpl: pb writing a file", e);
            }
            
        try {
                output.close();
        } catch (IOException e) {
                throw new JadeException("ParseConfigFileImpl: pb closing a file", e);
        }
    }
    
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
    public String setProperty(String prop, String value) {
        StringTokenizer st;
        String tok;
        int i;
        
        for (i = 0; i < index; i++) {
        	if ((keepComments) && (buffer[i].startsWith("#"))) continue;
            st = new StringTokenizer(buffer[i], " \t\n\r", false);
            try {
                tok = st.nextToken();
            } catch (Exception ex) {
                continue;
            }
            if (tok.equals(prop)) {
            	String oldvalue = st.nextToken();
                buffer[i] = prop + " " + value;
                return oldvalue;
            }
        }
        return null;
    }
    
    /**
     * get the value associated with a property
     * 
     * @param prop
     *            the property name
     * @return the property value, null if the property was not found
     */
    public String getProperty(String prop) {
        StringTokenizer st;
        String tok;
        int i;
        
        for (i = 0; i < index; i++) {
        	if ((keepComments) && (buffer[i].startsWith("#"))) continue;
            st = new StringTokenizer(buffer[i], " \t\n\r", false);
            try {
                tok = st.nextToken();
                if (tok.equals(prop)) {
                    tok = st.nextToken();
                    return tok;
                }
            } catch (Exception ex) {
                continue;
            }
        }
        return null;
    }
    
    /**
     * substitute all occurence of a String by another String
     * 
     * @param from
     *            the string to replace
     * @param to
     *            the replacement string
     */
    public void substitute(String from, String to) {
        int i;
        
        for (i = 0; i < index; i++) {
        	if ((keepComments) && (buffer[i].startsWith("#"))) continue;
            buffer[i] = buffer[i].replaceAll(from, to);
        }
    }
    
    /**
     * append a String as a new line at the end of the file
     * 
     * @param line
     *            the string to append
     * @throws JadeException
     */
    public void append(String line) throws JadeException {
        if (index == buffer.length)
            throw new JadeException("ParseConfigFileImpl: Buffer overflow");
        buffer[index++] = line;
    }
    
    /*
     * public static void main(String args[]) {
     * 
     * ParseConfigFile p = new ParseConfigFileImpl("httpd.conf"); try { p.open(500,false); }
     * catch (JadeException e) { e.printStackTrace(); }
     * 
     * System.out.println(p.getProperty("PidFile")); p.setProperty("PidFile",
     * "/toto/titi/tutu"); p.substitute("/usr/local/",
     * "/users/hagimont/apache/");
     * 
     * p.append("vas te faire foutre du gland");
     * 
     * try { p.close(); } catch (JadeException e) {
     * 
     * e.printStackTrace(); }
     *  
     */
}
