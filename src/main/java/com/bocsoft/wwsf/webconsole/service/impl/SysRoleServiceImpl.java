package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bocsoft.wwsf.webconsole.mapper.SysRoleMapper;
import com.bocsoft.wwsf.webconsole.model.SysAuth;
import com.bocsoft.wwsf.webconsole.model.SysRole;
import com.bocsoft.wwsf.webconsole.service.SysAuthService;
import com.bocsoft.wwsf.webconsole.service.SysRoleService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("sysRoleService")
public class SysRoleServiceImpl implements SysRoleService {
	@Autowired
	private SysRoleMapper sysRoleMapper;
	
	@Autowired
	private SysAuthService sysAuthService;

	@Override
	public PageInfo<SysRole> querySysRolePage(Map<String, Object> condition, Integer pageNo, Integer pageSize) throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				sysRoleMapper.querySysRolePage(condition);
			}
		});
	}

	@Transactional
	@Override
	public int insertSysRole(SysRole sysRole) throws Exception {
		Map<String, Object> condition = new HashMap<>();
		condition.put("roleId", sysRole.getRoleId());
		SysRole tSysRole = querySysRole(condition);
		int result = 0;
		if (tSysRole==null) {
			// 插入角色表基本信息
			result += sysRoleMapper.insertSysRole(sysRole);
		}else {
			if ("1".equals(tSysRole.getDelFlag())) {
				throw new Exception("保存失败,该角色已存在");
			}
			// 更新角色的删除标记
			result += sysRoleMapper.updateSysRole(sysRole);
		}
		// 插入角色-权限关联关系
		String auths = sysRole.getRoleType();
		if (auths!=null && !"".equals(auths)) {
			Map<String, Object> roleAuthMap = new HashMap<>();
			roleAuthMap.put("roleId", sysRole.getRoleId());
			String[] authArr = auths.split(",");
			for (String auth : authArr) {
				roleAuthMap.put("authId", auth);
				result += sysRoleMapper.insertSysRoleAuth(roleAuthMap);
			}
		}
		return result;
	}

	@Transactional
	@Override
	public int updateSysRole(SysRole sysRole) throws Exception {
		int result = 0;
		// 更新角色基本信息
		result += sysRoleMapper.updateSysRole(sysRole);
		// 更新角色-权限关联关系
		String auths = sysRole.getRoleType();
		Map<String, Object> roleAuthMap = new HashMap<>();
		roleAuthMap.put("roleId", sysRole.getRoleId());
		sysRoleMapper.deleteSysRoleAuth(roleAuthMap);
		if (auths != null && !"".equals(auths)) {
			String[] authArr = auths.split(",");
			for (String auth : authArr) {
				roleAuthMap.put("authId", auth);
				result += sysRoleMapper.insertSysRoleAuth(roleAuthMap);
			}
		}
		return result;
	}

	@Override
	public List<SysRole> querySysRoleList(Map<String, Object> condition) throws Exception {
		return sysRoleMapper.querySysRolePage(condition);
	}

	@Override
	public SysRole querySysRole(Map<String, Object> condition) throws Exception {
		List<SysRole> list = querySysRoleList(condition);
		if (list==null || list.isEmpty()) {
			return null;
		}
		SysRole sysRole = list.get(0);
		Map<String, Object> roleAuthMap = new HashMap<>();
		roleAuthMap.put("roleId", sysRole.getRoleId());
		List<String> authList = sysRoleMapper.querySysRoleAuth(roleAuthMap);
		sysRole.setRoleType(authList.toString());
		return sysRole;
	}

	@Transactional
	@Override
	public int deleteSysRole(SysRole sysRole) throws Exception {
		Map<String, Object> condition = new HashMap<>();
		String roleId = sysRole.getRoleId();
		condition.put("roleId", roleId);
		SysRole tSysRole = querySysRole(condition);
		if (tSysRole != null) {
			// 逻辑删除角色信息
			tSysRole.setDelFlag("0");
			sysRoleMapper.updateSysRole(tSysRole);
			// 物理删除角色-权限关联关系
			Map<String, Object> roleAuthMap = new HashMap<>();
			roleAuthMap.put("roleId", roleId);
			sysRoleMapper.deleteSysRoleAuth(roleAuthMap);
			// 物理删除角色-用户关联关系
			deleteSysUserRoleRef(null, roleId);
		}
		return 1;
	}

	@Transactional
	@Override
	public int batchDelete(String roleIds) throws Exception{
		String[] roleArr = roleIds.split(",");
		SysRole sysRole = new SysRole();
		int rs = 0;
		for(int i=0, len=roleArr.length; i<len; i++) {
			sysRole.setRoleId(roleArr[i]);
			rs += deleteSysRole(sysRole);
		}
 		return rs;
	}
	
	public List<SysRole> getSysRoleByCondition(Map<String, Object> condition) throws Exception{
		return sysRoleMapper.getSysRoleByCondition(condition);
	}
	
	public int insertSysUserRoleRef(Map<String, Object> condition) {
		return sysRoleMapper.insertSysUserRoleRef(condition);
	}
	
	public int deleteSysUserRoleRef(String userId,String roleId) {
		Map<String, Object> condition = new HashMap<String, Object>();
		if(null != userId && !userId.equals("")){
			condition.put("userId", userId);
		}
		if(null != roleId && !roleId.equals("")){
			condition.put("roleId", roleId);
		}
		return sysRoleMapper.deleteSysUserRoleRef(condition);
	}
	
	public List<Map<String,Object>> getMenuTreeData(String roleId) throws Exception{
		Map<String,Object> condition = new HashMap<String,Object>();
		List<SysAuth> sysAuthList = sysAuthService.querySysAuth(condition);
		List<String> selectedSysAuth = null;
		if(null != roleId && !roleId.equals("")) {
			condition.put("roleId", roleId);
			selectedSysAuth = sysRoleMapper.querySysRoleAuth(condition);
		}
		return transformData(sysAuthList,null,selectedSysAuth);
	}
	
	public List<Map<String,Object>> transformData(List<SysAuth> sysAuthList,String authId,List<String> selectedSysAuth) throws Exception{
		List<Map<String,Object>> children = null;
		for(SysAuth fSysAuth : sysAuthList) {
			if(((null == fSysAuth.getPid() || fSysAuth.getPid().equals("")) && null == authId) || (null != fSysAuth.getPid() && fSysAuth.getPid().equals(authId))){
				Map<String,Object> data = new HashMap<String,Object>();
				data.put("key", fSysAuth.getId());
				data.put("title", fSysAuth.getName());
				if(null != selectedSysAuth && selectedSysAuth.contains(fSysAuth.getId())){
					data.put("selected", true);
				}else{
					data.put("selected", false);
				}
				List<Map<String,Object>> cld = transformData(sysAuthList, fSysAuth.getId(), selectedSysAuth);
				if(null != cld && cld.size() > 0){
					data.put("folder", true);
					data.put("children", cld);
					data.put("expanded", true);
				}else{
					data.put("expanded", false);
				}
				if(children == null){
					children = new ArrayList<Map<String,Object>>();
				}
				children.add(data);
			}
		}
		return children;
	}
	
}
