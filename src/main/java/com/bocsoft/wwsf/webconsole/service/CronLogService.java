package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.CronLog;
import com.github.pagehelper.PageInfo;

public interface CronLogService {
	
	public List<CronLog> getCronLog(Map<String, Object> condition) throws Exception;
	
	public PageInfo<CronLog> getCronLog(Map<String, Object> condition, int pageNo, int pageSize) throws Exception;
	
	public List<CronLog> getCronLogInformation(String logId) throws Exception;
	
}
