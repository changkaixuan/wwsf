package com.bocsoft.wwsf.webconsole.control;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.CalendarUtil;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.encoder.PasswordEncoder;
import com.bocsoft.wwsf.webconsole.model.Product;
import com.bocsoft.wwsf.webconsole.model.SysLog;
import com.bocsoft.wwsf.webconsole.model.SysRole;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.SysLogService;
import com.bocsoft.wwsf.webconsole.service.SysUserService;
import com.github.pagehelper.PageInfo;

@RestController
public class SysUserController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	SysUserService sysUserService;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	private SysLogService sysLogService;
	
	//@Autowired
	//ProductService productService;
	
	//@Autowired
	//SysRoleService sysRoleService;
	
	@GetMapping("/sysUser/list")
	@RequiresPermissions("menu:sysUser-list")
	public RestResponse list(
			String loginName,
			String userName,  
			//String userEmail,
			//String delFlag,
			String lockFlag,
			Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != loginName && !loginName.equals("")) {
				conditions.put("lLoginName", loginName);
			}
			if(null != userName && !userName.equals("")) {
				conditions.put("lUserName", userName);
			}
			/*if(null != delFlag && !delFlag.equals("")) {
				conditions.put("delFlag", delFlag);
			}*/
			conditions.put("delFlag", 1);
			if(null != lockFlag && !lockFlag.equals("")) {
				conditions.put("lockFlag", lockFlag);
			}
			PageInfo<SysUser> pageList = sysUserService.querySysUser(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取用户列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
	
	@GetMapping(value="/sysUser/get",produces=MediaType.APPLICATION_JSON_VALUE)
	public String get(String loginName) {
		RestResponse response = new RestResponse();
		try {
			SysUser sysUser = sysUserService.getSysUser(loginName);
			if (null != sysUser) {
				response.put("sysUser", sysUser);
				//产品
				Map<String, Object> condition = new HashMap<String, Object>();
				condition.put("userId", loginName);
				List<Product> productList = sysUserService.getProductByCondition(condition);
				if(null != productList && productList.size()>0){
					response.put("productList", productList);
				}
				//角色
				condition.put("delFlag", 1);
				List<SysRole> sysRoleList = sysUserService.getSysRoleByCondition(condition);
				if(null != sysRoleList && sysRoleList.size()>0){
					response.put("sysRoleList", sysRoleList);
				}
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get system user failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
	@PostMapping(value="/sysUser/insert",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String insert(@RequestBody SysUser sysUser, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
			SysUser tSysUser = sysUserService.getSysUser(sysUser.getLoginName());
			if(null != tSysUser) {
				if(tSysUser.getDelFlag() == 1) {//DelFlag=1未删除
					response.setInfo("保存失败.用户已存在");
					response.setSuccess(false);
					return JSONObject.toJSONString(response);
				}else{
					sysUser.setDelFlag(1);
					sysUser.setdValidDate(CalendarUtil.parseDate(sysUser.getValidDate(),"yyyy-MM-dd"));
					sysUser.setdInvalidDate(CalendarUtil.parseDate(sysUser.getInvalidDate(),"yyyy-MM-dd"));
					sysUserService.updateSysUser(sysUser);
				}
			}else {
				sysUser.setUserPwd(passwordEncoder.encodePassword("111111",""));
				sysUser.setDelFlag(1); //账号状态 未删除
				sysUser.setLockFlag("1"); //是否锁定 未锁定
				sysUser.setdCreateTime(new Date());
				sysUser.setUserStat("1");
				sysUser.setdValidDate(CalendarUtil.parseDate(sysUser.getValidDate(),"yyyy-MM-dd"));
				sysUser.setdInvalidDate(CalendarUtil.parseDate(sysUser.getInvalidDate(),"yyyy-MM-dd"));
				sysUserService.insertSysUser(sysUser);
			}
			response.setSuccess(true);
			// 记录用户操作日志
			try {
				SysLog sysLog = sysLogService.getSysLogTemplate(request);
				sysLog.setModName("用户管理");
				sysLog.setOptName("新增");
				sysLog.setLogDesc(String.format("用户[%s]新增了用户[%s]", sysLog.getUserId(), sysUser.getLoginName()));
				sysLog.setOptRst(response.isSuccess()?"成功":"失败");
				sysLogService.insertSysLog(sysLog);
			} catch (Exception e) {
				logger.error("插入用户日志异常：",e);
			}
		} catch (Exception e) {
			logger.error("insert system user failed ",e);
			response.setSuccess(false);
			response.setInfo("保存失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
	@PostMapping(value="/sysUser/update",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String update(@RequestBody SysUser sysUser, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
			SysUser tSysUser = sysUserService.getSysUser(sysUser.getLoginName());
			if(null == tSysUser || (null != tSysUser && tSysUser.getDelFlag() == 0)) {//DelFlag = 0 删除
				response.setInfo("保存失败,当前用户不存在");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			sysUser.setDelFlag(1);
			sysUser.setdValidDate(CalendarUtil.parseDate(sysUser.getValidDate(),"yyyy-MM-dd"));
			sysUser.setdInvalidDate(CalendarUtil.parseDate(sysUser.getInvalidDate(),"yyyy-MM-dd"));
			sysUserService.updateSysUser(sysUser);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("update system user failed ",e);
			response.setSuccess(false);
			response.setInfo("保存失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("用户管理");
			sysLog.setOptName("修改");
			sysLog.setLogDesc(String.format("用户[%s]修改了用户[%s]的信息:"
					+ "userName=%s, "
					+ "userMobile=%s, "
					+ "userEmail=%s, "
					+ "invalidDate=%s, "
					+ "validDate=%s, "
					+ "sysRoleList=%s, "
					+ "productList=%s", sysLog.getUserId(), sysUser.getLoginName(),
					sysUser.getUserName(),
					sysUser.getUserMobile(),
					sysUser.getUserEmail(),
					sysUser.getInvalidDate(),
					sysUser.getValidDate(),
					sysUser.getSysRoleList(),
					sysUser.getProductList()));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSONObject.toJSONString(response);
	}
	
	@DeleteMapping(value="/sysUser/delete/{loginNames}",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String delete(@PathVariable String loginNames, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
			sysUserService.deleteSysUsers(loginNames.trim().split(","));
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("delete system user failed ",e);
			response.setSuccess(false);
			response.setInfo("删除失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("用户管理");
			sysLog.setOptName("删除");
			sysLog.setLogDesc(String.format("用户[%s]删除了用户[%s]", sysLog.getUserId(), loginNames));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSONObject.toJSONString(response);
	}
	
	@GetMapping(value="/sysUser/getRoleAndProduct",produces=MediaType.APPLICATION_JSON_VALUE)
	public String getRoleAndProduct() {
		RestResponse response = new RestResponse();
		try {
			//取产品表中所有产品
			Map<String,Object> condition = new HashMap<String,Object>();
			List<Product> productList = sysUserService.selectProduct(condition);
			if(null != productList && productList.size() > 0) {
				response.put("productList", productList);
			}
			//取角色表中所有角色
			condition.put("delFlag",1);
			List<SysRole> sysRoleList = sysUserService.querySysRoleList(condition);
			if(null != sysRoleList && sysRoleList.size() > 0) {
				response.put("sysRoleList", sysRoleList);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get role and product failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
	@PostMapping(value="/sysUser/updatePassword",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse updatePassword(@RequestBody SysUser sysUser, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
			SysUser curLoginSysUser = UserUtils.getCurLoginSysUser();
			String oldPwd = passwordEncoder.encodePassword(sysUser.getOldUserPwd(),"");
			if(!curLoginSysUser.getUserPwd().equals(oldPwd)){
				response.setInfo("旧密码输入错误");
				response.setSuccess(false);
				return response;
			}
			String newPwd = passwordEncoder.encodePassword(sysUser.getUserPwd(),"");
			Map<String,Object> uMap = new HashMap<String,Object>();
			uMap.put("loginName", curLoginSysUser.getLoginName());
			uMap.put("userPwd", newPwd);
			sysUserService.updateSysUserPassword(uMap);
			UserUtils.setCurLoginSysUser(sysUserService.getSysUser(curLoginSysUser.getLoginName()));
			response.setSuccess(true);
		}catch(AuthenticationException ae) {
			logger.error("update password failed ",ae);
			response.setSuccess(false);
			response.setInfo("修改密码失败");
			response.setDetailInfo(StringUtil.stringifyException(ae));
		}catch(Exception e) {
			logger.error("update password failed ",e);
			response.setSuccess(false);
			response.setInfo("修改密码失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("用户管理");
			sysLog.setOptName("修改密码");
			sysLog.setLogDesc(String.format("用户[%s]修改了密码", sysLog.getUserId()));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return response;
	}
	
}
