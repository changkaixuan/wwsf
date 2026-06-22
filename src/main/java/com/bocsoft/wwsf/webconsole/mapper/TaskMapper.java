package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.Task;
import com.bocsoft.wwsf.webconsole.model.TaskGraph;
import com.bocsoft.wwsf.webconsole.model.TaskLean;

public interface TaskMapper {

	public List<Task> queryTaskPage(Map<String,Object> condition);
	
	public int insertTask(Task task);
	
	public int deleteTask(Map<String,Object> dMap);
	
	public int deleteTaskByProduct(@Param("product") String product);
	
	//task位置坐标start
	public List<TaskGraph> queryTaskGraph(Map<String,Object> condition);
	
	public int insertTaskGraph(TaskGraph taskGraph);
	
	public int deleteTaskGraph(Map<String,Object> dMap);
	
	public TaskGraph getMaxCorrdinate(Map<String,Object> condition);
	
	public int getCountByCondition(Map<String,Object> condition);
	//task位置坐标end

	public void batchInsertTask(List<Task> taskList);

	public void updateTasklean(TaskLean taskLean);

	public List<Map<String,Object>> getAgentScopeByJobIds(Map<String, Object> queryMap);
	
	public List<Map<String,Object>> getWorkingDayByJobIds(Map<String, Object> queryMap);
	
	public List<Map<String,Object>> getTaskPluginByJobIds(Map<String, Object> queryMap);
	
}
