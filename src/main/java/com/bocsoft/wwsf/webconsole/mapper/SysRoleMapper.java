package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.SysRole;

public interface SysRoleMapper {
	
	public List<SysRole> querySysRolePage(Map<String, Object> condition);
	
	public int insertSysRole(SysRole sysRole);
	
	public int updateSysRole(SysRole sysRole);
	
	public List<SysRole> getSysRoleByCondition(Map<String, Object> condition);
	
    public int insertSysUserRoleRef(Map<String, Object> condition);
	
	public int deleteSysUserRoleRef(Map<String, Object> condition);
	
	public int insertSysRoleAuth(Map<String, Object> condition);
	
	public int deleteSysRoleAuth(Map<String, Object> condition);
	
	public List<String> querySysRoleAuth(Map<String, Object> condition);
	
}
