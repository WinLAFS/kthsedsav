package dks.comm;

import java.math.BigInteger;

import org.apache.mina.core.future.WriteFuture;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.niche.interfaces.MessageManagerInterface;
import dks.niche.interfaces.NicheMessageInterface;

public class SendJob {
			
	NicheMessageInterface message;
	MessageManagerInterface messageManager;
	WriteFuture myWriteFuture;
	BigInteger destinationId;
	DKSRef destinationNode;
	boolean sendToNode;
	int retryCounter = 0;
	boolean connectionFailure;
	long timeStamp;
	public String messageType;
	boolean localOperation;
	
	public SendJob(String messageType) {
		this.messageType = messageType;
	}

	public SendJob(
			NicheMessageInterface message,
			BigInteger destinationId,
			DKSRef destinationNode,
			boolean sendToNode,
			MessageManagerInterface messageManager /* can be null */
	) {
		this.message = message;
		this.destinationId = destinationId;
		this.destinationNode = destinationNode;
		this.sendToNode = sendToNode;
		this.messageManager = messageManager;
		
	}
	/**
	 * @return Returns the message.
	 */
	public NicheMessageInterface getMessage() {
		return message;
	}

	/**
	 * @param message The message to set.
	 */
	public void setMessage(NicheMessageInterface message) {
		this.message = message;
	}

	/**
	 * @return Returns the messageManager.
	 */
	public MessageManagerInterface getMessageManager() {
		return messageManager;
	}

	/**
	 * @param messageManager The messageManager to set.
	 */
	public void setMessageManager(MessageManagerInterface messageManager) {
		this.messageManager = messageManager;
	}

	/**
	 * @return Returns the myWriteFuture.
	 */
	public WriteFuture getWriteFuture() {
		return myWriteFuture;
	}

	/**
	 * @param myWriteFuture The myWriteFuture to set.
	 */
	public SendJob setWriteFuture(WriteFuture writeFuture) {
		this.myWriteFuture = writeFuture;
		return this;
	}

	/**
	 * @return Returns the destinationId.
	 */
	public BigInteger getDestinationId() {
		return destinationId;
	}

	/**
	 * @param destinationId The destinationId to set.
	 */
	public void setDestinationId(BigInteger destinationId) {
		this.destinationId = destinationId;
	}

	/**
	 * @return Returns the retryCounter.
	 */
	public int getRetryCounter() {
		return retryCounter;
	}

	/**
	 * @param retryCounter The retryCounter to set.
	 */
	public void setRetryCounter(int retryCounter) {
		this.retryCounter = retryCounter;
	}

	/**
	 * @return Returns the operationId.
	 */
	public int getOperationId() {
		return message.getMessageId();
	}

	
	public SendJob setConnectionFailure(boolean connectionFailure) {
		this.connectionFailure = connectionFailure;
		return this;
	}
	public boolean sendToNode() {
		return sendToNode;
	}
	public boolean sendToId() {
		return !sendToNode;
	}
	
	
	/**
	 * @return Returns the timeStamp.
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp The timeStamp to set.
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public SendJob setLocalOperation(boolean localOperation) {
		this.localOperation = localOperation;
		return this;
	}
	public boolean isLocalOperation() {
		return localOperation;
	}

}
		