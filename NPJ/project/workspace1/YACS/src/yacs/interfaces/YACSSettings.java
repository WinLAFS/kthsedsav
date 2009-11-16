package yacs.interfaces;

public class YACSSettings {
	
	/**
	 * Rough layout
	 * - Service settings
	 * - Individual ME component settings
	 * - Individual functional component settings
	 * - Monitoring
	 * - TEST settings
	 */
	
	// Other
	public static final String YACS_VERSION = "@YACS v.2009-05-09 >10:36";
	
	
	// SERVICE SETTINGS
	// YACS service self-management
	// Enable the (RS) ServiceWatcher ME
	public static final boolean RESOURCE_SERVICE_SELF_MANAGEMENT = System.getProperty("yacs.res.service.self.mgmt") instanceof String ?
			(0 < Integer.parseInt(System.getProperty("yacs.res.service.self.mgmt")) ? true : false) 
			: true;
	
	// Enable service ME, i.e. the ServiceWatcher, Service-, Master- and WorkerAggregators, and the ConfigurationManagers (used to turn off when exclusively testing the CreateJobGroupManager, MasterWatcher and WorkerWatcher which are "always" present)  
	public static final boolean SYSTEM_SELF_MANAGEMENT = System.getProperty("yacs.system.self.mgmt") instanceof String ?
			(0 < Integer.parseInt(System.getProperty("yacs.system.self.mgmt")) ? true : false) 
			: true;
			
	// Replication of MEs
	public static final int SELF_MANAGEMENT_REPLICATION = System.getProperty("yacs.self.mgmt.rep.degree") instanceof String ?
								Integer.parseInt(System.getProperty("yacs.self.mgmt.rep.degree")) 
								: 3;
			
	// CO-Location, set if MEs should be colocated with associated groups
	// COLOCATE_CONFIGURATION_MANAGER: most likely adding workers, so co-locate with worker group... or should it be the RS group?
	public static final boolean COLOCATE_CONFIGURATION_MANAGER		= System.getProperty("yacs.coloc.cm"  ) instanceof String ? (0 < Integer.parseInt(System.getProperty("yacs.coloc.cm")  ) ? true : false) : false; 
	public static final boolean COLOCATE_CREATE_JOB_GROUP_MANAGER	= System.getProperty("yacs.coloc.cjgm") instanceof String ? (0 < Integer.parseInt(System.getProperty("yacs.coloc.cjgm")) ? true : false) : true;
	public static final boolean COLOCATE_SERVICE_AGGREGATOR			= System.getProperty("yacs.coloc.sa"  ) instanceof String ? (0 < Integer.parseInt(System.getProperty("yacs.coloc.sa")  ) ? true : false) : true;
	public static final boolean COLOCATE_MASTER_AGGREGATOR			= System.getProperty("yacs.coloc.ma"  ) instanceof String ? (0 < Integer.parseInt(System.getProperty("yacs.coloc.ma")  ) ? true : false) : true;
	public static final boolean COLOCATE_WORKER_AGGREGATOR			= System.getProperty("yacs.coloc.wa"  ) instanceof String ? (0 < Integer.parseInt(System.getProperty("yacs.coloc.wa")  ) ? true : false) : true;
	public static final boolean COLOCATE_SERVICE_WATCHER			= System.getProperty("yacs.coloc.sw"  ) instanceof String ? (0 < Integer.parseInt(System.getProperty("yacs.coloc.sw")  ) ? true : false) : true;
	public static final boolean COLOCATE_MASTER_WATCHER				= System.getProperty("yacs.coloc.mw"  ) instanceof String ? (0 < Integer.parseInt(System.getProperty("yacs.coloc.mw")  ) ? true : false) : false;
	public static final boolean COLOCATE_WORKER_WATCHER				= System.getProperty("yacs.coloc.ww"  ) instanceof String ? (0 < Integer.parseInt(System.getProperty("yacs.coloc.ww")  ) ? true : false) : false;
	
	// ME COMPONENT SETTINGS
	// ServiceAggregator: See yacs.resources.aggregators.ServiceAggregator.java
	
