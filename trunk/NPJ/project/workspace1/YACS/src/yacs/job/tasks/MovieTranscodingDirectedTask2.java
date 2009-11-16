package yacs.job.tasks;

import java.io.*;
import java.util.ArrayList;

import yacs.interfaces.YACSNames;

public class MovieTranscodingDirectedTask2 extends DirectedTask {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String exec_command;
	private String video_codec, video_bitrate;
	private String audio_codec, audio_bitrate;
	private String scale;
	
	private ArrayList<String> fileins = new ArrayList<String>();
	private ArrayList<String> fileouts = new ArrayList<String>();
	
	public MovieTranscodingDirectedTask2(){}
	
	// direction-task logic
	@Override
	public void doTask() {
		try {
			this.transcode();
			this.setResultCode( YACSNames.RESULT_OK );
		}
		catch( Exception e ){
			log( e.getMessage() );
			e.printStackTrace();
			this.setResultCode( YACSNames.RESULT_ERROR );
		}

	}
	
	// transcoding logic
	private void transcode() throws Exception {
		log( "Transcoding " + fileins.size() + " parts..." );
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
	}
	private void processChunk( int i, String infile, String outfile ) throws Exception {
		
		log("\tAbout to execute command:");
		
		String fullCommand = exec_command + " "
								+ infile  + " "
								+ outfile + " "
								+ video_codec + " "
								+ video_bitrate + " "
								+ scale + " "
								+ audio_codec + " "
								+ audio_bitrate;
		
		log( "\t"+fullCommand );
		
		Process p = Runtime.getRuntime().exec( fullCommand );
		
		int result = p.waitFor();
		log("\tWaiting for result: " + result);
		
		if( result != 0 )
			throw new Exception( "VLC did not complete successfully: " + result );
	}
	
	// task-class logic
	@Override
	protected void initTaskInfo(){
		this.logName = "MTDTask2";
	}
	@Override
	public void initFromState(){
		commandFile 	= (String)this.getState()[0];
		checkpointName 	= (String)this.getState()[1];
		
		exec_command	= (String)this.getState()[2];
		video_codec 	= (String)this.getState()[3];
		video_bitrate 	= (String)this.getState()[4];
		audio_codec 	= (String)this.getState()[5];
		audio_bitrate 	= (String)this.getState()[6];
		scale 			= (String)this.getState()[7];
		
		fileins.add( (String)this.getState()[8] );
		fileouts.add( (String)this.getState()[9] );
	}
	@Override
	public void prepareMetacheckpoint(){
		log("MovieTranscodingDirectedTask.prepareCheckpoint");
		
		Serializable[] oldState = this.getState();
		
		this.setState( 
				new Serializable[]{
						this.commandFile,
						this.checkpointName,
						oldState[2],
						oldState[3],
						oldState[4],
						oldState[5],
						oldState[6],
						oldState[7],
						oldState[8],
						oldState[9],					                
				} 
			);
	}
}
