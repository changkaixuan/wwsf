package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.TaskInstance;

public interface TaskInstanceMapper {
	
	public List<TaskInstance> taskInsList(Map<String,Object> condition);
	
	public List<TaskInstance> getTaskInsLean(Map<String,Object> condition);
	
	public List<String> getTaskInsBeLean(Map<String, Object> condition);
	
	public int saveTaskIns(Map<String,Object> condition);
	
	public List<Map<String,Object>> jobInsAndTaskInsList(Map<String,Object> condition);

	public List<Map<String,Object>> getTaskState(Map<String,Object> condition);
	
	public int delTaskInsByBatch(Map<String,Object> condition);
	
	public int delTaskInsLeanByBatch(Map<String,Object> condition);
	
	public int delTaskInsByProduct(@Param("product") String product);
	
	public int delTaskInsLeanByProduct(@Param("product") String product);
	
	public List<Map<String, Object>> getTaskByBatchAndProduct(Map<String,Object> condition);
	
	public List<TaskInstance> getTaskInsInfo(Map<String, Object> condition);
	
	public TaskInstance selectByPrimaryKey(Map<String, Object> condition);

	public int delTaskInsByJobInsId(Map<String, Object> conditions);

	public int delTaskInsLeanJobInsId(Map<String, Object> conditions);
}
