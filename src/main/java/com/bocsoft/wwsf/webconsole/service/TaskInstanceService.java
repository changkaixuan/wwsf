package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.TaskInstance;
import com.github.pagehelper.PageInfo;

public interface TaskInstanceService {
	
	public PageInfo<TaskInstance> taskInsList(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;
	
	public int saveTaskIns(Map<String, Object> conditions) throws Exception;
	
	public List<TaskInstance> getTaskInstanceByJobInsId(long jobInsId) throws Exception;
	
	public PageInfo<Map<String,Object>> jobInsAndTaskInsList(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;
	public List<Map<String,Object>> jobInsAndTaskInsList(Map<String, Object> condition) throws Exception;
	
	public List<Map<String,Object>> getTaskState(Map<String,Object> condition) throws Exception;
	
	public TaskInstance setTaskInsLean(TaskInstance list) throws Exception;
	
	public List<Map<String, Object>> getTaskByBatchAndProduct(Map<String, Object> conditions) throws Exception;
	
	public Map<String, Object> countTaskStateByTitle(List<Map<String, Object>> list) throws Exception;
	
	public List<TaskInstance> getTaskInsInfo(Map<String, Object> condition) throws Exception;
	
	public List<TaskInstance> getTaskIns(Map<String, Object> condition) throws Exception;
	
	public TaskInstance selectByPrimaryKey(long jobInsId, String taskInsId) throws Exception;
	
	public int deleteTaskInsByBatch(Map<String, Object> conditions) throws Exception;
	
	public int deleteTaskInsLeanByBatch(Map<String, Object> conditions) throws Exception;
	
	public int deleteTaskInsByProduct(String product) throws Exception;
	
	public int deleteTaskInsLeanByProduct(String product) throws Exception;
}
