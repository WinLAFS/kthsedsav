package yacs.frontend;

import yacs.job.Job;
import yacs.job.TaskContainer;

public interface FrontendClientInterface {
	
	public void taskChange( TaskContainer task );
	public void jobResult( Job result );

}
