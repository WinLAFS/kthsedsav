package yacs.job.interfaces;

import dks.niche.ids.ComponentId;
import yacs.job.state.CheckpointInformation;

public interface StateChangeInterface {
	
	public void checkpoint( ComponentId source, CheckpointInformation information );

}
