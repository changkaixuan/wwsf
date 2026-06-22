package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Job;
import com.bocsoft.wwsf.webconsole.model.JobDimension;

public interface JobDimensionService {
	
	public List<JobDimension> getJobDimensionList(Map<String, Object> condition) throws Exception;
	
	public int insertJobDimension(JobDimension jobDimension) throws Exception;
	
	public int deleteJobDimension(Map<String, Object> dMap) throws Exception;
	
	public List<JobDimension> getJobDimension(Job job) throws Exception;
	
	public boolean batchInsertJobDimension(String type,Job job) throws Exception;
	
}
