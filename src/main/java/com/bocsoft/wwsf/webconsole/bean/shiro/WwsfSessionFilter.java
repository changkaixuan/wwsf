package com.bocsoft.wwsf.webconsole.bean.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

public class WwsfSessionFilter extends FormAuthenticationFilter{

	public final static String X_REQUESTED_WITH_SREING = "X-Requested-With";
	public final static String XML_HTTP_REQUEST_STRING ="XMLHttpRequest";
	public final static String SESSION_OUT_STRING ="sessionOut";
	
	public final static String Session_CurLoginSysUser = "Session_CurLoginSysUser";
	public final static String Session_CurLoginSysUserMenu = "Session_CurLoginSysUserMenu";
	public final static String Session_CurLoginIsSSO = "Session_CurLoginIsSSO";

	@Override
	protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
		if(this.isLoginRequest(servletRequest, servletResponse)){
			if(this.isLoginSubmission(servletRequest, servletResponse)){
				return this.executeLogin(servletRequest, servletResponse);
			}else{
				return true;
			}
		}else{
			if(isAjax((HttpServletRequest)servletRequest)){
				servletResponse.getWriter().print(SESSION_OUT_STRING);
			}else{
				this.saveRequestAndRedirectToLogin(servletRequest, servletResponse);
			}
			return false;
		}
	}
	
	public boolean isAjax(HttpServletRequest httpServletRequest){
		String header = httpServletRequest.getHeader(X_REQUESTED_WITH_SREING);
		if(XML_HTTP_REQUEST_STRING.equals(header)){
			//logger.debug("当前请求为Ajax请求：{}",httpServletRequest.getRequestURI());
			return Boolean.TRUE;
		}
		//logger.debug("当前请求非Ajax请求：{}",httpServletRequest.getRequestURI());
		return Boolean.FALSE;
	}
	
}
