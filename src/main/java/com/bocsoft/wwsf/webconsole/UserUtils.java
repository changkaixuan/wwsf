package com.bocsoft.wwsf.webconsole;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;

import com.bocsoft.wwsf.webconsole.bean.shiro.WwsfSessionFilter;
import com.bocsoft.wwsf.webconsole.model.SysUser;

public class UserUtils {

	public static SysUser getCurLoginSysUser() {
		return (SysUser)SecurityUtils.getSubject().getSession().getAttribute(WwsfSessionFilter.Session_CurLoginSysUser);
	}
	
	public static void setCurLoginSysUser(SysUser sysUser) {
		SecurityUtils.getSubject().getSession().setAttribute(WwsfSessionFilter.Session_CurLoginSysUser,sysUser);
	}
	
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
	}
}
