/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.messages;

import dks.bcast.messages.DirectIntervalAggregationSubTotalMessage;
import dks.bcast.messages.PseudoReliableIntervalBroadcastAckMessage;
import dks.bcast.messages.PseudoReliableIntervalBroadcastMessage;
import dks.bcast.messages.SimpleIntervalBroadcastMessage;
import dks.dht.messages.GetRequestMessage;
import dks.dht.messages.GetResponseMessage;
import dks.dht.messages.PredecessorHandoverMessage;
import dks.dht.messages.PutAckMessage;
import dks.dht.messages.PutRequestMessage;
import dks.dht.messages.RemoveAckMessage;
import dks.dht.messages.RemoveRequestMessage;
import dks.dht.messages.SuccessorHandoverMessage;

/**
 * The <code>MessageTypeTable</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: MessageTypeTable.java 496 2007-12-20 15:39:02Z roberto $
 */
public class MessageTypeTable {

	public static final int MSG_TYPE_HELLO = 0;

	public static final int MSG_TYPE_JOIN_REQ = 1;

	public static final int MSG_TYPE_JOIN_RETRY = 2;

	public static final int MSG_TYPE_JOIN_POINT = 3;

	public static final int MSG_TYPE_JOIN_NEW_SUCC = 4;

	public static final int MSG_TYPE_JOIN_NEW_SUCC_ACK = 5;

	public static final int MSG_TYPE_JOIN_DONE = 6;

	public static final int MSG_TYPE_LEAVE_REQ = 7;

	public static final int MSG_TYPE_LEAVE_GRANT = 8;

	public static final int MSG_TYPE_LEAVE_RETRY = 9;

	public static final int MSG_TYPE_LEAVE_POINT = 10;

	public static final int MSG_TYPE_LEAVE_UPDATE_SUCC = 11;

	public static final int MSG_TYPE_LEAVE_UPDATE_SUCC_ACK = 12;

	public static final int MSG_TYPE_LEAVE_DONE = 13;

	public static final int MSG_TYPE_STAB_GET_PRED_REQ = 14;

	public static final int MSG_TYPE_STAB_GET_PRED_RESP = 15;

	public static final int MSG_TYPE_STAB_GET_SUCC_LIST_REQ = 16;

	public static final int MSG_TYPE_STAB_GET_SUCC_LIST_RESP = 17;

	public static final int MSG_TYPE_STAB_NOTIFY = 18;

	public static final int MSG_TYPE_DUMMY = 19;

	public static final int MSG_TYPE_TRANSITIVE_LOOKUP_OP = 20;

	public static final int MSG_TYPE_TRANSITIVE_LOOKUP_OP_REPLY = 21;

	public static final int MSG_TYPE_RECURSIVE_LOOKUP_OP = 22;

	public static final int MSG_TYPE_RECURSIVE_LOOKUP_OP_REPLY = 23;

	public static final int MSG_TYPE_DELIVER_MESSAGE = 24;

	public static final int MSG_TYPE_IDENTIFIER_ALREADY_TAKEN = 25;

	public static final int MSG_TYPE_CHECK_LIVENESS = 26;

	public static final int MSG_TYPE_ADD_BL_ENTRY = 27;

	public static final int MSG_TYPE_ADD_BL_ENTRY_ACK = 28;

	public static final int MSG_TYPE_REM_RT_ENTRY = 29;

	public static final int MSG_TYPE_REM_RT_ENTRY_ACK = 30;

	// Added by Ahmad
	public static final int MSG_TYPE_SIMPLE_INTERVAL_BROADCAST = 31;

	// Added by Ahmad
	public static final int MSG_TYPE_DIRECT_INTERVAL_AGGREGATION_SUBTOTAL = 32;

	// Added by Ahmad
	public static final int MSG_TYPE_PSEUDO_RELIABLE_INTERVAL_BROADCAST = 33;

	// Added by Ahmad
	public static final int MSG_TYPE_PSEUDO_RELIABLE_INTERVAL_BROADCAST_ACK = 34;

	// Added by Joel
	public static final int MSG_TYPE_PUT_REQUEST = 35;

	// Added by Joel
	public static final int MSG_TYPE_PUT_ACK = 36;

	// Added by Joel
	public static final int MSG_TYPE_GET_REQUEST = 37;

	// Added by Joel
	public static final int MSG_TYPE_GET_RESPONSE = 38;

	public static final int MSG_TYPE_REMOVE_REQUEST = 39;

	public static final int MSG_TYPE_REMOVE_ACK = 40;

	// Added by Ahmad
	public static final int MSG_TYPE_DHT_SUCC_HANDOVER = 41;

	// Added by Ahmad
	public static final int MSG_TYPE_DHT_PRED_HANDOVER = 42;

	public static final int MSG_TYPE_JOIN_RES = 43;

	public static final int MSG_TYPE_SUCC_LEAVE = 44;

	public static final int MSG_TYPE_PRED_LEAVE = 45;

	public static Class[] messageTypes = new Class[MessageTypeTable.class
			.getFields().length - 1];

