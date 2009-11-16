package yass.storage;

import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;

public interface FileWriteRequestAck {
	public void fileWriteRequestAccepted(String uniqueFileName, GroupId globalFileGroupID, long timeUsed, int hopsUsed, ComponentId initiator);
	public void fileWriteRequestDenied(String uniqueFileName, int hopsUsed, ComponentId initiator);
}
