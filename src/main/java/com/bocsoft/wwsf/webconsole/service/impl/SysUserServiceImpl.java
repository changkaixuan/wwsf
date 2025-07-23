package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bocsoft.wwsf.webconsole.mapper.SysUserMapper;
import com.bocsoft.wwsf.webconsole.model.Product;
import com.bocsoft.wwsf.webconsole.model.SysRole;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.ProductService;
import com.bocsoft.wwsf.webconsole.service.SysRoleService;
import com.bocsoft.wwsf.webconsole.service.SysUserService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("sysUserService")
public class SysUserServiceImpl implements SysUserService{

	@Autowired
	private SysUserMapper sysUserMapper;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private SysRoleService sysRoleService;
	
	public PageInfo<SysUser> querySysUser(Map<String, Object> condition,int pageNo, int pageSize) {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				sysUserMapper.querySysUser(condition);
			}
		});
	}
	
	public SysUser getSysUser(String loginName) throws Exception{
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("loginName", loginName);
		condition.put("delFlag", "1");
		List<SysUser> list = sysUserMapper.querySysUser(condition);
		if(null == list || list.size()<1) {
			return null;
		}
		return list.get(0);
	}
	
	public List<SysUser> getSysUserList(Map<String, Object> condition) {
		return sysUserMapper.querySysUser(condition);
	}
	
	@Transactional
	public int insertSysUser(SysUser sysUser) throws Exception{
		//保存产品和角色
		Map<String,Object> diMap = new HashMap<String,Object>();
		diMap.put("userId", sysUser.getLoginName());
		//产品
		productService.deleteSysUserProductRef(sysUser.getLoginName(), null);
		List<String> productList = sysUser.getProductList();
		if(null != productList && productList.size()>0) {
			for(String fProductId : productList) {
				diMap.put("productId", fProductId);
				productService.insertSysUserProductRef(diMap);
			}
		}
		//角色
		sysRoleService.deleteSysUserRoleRef(sysUser.getLoginName(), null);
		List<String> sysRoleList = sysUser.getSysRoleList();
		if(null != sysRoleList && sysRoleList.size()>0) {
			for(String fRoleId : sysRoleList) {
				diMap.put("roleId", fRoleId);
				sysRoleService.insertSysUserRoleRef(diMap);
			}
		}
		return sysUserMapper.insertSysUser(sysUser);
	}
	
	@Transactional
	public int updateSysUser(SysUser sysUser) throws Exception{
		//保存产品和角色
		Map<String,Object> diMap = new HashMap<String,Object>();
		diMap.put("userId", sysUser.getLoginName());
		//产品
		productService.deleteSysUserProductRef(sysUser.getLoginName(), null);
		List<String> productList = sysUser.getProductList();
		if(null != productList && productList.size()>0) {
			for(String fProductId : productList) {
				diMap.put("productId", fProductId);
				productService.insertSysUserProductRef(diMap);
			}
		}
		//角色
		sysRoleService.deleteSysUserRoleRef(sysUser.getLoginName(), null);
		List<String> sysRoleList = sysUser.getSysRoleList();
		if(null != sysRoleList && sysRoleList.size()>0) {
			for(String fRoleId : sysRoleList) {
				diMap.put("roleId", fRoleId);
				sysRoleService.insertSysUserRoleRef(diMap);
			}
		}
		return sysUserMapper.updateSysUser(sysUser);
	}
	
	public int updateSysUserPassword(Map<String,Object> uMap) throws Exception{
		return sysUserMapper.updateSysUserPassword(uMap);
	}
	
	@Transactional
	public void deleteSysUsers(String loginNameArr[]) throws Exception{
		if(null != loginNameArr && loginNameArr.length>0) {
			for(String fLoginName : loginNameArr) {
				productService.deleteSysUserProductRef(fLoginName, null);
				sysRoleService.deleteSysUserRoleRef(fLoginName, null);
			}
		}
		Map<String,Object> delMap = new HashMap<String,Object>();
		delMap.put("loginNameArr", loginNameArr);
		sysUserMapper.deleteSysUsers(delMap);
	}
	
	public List<Product> selectProduct(Map<String,Object> condition) throws Exception{
		return productService.selectProduct(condition);
	}
	
	public List<Product> getProductByCondition(Map<String,Object> condition) throws Exception{
		return productService.getProductByCondition(condition);
	}
	
	//取角色表中角色
	public List<SysRole> querySysRoleList(Map<String,Object> condition) throws Exception{
		return sysRoleService.querySysRoleList(condition);
	}
		
	//取用户关联角色
	public List<SysRole> getSysRoleByCondition(Map<String, Object> condition) throws Exception{
		return sysRoleService.getSysRoleByCondition(condition);
	}
	
}
