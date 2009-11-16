package yacs.job.tasks.helpers;

import java.io.*;


import yacs.utils.YacsLogger;

/**
 * A thread that reads from the InputStream provided by a Process and logs the output.
 * As well as getting being able to log their valuable output this actually enables some
 * external programs to complete. Some programs launched by java.lang.Runtime.exec 
 * block when their output streams are full!
 * @author LTDATH
 */
public class ProcessStreamReader extends Thread {

	private String streamName;
	private InputStream stream;
	private YacsLogger logger;
	
	public ProcessStreamReader(String streamName, InputStream stream, YacsLogger logger ) {
		super();
		this.logger = logger;
		this.stream = stream;
		this.streamName = streamName;
	}
	
	public void run(){
		try {
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			while( (line=reader.readLine()) != null ){
				log( streamName+": " + line );
			}
			log("----PSR-done: " + streamName);
		}
		catch( Exception e ){
			log("----PSR-exception-"+streamName+": " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void log( String message ){
		if( logger == null )
			System.err.println( message );
		else
			logger.log( message );
	}
	
	
	
}
