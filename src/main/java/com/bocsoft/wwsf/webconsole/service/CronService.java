package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Cron;
import com.github.pagehelper.PageInfo;

public interface CronService {
	public PageInfo<Cron> kvspage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;
	
	public List<Cron> cronList(Map<String, Object> condition,boolean fetchState) throws Exception;

	public int cronAdd(Cron cron) throws Exception;
	
	public int deleteCron(String[] cronIds, boolean skipRunning) throws Exception;
	
	public void cronStart(String[] cronIds, Boolean clearShared) throws Exception;
	
	public void cronStop(String[] jobId) throws Exception;
	
	public int update(Cron cron) throws Exception;
	
	public List<Map<String,Object>> getCronCountByProduct(Map<String, Object> condition) throws Exception;
	
	public List<String> getCronIdByProduct(Map<String, Object> condition) throws Exception;
}
