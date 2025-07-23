package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;
import com.bocsoft.wwsf.webconsole.model.SysRole;
import com.github.pagehelper.PageInfo;

public interface SysRoleService {
	public PageInfo<SysRole> querySysRolePage(Map<String, Object> condition, Integer pageNo, Integer pageSize) throws Exception;
	
	//取角色表中角色
	public List<SysRole> querySysRoleList(Map<String, Object> condition) throws Exception;
	
	public SysRole querySysRole(Map<String, Object> condition) throws Exception;
	
	public int insertSysRole(SysRole sysRole) throws Exception;
	
	public int updateSysRole(SysRole sysRole) throws Exception;
	
	public int deleteSysRole(SysRole sysRole) throws Exception;
	
	public int batchDelete(String roleIds) throws Exception;
	
	//取用户关联角色
	public List<SysRole> getSysRoleByCondition(Map<String, Object> condition) throws Exception;
	
	public int insertSysUserRoleRef(Map<String, Object> condition);
	
	public int deleteSysUserRoleRef(String userId,String productId);
	
	public List<Map<String,Object>> getMenuTreeData(String roleId) throws Exception;
	
}
