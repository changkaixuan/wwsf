package com.bocsoft.wwsf.webconsole.bean.shiro;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.bocsoft.wwsf.webconsole.model.SysAuth;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.SysAuthService;
import com.bocsoft.wwsf.webconsole.service.SysUserService;

public class WwsfAuthorizingRealm extends AuthorizingRealm{

	@Autowired
	@Lazy
	private SysUserService sysUserService;
	
	@Autowired
	@Lazy
	SysAuthService sysAuthService;
	
	private Set<String> menuSet = null;
	
	//权限认证，即登录过后，每个身份不一定，对应的所能看的页面也不一样
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		SimpleAuthorizationInfo sai = new SimpleAuthorizationInfo();
		sai.setStringPermissions(menuSet);
		return sai;
	}
	
	//身份认证，即登录通过账号和密码验证登录人的身份信息
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken at) throws AuthenticationException{
		UsernamePasswordToken upt = (UsernamePasswordToken)at;
		String userName = upt.getUsername();
		String password = new String(upt.getPassword());
		SysUser sysUser = null;
		try {
			sysUser = sysUserService.getSysUser(userName);
		}catch(Exception e) {
			sysUser = null;
		}
		if(null == sysUser) {
			throw new UnknownAccountException("用户名不存在");
		}
		if(!password.equals(sysUser.getUserPwd())) {
			throw new UnknownAccountException("密码错误");
		}
		SecurityUtils.getSubject().getSession().setAttribute(WwsfSessionFilter.Session_CurLoginSysUser, sysUser);
		List<SysAuth> sysAuthList = new ArrayList<SysAuth>();
		try {
			sysAuthList = sysAuthService.getSysAuthByUserId(userName);
		}catch(Exception e2) {}
		menuSet = new HashSet<String>();
		if(null != sysAuthList && sysAuthList.size()>0) {
			SecurityUtils.getSubject().getSession().setAttribute(WwsfSessionFilter.Session_CurLoginSysUserMenu, sysAuthList);
			for(SysAuth fSysAuth : sysAuthList){
				if(null != fSysAuth.getRequiresPermissions() && !fSysAuth.getRequiresPermissions().equals("")) {
					menuSet.add(fSysAuth.getRequiresPermissions());
				}
			}
		}
		return new SimpleAuthenticationInfo(userName, password, getName());
	}
	
}
