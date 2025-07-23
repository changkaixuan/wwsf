package com.bocsoft.wwsf.webconsole.service;

import com.bocsoft.wwsf.webconsole.model.SysUser;

public interface SsoLoginService {
    boolean checkWwsToken(String token, String checkUrl) throws Exception;

    void login(String userName, String token);

    SysUser getSysUser(String userName, String token);
}
