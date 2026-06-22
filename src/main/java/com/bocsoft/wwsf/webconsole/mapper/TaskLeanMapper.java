package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.TaskEdges;
import com.bocsoft.wwsf.webconsole.model.TaskLean;

public interface TaskLeanMapper {

	public List<TaskLean> queryTaskLeanPage(Map<String,Object> condition);
	
	public int insertTaskLean(TaskLean taskLean);
	
	public int deleteTaskLean(Map<String,Object> dMap);

	public int deleteTaskLean2(Map<String,Object> dMap);
	
	public int deleteTaskLeanByProduct(@Param("product") String product);
	
	public List<TaskLean> queryTaskBeLean(Map<String,Object> condition);
	
	//task/edges start
	public List<TaskEdges> queryTaskEdges(Map<String,Object> condition);
	
	//task/edges end
	
	//任务项页面/设置依赖 
	public List<TaskLean> taskLeanPage(Map<String,Object> condition);
	
	public List<TaskLean> taskBeleanPage(Map<String,Object> condition);
	
	public void batchInsertTaskLean(List<TaskLean> taskLeanList);
	
}
