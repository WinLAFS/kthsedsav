package yass.storage;

import java.io.File;

import dks.niche.ids.ComponentId;

public interface FileReadAck {
	public void fileReadSuccessful(String uniqueFileName, File file, ComponentId initiator);
	public void fileReadFailed(String uniqueFileName, String errorMessage, ComponentId initiator);
}
