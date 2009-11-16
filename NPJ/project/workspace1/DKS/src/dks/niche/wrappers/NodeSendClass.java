package dks.niche.wrappers;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.niche.ids.BindElement;
import dks.niche.ids.ComponentId;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.MessageManagerInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.messages.SendThroughBindingMessage;

public class NodeSendClass implements MessageManagerInterface {

		
//		String id;
		DKSRef receiver;
		
		Message message;
		
		boolean sendFailure = false; 

		NicheAsynchronousInterface myCommunicator;
		
		MessageManagerInterface messageManager;

		public NodeSendClass(NicheAsynchronousInterface communicator, DKSRef receiver, Message message, MessageManagerInterface messageManager) {
		
			this.myCommunicator = communicator;
			this.message = message;
			this.receiver = receiver;
			this.messageManager = messageManager;
		}

		public void run() {
			
			if(sendFailure) {

				String logMessage = "NodeSendClass says: " +message.getClass().getSimpleName() + " to " + receiver.toString() +" could NOT be delivered. ";
				
				if (messageManager != null) {
					
					if (messageManager.invokeOnChannelError()) {
						messageManager.notify(1, message);
						//myThreadPool.execute(messageManager);
						//return;
						System.err.println("ERROR - Cannot execute message manager");
						logMessage = "ERROR - Cannot execute message manager";
					} else {
						logMessage += "I had a messageManager, but it didn't care about the nodeError ";
					}
				} else {
					logMessage += "No messageManager present, dropping the message";
				}

				/*#%*/ myCommunicator.log(logMessage);
			}
		}

		/* (non-Javadoc)
		 * @see dks.niche.interfaces.NicheNotifyInterface#notify(java.lang.Object)
		 */
		public void notify(Object message) {
			
			//sendFailure = error;
			
		}

		/* (non-Javadoc)
		 * @see dks.niche.interfaces.IdentifierInterface#getId()
		 */
		public String getMessageManagerId() {
			return null;
		}
		
		public boolean invokeOnIdError() {
			return true;
		}

		public boolean invokeOnNodeError() {
			return true;
		}

		/* (non-Javadoc)
		 * @see dks.niche.interfaces.MessageManagerInterface#dropMessage()
		 */
		@Override
		public boolean dropMessage() {
			// TODO Auto-generated method stub
			return false;
		}

		/* (non-Javadoc)
		 * @see dks.niche.interfaces.MessageManagerInterface#notify(int, java.lang.Object)
		 */
		@Override
		public void notify(int statusCode, Object object) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see dks.niche.interfaces.MessageManagerInterface#invokeOnChannelError()
		 */
		@Override
		public boolean invokeOnChannelError() {
			// TODO Auto-generated method stub
			return false;
		}

		/* (non-Javadoc)
		 * @see dks.niche.interfaces.MessageManagerInterface#invokeOnSendSuccess()
		 */
		@Override
		public boolean invokeOnSendSuccess() {
			// TODO Auto-generated method stub
			return false;
		}


		
	}