	// ConfigurationManager
	// Used when requesting ResourceRef for deployment
	public static final long RESOURCE_REQUIREMENTS_RESOURCE_SERVICE = System.getProperty("yacs.res.req.rs") instanceof String ?
																		Long.parseLong(System.getProperty("yacs.res.req.rs"))
																		: 100;
	public static final long RESOURCE_REQUIREMENTS_MASTER = System.getProperty("yacs.res.req.master") instanceof String ?
																Long.parseLong(System.getProperty("yacs.res.req.master"))
																: 500000;
	public static final long RESOURCE_REQUIREMENTS_WORKER = System.getProperty("yacs.res.req.worker") instanceof String ?
																Long.parseLong(System.getProperty("yacs.res.req.worker"))
																: 1000000;
																
	// WorkerWatcher
	public static final long WW_REPLACEMENT_WORKER_REQUEST_INTERVAL = System.getProperty("yacs.me.ww.replacement.worker.request.interval.msek") instanceof String ?
																		Long.parseLong(System.getProperty("yacs.me.ww.replacement.worker.request.interval.msek"))
																		: 3000;

	
	// FUNCTIONAL COMPONENT SETTINGS
	// ResourceService component: See yacs.resources.ResourceComponent.java
																		
	// Functional resources such as Masters and Workers should report to the RS service every X msek
	public static final long FUNC_RES_STATUS_REPORT_INTERVAL = System.getProperty("yacs.rs.resource.status.report.interval.msek") instanceof String ?
																	Long.parseLong(System.getProperty("yacs.rs.resource.status.report.interval.msek"))
																	: 10000;
																
	// Master
	public static final long MASTER_JOB_MGMG_ITERATION_INTERVAL = System.getProperty("yacs.master.job.mgmt.interval.msek") instanceof String ?
																		Long.parseLong(System.getProperty("yacs.master.job.mgmt.interval.msek"))
																		: 5000;
																		
																		
	
																	
	
	
	// MONITORING
	public static final String MONITORING_HOST = System.getProperty("yacs.monitoring.host") instanceof String ?
														System.getProperty("yacs.monitoring.host") :
														"localhost";
	public static final int MONITORING_PORT = System.getProperty("yacs.monitoring.port") instanceof String ?
														Integer.parseInt(System.getProperty("yacs.monitoring.port")) :
														44555;	
														
	public static final boolean MONITORING_ACTIVATED = System.getProperty("yacs.monitoring.active") instanceof String ?
															(0 < Integer.parseInt(System.getProperty("yacs.monitoring.active")) ? true : false) 
															: false;
	
	// TESTING
	public static boolean TEST__SUBMIT_JOB = System.getProperty("yacs.testing.submit") instanceof String ?
												(0 < Integer.parseInt(System.getProperty("yacs.testing.submit")) ? true : false) 
												: false;
	public static int TEST__NUMBER_OF_TASKS = System.getProperty("yacs.testing.task.count") instanceof String ?
														Integer.parseInt(System.getProperty("yacs.testing.task.count")) 
														: 1;
	public static long TEST__TASK_BUSY_WAIT_MSEK  = System.getProperty("yacs.testing.task.busy.wait.msek") instanceof String ?
															Long.parseLong(System.getProperty("yacs.testing.task.busy.wait.msek")) 
															: 0;
	public static final String TEST__TASK_CLASS_NAME = System.getProperty("yacs.testing.task.classname");
	public static final String TEST__TASK_CLASS_LOCATION = System.getProperty("yacs.testing.task.classfile.location");
	
	public static final boolean TEST__BUSY_UNTIL_DELETED = System.getProperty("yacs.testing.busy.until.deleted") instanceof String ?
																(0 < Integer.parseInt(System.getProperty("yacs.testing.busy.until.deleted")) ? true : false) 
																: false;
	
	// block replication of MEs. Just for testing... of individual MEs
	public static final boolean BLOCK_RELIABLE_MASTER_WATCHER = false; 
	public static final boolean BLOCK_RELIABLE_WORKER_WATCHER = false;
	public static final boolean BLOCK_RELIABLE_SERVICE_WATCHER = false;
	
	public static final boolean BLOCK_RELIABLE_MASTER_AGGREGATOR = false;
	public static final boolean BLOCK_RELIABLE_WORKER_AGGREGATOR = false;
	public static final boolean BLOCK_RELIABLE_SERVICE_AGGREGATOR = false;
	
	public static final boolean BLOCK_RELIABLE_CONFIGURATION_MANAGER = false;
	
	public static final boolean BLOCK_RELIABLE_CREATE_JOB_GROUP_MANAGER = false;
}
