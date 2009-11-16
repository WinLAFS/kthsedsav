package yacs.job.tasks;

import java.io.*;
import java.util.ArrayList;

import yacs.interfaces.YACSNames;

public class TranscodingTask extends Task implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<String> fileins = new ArrayList<String>();
	private ArrayList<String> fileouts = new ArrayList<String>();
	
	public TranscodingTask(){}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
		try {
			
			// TODO: TranscodingTask sleep delay for testing
			//if( getTid() == 2 )
			{
				log("TranscodingTask: Test sleep for 10000 secs!");
				Thread.sleep(15000);
			}
			
			for( int a=0; a<fileins.size(); a++ ){
				String filein = fileins.get(a);
				String fileout = fileouts.get(a);
				String tmpfile = fileout+".tmp";
				
				
				// copy input data to local store so can use "normal" IO
				this.getExecutionContext().copyFile( filein, tmpfile );

				processChunk( a+1, tmpfile );

				// copy output data to client's desired location
				this.getExecutionContext().copyFile( tmpfile, fileout );
				
				// TODO: checkpoint the Transcoding here... maybe if not last Moviepart?
				
				this.getExecutionContext().deleteFile( tmpfile );
			}
			this.setResultCode( YACSNames.RESULT_OK );
		}
		catch( Exception e ){
			e.printStackTrace();
			this.setResultCode( YACSNames.RESULT_ERROR );
		}

	}
	
	private void processChunk( int i, String tmpFile ) throws Exception {
		
		StringBuffer buffer = null;
		{
			File f = new File(tmpFile);
			
			buffer = new StringBuffer( (int)f.length() );
			
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String read;
			while( (read=reader.readLine()) != null ){
				
				if( read.endsWith("\n") )
					buffer.append( read + "\n" );
				else
					buffer.append( read );
			}
			reader.close();
		}
		{
			File f = new File(tmpFile);
			BufferedWriter writer = new BufferedWriter( new FileWriter(f) );
			
			writer.write( buffer.length() + ":" + buffer.toString() );
			writer.flush();
			
			writer.close();
			
		}
	}
	
	@Override
	public void initFromState(){
		fileins.add( (String)this.getState()[0] );
		fileouts.add( (String)this.getState()[1] );
	}
}
