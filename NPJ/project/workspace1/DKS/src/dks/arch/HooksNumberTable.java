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

/**
 * The <code>HooksNumberTable</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: HooksNumberTable.java 176 2007-02-01 13:55:59Z Roberto $
 */
public class HooksNumberTable {

	public static final int HOOK_JOIN_BEFORE_REQUEST = 0;

	public static final int HOOK_JOIN_AFTER_REQUEST = 1;

	public static final int HOOK_JOIN_BEFORE_FORWARD = 2;

	public static final int HOOK_JOIN_AFTER_FORWARD = 3;

	public static final int HOOK_JOIN_BEFORE_POINT = 4;

	public static final int HOOK_JOIN_AFTER_POINT = 5;

	public static final int HOOK_JOIN_BEFORE_NEWSUCC = 6;

	public static final int HOOK_JOIN_AFTER_NEWSUCC = 7;

	public static final int HOOK_JOIN_BEFORE_NEWSUCCACC = 8;

	public static final int HOOK_JOIN_AFTER_NEWSUCCACC = 9;

	public static final int HOOK_JOIN_BEFORE_DONE = 10;

	public static final int HOOK_JOIN_AFTER_DONE = 11;

	public static final int HOOK_LEAVE_BEFORE_REQUEST = 12;

	public static final int HOOK_LEAVE_AFTER_REQUEST = 13;

	public static final int HOOK_LEAVE_BEFORE_GRANT = 14;

	public static final int HOOK_LEAVE_AFTER_GRANT = 15;

	public static final int HOOK_LEAVE_BEFORE_FORWARD = 16;

	public static final int HOOK_LEAVE_AFTER_FORWARD = 17;

	public static final int HOOK_LEAVE_BEFORE_POINT = 18;

	public static final int HOOK_LEAVE_AFTER_POINT = 19;

	public static final int HOOK_LEAVE_BEFORE_UPDATESUCC = 20;

	public static final int HOOK_LEAVE_AFTER_UPDATESUCC = 21;

	public static final int HOOK_LEAVE_BEFORE_UPDATESUCCACK = 22;

	public static final int HOOK_LEAVE_AFTER_UPDATESUCCACK = 23;

	public static final int HOOK_LEAVE_BEFORE_DONE = 24;

	public static final int HOOK_LEAVE_AFTER_DONE = 25;

	public static final int HOOK_STAB_AFTER_SUCC_LIST_RESP = 26;
}
