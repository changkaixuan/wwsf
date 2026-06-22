package com.bocsoft.wwsf.webconsole.control;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.model.SysLog;
import com.bocsoft.wwsf.webconsole.model.SysRole;
import com.bocsoft.wwsf.webconsole.service.SysLogService;
import com.bocsoft.wwsf.webconsole.service.SysRoleService;
import com.github.pagehelper.PageInfo;

@RestController
public class SysRoleController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SysRoleService sysRoleService;
	
	@Autowired
	private SysLogService sysLogService;
	
	@GetMapping("/sysRole/pageList")
	@RequiresPermissions("menu:sysRole-list")
	public RestResponse pageList(String roleName, String roleId, Integer pageSize,Integer pageNumber){
		RestResponse response = new RestResponse();
		try{
			Map<String, Object> condition = new HashMap<>();
			if (roleName != null && !"".equals(roleName)) {
				condition.put("lRoleName", roleName);
			}
			if (roleId != null && !"".equals(roleId)) {
				condition.put("lRoleId", roleId);
			}
			condition.put("delFlag", "1");
			PageInfo<SysRole> pageList = sysRoleService.querySysRolePage(condition, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("get system role list failed ",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
	
	@GetMapping(value="/sysRole/get",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse get(String roleId) {
		RestResponse response = new RestResponse();
		try {
			response.put("sysAuthList", sysRoleService.getMenuTreeData(roleId));
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get system role failed ",e);
			response.setSuccess(false);
			response.setInfo("获取系统角色失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@PostMapping(value="/sysRole/insert",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RestResponse insert(@RequestBody SysRole sysRole, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		response.setSuccess(false);
		try {
			String roleId = sysRole.getRoleId();
			if (roleId==null || "".equals(roleId)) {
				response.setInfo("roleId 不能为空");
				return response;
			}
			sysRoleService.insertSysRole(sysRole);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("insert ststem role failed ",e);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("角色管理");
			sysLog.setOptName("新增");
			sysLog.setLogDesc(String.format("用户[%s]新增了角色[%s]", sysLog.getUserId(), sysRole.getRoleId()));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return response;
	}
	
	@PostMapping(value="/sysRole/update",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RestResponse update(@RequestBody SysRole sysRole, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		response.setSuccess(false);
		try {
			Map<String, Object> condition = new HashMap<>();
			condition.put("roleId", sysRole.getRoleId());
			SysRole tSysrole = sysRoleService.querySysRole(condition);
			if(null == tSysrole || (null != tSysrole && "0".equals(tSysrole.getDelFlag()))) {
				response.setInfo("保存失败,当前角色不存在");
				return response;
			}
			sysRoleService.updateSysRole(sysRole);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("update system role failed ",e);
			response.setInfo("保存失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("角色管理");
			sysLog.setOptName("修改");
			sysLog.setLogDesc(String.format("用户[%s]修改了角色[%s]信息:"
					+ "roleName=%s, "
					+ "roleDesc=%s, "
					+ "auth=[%s]", sysLog.getUserId(), sysRole.getRoleId(),
					sysRole.getRoleName(),
					sysRole.getRoleDesc(),
					sysRole.getRoleType()));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return response;
	}
	
	@DeleteMapping(value="/sysRole/delete",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RestResponse delete(String roleIds, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		response.setSuccess(false);
		try {
			sysRoleService.batchDelete(roleIds);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("delete system role failed ",e);
			response.setInfo("删除失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("角色管理");
			sysLog.setOptName("删除");
			sysLog.setLogDesc(String.format("用户[%s]删除了角色[%s]", sysLog.getUserId(), roleIds));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return response;
	}
}
