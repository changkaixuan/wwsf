package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.TaskEdges;
import com.bocsoft.wwsf.webconsole.model.TaskLean;
import com.bocsoft.wwsf.webconsole.model.TaskSpecialLean;

public interface TaskSpecialLeanMapper {
	
	public List<TaskSpecialLean> getTaskSpecialLean(Map<String, Object> conditions);
	
	public int insertTaskSpecialLean(TaskSpecialLean taskSpecialLean);
	
	public int deleteTaskSpecialLean(Map<String,Object> dMap);
	
	public int deleteTaskSpecialLeanByProduct(@Param("product") String product);
	
	public List<TaskEdges> queryTaskSpecialEdges(Map<String,Object> condition);

	public List<TaskSpecialLean> queryTaskSpecialLean(Map<String,Object> condition);

	public int deleteTaskSpecialLean2(Map<String,Object> dMap);
	
	public List<TaskLean> queryTaskLean(Map<String,Object> condition);
	
	public int batchInsertTaskSpecialLean(List<TaskSpecialLean> taskSpecialLeanList);
	
}
