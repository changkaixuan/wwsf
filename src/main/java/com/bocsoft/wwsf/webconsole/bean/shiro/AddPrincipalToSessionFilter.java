package com.bocsoft.wwsf.webconsole.bean.shiro;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.servlet.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bocsoft.wwsf.webconsole.AppUtil;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.SsoLoginService;

public class AddPrincipalToSessionFilter extends OncePerRequestFilter {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    @Override
    protected void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            
            SysUser sysUserSession = (SysUser) SecurityUtils.getSubject().getSession().getAttribute(WwsfSessionFilter.Session_CurLoginSysUser);
            if (sysUserSession == null) {
            	String userName = null;
                String token = null;
                Cookie[] cookies = request.getCookies();
                if (cookies != null && cookies.length > 0) {
                	for(Cookie cookie: cookies) {
                        if("userName".equals(cookie.getName())) {
                            userName = cookie.getValue();
                        }
                        if("token".equals(cookie.getName())) {
                            token = cookie.getValue();
                        }
                    }
                }
                SysUser sysUser = null;
                //判断用户是否为自动登录SSO
                if(userName != null && token != null) {
                    SsoLoginService ssoLoginService = AppUtil.getBean("ssoLoginService", SsoLoginService.class);
                    //根据用户名查询该用户信息
                    sysUser = ssoLoginService.getSysUser(userName, token);
                    
                    //设置sesson
                    if(sysUser != null) {
                        SecurityUtils.getSubject().getSession().setAttribute(WwsfSessionFilter.Session_CurLoginSysUser, sysUser);
                        SecurityUtils.getSubject().getSession().setAttribute(WwsfSessionFilter.Session_CurLoginIsSSO, "1");
                    }
                }
            }
            
        }catch (Exception e) {
            //校验session或是token失败，重定向到首页
            servletResponse.getWriter().print(WwsfSessionFilter.SESSION_OUT_STRING);
            logger.error("",e);
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

}

