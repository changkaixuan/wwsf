package com.bocsoft.wwsf.webconsole.control;

import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.model.SysLog;
import com.bocsoft.wwsf.webconsole.service.SysLogService;
import com.github.pagehelper.PageInfo;
@RestController
public class SysLogController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SysLogService sysLogService;
	
	@GetMapping("/sysLog/pageList")
	@RequiresPermissions("menu:userLog-list")
	public RestResponse pageList(String modName, String optDateStart, String optDateEnd, String optName, String userId, String optRst, Integer pageSize,Integer pageNumber){
		RestResponse response = new RestResponse();
		try{
			Map<String, Object> condition = new HashMap<>();
			if (modName != null && !"".equals(modName)) {
				condition.put("lModName", modName);
			}
			if (optDateStart != null && !"".equals(optDateStart)) {
				optDateStart += "000000";
				condition.put("optDateStart", optDateStart);
			}
			if (optDateEnd != null && !"".equals(optDateEnd)) {
				optDateEnd += "235959";
				condition.put("optDateEnd", optDateEnd);
			}
			if (optName != null && !"".equals(optName)) {
				condition.put("lOptName", optName);
			}
			if (userId != null && !"".equals(userId)) {
				condition.put("lUserId", userId);
			}
			if (optRst != null && !"".equals(optRst)) {
				condition.put("optRst", optRst);
			}
			PageInfo<SysLog> pageList = sysLogService.querySysLog(condition, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("get system log failed ", e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
}
