package com.bocsoft.wwsf.webconsole.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.bocsoft.wwsf.webconsole.model.SysLog;
import com.github.pagehelper.PageInfo;

public interface SysLogService {
	
	public PageInfo<SysLog> querySysLog(Map<String, Object> condition, Integer pageNo, Integer pageSize) throws Exception;
	
	public int insertSysLog(SysLog sysLog) throws Exception;
	
	public SysLog getSysLogTemplate(HttpServletRequest request) throws Exception;
	
	public SysLog getDefaultSysLog(HttpServletRequest request,String modName,String optName,String optRst,String logDesc) throws Exception;
		
}
