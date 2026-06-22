package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bocsoft.wwsf.webconsole.mapper.SysAuthMapper;
import com.bocsoft.wwsf.webconsole.model.SysAuth;
import com.bocsoft.wwsf.webconsole.service.SysAuthService;

@Service("sysAuthService")
public class SysAuthServiceImpl implements SysAuthService{

	@Autowired
	private SysAuthMapper sysAuthMapper;
	
	public List<SysAuth> querySysAuth(Map<String, Object> condition) throws Exception{
		return sysAuthMapper.querySysAuth(condition);
	}
	
	public List<SysAuth> getSysAuthByUserId(String userId) throws Exception{
		Map<String,Object> condition = new HashMap<String,Object>();
		condition.put("userId", userId);
		return sysAuthMapper.getSysAuthByUserId(condition);
	}
	
}
