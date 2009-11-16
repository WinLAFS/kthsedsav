package yacs.zemulation;

import dks.niche.ids.ComponentId;
import yacs.job.interfaces.StateChangeInterface;
import yacs.job.state.CheckpointInformation;

import java.io.*;

public class StateChangeSensorEmulator implements StateChangeInterface {

	private byte[] lastCheckpoint;
	
	
	@Override
	public void checkpoint(ComponentId source, CheckpointInformation information) {
		// do nothing for now
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
			ObjectOutputStream oos = new ObjectOutputStream( bos );
			oos.writeObject( information );
			oos.close();
			
			lastCheckpoint = bos.toByteArray();
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		
	}

}
