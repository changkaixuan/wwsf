package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.JobDimension;

public interface JobDimensionMapper {

	public List<JobDimension> queryJobDimensionPage(Map<String,Object> condition);
	
	public int insertJobDimension(JobDimension jobDimension);
	
	public int deleteJobDimension(Map<String,Object> dMap);
	
	public int deleteJobDimensionByProduct(@Param("product") String product);
	
	public List<String> distinctDmsnName(Map<String,Object> condition);

	public List<String> distinctJobDimName(Map<String, Object> condition);
	
	public List<String> getJobDmsnNameList(Map<String,Object> condition);
	
	public List<String> getJobEntityNameList(Map<String,Object> condition);
	
	public void batchInsertJobDimension(List<JobDimension> jobDimensionList);
	
}
