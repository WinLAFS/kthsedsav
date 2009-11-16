package yass.storage;

import dks.niche.ids.ComponentId;

public interface FileRead {
	public YassResult fileRead(String uniqueFileName, ComponentId initiator, boolean flag);
}
