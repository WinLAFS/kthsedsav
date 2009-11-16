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

package org.objectweb.jasmine.jade.service.nodediscovery.listener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class UDPNodeDiscoveryListener extends AbstractNodeDiscoveryListener {

    private DatagramSocket socket;

    private byte[] buffer = new byte[128];

    private DatagramPacket packet;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public UDPNodeDiscoveryListener() {
    }

    // ------------------------------------------------------------------------
    // Implementation of NodeDiscoveryListener interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.service.deployment.nodediscovery.NodeDiscoveryListener#listenHeartBeat()
     */
    public void listenHeartBeat() {
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // Implementation of Runnable interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            socket = new DatagramSocket(new Integer(port).intValue());
            packet = new DatagramPacket(buffer, buffer.length);

        } catch (SocketException e) {
            e.printStackTrace();
        }
        System.out.println("[NodeDiscovery service] listen on port " + port);
        while (started && !thread.isInterrupted()) {
            listenHeartBeat();

            String message = new String(packet.getData(), 0, packet.getLength());
            StringTokenizer st = new StringTokenizer(message, ":");
            String header;
            if (st.hasMoreTokens()) {
                header = st.nextToken();

                if (st.hasMoreTokens() && header.equals("JADENODE")) {
                	String str=st.nextToken();              
                	analyser.analyse(str);
                }
            }
        }
    }
}
