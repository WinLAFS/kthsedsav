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
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author <a href="mailto:christophe.taton@inrialpes.fr">Christophe Taton
 * 
 */
public class StreamForwarder extends Thread {
    private OutputStream ostream;

    private InputStream istream;

    private String prefix;

    private boolean found;

    private String wantedStr;

    public StreamForwarder(String prefix, InputStream istream,
            OutputStream ostream) {
        this.ostream = ostream;
        this.istream = istream;
        this.prefix = prefix;
    }

    public void prepareWaitForString(String str) throws JadeException {
        synchronized (this) {
            if (wantedStr != null)
                throw new JadeException("Only one grep at a time");
            wantedStr = str;
            found = false;
        }
    }

    public void waitForString() throws JadeException {
        synchronized (this) {
            if (!found) {
                if (wantedStr == null)
                    throw new JadeException("No search prepared");
                try {
                    wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(istream));
        PrintStream pstream = new PrintStream(ostream);

        pstream.println(prefix + "-- begin --");
        try {
            while (true) {
                String next = br.readLine();
                if (next == null)
                    break;
                pstream.println(prefix + next);
                synchronized (this) {
                    if (wantedStr != null && next.indexOf(wantedStr) != -1) {
                        found = true;
                        wantedStr = null;
                        notify();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pstream.println(prefix + "--- end ---");
        synchronized (this) {
            if (wantedStr != null) {
                Logger
                        .println("WARNING: StreamForwarder.waitForStdout never returned! Releasing thread");
                found = true;
                wantedStr = null;
                notify();
            }
        }
    }
}
