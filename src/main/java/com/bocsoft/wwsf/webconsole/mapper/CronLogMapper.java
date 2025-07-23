package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.bocsoft.wwsf.webconsole.model.CronLog;

public interface CronLogMapper {
	
	public List<CronLog> getCronLog(Map<String, Object> conditions);
	
	public int deleteCronLogByProduct(@Param("product") String product);
	
	public List<CronLog> getCronLogInformation(String logId);
	
}
