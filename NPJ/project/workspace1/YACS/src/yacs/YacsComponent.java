package yacs;

import yacs.utils.YacsLogger;
import dks.niche.interfaces.NicheAsynchronousInterface;

/**
 * For stuff common to all components in YACS, be it functional or management.
 * Right now its only logging that is common to all.
 * @author LTDATH
 */
public class YacsComponent {

	private NicheAsynchronousInterface nicheLogger;
	protected YacsLogger yacsLog;
	
	protected void log( String message ){
		if( this.yacsLog != null )
			yacsLog.log( message );
		else
			System.err.println( message );
	}
	protected void nlog( String message ){
		if( this.yacsLog != null )
			yacsLog.nlog( message );
	}
	
	protected void time( String role, String roleId, String timingName, String iterationId, long time, String free ){
		nlog( 	role + "-yacstime;" +
				(roleId!=null?roleId:"-") + ";" +
				timingName + (iterationId!=null?"-"+iterationId:"") + ";" + 
				time +
				(free!=null?";"+free:"") );
	}
	protected void timefx( String role, String roleId, long tuid, String timingName, String iterationId, long time, String free ){
		nlog( 	role + "-yacstimefx;" +
				(roleId!=null?roleId:"-") + ";" +
				tuid + ";" + 
				timingName + (iterationId!=null?"-"+iterationId:"") + ";" + 
				time +
				(free!=null?";"+free:"") );
	}
	
	protected void createYacsLogger( 	String name, String id, 
										boolean toNiche, boolean toConsole, 
										NicheAsynchronousInterface logger )
	{
		nicheLogger = logger;
		yacsLog = new YacsLogger( name, id, toNiche, toConsole, logger );
	}
	
	public YacsLogger createDerivedLogger(	String name, String id, 
											boolean toNiche, boolean toConsole ){
		return new YacsLogger( name, id, toNiche, toConsole, nicheLogger );
	}
}
