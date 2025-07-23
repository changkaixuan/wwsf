package com.bocsoft.wwsf.webconsole.control;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bocsoft.wwsf.webconsole.CalendarUtil;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.bean.shiro.WwsfSessionFilter;
import com.bocsoft.wwsf.webconsole.encoder.PasswordEncoder;
import com.bocsoft.wwsf.webconsole.model.Login;
import com.bocsoft.wwsf.webconsole.model.SysAuth;
import com.bocsoft.wwsf.webconsole.model.SysLog;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.SysLogService;

@RestController
public class LoginController {
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	private SysLogService sysLogService;
	
    @Value("${sso.close.login:'0'}")
    private String closeLogin;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@GetMapping(value="/ssoCloseLogin",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getCloseLogin() {
		RestResponse response = new RestResponse();
		response.setSuccess(("1".equals(closeLogin) ? true : false));
		return response;
	}
	@PostMapping(value="/login",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse login(@RequestBody Login login, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		Subject subject = SecurityUtils.getSubject();
		try {
			subject.login(new UsernamePasswordToken(login.getUserName(),passwordEncoder.encodePassword(login.getPassword(),"")));
			response.put("sysUser", subject.getSession().getAttribute(WwsfSessionFilter.Session_CurLoginSysUser));
			response.put("sysAuthList", subject.getSession().getAttribute(WwsfSessionFilter.Session_CurLoginSysUserMenu));
			response.put("SysAuth_Root", SysAuth.SysAuth_Root);
			response.setSuccess(true);
		}catch(AuthenticationException e) {
			logger.error("login failed ", e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = new SysLog();
			SysUser sysUer = UserUtils.getCurLoginSysUser();
			sysLog.setLogType("用户操作");
			sysLog.setOptDate(CalendarUtil.fmtDate(new Date(), "yyyyMMddHHmmss"));
			if (sysUer==null) {
				sysLog.setUserId(login.getUserName());
			}else {
				sysLog.setUserId(sysUer.getLoginName());
			}
			sysLog.setId(String.valueOf(System.currentTimeMillis()));
			sysLog.setUserUip(UserUtils.getIpAddr(request));
			sysLog.setModName("用户登录");
			sysLog.setOptName("登录");
			sysLog.setLogDesc(String.format("用户[%s]登录系统", sysLog.getUserId()));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return response;
	}
	
	@GetMapping(value="/getSessionMenu",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getSessionMenu(HttpSession httpSession) {
		RestResponse response = new RestResponse();
		try {
			Object obj = httpSession.getAttribute(WwsfSessionFilter.Session_CurLoginSysUser);
			if(null != obj){
				String isSSOLogin = (String)httpSession.getAttribute(WwsfSessionFilter.Session_CurLoginIsSSO);
				response.put("sysUser", obj);
				response.put("sysAuthList", httpSession.getAttribute(WwsfSessionFilter.Session_CurLoginSysUserMenu));
				response.put("isSSOLogin", StringUtil.hasText(isSSOLogin) ? isSSOLogin : "0");
				response.put("SysAuth_Root", SysAuth.SysAuth_Root);
				response.setSuccess(true);
			}else{
				response.setSuccess(false);
			}
		}catch(Exception e) {
			logger.error("get session menu failed ", e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
	
	//shiro会自动清理session  ShiroConfiguration.java/shiroFilterFactoryBean
	/*@GetMapping(value="/logout",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse logout(HttpSession httpSession) {
		RestResponse response = new RestResponse();
		try {
			Object obj = httpSession.getAttribute(WwsfSessionFilter.Session_CurLoginSysUser);
			if(null != obj){
				httpSession.removeAttribute(WwsfSessionFilter.Session_CurLoginSysUser);
				httpSession.removeAttribute(WwsfSessionFilter.Session_CurLoginSysUserMenu);
			}
			response.setSuccess(true);
		}catch(AuthenticationException e) {
			e.printStackTrace();
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}*/
	
}
