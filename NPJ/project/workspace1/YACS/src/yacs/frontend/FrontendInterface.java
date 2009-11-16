package yacs.frontend;

import yacs.job.Job;

public interface FrontendInterface {

	public abstract String submit(Job job) throws Exception;

	public abstract boolean deleteJob(Job job);

}