package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.SysAuth;

public interface SysAuthService {

	//查询菜单表SysAuth
	public List<SysAuth> querySysAuth(Map<String, Object> condition) throws Exception;
	
	//根据用户编号（登陆名）取菜单
	public List<SysAuth> getSysAuthByUserId(String userId) throws Exception;
	
}
