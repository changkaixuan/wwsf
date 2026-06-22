package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.JobPanelInfo;
import com.bocsoft.wwsf.webconsole.model.Task;
import com.bocsoft.wwsf.webconsole.model.TaskLean;
import com.bocsoft.wwsf.webconsole.model.TaskSpecialLean;
import com.github.pagehelper.PageInfo;

public interface TaskService {
	
	public PageInfo<Task> queryTaskPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;
	
	public List<Task> getTaskList(Map<String, Object> condition) throws Exception;
	
	public Task getTask(String jobId,String taskId) throws Exception;
	
	//保存task调用（saveTaskRela和saveTask）
	public void saveTaskRela(JobPanelInfo jobPanelInfo) throws Exception;
	public void saveTask(Task task) throws Exception;
	
	public void deleteTask(String jobId,String taskIdArr[]) throws Exception;
	
	//other
	public JobPanelInfo getJobPanelInfo(String product,String jobId) throws Exception;
	
	public void saveTaskLean(String jobId, String taskId, String leanJobId, String leanTaskIds) throws Exception;
	
	public PageInfo<TaskLean> queryTaskLeanPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;
	
	public void deleteTaskLean(String jobId, String taskId, String leanTaskIdArr[]) throws Exception;
	
	public String isContainLoop(JobPanelInfo jobPanelInfo) throws Exception;
	
	public PageInfo<TaskLean> taskLeanPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;
	
	public List<String> getDmsnNameList(String jobId,String taskId) throws Exception;
	
	public PageInfo<TaskLean> taskBeleanPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;
	
	public PageInfo<TaskSpecialLean> taskSpecialLeanPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;
	
	public void deleteTaskLean(String jobId, String taskId, String leanTaskIdArr[], String specialLeanTaskIdArr[]) throws Exception;
	
	public List<String> getEntityNameList(String jobId,String taskId, String entityName) throws Exception;
	
	public void saveTaskSpecialLean(List<TaskSpecialLean> taskSpecialLeanList) throws Exception;
	
}
