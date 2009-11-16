package yacs.job.interfaces;

import dks.niche.ids.GroupId;

public interface InformationInterface {
	public String componentType();
	public GroupId workerGroup();
	public GroupId masterGroup();
}
