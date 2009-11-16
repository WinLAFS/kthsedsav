package yass.storage;

import dks.niche.ids.ComponentId;

public interface FileWriteAck {
	public void fileWriteSucceeded(String uniqueFileName, ComponentId initiator);
	public void fileWriteFailed(String uniqueFileName, ComponentId initiator);
}
