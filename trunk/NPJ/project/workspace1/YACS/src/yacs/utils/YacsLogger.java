package yacs.utils;

import java.io.PrintStream;
import dks.niche.interfaces.NicheAsynchronousInterface;

public class YacsLogger {
	
	private String owner;
	private String id;
	private NicheAsynchronousInterface nicheLogger;
	private PrintStream console = System.err;
	
	private String format;
	private boolean toConsole = true;
	private boolean toNiche = true;
	
	public YacsLogger( 	String owner, String id,
						boolean logToNiche, boolean logToConsole,
						NicheAsynchronousInterface nicheLogger ){
		this.owner = owner;
		this.id = id;
		this.toNiche = logToNiche;
		this.toConsole = logToConsole;
		this.nicheLogger = nicheLogger;
		
		format = formatBuilder( owner, id );
	}
	
	private String formatBuilder( String myOwner, String myId ){
		String formatBuilder = "YLM{";
		if( !isNullOrEmpty(myId) )
			formatBuilder += myOwner + "("+myId+"): ";
		else
			formatBuilder += myOwner + ": ";
		return formatBuilder;
	}
	private boolean isNullOrEmpty( String str){
		if( str == null || str.length() == 0 )
			return true;
		else
			return false;
	}
	
	/**
	 * Logs according to set internal settings.
	 * @param message The message to be logged
	 */
	public void log( String message ){
		if( toNiche && toConsole ){
			console.println( message );
			if( nicheLogger != null ) nicheLogger.log( format + message );
		}
		else if( toConsole ){
			console.println( message );
		}
		else if( toNiche ){
			nicheLogger.log( format + message );
		}
			
	}
	/**
	 * Log only to the Niche.logger.
	 * @param message The message to be logged
	 */
	public void nlog( String message ){
		nicheLogger.log( format+message );
	}
	/**
	 * Log only to the console.
	 * @param message The message to be logged
	 */
	public void clog( String message ){
		console.println( format+message );
	}
	
	// 
	public String getOwner() {
		return owner;
	}
	public String getId() {
		return id;
	}
	
	public void setOwner(String nOwner) {
		this.format = formatBuilder( nOwner, id );
		this.owner = nOwner;
	}
	public void setId(String nId) {
		this.format = formatBuilder( owner, nId );
		this.id = nId;
	}
	
	public void setConsole( PrintStream console ){
		this.console = console;
	}
}
