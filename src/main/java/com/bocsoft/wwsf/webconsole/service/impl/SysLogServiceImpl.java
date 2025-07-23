package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocsoft.wwsf.webconsole.CalendarUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.mapper.SysLogMapper;
import com.bocsoft.wwsf.webconsole.model.SysLog;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.SysLogService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
@Service("sysLogService")
public class SysLogServiceImpl implements SysLogService {
	@Autowired
	private SysLogMapper sysLogMapper;
	
	@Override
	public PageInfo<SysLog> querySysLog(Map<String, Object> condition, Integer pageNo, Integer pageSize) throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				sysLogMapper.querySysLog(condition);
			}
		});
	}

	@Override
	public int insertSysLog(SysLog sysLog) throws Exception {
		return sysLogMapper.insertSysLog(sysLog);
	}

	@Override
	public SysLog getSysLogTemplate(HttpServletRequest request) throws Exception {
		SysLog sysLog = new SysLog();
		SysUser sysUer = UserUtils.getCurLoginSysUser();
		sysLog.setLogType("用户操作");
		sysLog.setOptDate(CalendarUtil.fmtDate(new Date(), "yyyyMMddHHmmss"));
		sysLog.setUserId(sysUer.getLoginName());
		sysLog.setId(String.valueOf(System.currentTimeMillis()));
		sysLog.setUserUip(UserUtils.getIpAddr(request));
		return sysLog;
	}
	
	public SysLog getDefaultSysLog(HttpServletRequest request,String modName,String optName,String optRst,String logDesc) throws Exception{
		SysLog sysLog = getSysLogTemplate(request);
		sysLog.setModName(modName);
		sysLog.setOptName(optName);
		sysLog.setOptRst(optRst);
		sysLog.setLogDesc(logDesc);
		return sysLog;
	}
	

}
