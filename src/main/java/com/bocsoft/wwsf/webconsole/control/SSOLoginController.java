package com.bocsoft.wwsf.webconsole.control;

import com.bocsoft.wwsf.webconsole.encoder.PasswordEncoder;
import com.bocsoft.wwsf.webconsole.service.SsoLoginService;
import com.bocsoft.wwsf.webconsole.service.SysLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class SSOLoginController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSOLoginController.class);
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private SsoLoginService ssoLoginService;

    @GetMapping(value="/ssoLogin")
    public Object ssoLogin(@RequestParam(value = "userName") String userName,
                           @RequestParam(value = "token") String token, HttpServletResponse response
            , HttpServletRequest request) {
        ssoLoginService.login(userName, token);
        //将userName与token存入cookie中
        Cookie userNameCookie = new Cookie("userName", userName);
        Cookie tokenCookie = new Cookie("token", token);
        userNameCookie.setPath("/");
        userNameCookie.setMaxAge(60*60*24);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(60*60*24);

        response.addCookie(userNameCookie);
        response.addCookie(tokenCookie);
        return "redirect:/";
    }
}
