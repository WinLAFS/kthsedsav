package yass.interfaces;

import yass.storage.YassResult;
import dks.niche.ids.GroupId;

public interface ReplicaRestoreRequest {
	public YassResult replicaRestoreRequest(GroupId groupId);
}
