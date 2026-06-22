package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.BatchStatisticVo;
import com.bocsoft.wwsf.webconsole.model.JobInstance;
import com.github.pagehelper.PageInfo;

public interface JobInstanceService {
	
	public PageInfo<JobInstance> jobInsList(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;
	
	public JobInstance getJobInstance(Map<String, Object> condition) throws Exception;

	public int delJobIns(Map<String, Object> conditions) throws Exception;
	
	public int delJobIns(String products, String jobInsIds) throws Exception;

	public int saveJobIns(Map<String, Object> conditions) throws Exception;
	
	public BatchStatisticVo getBatchStatisticVo(Map<String, Object> conditions) throws Exception;
	
	public int clearBatch(String product, String batch) throws Exception;
	
	public void delBatches(String productArr, String batchArr) throws Exception;
	
	public PageInfo<BatchStatisticVo> redisBatchs(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;
	
	public List<BatchStatisticVo> setBatchStatisticVo(List<BatchStatisticVo> dBatchStatisticVoList) throws Exception;
	
	public void jobRerunfailed(String[] jobInsIdArr) throws Exception;

	public void jobRerunUnpassed(String[] productArr, String[] jobInsIdArr) throws Exception;

	public void jobRerun(String[] productArr, String[] jobInsIdArr) throws Exception;
	
	public void jobRenovate(String[] productArr, String[] jobInsIdArr) throws Exception;

	public void jobDisabled(String[] productArr, String[] jobInsIdArr) throws Exception;
	
	public void jobPause(String[] productArr, String[] jobInsIdArr) throws Exception;

	public void jobContinue(String[] productArr, String[] jobInsIdArr) throws Exception;
	
	public void jobDriver(Long[] jobInsId) throws Exception;
	
	//单批量统计
	public List<JobInstance> queryJobInstance(Map<String, Object> condition) throws Exception;
	
	public PageInfo<JobInstance> queryJobInstance(Map<String, Object> condition, int pageNo, int pageSize) throws Exception;
	
	public List<Map<String,Object>> getJobInstanceBatch(Map<String, Object> condition) throws Exception;
	
	public List<Map<String,Object>> getJobInstanceCountByProduct(Map<String, Object> condition) throws Exception;
	
}