	static {
		messageTypes[MSG_TYPE_HELLO] = HelloMessage.class;
		messageTypes[MSG_TYPE_JOIN_REQ] = JoinRequestMessage.class;
		messageTypes[MSG_TYPE_JOIN_RETRY] = RetryJoinMessage.class;
		messageTypes[MSG_TYPE_JOIN_POINT] = JoinPointMessage.class;
		messageTypes[MSG_TYPE_JOIN_NEW_SUCC] = JoinNewSuccMessage.class;
		messageTypes[MSG_TYPE_JOIN_NEW_SUCC_ACK] = JoinNewSuccAckMessage.class;
		messageTypes[MSG_TYPE_JOIN_DONE] = JoinDoneMessage.class;
		messageTypes[MSG_TYPE_LEAVE_REQ] = LeaveReqMessage.class;
		messageTypes[MSG_TYPE_LEAVE_GRANT] = GrantLeaveMessage.class;
		messageTypes[MSG_TYPE_LEAVE_RETRY] = RetryLeaveMessage.class;
		messageTypes[MSG_TYPE_LEAVE_POINT] = LeavePointMessage.class;
		messageTypes[MSG_TYPE_LEAVE_UPDATE_SUCC] = LeaveUpdateSuccMessage.class;
		messageTypes[MSG_TYPE_LEAVE_UPDATE_SUCC_ACK] = LeaveUpdateSuccessorAckMessage.class;
		messageTypes[MSG_TYPE_LEAVE_DONE] = LeaveDoneMessage.class;
		messageTypes[MSG_TYPE_STAB_GET_PRED_REQ] = StabGetPredecessorReqMessage.class;
		messageTypes[MSG_TYPE_STAB_GET_PRED_RESP] = StabGetPredecessorRespMessage.class;
		messageTypes[MSG_TYPE_STAB_GET_SUCC_LIST_REQ] = StabGetSuccListReqMessage.class;
		messageTypes[MSG_TYPE_STAB_GET_SUCC_LIST_RESP] = StabGetSuccListRespMessage.class;
		messageTypes[MSG_TYPE_STAB_NOTIFY] = StabNotifyMessage.class;
		messageTypes[MSG_TYPE_TRANSITIVE_LOOKUP_OP] = TransitiveLookupOperationRequestMessage.class;
		messageTypes[MSG_TYPE_TRANSITIVE_LOOKUP_OP_REPLY] = TransitiveLookupOperationResponseMessage.class;
		messageTypes[MSG_TYPE_RECURSIVE_LOOKUP_OP] = RecursiveLookupOperationRequestMessage.class;
		messageTypes[MSG_TYPE_RECURSIVE_LOOKUP_OP_REPLY] = RecursiveLookupOperationResponseMessage.class;
		messageTypes[MSG_TYPE_DELIVER_MESSAGE] = DeliverMessage.class;
		messageTypes[MSG_TYPE_IDENTIFIER_ALREADY_TAKEN] = RingIdentifierAlreadyTakenMessage.class;
		messageTypes[MSG_TYPE_CHECK_LIVENESS] = CheckLivenessMessage.class;
		messageTypes[MSG_TYPE_ADD_BL_ENTRY] = AddBackListEntryMessage.class;
		messageTypes[MSG_TYPE_ADD_BL_ENTRY_ACK] = AddBackListEntryAckMessage.class;
		messageTypes[MSG_TYPE_REM_RT_ENTRY] = RemEntryMessage.class;
		messageTypes[MSG_TYPE_REM_RT_ENTRY_ACK] = RemEntryAckMessage.class;
		messageTypes[MSG_TYPE_SIMPLE_INTERVAL_BROADCAST] = SimpleIntervalBroadcastMessage.class;
		messageTypes[MSG_TYPE_DIRECT_INTERVAL_AGGREGATION_SUBTOTAL] = DirectIntervalAggregationSubTotalMessage.class;
		messageTypes[MSG_TYPE_PSEUDO_RELIABLE_INTERVAL_BROADCAST] = PseudoReliableIntervalBroadcastMessage.class;
		messageTypes[MSG_TYPE_PSEUDO_RELIABLE_INTERVAL_BROADCAST_ACK] = PseudoReliableIntervalBroadcastAckMessage.class;

		messageTypes[MSG_TYPE_PUT_REQUEST] = PutRequestMessage.class;
		messageTypes[MSG_TYPE_PUT_ACK] = PutAckMessage.class;
		messageTypes[MSG_TYPE_GET_REQUEST] = GetRequestMessage.class;
		messageTypes[MSG_TYPE_GET_RESPONSE] = GetResponseMessage.class;
		messageTypes[MSG_TYPE_REMOVE_REQUEST] = RemoveRequestMessage.class;
		messageTypes[MSG_TYPE_REMOVE_ACK] = RemoveAckMessage.class;

		messageTypes[MSG_TYPE_DHT_SUCC_HANDOVER] = SuccessorHandoverMessage.class;
		messageTypes[MSG_TYPE_DHT_PRED_HANDOVER] = PredecessorHandoverMessage.class;

		messageTypes[MSG_TYPE_JOIN_RES] = JoinResponseMessage.class;

		messageTypes[MSG_TYPE_SUCC_LEAVE] = SuccessorLeaveMessage.class;
		messageTypes[MSG_TYPE_PRED_LEAVE] = PredecessorLeaveMessage.class;

	}

	public static int getMessageType(Class messageClass) {
		for (int i = 0; i < MessageTypeTable.messageTypes.length; i++) {
			Class temp = MessageTypeTable.messageTypes[i];
			if (temp.equals(messageClass))
				return i;
		}
		return -1;
	}

	public static Class getMessageTypeClass(int messageType) {
		return messageTypes[messageType];
	}

}
