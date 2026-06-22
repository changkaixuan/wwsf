package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.JobExeInfo;
import com.bocsoft.wwsf.webconsole.model.Task;
import com.github.pagehelper.PageInfo;

public interface JobExeInfoService {
	
	 public List<JobExeInfo> queryJobExeInfo(Map<String, Object> condition) throws Exception;
	 
	 public PageInfo<JobExeInfo> queryJobExeInfoPage(Map<String, Object> condition, int pageNo, int pageSize) throws Exception;
	 
	 public Long insertJobExeInfo(String jobExeParameter) throws Exception;
	
	 public List<Task> queryJobExeDetail(Map<String, Object> condition) throws Exception;
	 
	 public void insertJobExeDetail(Task task) throws Exception;
	 
	 public PageInfo<Task> getJobExeInfoSelect(Integer pageNumber, Integer pageSize, String jobExeInfoId, String jobId, String filterTaskId) throws Exception;

	 public PageInfo<Task> getJobExeInfoUnselect(Integer pageNumber, Integer pageSize, String jobExeInfoId, String jobId, String filterTaskId) throws Exception;
	 
	 public void addTempJobExeInfo(String jobExeInfoId, String jobId) throws Exception;
	 
	 public String addJobExeDetail(String product, String jobId, String taskIds) throws Exception;

	 public String removeJobExeDetail(String taskIds) throws Exception;
	 
	 public void topRecord(String jobExeInfoId, boolean flag) throws Exception;

	 public void editJobExeInfo(String jobExeInfoId, String desc) throws Exception;
}
