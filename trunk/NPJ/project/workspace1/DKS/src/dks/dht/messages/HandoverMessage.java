/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.dht.messages;

import dks.addr.DKSRef;

/**
 * The <code>HandoverMessage</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: HandoverMessage.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public interface HandoverMessage {

    public Object getBuffer();

    public void setBuffer(Object buffer);

    public int getChunkID();

    public void setChunkID(int chunkID);

    public int getTotalChunks();

    public void setTotalChunks(int totalChunks);

//  for local use! not serialized
    public DKSRef getFrom();

//  for local use! not serialized
    public void setFrom(DKSRef from);

//  for local use! not serialized
    public DKSRef getTo();

//  for local use! not serialized
    public void setTo(DKSRef to);

}
