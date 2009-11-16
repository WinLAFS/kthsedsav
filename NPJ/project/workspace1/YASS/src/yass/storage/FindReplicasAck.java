package yass.storage;

public interface FindReplicasAck {
	public void replicaStorageAccepted(Object replicaGlobalID, String uniqueFileName, int ttl, Object leaderID);
	public void replicaStoregeDenied(Object replicaGlobalID, String uniqueFileName, int ttl, Object leaderID);
	public void replicaRestoreAccepted(Object replicaGlobalID, String uniqueFileName, int ttl, Object groupId, Object leaderID);
	public void replicaRestoreDenied(Object replicaGlobalID, String uniqueFileName, int ttl, Object groupId, Object leaderID);
}
