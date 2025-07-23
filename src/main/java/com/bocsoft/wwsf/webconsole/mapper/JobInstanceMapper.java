package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.BatchStatisticVo;
import com.bocsoft.wwsf.webconsole.model.JobInstance;

public interface JobInstanceMapper {
	
	public List<JobInstance> jobInsList(Map<String, Object> conditions);
	
	public List<JobInstance> getJobInstance(Map<String, Object> conditions);
	
	public int delJobIns(Map<String, Object> conditions);
	
	public int delJobInsByBatch(Map<String, Object> conditions);
	
	public int delJobInsByProduct(@Param("product") String product);
	
	public int saveJobIns(Map<String, Object> conditions);
	
	public List<String> getAllBatch(Map<String, Object> conditions);
	
	public List<BatchStatisticVo> redisBatchs(Map<String, Object> conditions);
	
	public List<JobInstance> queryJobInstance(Map<String, Object> conditions);
	
	public List<Map<String,Object>> getJobInstanceBatch(Map<String, Object> conditions);
	
	public List<Map<String,Object>> getJobInstanceCountByProduct(Map<String, Object> condition);
	
}
