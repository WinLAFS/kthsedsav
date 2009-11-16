package yacs.job.tasks;

import java.io.*;
import java.util.ArrayList;

import yacs.interfaces.YACSNames;

public class MovieTranscodingTask extends Task implements Serializable {
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
	
	// testing
	private boolean checkpointingDone = false;
	
	public MovieTranscodingTask(){}

	@Override
	public void execute() {
		try {
			log("MovieTranscodingTask.execute: task.#" + this.getTid());
			// TODO: TranscodingTask sleep delay for testing
			//if( getTid() == 2 )
			/*{
				log("TranscodingTask: Test sleep for 10000 secs!");
				Thread.sleep(15000);
			}*/
			
			// TODO: remove: only for healing test
			/*if( this.getTid() == 1 && !checkpointingDone ){
				this.checkpointingDone = true;
				this.getTaskProcessor().checkpoint();
				while( true ){
					log("\tSleeping forever...");
					yacs.utils.YacsUtils.ignorantSleep(60000);
				}
			}*/
			
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
			this.setResultCode( YACSNames.RESULT_OK );
		}
		catch( Exception e ){
			e.printStackTrace();
			this.setResultCode( YACSNames.RESULT_ERROR );
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
	
	@Override
	public void initFromState(){
		exec_command	= (String)this.getState()[0];
		video_codec 	= (String)this.getState()[1];
		video_bitrate 	= (String)this.getState()[2];
		audio_codec 	= (String)this.getState()[3];
		audio_bitrate 	= (String)this.getState()[4];
		scale 			= (String)this.getState()[5];
		
		fileins.add( (String)this.getState()[6] );
		fileouts.add( (String)this.getState()[7] );
		
		if( this.getState().length == 9 )
			checkpointingDone = (Boolean)this.getState()[8];
	}
	
	@Override
	public void prepareMetacheckpoint(){
		log("MovieTranscodingTask.prepareCheckpoint: " + checkpointingDone);
		
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
						new Boolean(checkpointingDone)					                
				} 
			);
	}
}
