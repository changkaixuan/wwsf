package com.bocsoft.wwsf.webconsole.service;

import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.CronShared;
import com.github.pagehelper.PageInfo;

public interface CronSharedService {
	
	public PageInfo<CronShared> queryCronShared(Map<String, Object> condition,int pageNo,int pageSize) throws Exception;
	
	public CronShared getCronShared(String jobId,String product,String agent) throws Exception;
	
	public void saveCronShared(String operationType,CronShared cronShared) throws Exception;
	
	public void deleteCronShared(String jobId,String productArr[],String agentArr[]) throws Exception;
	
}
