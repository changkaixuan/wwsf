package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Product;
import com.bocsoft.wwsf.webconsole.model.SysRole;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.github.pagehelper.PageInfo;

public interface SysUserService {

	public PageInfo<SysUser> querySysUser(Map<String, Object> condition,int pageNo, int pageSize);
	
	public SysUser getSysUser(String loginName) throws Exception;
	
	public List<SysUser> getSysUserList(Map<String, Object> condition);

	public int insertSysUser(SysUser sysUser) throws Exception;
	
	public int updateSysUser(SysUser sysUser) throws Exception;
	
	public int updateSysUserPassword(Map<String,Object> uMap) throws Exception;
	
	public void deleteSysUsers(String loginNameArr[]) throws Exception;
	
	//取产品表中产品
	public List<Product> selectProduct(Map<String,Object> condition) throws Exception;
	
	//取用户关联产品
	public List<Product> getProductByCondition(Map<String,Object> condition) throws Exception;
	
	//取角色表中角色
	public List<SysRole> querySysRoleList(Map<String,Object> condition) throws Exception;
		
	//取用户关联角色
	public List<SysRole> getSysRoleByCondition(Map<String, Object> condition) throws Exception;
	
}
