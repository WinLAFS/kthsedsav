/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.arch;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The <code>HooksRegistry</code> class
 * 
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: HooksRegistry.java 261 2007-03-27 16:07:37Z Roberto $
 */
public class HooksRegistry {

	private ConcurrentHashMap<Integer, Set<Hook>> hooksRegistry;

	/**
	 * Registry used to manage and execute various types of hooks that must be
	 * executed blockingly and sequencially at some point of the execution of a
	 * component
	 */
	public HooksRegistry() {
		hooksRegistry = new ConcurrentHashMap<Integer, Set<Hook>>();
	}

	public void callHook(int hooknumber) {
		Set<Hook> hooksSet = hooksRegistry.get(hooknumber);
		if (hooksSet != null) {
			for (Hook hook : hooksSet) {
				try {
					hook.getHandler().invoke(hook.getComponent(),
							new Object[] {null});
				} catch (IllegalArgumentException e) {
					// TODO Handle exception
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Handle exception
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Handle exception
					e.printStackTrace();
				}
			}
		}
	}

	public void callHook(int hooknumber, Object attachment) {
		Set<Hook> hooksSet = hooksRegistry.get(hooknumber);
		if (hooksSet != null) {
			for (Hook hook : hooksSet) {
				try {
					hook.getHandler().invoke(hook.getComponent(), attachment);
				} catch (IllegalArgumentException e) {
					// TODO Handle exception
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Handle exception
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Handle exception
					e.printStackTrace();
				}
			}
		}
	}

	public void registerHook(int hookNumber, Hook hook) {
		if (!hooksRegistry.containsKey(hookNumber)) {
			Set<Hook> set = new HashSet<Hook>();
			set.add(hook);
			hooksRegistry.put(hookNumber, set);
		} else {
			Set set = hooksRegistry.get(hookNumber);
			set.add(hook);
		}
	}

}
