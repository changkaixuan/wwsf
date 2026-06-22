package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.Job;

public interface JobMapper {

	public List<Job> queryJobPage(Map<String,Object> condition);
	
	public int insertJob(Job job);
	
	public int updateJob(Job job);
	
	public int deleteJob(Map<String,Object> dMap);
	
	public int deleteJobByProduct(@Param("product") String product);
	
	public List<Job> getJobListIgnoreMode();
	
	public List<Map<String,Object>> getCountByProduct(Map<String,Object> condition);
	
}
