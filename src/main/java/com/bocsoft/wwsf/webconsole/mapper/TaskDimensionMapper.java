package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.TaskDimension;

public interface TaskDimensionMapper {

	public List<TaskDimension> queryTaskDimensionPage(Map<String,Object> condition);
	
	public int insertTaskDimension(TaskDimension taskDimension);
	
	public int deleteTaskDimension(Map<String,Object> dMap);
	
	public int deleteTaskDimensionByProduct(@Param("product") String product);
	
	public List<String> distinctTaskDimName(Map<String, Object> condition);
	
	public List<String> getTaskDmsnNameList(Map<String,Object> condition);
	
	public List<String> getTaskEntityNameList(Map<String,Object> condition);
	
}
