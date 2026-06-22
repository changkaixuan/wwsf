package com.bocsoft.wwsf.webconsole.bean.shiro;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.Filter;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class ShiroConfiguration {

	@Bean
	WwsfAuthorizingRealm wwsfRealm() {
		return new WwsfAuthorizingRealm();
	}
	
	@Bean
	DefaultWebSessionManager wwsfDefaultWebSessionManager() {
		DefaultWebSessionManager dwsm = new DefaultWebSessionManager();
		dwsm.setGlobalSessionTimeout(1800000); //session默认30分钟超时
		dwsm.setDeleteInvalidSessions(true);
		return dwsm;
	}
	
	@Bean
	SecurityManager securityManager() {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealm(wwsfRealm());
		securityManager.setSessionManager(wwsfDefaultWebSessionManager());
		return securityManager;
	}
	
	@Bean
	ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
		ShiroFilterFactoryBean sffBean = new ShiroFilterFactoryBean();
		sffBean.setSecurityManager(securityManager);
		sffBean.setLoginUrl("/index.html"); // "/login"
		sffBean.setSuccessUrl("/index.html"); // "/index"
		sffBean.setUnauthorizedUrl("/403");
		Map<String, String> fcdMap = new LinkedHashMap<String, String>();
		//开放API
		fcdMap.put("/wwsapi/**", "anon");
		//系统样式、js库
		fcdMap.put("/application/**", "anon");
		fcdMap.put("/release/**", "anon");
		fcdMap.put("/resource/**", "anon");
		fcdMap.put("/css/**", "anon");
		fcdMap.put("/img/**", "anon");
		fcdMap.put("/js/**", "anon");
		fcdMap.put("/webfonts/**", "anon");
		//主页
		fcdMap.put("/index.html", "anon");
		fcdMap.put("/main.js", "anon");
		fcdMap.put("/ssoCloseLogin", "anon");
		//登陆
		fcdMap.put("/login", "anon");
		//sso登陆
		fcdMap.put("/ssoLogin", "anon");
		//登出
		fcdMap.put("/logout", "logout");
		//session
		fcdMap.put("/**", "addPrincipal,authc");
		sffBean.setFilterChainDefinitionMap(fcdMap);
		Map<String,Filter> filters = new LinkedHashMap<String,Filter>();
		filters.put("addPrincipal", new AddPrincipalToSessionFilter());
		filters.put("authc", new WwsfSessionFilter());
		sffBean.setFilters(filters);
		return sffBean;
	}
	
	//shiro生命周期处理器
	@Bean
	public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}
	
	/*
	 * 开启shiro的注解(如@RequiresRoles, @RequiresPermissions),需借助SpringAOP扫描使用shiro注解类,并在必要时进行安全逻辑验证
	 * 配置以下两个Bean(DefaultAdvisorAutoProxyCreator(可选)和AuthorizationAttributeSourceAdvisor)即可实现此功能
	 * DefaultAdvisorAutoProxyCreator会出现二次代理的问题
	 */
	
	@Bean 
	@DependsOn({"lifecycleBeanPostProcessor"})
	public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
		DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
		defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
		return defaultAdvisorAutoProxyCreator;
	}
	
	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager){
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}
	
}
