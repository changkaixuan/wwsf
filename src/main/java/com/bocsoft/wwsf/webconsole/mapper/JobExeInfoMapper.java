package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.JobExeInfo;
import com.bocsoft.wwsf.webconsole.model.Task;

public interface JobExeInfoMapper {

	public List<JobExeInfo> queryJobExeInfo(Map<String, Object> condition);
	
	public void insertJobExeInfo(JobExeInfo jobHistory);
	
	public void deleteJobExeInfo(Map<String, Object> condition);
	
	public void updateJobExeInfo(Map<String, Object> condition);
	
	public List<Task> queryJobExeDetail(Map<String, Object> condition);
	
	public void insertJobExeDetail(Task task);
	
	public void deleteJobExeDetail(Map<String, Object> condition);
}
