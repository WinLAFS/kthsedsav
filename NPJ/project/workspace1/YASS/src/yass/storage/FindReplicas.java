package yass.storage;


public interface FindReplicas {
	
	public YassResult findReplicas(String uniqueFileName, long size, int uid, int replicationDegree, Object userRef, Object leaderID, int ttl);
	public YassResult restoreReplicas(String uniqueFileName, long size, int uid, int replicationDegree, Object groupId, Object leaderID, int ttl);
}
