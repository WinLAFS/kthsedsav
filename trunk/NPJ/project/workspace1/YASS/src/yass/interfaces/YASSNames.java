package yass.interfaces;

import yass.aggregators.FileReplicaAggregator;
import yass.aggregators.StorageAggregator;
import yass.managers.ConfigurationManager;
import yass.managers.CreateFileGroupManager;
import yass.managers.FileReplicaManager;
import yass.managers.MetaManager;
import yass.watchers.LoadWatcher;

public class YASSNames {

	public final static String APPLICATION_PREFIX = "Yass_0/";
	public final static String STORAGE_COMPONENT = "storage";
	public final static String FRONTEND_COMPONENT = "frontend";
	public final static String COMPOSITE_COMPONENT = "composite";
	
	public final static String TYPE_REPLICA_GROUP = "yassReplicaGroup";
	public final static String TYPE_FRONTEND_GROUP = "yassFrontendGroup";
	
	//Main group interfaces:
	
	public final static String CLIENT_INTERFACE_FILE_WRITE_REQUEST = "fileWriteRequest";
	public final static String SERVER_INTERFACE_FILE_WRITE_REQUEST = "fileWriteRequest";
	
	public final static String CLIENT_INTERFACE_FILE_WRITE_REQUEST_ACK = "fileWriteRequestAck";
	public final static String SERVER_INTERFACE_FILE_WRITE_REQUEST_ACK = "fileWriteRequestAck";

	public final static String CLIENT_INTERFACE_FILE_WRITE = "fileWrite";
	public final static String SERVER_INTERFACE_FILE_WRITE = "fileWriteS";

	public final static String CLIENT_INTERFACE_FILE_WRITE_ACK = "fileWriteAck";
	public final static String SERVER_INTERFACE_FILE_WRITE_ACK = "fileWriteAck";

	public final static String CLIENT_INTERFACE_FILE_READ = "fileRead";
	public final static String SERVER_INTERFACE_FILE_READ = "fileRead";

	public final static String CLIENT_INTERFACE_FILE_READ_ACK = "fileReadAck";
	public final static String SERVER_INTERFACE_FILE_READ_ACK = "fileReadAck";

	public final static String CLIENT_INTERFACE_FILE_REMOVE = "fileRemove";
	public final static String SERVER_INTERFACE_FILE_REMOVE  = "fileRemove";
	
	public final static String CLIENT_INTERFACE_FIND_REPLICA = "findReplicas";
	public final static String SERVER_INTERFACE_FIND_REPLICA = "findReplicasS";

	public final static String CLIENT_INTERFACE_FIND_REPLICA_ACK = "findReplicasAck";
	public final static String SERVER_INTERFACE_FIND_REPLICA_ACK = "findReplicasAckS";

	public final static String CLIENT_INTERFACE_RESTORE_REPLICA_REQUEST = "restoreReplica";
	public final static String SERVER_INTERFACE_RESTORE_REPLICA_REQUEST = "restoreReplicaRequestServer";

	
	public final static String FILE_REPLICA_AGGREGATOR_CLASS_NAME = FileReplicaAggregator.class.getName();
	public final static String FILE_REPLICA_AGGREGATOR_ADL_NAME = "FRA";

	public final static String STORAGE_AGGREGATOR_CLASS_NAME = StorageAggregator.class.getName();
	public final static String STORAGE_AGGREGATOR_ADL_NAME = "SA";
	
	public final static String CONFIGURATION_MANAGER_CLASS_NAME = ConfigurationManager.class.getName();
	public final static String CONFIGURATION_MANAGER_ADL_NAME = "CM";
	
	public final static String CREATE_FILE_GROUP_MANAGER_CLASS_NAME = CreateFileGroupManager.class.getName();
	public final static String CREATE_FILE_GROUP_MANAGER_ADL_NAME = "CFGM";
	
	public final static String FILE_REPLICA_MANAGER_CLASS_NAME = FileReplicaManager.class.getName();
	public final static String FILE_REPLICA_MANAGER_ADL_NAME = "FRM";
	
	public final static String LOAD_WATCHER_CLASS_NAME = LoadWatcher.class.getName();
	public final static String LOAD_WATCHER_ADL_NAME = "LoadWatcher";

	public final static String META_MANAGER_CLASS_NAME = MetaManager.class.getName();
	public final static String META_MANAGER_ADL_NAME = "MM";
		
	

}
