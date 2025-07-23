package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.CronShared;

public interface CronSharedMapper {
	
	public List<CronShared> queryCronShared(Map<String, Object> conditions);
	
	public int insertCronShared(CronShared cronShared);
	
	public int updateCronShared(CronShared cronShared);
	
	public int deleteCronShared(Map<String,Object> dMap);
	
}
