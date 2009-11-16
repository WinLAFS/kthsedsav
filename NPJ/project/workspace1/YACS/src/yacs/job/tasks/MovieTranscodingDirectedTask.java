package yacs.job.tasks;

import java.io.*;
import java.util.ArrayList;

import yacs.interfaces.YACSNames;
import yacs.job.tasks.helpers.ProcessStreamReader;
import yacs.utils.YacsUtils;

public class MovieTranscodingDirectedTask extends Task implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String exec_command;
	private String video_codec, video_bitrate;
	private String audio_codec, audio_bitrate;
	private String scale;
	private long minProcessTime = 0;
	
	private ArrayList<String> fileins = new ArrayList<String>();
	private ArrayList<String> fileouts = new ArrayList<String>();
	
	private String cmdFile = null;
	
	// testing
	private String checkpointValue = "";
	
	private static final String TRANSCODE = "T";
	private static final String CHECKPOINT = "C";
	private static final String FAIL = "F";
	
	public MovieTranscodingDirectedTask(){}
	
	protected void initTaskInfo(){
		this.logName = "MTDTask";
	}

	@Override
	public void execute() {
		try {
			log("MovieTranscodingTask.execute: task.#" + this.getTid());
			if( checkpointValue != null && checkpointValue.length() != 0 )
				log("\tStarting from checkpoint: " + checkpointValue);
			log("\tWaiting for commands...");
			
			String command = null;
			do{
				command = readCommand();
				
				if( command!=null && command.equals(CHECKPOINT) ){
					log("\t\tCheckpointing: " + checkpointValue);
					this.getExecutionContext().metacheckpoint();
				}
				else if( command!=null && command.equals(TRANSCODE) ){
					log("\t\tTranscoding!");
					this.transcode();
					this.setResultCode( YACSNames.RESULT_OK );
				}
				else if( command!=null && command.equals(FAIL) ){
					log("\t\tFail!");
					this.setResultCode( YACSNames.RESULT_ERROR );	
				}
				else if( command != null ){
					log("\tUnknown command: " + command);
				}
				else
					Thread.sleep(5000);
			}
			while( command == null || (	
										!command.equals(TRANSCODE) &&
										!command.equals(FAIL)	
					) );
			
			
		}
		catch( Exception e ){
			e.printStackTrace();
			this.setResultCode( YACSNames.RESULT_ERROR );
		}

	}
	
	private void transcode() throws Exception {
		long start = System.currentTimeMillis();
		
		for( int a=0; a<fileins.size(); a++ ){
			String filein = fileins.get(a);
			String fileout = fileouts.get(a);
			String localin = fileout+".lin";
			String localout = localin+".lout";
			
			
			// copy input data to local store so can use "normal" IO
			this.getExecutionContext().copyFile( filein, localin );

			processChunk( a+1, localin, localout );

			// copy output data to client's desired location
			this.getExecutionContext().copyFile( localout, fileout );
			
			// TODO: checkpoint the Transcoding here... maybe if not last Moviepart?
			
			this.getExecutionContext().deleteFile( localin );
			this.getExecutionContext().deleteFile( localout );
		}
		
		long actualProcessTime = System.currentTimeMillis() - start;
		log("Transcoding task:" + this.getTid() + " took msek: " + actualProcessTime);
		if( actualProcessTime < minProcessTime ){
			log( "Minimum transcoding time not reached. Sleeping for msek: " + (minProcessTime - actualProcessTime) );
			YacsUtils.ignorantSleep( minProcessTime - actualProcessTime );
		}
	}
	private void processChunk( int i, String infile, String outfile ) throws Exception {
		
		log("\tAbout to execute command:");
		
		String cmdPrefix = "", cmdPostfix = "";
		if( System.getProperty("os.name").indexOf("Windows") == -1 ){
			cmdPrefix = "bash ";
			cmdPostfix = ".sh";
		}
		else
			cmdPostfix = ".bat";
		
		String fullCommand = cmdPrefix + exec_command + cmdPostfix + " "
								+ infile  + " "
								+ outfile + " "
								+ video_codec + " "
								+ video_bitrate + " "
								+ scale + " "
								+ audio_codec + " "
								+ audio_bitrate;
		
		// TODO: remove
		//String test = "C:\\Progra~1\\VideoLAN\\VLC\\vlc.exe --color -I dummy "+infile+" --sout \"#transcode{vcodec="+video_codec+",vb="+video_bitrate+",scale="+scale+",acodec="+audio_codec+",ab="+audio_bitrate+",channels=6}:standard{mux=\"TS\",dst=\""+outfile+"\",access=file}\" vlc://quit";
		//fullCommand = test;
		
		log( "\t"+fullCommand );
		
		Process p = Runtime.getRuntime().exec( fullCommand );
		
		ProcessStreamReader stdout = new ProcessStreamReader( "stdout", p.getInputStream(), getLogger() );
		ProcessStreamReader stderr = new ProcessStreamReader( "stderr", p.getErrorStream(), getLogger() );
		
		stdout.start();
		stderr.start();
		log("\tStreamReaders started.");
		
		int result = p.waitFor();
		log("\tProcess result: " + result);
		
		stdout.join();
		stderr.join();
		
		if( result != 0 )
			throw new Exception( "VLC did not complete successfully: " + result );
	}
	
	private String lastCommand;
	private String readCommand() throws Exception {
	
		/**
		 * LINE=[TID]:[COMMAND]
		 * TID=[0-9]+
		 * COMMAND=[TRANSCODE|CHECKPOINT|FAIL]
		 * TRANSCODE=T
		 * FAIL=F
		 * CHECKPOINT=V[A-z0-9]+
		 * 
		 * // [] reference to DEF
		 * // + => at least one instance of left-adjacent [DEF]
		 */
		
		
		BufferedReader reader = null;
		try{
			reader = new BufferedReader( new FileReader(cmdFile) );
			
			String line; int i=-1;
			while( (line=reader.readLine()) != null ){
				
				// if not a command for this task
				if( !line.startsWith(this.getTid()+":") )
					continue;
				
				// if same as last command... will not be remembered between checkpoints
				if( lastCommand != null && lastCommand.equals(line) )
					return null;
				
				lastCommand = line;
				
				// if transcode command
				if( (i=line.indexOf(":T")) != -1 )
					return TRANSCODE;
				// if checkpoint command
				else if( (i=line.indexOf(":F")) != -1 )
					return FAIL;
				else if( (i=line.indexOf(":C")) != -1 ){
					String valueToCheckpoint = line.substring(i+2);
					
					// if same as last time then
					if( valueToCheckpoint.equals(checkpointValue) )
						return null;
					else {
						checkpointValue = valueToCheckpoint;
						return CHECKPOINT;
					}
				}								
			}
		
			return null;
		}
		catch( Exception e ){
			log( "Unable to read command: " + e.getMessage() );
			// so that the task wont block forever
			return TRANSCODE;
		}
		finally {
			if( reader != null )
				reader.close();
		}
	}
	
	@Override
	public void initFromState(){
		cmdFile			= YacsUtils.nfsRelativeToNfsAbsolute((String)this.getState()[0]);
		exec_command	= YacsUtils.nfsRelativeToNfsAbsolute((String)this.getState()[1]);
		video_codec 	= (String)this.getState()[2];
		video_bitrate 	= (String)this.getState()[3];
		audio_codec 	= (String)this.getState()[4];
		audio_bitrate 	= (String)this.getState()[5];
		scale 			= (String)this.getState()[6];
		minProcessTime 	= (Long)this.getState()[7];
		
		
		fileins.add( 	YacsUtils.nfsRelativeToNfsAbsolute((String)this.getState()[8]) );
		fileouts.add( 	YacsUtils.nfsRelativeToNfsAbsolute((String)this.getState()[9]) );
		
		if( this.getState().length == 11 )
			checkpointValue = (String)this.getState()[10];
	}
	
	@Override
	public void prepareMetacheckpoint(){
		log("MovieTranscodingDirectedTask.prepareCheckpoint: " + checkpointValue);
		
		Serializable[] oldState = this.getState();
		
		this.setState( 
				new Serializable[]{
						oldState[0],
						oldState[1],
						oldState[2],
						oldState[3],
						oldState[4],
						oldState[5],
						oldState[6],
						oldState[7],
						oldState[8],
						oldState[9],
						checkpointValue					                
				} 
			);
	}
}
