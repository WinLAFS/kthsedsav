/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.dht;

import java.math.BigInteger;
import java.util.ArrayList;

import dks.dht.DHTComponent.getFlavor;
import dks.dht.DHTComponent.putFlavor;
import dks.dht.DHTComponent.removeFlavor;

/**
 * The <code>LocalStore</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: LocalStore.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public interface LocalStore {

    public abstract boolean put(BigInteger id, Object key, Object value,
	    putFlavor flavor);

    /**
     * @param id
     * @param key
     * @param value
     * @param flavor
     * @param multiVal if true then value must be an ArrayList of Objects
     * @return
     */
    public abstract boolean put(BigInteger id, Object key, Object value,
	    putFlavor flavor, boolean multiVal);

    public abstract Object get(BigInteger id, Object key, getFlavor flavor);

    /**
     * @param id
     * @param key
     * @param flavor
     * @param position This is used only with the GET_AT flavor.
     * @return returns an ArrayList if flavor is GET_ALL otherwise returns an object. Null if not found. Note that you must cast to the correct type.
     */
    public abstract Object get(BigInteger id, Object key, getFlavor flavor,
	    int position);

    /**
     * @param id
     * @param key
     * @param flavor
     * @param position This is used only with the REMOVE_AT flavor.
     * @return returns an ArrayList if flavor is REMOVE_ALL otherwise returns an object. Null if not found. Note that you must cast to the correct type.
     */
    public abstract Object remove(BigInteger id, Object key,
	    removeFlavor flavor, int position);

    public abstract ArrayList<Object> getRange(BigInteger start,
	    BigInteger end, int size);

    public abstract ArrayList<Object> getAll(int size);

    public abstract void putAll(Object store);

    public abstract void removeRange(BigInteger start, BigInteger end);

    public abstract void removeAll();

}