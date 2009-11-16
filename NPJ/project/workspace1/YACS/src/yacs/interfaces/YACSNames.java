package yacs.interfaces;

import yacs.job.watchers.MasterWatcher;
import yacs.job.watchers.WorkerWatcher;
import yacs.managers.CreateJobGroupManager;
import yacs.managers.Watchdog;
import yacs.resources.watchers.ServiceWatcher;
import yacs.resources.aggregators.ServiceAggregator;
import yacs.job.aggregators.MasterAggregator;
import yacs.job.aggregators.WorkerAggregator;
import yacs.managers.ConfigurationManager;

public class YACSNames {

	public final static String APPLICATION_PREFIX = "Yacs_0/";
	public final static String FRONTEND_COMPONENT = "frontend";
	public final static String COMPOSITE_COMPONENT = "composite";
	public final static String MASTER_COMPONENT = "master";
	public final static String WORKER_COMPONENT = "worker";
	public final static String RESOURCE_SERVICE_COMPONENT = "resourceService";
	
	public final static boolean DUMMY_PARAM = false;
	
	// classes and components
	public final static String WORKER_WATCHER_CLASS_NAME = WorkerWatcher.class.getName();
	public final static String WORKER_WATCHER_ADL_NAME = "WW";
	
	public final static String MASTER_WATCHER_CLASS_NAME = MasterWatcher.class.getName();
	public final static String MASTER_WATCHER_ADL_NAME = "MW";
		
	public final static String CREATE_JOB_GROUP_MANAGER_CLASS_NAME = CreateJobGroupManager.class.getName();
	public final static String CREATE_JOB_GROUP_MANAGER_ADL_NAME = "CJGM";
	
	public final static String WATCHDOG_CLASS_NAME = Watchdog.class.getName();
	public final static String WATCHDOG_ADL_NAME = "WD";
	
	public final static String SERVICE_WATCHER_CLASS_NAME = ServiceWatcher.class.getName();
	public final static String SERVICE_WATCHER_ADL_NAME = "SW";
	
	public final static String SERVICE_AGGREGATOR_CLASS_NAME = ServiceAggregator.class.getName();
	public final static String SERVICE_AGGREGATOR_ADL_NAME = "SA";
	
	public final static String MASTER_AGGREGATOR_CLASS_NAME = MasterAggregator.class.getName();
	public final static String MASTER_AGGREGATOR_ADL_NAME = "MA";
	
	public final static String WORKER_AGGREGATOR_CLASS_NAME = WorkerAggregator.class.getName();
	public final static String WORKER_AGGREGATOR_ADL_NAME = "WA";
	
	public final static String CONFIGURATION_MANAGER_CLASS_NAME = ConfigurationManager.class.getName();
	public final static String CONFIGURATION_MANAGER_ADL_NAME = "CM";
	
	// logical constants
	public final static int TASK_NOT_INITIALIZED = -1;
	public final static int TASK_IS_PROCESSING = 0;
	public final static int TASK_COMPLETED = 1;
	public final static int TASK_FAILED = 2;
	
	public final static int RESULT_NOT_SET = -1;
	public final static int RESULT_OK = 0;
	public final static int RESULT_ERROR = 1;
	public final static int RESULT_TASK_INITALIZATION_FAILED = 2;
	
	public final static long AVAILABILITY_STATUS__BUSY = 0;
	public final static long AVAILABILITY_STATUS__FREE = 1;
	public final static long AVAILABILITY_STATUS__PRELIMINARILY_ASSIGNED = 2;
	
	public final static boolean DEFAULT_REDEPLOYABLE = true;

	// interfaces	
	public final static String JOB_MANAGEMENT_CLIENT_INTERFACE = "jobManagementClient";
	public final static String JOB_MANAGEMENT_SERVER_INTERFACE = "jobManagementServer";
	
	public final static String TASK_MANAGEMENT_CLIENT_INTERFACE = "taskManagementClient";
	public final static String TASK_MANAGEMENT_SERVER_INTERFACE = "taskManagementServer";
	
	public final static String INFORMATION_CLIENT_INTERFACE = "informationInterfaceClient";
	public final static String INFORMATION_SERVER_INTERFACE = "informationInterfaceServer";
		
	public final static String MASTER_CLIENT_INTERFACE = "masterInterfaceClient";
	public final static String MASTER_SERVER_INTERFACE = "masterInterfaceServer";
	
	public final static String WORKER_GROUP_CLIENT_INTERFACE = "workerGroupInterface";
	public final static String WORKER_GROUP_SERVER_INTERFACE = "workerGroupInterface";
	
	public final static String STATE_CHANGE_CLIENT_INTERFACE = "stateChangeInterface";
	public final static String STATE_CHANGE_SERVER_INTERFACE = "stateChangeInterface";
	
	public final static String JOB_RESULT_CLIENT_INTERFACE = "jobResult";
	public final static String JOB_RESULT_SERVER_INTERFACE = "jobResult";
	
	// interfaces - resources
	public final static String RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE = "resourceServiceRequest";
	public final static String RESOURCE_SERVICE_REQUEST_SERVER_INTERFACE = "resourceServiceRequest";
	
	public final static String RESOURCE_SERVICE_STATE_CLIENT_INTERFACE = "resourceServiceState";
	public final static String RESOURCE_SERVICE_STATE_SERVER_INTERFACE = "resourceServiceState";
	public final static String RESOURCE_SERVICE_STATE_MASTER_CLIENT_INTERFACE = "resourceServiceState";
	public final static String RESOURCE_SERVICE_STATE_MASTER_SERVER_INTERFACE = "resourceServiceState";
	
	public final static String RESOURCE_MANAGEMENT_CLIENT_INTERFACE = "resourceManagementClient";
	public final static String RESOURCE_MANAGEMENT_SERVER_INTERFACE = "resourceManagementServer";
	public final static String RESOURCE_MANAGEMENT_INIT_CLIENT_INTERFACE = "resourceManagementInitClient";
	public final static String RESOURCE_MANAGEMENT_INIT_SERVER_INTERFACE = "resourceManagementInitServer";
	
	public final static String LOAD_STATE_CLIENT_INTERFACE = "loadState";
	public final static String LOAD_STATE_SERVER_INTERFACE = "loadState";
	
	
	
	// TODO: "deploySensor" should be a DCMS constant
	public final static String DEPLOY_SENSOR = "deploySensor";
}
