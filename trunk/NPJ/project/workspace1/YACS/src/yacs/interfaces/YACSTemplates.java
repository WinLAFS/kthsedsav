package yacs.interfaces;

import dks.niche.ids.GroupId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.JadeBindInterface;

public class YACSTemplates {
	
	public static GroupId frontendGroup( NicheActuatorInterface actuator ){
		GroupId template = actuator.getGroupTemplate();
		template.addServerBinding( YACSNames.JOB_RESULT_SERVER_INTERFACE, JadeBindInterface.ONE_TO_ONE );
		return template;
	}
	
	public static GroupId resourceServiceGroup( NicheActuatorInterface actuator ){
		GroupId template = actuator.getGroupTemplate();
		template.addServerBinding( YACSNames.RESOURCE_SERVICE_STATE_SERVER_INTERFACE, JadeBindInterface.ONE_TO_ANY );
		template.addServerBinding( YACSNames.RESOURCE_SERVICE_REQUEST_SERVER_INTERFACE, JadeBindInterface.ONE_TO_ANY );
		template.addServerBinding( YACSNames.RESOURCE_MANAGEMENT_SERVER_INTERFACE, JadeBindInterface.ONE_TO_MANY  | JadeBindInterface.NO_SEND_TO_SENDER );
		return template;
	}
	
	public static GroupId masterGroup( NicheActuatorInterface actuator ){
		GroupId template = actuator.getGroupTemplate();		
		//template.addServerBinding( YACSNames.JOB_MANAGEMENT_SERVER_INTERFACE, JadeBindInterface.ONE_TO_ANY );		
		template.addServerBinding( YACSNames.JOB_MANAGEMENT_SERVER_INTERFACE, JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE );
		template.addServerBinding( YACSNames.INFORMATION_SERVER_INTERFACE, JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE );
		template.addServerBinding( YACSNames.MASTER_SERVER_INTERFACE, JadeBindInterface.ONE_TO_MANY );
		return template;
	}
	
	public static GroupId workerGroup( NicheActuatorInterface actuator ){
		GroupId template = actuator.getGroupTemplate();		
		template.addServerBinding( YACSNames.TASK_MANAGEMENT_SERVER_INTERFACE, JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE );
		template.addServerBinding( YACSNames.INFORMATION_SERVER_INTERFACE, JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE );		
		template.addServerBinding( YACSNames.WORKER_GROUP_SERVER_INTERFACE, JadeBindInterface.ONE_TO_MANY );
		return template;
	}

}
