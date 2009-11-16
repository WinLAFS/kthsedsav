package yacs.job.helpers;

import java.io.Serializable;
import dks.niche.ids.GroupId;

/**
 * Reply from Master after receiving a job. 
 * Most important information contained is whether it could take on the Job.
 * @author LTDATH
 */
public class SubmissionReply implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Was it possible to accept the job?
	 */
	private boolean accepted = false;
	
	/**
	 * The Master group responsible for the job. 
	 */
	private GroupId masterGroup;
	
	public SubmissionReply( boolean accepted, GroupId masterGroup ){
		this.accepted = accepted;
		this.masterGroup = masterGroup;
	}
	public SubmissionReply( boolean accepted ){
		this(accepted,null);
	}
	
	// getters and setters
	public boolean isAccepted(){ return accepted; }

	/**
	 * Id of Master group is responsible for managing the job. 
	 * @return Id of Master group is responsible for managing the job.
	 */
	public GroupId getMasterGroup() {
		return masterGroup;
	}
	/**
	 * Set the Master group responsible for managing the job.
	 * @param masterGroup Id of Master group responsible for managing the job.
	 */
	public void setMasterGroup(GroupId masterGroup) {
		this.masterGroup = masterGroup;
	}
}
