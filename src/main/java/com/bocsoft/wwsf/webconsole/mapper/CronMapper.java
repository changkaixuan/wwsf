package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.Cron;

public interface CronMapper {
	public List<Cron> kvspage(Map<String, Object> conditions);
	
	public int cronAdd(Cron cron);
	
	public int deleteCronLog(String jobId);
	
	public List<Map<String,Object>> getCronCountByProduct(Map<String, Object> condition);
	
	public int updateCron(Cron cron);
	
	public int updateCronNotAutoStart(@Param("ids") String[] jobId);
	
	public int deleteCronByProduct(@Param("product") String product);
}
