package yacs.resources.helpers;

import java.util.Random;

import yacs.interfaces.YACSSettings;
import yacs.resources.interfaces.*;
import yacs.resources.data.*;
import yacs.utils.YacsLogger;

/**
 * Helper class for periodic reporting of resource state to the resource service
 * @author LTDATH
 */
public class ResourceReporter extends Thread {
	
	private ResourceInterface holder;
	private ResourceServiceStateInterface reportingInterface;
	
	private boolean run = true;
	
	private YacsLogger logger;
	
	public ResourceReporter( ResourceInterface holder, ResourceServiceStateInterface reportingInterface){
		logger = holder.createLogger( "ResourceReporter", holder.getId(), true, true );
		this.holder = holder;
		this.reportingInterface = reportingInterface;
	}
	
	public void stopReporting(){
		run = false;
	}
	
	public void forceReporting( ResourceInfo currentState ){
		synchronized( reportingInterface ){
			reportingInterface.advertise( currentState );
		}
	}
	
	public void run(){
		logger.log("ResourceReporter starting for: " + holder.getId());
		try{
			/*Random r = new Random();			
			sleep( (long)(r.nextDouble()*5000) );			
			int requests = 0;*/
			
			while( run ){				
				ResourceInfo status = holder.getStatusInfo();
				logger.nlog( "Reporting status: " + status );
				
				if( status != null ){
					synchronized( reportingInterface ){
						long start = System.currentTimeMillis();
						reportingInterface.advertise( status );
						long end = System.currentTimeMillis();
						logger.nlog( "Reported to RS in: " + (end-start) +" msek" );
					}
				}
				else
					logger.nlog( "WARNING: RS status is null!?" );
				
				if( run ) { // in case it was stopped while invoking interface above, else quit
					/*long sleeptime = 10000 - (requests++*500);
					if( sleeptime <= 500 ) 
						sleeptime = 500;					
					sleep( sleeptime );*/
					sleep( YACSSettings.FUNC_RES_STATUS_REPORT_INTERVAL );
				}
			}
		}
		catch( Exception e ){
			logger.log("ResourceReporter error for: " + holder.getId() + ": " + e.getMessage());
			e.printStackTrace();
		}		
	}
}
