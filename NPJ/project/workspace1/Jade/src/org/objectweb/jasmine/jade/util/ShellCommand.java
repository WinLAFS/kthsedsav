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
 * Representation and manipulation of an executable command
 * 
 * @author <a href="mailto:Fabienne.Boyer@imag.fr">Fabienne Boyer
 * @author <a href="mailto:Daniel.Hagimont@imag.fr">Daniel Hagimont
 * 
 */
public class ShellCommand {
    private static Runtime runtime = Runtime.getRuntime();

    /**
     * execute a command asynchronously don't redirect stdout or stderr
     * 
     * @throws JadeException
     *             if the execution failed
     */
    public static ShellCommand asyncExec(String cmd, String envp[])
            throws JadeException {
        try {
            Process process = runtime.exec(cmd, envp);
            return new ShellCommand(cmd, process);
        } catch (Exception e) {
            throw new JadeException("ShellCommand: cannot execute cmd " + cmd,
                    e);
        }
    }

    /**
     * execute the command synchronously redirect both stdout and stderr
     * 
     * @throws JadeException
     *             if the execution failed
     */
    public static void syncExec(String cmd, String envp[]) throws JadeException {
        try {
            ShellCommand shcmd = asyncExec(cmd, envp);
            shcmd.forwardAll();
            shcmd.process.waitFor();
        } catch (Exception e) {
            throw new JadeException("ShellCommand: cannot execute cmd " + cmd,
                    e);
        }
    }

    public String command;

    public Process process;

    public StreamForwarder outFwder;

    public StreamForwarder errFwder;

    private ShellCommand(String command, Process process) {
        this.command = command;
        this.process = process;
    }

    public void forwardStdoutAndWaitForString(String prefix, String wantedStr)
            throws JadeException {
        if (outFwder != null)
            return;

        outFwder = new StreamForwarder(prefix, process.getInputStream(),
                System.out);
        if (wantedStr != null)
            outFwder.prepareWaitForString(wantedStr);
        outFwder.start();
        if (wantedStr != null)
            outFwder.waitForString();
    }

    public void forwardStdout(String prefix) throws JadeException {
        forwardStdoutAndWaitForString(prefix, null);
    }

    public void forwardStdoutAndWaitForString(String wantedStr)
            throws JadeException {
        forwardStdoutAndWaitForString("[" + command + "] out> ", wantedStr);
    }

    public void forwardStdout() throws JadeException {
        forwardStdoutAndWaitForString(null);
    }

    public void forwardStderr(String prefix) {
        if (errFwder != null)
            return;

        errFwder = new StreamForwarder(prefix, process.getErrorStream(),
                System.err);
        errFwder.start();
    }

    public void forwardStderr() throws JadeException {
        forwardStderr("[" + command + "] err> ");
    }

    public void forwardAll(String prefix) throws JadeException {
        forwardStdout(prefix);
        forwardStderr(prefix);
    }

    public void forwardAll() throws JadeException {
        forwardStdout();
        forwardStderr();
    }
}
