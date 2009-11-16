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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import dks.dht.DHTComponent.getFlavor;
import dks.dht.DHTComponent.putFlavor;
import dks.dht.DHTComponent.removeFlavor;

/**
 * The <code>LocalStore</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: LocalStore.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class HashtableLocalStore implements LocalStore {
    protected Hashtable<BigInteger, Hashtable<Object, ArrayList<Object>>> store;

//    public static enum getFlavor {GET_ALL, GET_ANY, GET_FIRST, GET_LAST, GET_AT};
//    public static enum putFlavor {PUT_ADD, PUT_OVERWRITE}; //PUT_AT is not implemented yet
//    public static enum removeFlavor {REMOVE_ALL, REMOVE_ANY, REMOVE_FIRST, REMOVE_LAST, REMOVE_AT};

    /**
     * 
     */
    public HashtableLocalStore() {
	super();
	store = new Hashtable<BigInteger, Hashtable<Object,ArrayList<Object>>>();
    }


    /* (non-Javadoc)
     * @see dks.dht.LocalStore#put(java.math.BigInteger, java.lang.Object, java.lang.Object, dks.dht.DHTComponent.putFlavor)
     */
    public synchronized boolean put(BigInteger id, Object key, Object value, putFlavor flavor) {
	return put(id, key, value, flavor, false);
    }


    /* (non-Javadoc)
     * @see dks.dht.LocalStore#put(java.math.BigInteger, java.lang.Object, java.lang.Object, dks.dht.DHTComponent.putFlavor, boolean)
     */
    public synchronized boolean put(BigInteger id, Object key, Object value, putFlavor flavor, boolean multiVal) {

	Hashtable<Object, ArrayList<Object>> ht = store.get(id);
	if(ht == null){ //This is the first Key associated with the ID
	    //For both flavors just add a new entry.
	    //it is possible that more than one key maps to the same ID
	    //with a very small probability in large ID spaces.
	    ht = new Hashtable<Object, ArrayList<Object>>();
	    ArrayList<Object> arr = new ArrayList<Object>();
	    if(!multiVal)
		arr.add(value);
	    else
		arr.addAll((ArrayList<Object>)value);
	    ht.put(key, arr);
	    store.put(id, ht);
	} else if(flavor == putFlavor.PUT_ADD) {
	    ArrayList<Object> arr = ht.get(key);
	    if(arr == null) { //This is a new key that maps to the ID (very rare case)
		arr = new ArrayList<Object>();
		if(!multiVal)
		    arr.add(value);
		else
		    arr.addAll((ArrayList<Object>)value);
		ht.put(key, arr);
	    } else {		//This key is already here so just add the value
		if(!multiVal)
		    arr.add(value);
		else
		    arr.addAll((ArrayList<Object>)value);
	    }
	} else if (flavor == putFlavor.PUT_OVERWRITE) {
	    //There is a Key(s) already associated with this ID
	    ArrayList<Object> arr =  new ArrayList<Object>();
	    if(!multiVal)
		arr.add(value);
	    else
		arr.addAll((ArrayList<Object>)value);
	    ht.put(key, arr);//adds a new entry or overrites an old one.
	}

	return true;

    }


    /* (non-Javadoc)
     * @see dks.dht.LocalStore#get(java.math.BigInteger, java.lang.Object, dks.dht.DHTComponent.getFlavor)
     */
    public synchronized Object get(BigInteger id, Object key, getFlavor flavor) {
	return get(id,  key, flavor, 0);
    }

    /* (non-Javadoc)
     * @see dks.dht.LocalStore#get(java.math.BigInteger, java.lang.Object, dks.dht.DHTComponent.getFlavor, int)
     */
    public synchronized Object get(BigInteger id, Object key, getFlavor flavor, int position ) {
	Hashtable<Object, ArrayList<Object>> ht = store.get(id);
	if(ht == null)
	    return null;
	ArrayList<Object> arr = ht.get(key);
	if(arr == null)
	    return null;
	Object value = null;

	switch(flavor) {
	case GET_ALL:
	    value = arr;
	    break;
	case GET_ANY:
	    int pos = (int)(Math.random() * arr.size());
	    value = arr.get(pos);
	    break;
	case GET_FIRST:
	    value = arr.get(0);
	    break;
	case GET_LAST:
	    value = arr.get(arr.size()-1);
	    break;
	case GET_AT:
	    if(position>= arr.size())
		return null;
	    value = arr.get(position);
	    break;
	}
	return value;
    }
    
    /* (non-Javadoc)
     * @see dks.dht.LocalStore#remove(java.math.BigInteger, java.lang.Object, dks.dht.DHTComponent.removeFlavor, int)
     */
    public synchronized Object remove(BigInteger id, Object key, removeFlavor flavor, int position ) {
	Hashtable<Object, ArrayList<Object>> ht = store.get(id);
	if(ht == null)
	    return null;
	ArrayList<Object> arr = ht.get(key);
	if(arr == null)
	    return null;
	Object value = null;

	switch(flavor) {
	case REMOVE_ALL:
	    value =ht.remove(key);
	    break;
	case REMOVE_ANY:
	    int pos = (int)(Math.random() * arr.size());
	    value = arr.remove(pos);
	    if(arr.size() == 0)
		ht.remove(key);
	    break;
	case REMOVE_FIRST:
	    value = arr.remove(0);
	    if(arr.size() == 0)
		ht.remove(key);
	    break;
	case REMOVE_LAST:
	    value = arr.remove(arr.size()-1);
	    if(arr.size() == 0)
		ht.remove(key);
	    break;
	case REMOVE_AT:
	    if(position>= arr.size())
		value = null;
	    else {
		value = arr.remove(position);
		if(arr.size() == 0)
			ht.remove(key);
	    }
	    break;
	}
	return value;
    }

    // Handover related functions

    /* (non-Javadoc)
     * @see dks.dht.LocalStore#getRange(java.math.BigInteger, java.math.BigInteger, int)
     */
    public synchronized ArrayList<Object> getRange(BigInteger start, BigInteger end, int size) {
	ArrayList<Object> list = new ArrayList<Object>();
	Hashtable<BigInteger, Hashtable<Object, ArrayList<Object>>> tempStore;
	tempStore = new Hashtable<BigInteger, Hashtable<Object,ArrayList<Object>>>();

	Set<Map.Entry<BigInteger, Hashtable<Object, ArrayList<Object>>>> s = store.entrySet();
	int count = 0;
	for (Map.Entry<BigInteger, Hashtable<Object, ArrayList<Object>>> e : s) {
	    BigInteger key = e.getKey();
	    if(start.compareTo(end) < 0) {
		if( key.compareTo(start) >= 0 && key.compareTo(end) <= 0) {
		    tempStore.put(key,e.getValue());
		    count++;
		}
	    } else {
		if(key.compareTo(start) <= 0 || key.compareTo(end) >= 0) {
		    tempStore.put(key,e.getValue());
		    count++;
		}
	    }
	    if(count == size) {
		count = 0;
		list.add(tempStore);
		tempStore = new Hashtable<BigInteger, Hashtable<Object,ArrayList<Object>>>();

	    }

	}
	if(tempStore.size() != 0 || list.size() == 0)
	    list.add(tempStore);
	return list;
    }

    /* (non-Javadoc)
     * @see dks.dht.LocalStore#getAll(int)
     */
    public synchronized ArrayList<Object> getAll(int size) {

	ArrayList<Object> list = new ArrayList<Object>();
	Hashtable<BigInteger, Hashtable<Object, ArrayList<Object>>> tempStore;
	tempStore = new Hashtable<BigInteger, Hashtable<Object,ArrayList<Object>>>();

	Set<Map.Entry<BigInteger, Hashtable<Object, ArrayList<Object>>>> s = store.entrySet();
	int count = 0;
	for (Map.Entry<BigInteger, Hashtable<Object, ArrayList<Object>>> e : s) {

	    tempStore.put(e.getKey(),e.getValue());
	    count++;
	    if(count == size) {
		count = 0;
		list.add(tempStore);
		tempStore = new Hashtable<BigInteger, Hashtable<Object,ArrayList<Object>>>();

	    }

	}
	if(tempStore.size() != 0 || list.size() == 0)
	    list.add(tempStore);
	return list;
    }

    /* (non-Javadoc)
     * @see dks.dht.LocalStore#putAll(java.lang.Object)
     */
    public synchronized void putAll(Object store) {
	this.store.putAll((Hashtable<BigInteger, Hashtable<Object, ArrayList<Object>>>)store);
    }

    /* (non-Javadoc)
     * @see dks.dht.LocalStore#removeRange(java.math.BigInteger, java.math.BigInteger)
     */
    public synchronized void removeRange(BigInteger start, BigInteger end) {

	//Set<Map.Entry<BigInteger, Hashtable<Object, ArrayList<Object>>>> s = store.entrySet();

	//for (Map.Entry<BigInteger, Hashtable<Object, ArrayList<Object>>> e : s) {
	ArrayList<BigInteger> remKeys = new ArrayList<BigInteger>();
	Enumeration<BigInteger> en = store.keys();
	while (en.hasMoreElements()) {
	    BigInteger key = en.nextElement();
	    if(start.compareTo(end) < 0) {
		if( key.compareTo(start) >= 0 && key.compareTo(end) <= 0) {
		    remKeys.add(key);
		}
	    } else {
		if(key.compareTo(start) <= 0 || key.compareTo(end) >= 0) {
		    remKeys.add(key);
		}
	    }
	}
	
	for (BigInteger key : remKeys) {
	    store.remove(key);
	}
    }
    
    /* (non-Javadoc)
     * @see dks.dht.LocalStore#removeAll()
     */
    public synchronized void removeAll() {
	store.clear();
    }

}
