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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Representation and manipulation of an executable command
 * 
 * @author <a href="mailto:Fabienne.Boyer@imag.fr">Fabienne Boyer 
 * @author <a href="mailto:Daniel.Hagimont@imag.fr">Daniel Hagimont
 * 
 */
public class ExecutableCmd {

    private static Runtime rt = Runtime.getRuntime();

    /**
     * execute the command
     * 
     * @throws JadeException
     *             if the execution failed
     */
    public static void syncExec(String cmd, String envp[]) throws JadeException {
        try {
            Process p = rt.exec(cmd, envp);
            p.waitFor();
        } catch (Exception e) {
            throw new JadeException(
                    "(ExecutableCmd: cannot execute cmd " + cmd, e);
        }
    }

    /**
     * execute a command asynchronously
     * 
     * @throws JadeException
     *             if the execution failed
     */
    public static Process asyncExec(String cmd, String envp[])
            throws JadeException {
        try {
            Process p = rt.exec(cmd, envp);
            return p;
        } catch (Exception e) {
            throw new JadeException("ExecutableCmd: cannot execute cmd " + cmd,
                    e);
        }
    }

    /**
     * get the result of the command execution
     * 
     * @return the result of the command execution (null if the command returns
     *         no result)
     * @throws JadeException
     *             in case of a problem when accessing the stdout/stderr streams
     *             of the process which executed the command
     */
    public static Object getResult(Process p) throws JadeException {
        StringBuffer result = new StringBuffer();
        try {
            // get stdout and stderr streams of the process
            InputStream stdout = p.getInputStream();
            InputStream stderr = p.getErrorStream();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(stdout));
            while (br.ready()) {
                result.append("out> " + br.readLine() + "\n");
            }

            br = new BufferedReader(new InputStreamReader(stderr));
            if (br.ready()) {
                result
                        .append("----------------------------------------------\n");
            }
            while (br.ready()) {
                result.append("err> " + br.readLine() + "\n");
            }
        } catch (Exception e) {
            throw new JadeException("(ExecutableCmd: cannot get result ", e);
        }
        result.append("----------------------------------------------\n");
        return result.toString();
    }
}
