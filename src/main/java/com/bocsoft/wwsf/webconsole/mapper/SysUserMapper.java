package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;
import com.bocsoft.wwsf.webconsole.model.SysUser;

public interface SysUserMapper {

	public List<SysUser> querySysUser(Map<String,Object> condition);
	
	public int insertSysUser(SysUser sysUser);
	
	public int updateSysUser(SysUser sysUser);
	
	public int updateSysUserPassword(Map<String,Object> uMap);
	
	public int deleteSysUsers(Map<String,Object> dMap);
	
}
