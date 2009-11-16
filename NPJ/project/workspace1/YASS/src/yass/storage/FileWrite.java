package yass.storage;

import java.io.File;

import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;

public interface FileWrite {
	
	//Used by the frontEnd(s)
	public void fileWrite(String pendingWriteFileName, ComponentId initiator, File f, GroupId id);

	//Used to restore replicas
	public void replicaFileWrite(String uniqueFileName, File theFile, GroupId fileGroup, ComponentId destination); 

}
