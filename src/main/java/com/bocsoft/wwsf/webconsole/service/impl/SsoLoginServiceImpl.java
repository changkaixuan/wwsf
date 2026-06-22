package com.bocsoft.wwsf.webconsole.service.impl;

import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.SsoLoginService;
import com.bocsoft.wwsf.webconsole.service.SysUserService;
import com.github.pagehelper.util.StringUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class SsoLoginServiceImpl implements SsoLoginService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SsoLoginServiceImpl.class);

    @Autowired
    private SysUserService sysUserService;

    @Value("${sso.checkToken.url}")
    private String checkUrl;

    @Override
    public boolean checkWwsToken(String token, String checkUrl) throws Exception {
        String urlstr = checkUrl + "?token=" + token;
        URL url = new URL(urlstr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setConnectTimeout(20000);//连接超时时间  ms
        connection.setReadTimeout(20000);//读取超时时间  ms
        connection.setDoOutput(true);//打开输出流
        connection.setUseCaches(false);//不启用用户缓存
        //获取响应状态
        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            return false;
        }
        //获取返回的数据
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        StringBuffer sb = new StringBuffer();
        String readLine;
        while((readLine = reader.readLine()) != null) {
            sb.append(readLine);
        }
        if(sb.toString().contains("success")){
            return true;
        }
        return false;
    }

    public void login(String userName, String token) {
        //获得用户账号密码
        SysUser sysUser = this.getSysUser(userName, token);
        //shiro登录
        Subject subject = SecurityUtils.getSubject();
        try {
            UsernamePasswordToken tk = new UsernamePasswordToken(sysUser.getLoginName(), sysUser.getUserPwd());
            //自动登录
            //tk.setRememberMe(true);
            subject.login(tk);
            //subject.login(new UsernamePasswordToken(sysUser.getLoginName(), sysUser.getUserPwd()));
        }catch(AuthenticationException e) {
            LOGGER.error("login failed ", e);
            throw e;
        }
    }

    @Override
    public SysUser getSysUser(String userName, String token) {
        //校验token是否生效
        boolean checkResult = false;
        try {
            checkResult = this.checkWwsToken(token, checkUrl);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
        if(!checkResult) {
            LOGGER.info("token is invalid!");
            throw new RuntimeException("token is invalid!");
        }
        //根据userName查询对应的密码
        SysUser sysUser;
        try {
            sysUser = sysUserService.getSysUser(userName);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        //未查询到用户或密码为空
        if(sysUser == null || StringUtil.isEmpty(sysUser.getUserPwd())) {
            throw new RuntimeException("user does not exist!");
        }

        return sysUser;
    }
}
