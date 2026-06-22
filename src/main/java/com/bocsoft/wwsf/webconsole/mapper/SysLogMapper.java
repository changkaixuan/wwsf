package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.SysLog;

public interface SysLogMapper {
	public List<SysLog> querySysLog(Map<String, Object> condition);
	
	public int insertSysLog(SysLog sysLog);
}
